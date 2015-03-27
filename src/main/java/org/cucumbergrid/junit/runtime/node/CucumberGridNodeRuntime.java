package org.cucumbergrid.junit.runtime.node;

import cucumber.runtime.model.CucumberFeature;
import org.cucumbergrid.junit.runner.CucumberGridNode;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.common.IOUtils;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class CucumberGridNodeRuntime extends CucumberGridRuntime implements CucumberGridClientHandler {

    private CucumberGridClient client;
    private Description description;
    private boolean hasFeatures;
    private RunNotifier currentNotifier;

    public CucumberGridNodeRuntime(Class clazz) {
        super(clazz);
        CucumberGridNode config = (CucumberGridNode) clazz.getDeclaredAnnotation(CucumberGridNode.class);

        client = new CucumberGridClient(config.hub(), config.port());
        client.setHandler(this);
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(clazz.getName());
        }
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        currentNotifier = notifier;
        client.init();

        hasFeatures = true;

        while (client.isConnectionPending()) {
            client.process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

         send(new Message(MessageID.REQUEST_FEATURE));
        // server has features to execute
        while (hasFeatures) {
            // process
            client.process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // wait for all features to finish


        //client.shutdown();
    }

    private void send(Message message) {
        try {
            client.send(IOUtils.serialize(message));
            Thread.sleep(100); // FIXME ???
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void process(Message message) {
        switch (message.getID()) {
            case EXECUTE_FEATURE:
                String path = new String(message.getData());
                System.out.println("Execute feature " + path);
                CucumberFeature feature = getFeatureByPath(path);
                if (feature == null) {
                    throw new IllegalArgumentException("Unknow feature: " + path);
                }
                Description featureDescription = getFeatureDescription(feature);
                testStarted(featureDescription);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                testFinished(featureDescription);
                System.out.println("Requesting new feature");
                send(new Message(MessageID.REQUEST_FEATURE));
                System.out.println("Feature requested");
                break;
            case NO_MORE_FEATURES:
                System.out.println("no more features");
                hasFeatures = false;
                break;
        }
    }

    private CucumberFeature getFeatureByPath(String path) {
        for (CucumberFeature feature : cucumberFeatures) {
            if (feature.getPath().equals(path)) {
                return feature;
            }
        }
        return null;
    }

    @Override
    public void onDataReceived(SelectionKey key, byte[] data) {
        try {
            Message message = IOUtils.deserialize(data);
            process(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testStarted(Description description) {
        //currentNotifier.fireTestStarted(description);
        try {
            send(new Message(MessageID.TEST_STARTED, IOUtils.serialize(description)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testFinished(Description description) {
        //currentNotifier.fireTestFinished(description);
        try {
            send(new Message(MessageID.TEST_FINISHED, IOUtils.serialize(description)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
