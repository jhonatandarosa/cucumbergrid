package org.cucumbergrid.junit.runtime.hub;

import cucumber.runtime.model.CucumberFeature;
import org.cucumbergrid.junit.runner.CucumberGridHub;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.common.IOUtils;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.*;

public class CucumberGridHubRuntime extends CucumberGridRuntime implements CucumberGridServerHandler {

    private Description description;
    private CucumberGridServer server;
    private LinkedList<CucumberFeature> featuresToExecute;
    private List<CucumberFeature> featuresExecuted;
    private Map<Description, CucumberFeature> descriptionCucumberFeatureMap;
    private RunNotifier notifier;

    public CucumberGridHubRuntime(Class clazz) {
        super(clazz);

        CucumberGridHub config = (CucumberGridHub) clazz.getDeclaredAnnotation(CucumberGridHub.class);
        server = new CucumberGridServer(config.port());
        server.setHandler(this);
        featuresToExecute = new LinkedList<>(cucumberFeatures);
        featuresExecuted = new ArrayList<>();
        descriptionCucumberFeatureMap = new HashMap<>();
        for (CucumberFeature feature : cucumberFeatures) {
            descriptionCucumberFeatureMap.put(getFeatureDescription(feature), feature);
        }
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(clazz.getName());
            for (CucumberFeature cucumberFeature : cucumberFeatures) {
                description.addChild(getFeatureDescription(cucumberFeature));
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
                e.printStackTrace();
            }
        }

        // when all features finished, send a shutdown message
        broadcast(new Message(MessageID.SHUTDOWN));
        // wait some time
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        server.shutdown();
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
            e.printStackTrace();
        }
    }

    private void send(SelectionKey key, Message message) {
        try {
            server.send(key, IOUtils.serialize(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(Message message) {
        try {
            server.broadcast(IOUtils.serialize(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message process(Message message) {
        switch (message.getID()) {
            case REQUEST_FEATURE:
                return processRequestFeature();
            case TEST_STARTED:
                try {
                    Description description = IOUtils.deserialize(message.getData());
                    notifier.fireTestStarted(description);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TEST_FINISHED:
                try {
                    Description description = IOUtils.deserialize(message.getData());
                    featuresExecuted.add(descriptionCucumberFeatureMap.get(description));
                    notifier.fireTestFinished(description);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    private Message processRequestFeature() {
        if (featuresToExecute.isEmpty()) {
            return new Message(MessageID.NO_MORE_FEATURES);
        }
        CucumberFeature feature = featuresToExecute.poll();
        String path = feature.getPath();
        return new Message(MessageID.EXECUTE_FEATURE, path.getBytes());
    }
}
