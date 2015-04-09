package org.cucumbergrid.junit.runtime.node;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberTagStatement;
import org.cucumbergrid.junit.runner.CucumberGridNode;
import org.cucumbergrid.junit.runtime.CucumberGridExecutionUnitRunner;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.CucumberUtils;
import org.cucumbergrid.junit.utils.IOUtils;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.cucumbergrid.junit.utils.ReflectionUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.logging.Level;

public class CucumberGridNodeRuntime extends CucumberGridRuntime implements CucumberGridClientHandler {

    private CucumberGridClient client;
    private Description description;
    private RunNotifier currentNotifier;
    private final JUnitReporter jUnitReporter;
    private final Runtime runtime;
    private CucumberGridRemoteFormatter formatter;

    public CucumberGridNodeRuntime(Class clazz) throws IOException, InitializationError {
        super(clazz);
        CucumberGridNode config = ReflectionUtils.getDeclaredAnnotation(clazz, CucumberGridNode.class);

        client = new CucumberGridClient(config);
        client.setHandler(this);

        ClassLoader classLoader = clazz.getClassLoader();
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class, Cucumber.Options.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        runtime = createRuntime(resourceLoader, classLoader, runtimeOptions);

        formatter = new CucumberGridRemoteFormatter(client);
        jUnitReporter = new JUnitReporter(formatter, formatter, runtimeOptions.isStrict());
    }

    /**
     * Create the Runtime. Can be overridden to customize the runtime or backend.
     */
    protected Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader,
                                                     RuntimeOptions runtimeOptions) throws InitializationError, IOException {
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        return new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
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
        notifier.addListener(new CucumberGridRunListener());
        client.init();

        while (client.isConnectionPending()) {
            client.process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (!client.isConnected()) return;

         send(new Message(MessageID.REQUEST_FEATURE));
        // server has features to execute
        while (client.isConnected()) {
            // process
            client.process();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        jUnitReporter.done();
        jUnitReporter.close();
        runtime.printSummary();
    }

    private void send(Message message) {
        try {
            client.send(IOUtils.serialize(message));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void process(Message message) throws InitializationError {
        switch (message.getID()) {
            case EXECUTE_FEATURE:
                onExecuteFeature(message);
                break;
            case NO_MORE_FEATURES:
                System.out.println("no more features");
                break;
            case SHUTDOWN:
                System.out.println("Shutdown received...");
                client.shutdown();
                break;
        }
    }

    private void onExecuteFeature(Message message) throws InitializationError {
        Serializable uniqueID = message.getData();
        System.out.println("Execute feature " + uniqueID);
        CucumberFeature cucumberFeature = getFeatureByID(uniqueID);
        if (cucumberFeature == null) {
            send(new Message(MessageID.UNKNOWN_FEATURE, uniqueID));
            return;
        }

        jUnitReporter.uri(cucumberFeature.getPath());
        jUnitReporter.feature(cucumberFeature.getGherkinFeature());
        Description featureDescription = getDescription(cucumberFeature);
        currentNotifier.fireTestStarted(featureDescription);

        List<CucumberTagStatement> featureElements = cucumberFeature.getFeatureElements();
        for (CucumberTagStatement cucumberTagStatement : featureElements) {
            if (cucumberTagStatement instanceof CucumberScenario) {
                CucumberScenario cucumberScenario = (CucumberScenario)cucumberTagStatement;
                ExecutionUnitRunner runner = new CucumberGridExecutionUnitRunner(this, runtime, cucumberScenario, jUnitReporter);
                runner.run(currentNotifier);
            }
        }
        currentNotifier.fireTestFinished(featureDescription);

        jUnitReporter.eof();


        System.out.println("Requesting new feature");
        send(new Message(MessageID.REQUEST_FEATURE));
        System.out.println("Feature requested");
    }

    @Override
    public void onDataReceived(SelectionKey key, byte[] data) {
        try {
            Message message = IOUtils.deserialize(data);
            process(message);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (InitializationError e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public class CucumberGridRunListener extends RunListener {
        void sendMessage(MessageID messageID, Description description) {

            Serializable uniqueID = CucumberUtils.getDescriptionUniqueID(description);
            //new Throwable(uniqueID.toString()).printStackTrace();
            send(new Message(messageID, uniqueID));

        }

        void sendMessage(MessageID messageID, Serializable value) {
            send(new Message(messageID, value));
        }

        @Override
        public void testStarted(Description description) throws Exception {
            sendMessage(MessageID.TEST_STARTED, description);
        }

        @Override
        public void testFinished(Description description) throws Exception {
            sendMessage(MessageID.TEST_FINISHED, description);
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            sendMessage(MessageID.TEST_IGNORED, description);
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            sendMessage(MessageID.TEST_FAILURE, failure);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            sendMessage(MessageID.TEST_ASSUMPTION_FAILURE, failure);
        }
    }
}
