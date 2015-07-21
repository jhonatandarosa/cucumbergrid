package org.cucumbergrid.junit.runtime.hub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.model.CucumberFeature;
import java.io.Serializable;
import org.cucumbergrid.junit.runner.CucumberGridHub;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.CucumberGridRuntimeOptionsFactory;
import org.cucumbergrid.junit.runtime.CucumberUtils;
import org.cucumbergrid.junit.runtime.common.FormatMessage;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.cucumbergrid.junit.runtime.common.NodeInfo;
import org.cucumbergrid.junit.runtime.common.admin.AdminMessage;
import org.cucumbergrid.junit.runtime.hub.server.GridServer;
import org.cucumbergrid.junit.utils.ReflectionUtils;
import org.jboss.netty.channel.Channel;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class CucumberGridHubRuntime extends CucumberGridRuntime implements CucumberGridServerHandler {

    private Description description;
    private GridServer server;
    private final LinkedList<CucumberFeature> featuresToExecute;
    private ConcurrentLinkedDeque<CucumberFeature> featuresExecuted;
    private ConcurrentHashMap<Integer, CucumberFeature> featureBeingExecuted = new ConcurrentHashMap<>();
    private RunNotifier notifier;
    private CucumberGridReporter reporter;
    private CucumberGridServerFormatterHandler formatterHandler;
    private ConcurrentHashMap<Integer, Set<CucumberFeature>> unknownFeaturesByClient = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, NodeInfo> nodeInfos = new ConcurrentHashMap<>();
    private CucumberGridRuntimeOptionsFactory runtimeOptionsFactory;

    public CucumberGridHubRuntime(Class clazz) {
        super(clazz);

        CucumberGridHub config = ReflectionUtils.getDeclaredAnnotation(clazz, CucumberGridHub.class);
        server = new GridServer(config);
        server.setHandler(this);

        ClassLoader classLoader = clazz.getClassLoader();
        runtimeOptionsFactory = new CucumberGridRuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class, Cucumber.Options.class});

        loadFeatures(runtimeOptionsFactory);

        featuresToExecute = new LinkedList<>(cucumberFeatures);
        featuresExecuted = new ConcurrentLinkedDeque<>();

        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        reporter = new CucumberGridReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict());
        formatterHandler = new CucumberGridServerFormatterHandler(this, reporter);
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


        reporter.done();
        reporter.close();

        server.shutdown();
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

    public void onFeatureFinished(Integer channelId) {
        featureBeingExecuted.remove(channelId);
    }

    @Override
    public void onNodeDisconnected(Channel channel) {
        CucumberFeature feature = featureBeingExecuted.remove(channel.getId());
        if (feature != null) {
            System.out.println("Adding " + CucumberUtils.getUniqueID(feature) + " to be executed again");
            synchronized (featuresToExecute) {
                featuresToExecute.add(feature);
            }
            formatterHandler.discardMessages(channel.getId());
        }
    }

    public Message process(Channel channel, Message message) {
        switch (message.getID()) {
            case REQUEST_FEATURE:
                return processRequestFeature(channel);
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
            case NODE_INFO:
                onNodeInfoMessage(channel, message);
                break;
            case CUCUMBER_OPTIONS:
                onCucumberOptions(channel, message);
                break;
            case ADMIN:
                onAdminMessage(channel, message);
                break;
            default:
                System.out.println("Unknown message: " + message.getID() + " " + message.getData());

        }
        return null;
    }

    private void onAdminMessage(Channel channel, Message message) {
        AdminMessage adminMessage = message.getData();
        switch (adminMessage.getID()) {
            case FINISH_GRACEFULLY:
                System.out.println("Finish requested by admin...");
                featuresExecuted.clear();
                cucumberFeatures.clear();
                break;
        }
    }

    private void onCucumberOptions(Channel channel, Message message) {
        ArrayList<String> args = runtimeOptionsFactory.buildArgsFromOptions();
        Message response = new Message(MessageID.CUCUMBER_OPTIONS, args);
        channel.write(response);
    }

    NodeInfo getNodeInfo(Integer channelId) {
        return nodeInfos.get(channelId);
    }

    private void onNodeInfoMessage(Channel channel, Message message) {
        NodeInfo nodeInfo = message.getData();
        nodeInfos.put(channel.getId(), nodeInfo);
    }

    private void onFormatMessage(Channel channel, Message message) {
        FormatMessage formatMessage = message.getData();
        formatterHandler.onFormatMessage(channel.getId(), formatMessage);
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

    private Message processRequestFeature(Channel channel) {
        if (featuresToExecute.isEmpty()) {
            return new Message(MessageID.NO_MORE_FEATURES);
        }
        CucumberFeature feature;
        synchronized (featuresToExecute) {
            feature = featuresToExecute.poll();
            featuresToExecute.notifyAll();
        }
        featureBeingExecuted.put(channel.getId(), feature);
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
