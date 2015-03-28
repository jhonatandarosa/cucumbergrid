package org.cucumbergrid.junit.runtime.node;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.*;
import cucumber.runtime.Runtime;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberTagStatement;
import org.cucumbergrid.junit.runner.CucumberGridNode;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.common.IOUtils;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

public class CucumberGridNodeRuntime extends CucumberGridRuntime implements CucumberGridClientHandler {

    private CucumberGridClient client;
    private Description description;
    private boolean hasFeatures;
    private RunNotifier currentNotifier;
    private final JUnitReporter jUnitReporter;
    private final Runtime runtime;

    public CucumberGridNodeRuntime(Class clazz) throws IOException, InitializationError {
        super(clazz);
        CucumberGridNode config = (CucumberGridNode) clazz.getDeclaredAnnotation(CucumberGridNode.class);

        client = new CucumberGridClient(config.hub(), config.port());
        client.setHandler(this);

        ClassLoader classLoader = clazz.getClassLoader();
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class, Cucumber.Options.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        runtime = createRuntime(resourceLoader, classLoader, runtimeOptions);

        jUnitReporter = new JUnitReporter(runtimeOptions.reporter(classLoader), runtimeOptions.formatter(classLoader), runtimeOptions.isStrict());
    }

    /**
     * Create the Runtime. Can be overridden to customize the runtime or backend.
     */
    protected cucumber.runtime.Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader,
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

        client.shutdown();
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

    private void process(Message message) throws InitializationError {
        switch (message.getID()) {
            case EXECUTE_FEATURE:
                onExecuteFeature(message);
                break;
            case NO_MORE_FEATURES:
                System.out.println("no more features");
                hasFeatures = false;
                break;
            case SHUTDOWN:
                System.out.println("Shutdown received...");
                hasFeatures = false;
                break;
        }
    }

    private void onExecuteFeature(Message message) throws InitializationError {
        String path = new String(message.getData());
        System.out.println("Execute feature " + path);
        CucumberFeature cucumberFeature = getFeatureByPath(path);
        if (cucumberFeature == null) {
            throw new IllegalArgumentException("Unknown feature: " + path);
        }

        jUnitReporter.uri(cucumberFeature.getPath());
        jUnitReporter.feature(cucumberFeature.getGherkinFeature());

        List<CucumberTagStatement> featureElements = cucumberFeature.getFeatureElements();
        for (CucumberTagStatement cucumberTagStatement : featureElements) {
            if (cucumberTagStatement instanceof CucumberScenario) {
                CucumberScenario cucumberScenario = (CucumberScenario)cucumberTagStatement;
                ExecutionUnitRunner runner = new ExecutionUnitRunner(runtime, cucumberScenario, jUnitReporter);
                runner.run(currentNotifier);
            }
        }

        jUnitReporter.eof();

        System.out.println("Requesting new feature");
        send(new Message(MessageID.REQUEST_FEATURE));
        System.out.println("Feature requested");
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
        } catch (InitializationError initializationError) {
            initializationError.printStackTrace();
        }
    }

    public class CucumberGridRunListener extends RunListener {

        @Override
        public void testStarted(Description description) throws Exception {
            try {
                send(new Message(MessageID.TEST_STARTED, IOUtils.serialize(description)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void testFinished(Description description) throws Exception {
            try {
                send(new Message(MessageID.TEST_FINISHED, IOUtils.serialize(description)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            System.out.println("super.testRunStarted(description)");
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            System.out.println("super.testRunFinished(result)");
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            System.out.println("super.testIgnored(description)");
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            System.out.println("super.testFailure(failure)");
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            System.out.println("super.testAssumptionFailure(failure)");
        }
    }
}
