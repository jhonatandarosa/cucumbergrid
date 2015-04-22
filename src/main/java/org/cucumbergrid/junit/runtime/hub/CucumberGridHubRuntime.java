package org.cucumbergrid.junit.runtime.hub;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.model.CucumberFeature;
import java.io.Serializable;
import org.cucumbergrid.junit.runner.CucumberGridHub;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.CucumberUtils;
import org.cucumbergrid.junit.runtime.common.FormatMessage;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.cucumbergrid.junit.runtime.hub.server.GridServer;
import org.cucumbergrid.junit.utils.ReflectionUtils;
import org.jboss.netty.channel.Channel;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class CucumberGridHubRuntime extends CucumberGridRuntime implements CucumberGridServerHandler {

    private Description description;
    private GridServer server;
    private LinkedList<CucumberFeature> featuresToExecute;
    private ConcurrentLinkedDeque<CucumberFeature> featuresExecuted;
    private RunNotifier notifier;
    private CucumberGridReporter reporter;
    private CucumberGridServerFormatterHandler formatterHandler;
    private ConcurrentHashMap<Integer, Set<CucumberFeature>> unknownFeaturesByClient = new ConcurrentHashMap<>();

    public CucumberGridHubRuntime(Class clazz) {
        super(clazz);

        CucumberGridHub config = ReflectionUtils.getDeclaredAnnotation(clazz, CucumberGridHub.class);
        server = new GridServer(config);
        server.setHandler(this);
        featuresToExecute = new LinkedList<>(cucumberFeatures);
        featuresExecuted = new ConcurrentLinkedDeque<>();

        ClassLoader classLoader = clazz.getClassLoader();
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class, Cucumber.Options.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        reporter = new CucumberGridReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict());
        formatterHandler = new CucumberGridServerFormatterHandler(reporter);
    }

    private Set<CucumberFeature> getUnknownFeatures(Channel channel) {
        Integer nodeId = channel.getId();
        Set<CucumberFeature> set = unknownFeaturesByClient.get(nodeId);
        if (set == null) {
            set = new HashSet<>();
            unknownFeaturesByClient.put(nodeId, set);
        }
        return set;
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(clazz.getName());
            for (CucumberFeature cucumberFeature : cucumberFeatures) {
                description.addChild(getDescription(cucumberFeature));
            }
        }
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        this.notifier = notifier;
        server.init();

        // has more features
        while (featuresExecuted.size() != cucumberFeatures.size()) {
            // process
//            server.process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        // wait all results
        while (formatterHandler.hasUnprocessedMessages()) {
            // process
//            server.process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        // when all features finished, send a shutdown message
        server.broadcast(new Message(MessageID.SHUTDOWN));


        server.shutdown();

        reporter.done();
        reporter.close();
//        runtime.printSummary();
    }

    @Override
    public void onDataReceived(Channel channel, Message message) {

        Message response = process(channel, message);
//        System.out.println("Data received " + message.getID());
//        System.out.println("Response " + response);
        if (response != null) {
            server.send(channel, response);
//            System.out.println("response sent");
        }

    }

    public Message process(Channel channel, Message message) {
        switch (message.getID()) {
            case REQUEST_FEATURE:
                return processRequestFeature();
            case UNKNOWN_FEATURE:
                return processUnknownFeature(channel, message);
            case TEST_STARTED:
                onTestStarted(message);
                break;
            case TEST_FINISHED:
                onTestFinished(message);
                break;
            case TEST_IGNORED:
                onTestIgnored(message);
                break;
            case TEST_FAILURE:
                onTestFailure(message);
                break;
            case TEST_ASSUMPTION_FAILURE:
                onTestAssumptionFailure(message);
                break;
            case FORMAT:
                onFormatMessage(channel, message);
                break;
            default:
                System.out.println("Unknown message: " + message.getID() + " " + message.getData());

        }
        return null;
    }

    private void onFormatMessage(Channel channel, Message message) {
        FormatMessage formatMessage = message.getData();
        String token = "node" + channel.getId();
        formatterHandler.onFormatMessage(token, formatMessage);
    }

    private void onTestAssumptionFailure(Message message) {
        Failure failure = message.getData();
        notifier.fireTestAssumptionFailed(failure);
    }

    private void onTestFailure(Message message) {
        Failure failure = message.getData();
        notifier.fireTestFailure(failure);
    }

    private void onTestIgnored(Message message) {
        Description description = getDescriptionByUniqueID(message.getData());
        notifier.fireTestIgnored(description);
    }

    private void onTestFinished(Message message) {
        Serializable uniqueID = message.getData();
        Description description = getDescriptionByUniqueID(uniqueID);
        notifier.fireTestFinished(description);

        CucumberFeature feature = getFeatureByID(uniqueID);
        if (feature != null) {
            featuresExecuted.add(feature);
        }
    }

    private void onTestStarted(Message message) {
        Description description = getDescriptionByUniqueID(message.getData());
        notifier.fireTestStarted(description);
    }

    private Message processRequestFeature() {
        if (featuresToExecute.isEmpty()) {
            return new Message(MessageID.NO_MORE_FEATURES);
        }
        CucumberFeature feature;
        synchronized (featuresToExecute) {
            feature = featuresToExecute.poll();
            featuresToExecute.notifyAll();
        }
        return new Message(MessageID.EXECUTE_FEATURE, CucumberUtils.getUniqueID(feature));
    }

    private Message processUnknownFeature(Channel channel, Serializable featureID) {
        CucumberFeature feature = getFeatureByID(featureID);
        Set<CucumberFeature> set = getUnknownFeatures(channel);
        set.add(feature);

        synchronized (featuresToExecute) {
            featuresToExecute.push(feature);
            feature = null;
            for (int i = 0; i < featuresToExecute.size(); i++) {
                feature = featuresToExecute.get(i);
                if (!set.contains(feature)) {
                    break;
                }
            }
            if (feature != null) {
                featuresToExecute.remove(feature);
            }
            featuresToExecute.notifyAll();
        }

        if (feature != null) {
            return new Message(MessageID.EXECUTE_FEATURE, CucumberUtils.getUniqueID(feature));
        } else {
            return new Message(MessageID.NO_MORE_FEATURES);
        }
    }
}
