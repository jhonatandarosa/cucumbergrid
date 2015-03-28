package org.cucumbergrid.junit.runtime.hub;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import org.cucumbergrid.junit.runner.CucumberGridHub;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.CucumberUtils;
import org.cucumbergrid.junit.runtime.common.IOUtils;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class CucumberGridHubRuntime extends CucumberGridRuntime implements CucumberGridServerHandler {

    private Description description;
    private CucumberGridServer server;
    private LinkedList<CucumberFeature> featuresToExecute;
    private List<CucumberFeature> featuresExecuted;
    private RunNotifier notifier;
    private JUnitReporter jUnitReporter;

    public CucumberGridHubRuntime(Class clazz) {
        super(clazz);

        CucumberGridHub config = (CucumberGridHub) clazz.getDeclaredAnnotation(CucumberGridHub.class);
        server = new CucumberGridServer(config);
        server.setHandler(this);
        featuresToExecute = new LinkedList<>(cucumberFeatures);
        featuresExecuted = new ArrayList<>();

        ClassLoader classLoader = clazz.getClassLoader();
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class, Cucumber.Options.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict());
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
            server.process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        // wait all results

        // when all features finished, send a shutdown message
        broadcast(new Message(MessageID.SHUTDOWN));


        server.shutdown();

        jUnitReporter.done();
        jUnitReporter.close();
//        runtime.printSummary();
    }

    @Override
    public void onDataReceived(SelectionKey key, byte[] data) {
        try {
            Message message = IOUtils.deserialize(data);
            Message response = process(message);
//            System.out.println("Data received " + message.getID());
//            System.out.println("Response " + response);
            if (response != null) {
                send(key, response);
//                System.out.println("response sent");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void send(SelectionKey key, Message message) {
        try {
            server.send(key, IOUtils.serialize(message));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void broadcast(Message message) {
        try {
            server.broadcast(IOUtils.serialize(message));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public Message process(Message message) {
        switch (message.getID()) {
            case REQUEST_FEATURE:
                return processRequestFeature();
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
            default:
                System.out.println("Unknown message: " + message.getID() + " " + message.getData());

        }
        return null;
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
        CucumberFeature feature = featuresToExecute.poll();
        return new Message(MessageID.EXECUTE_FEATURE, CucumberUtils.getUniqueID(feature));
    }
}
