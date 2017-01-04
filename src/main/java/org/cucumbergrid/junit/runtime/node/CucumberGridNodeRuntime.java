package org.cucumbergrid.junit.runtime.node;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.cucumbergrid.junit.runner.CucumberGridNode;
import org.cucumbergrid.junit.runner.NodePropertyRetriever;
import org.cucumbergrid.junit.runtime.CucumberGridExecutionUnitRunner;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.CucumberGridRuntimeOptionsFactory;
import org.cucumbergrid.junit.runtime.CucumberUtils;
import org.cucumbergrid.junit.runtime.common.FeatureListener;
import org.cucumbergrid.junit.runtime.common.Message;
import org.cucumbergrid.junit.runtime.common.MessageID;
import org.cucumbergrid.junit.runtime.common.NodeInfo;
import org.cucumbergrid.junit.runtime.node.client.GridClient;
import org.cucumbergrid.junit.sysinfo.SysInfo;
import org.cucumbergrid.junit.utils.ReflectionUtils;
import org.jboss.netty.channel.Channel;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;

public class CucumberGridNodeRuntime extends CucumberGridRuntime implements CucumberGridClientHandler {

    private GridClient client;
    private RunNotifier currentNotifier;
    private JUnitReporter jUnitReporter;
    private Runtime runtime;
    private CucumberGridRemoteFormatter formatter;
    private NodeInfo nodeInfo;
    private final CucumberGridNode config;

    public CucumberGridNodeRuntime(Class clazz) throws IOException, InitializationError {
        super(clazz);
        config = ReflectionUtils.getDeclaredAnnotation(clazz, CucumberGridNode.class);

        client = new GridClient(config);
        client.setHandler(this);

        nodeInfo = createNodeInfo();
    }

    private void initCucumber(List<String> args) throws InitializationError, IOException {
        ClassLoader classLoader = clazz.getClassLoader();
        CucumberGridRuntimeOptionsFactory runtimeOptionsFactory = new CucumberGridRuntimeOptionsFactory(args);

        loadFeatures(runtimeOptionsFactory);

        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        runtime = createRuntime(resourceLoader, classLoader, runtimeOptions);

        formatter = new CucumberGridRemoteFormatter(client);
        jUnitReporter = new JUnitReporter(formatter, formatter, runtimeOptions.isStrict());
    }

    private NodeInfo createNodeInfo() {
        NodeInfo info = new NodeInfo();
        SysInfo sysInfo = SysInfo.getInstance();
        info.setOs(sysInfo.getOperatingSystem());
        info.setAddress(sysInfo.getAddress().getHostName());

        Class<? extends NodePropertyRetriever> retrieverClass = config.retriever();
        if (!NodePropertyRetriever.class.equals(retrieverClass)) {
            try {
                NodePropertyRetriever retriever = retrieverClass.newInstance();
                info.setProperties(retriever.getProperties());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return info;
    }

    private List<RunListener> getListeners() {
        ArrayList<RunListener> listeners = new ArrayList<>();
        for (Class<? extends RunListener> clazz : config.listeners()) {
            try {
                listeners.add(clazz.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return listeners;
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

        for (RunListener listener : getListeners()) {
            notifier.addListener(listener);
        }

        notifier.addListener(new CucumberGridRunListener());
        client.init();

        if (!client.isConnected()) return;
        nodeInfo.setAddress(client.getAddress().toString());

        send(new Message(MessageID.NODE_INFO, nodeInfo));

        send(new Message(MessageID.CUCUMBER_OPTIONS));

        // server has features to execute
        while (client.isConnected()) {
            // process
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        client.releaseExternalResources();

        jUnitReporter.done();
        jUnitReporter.close();
        runtime.printSummary();
    }

    private void process(Message message) throws InitializationError {
        switch (message.getID()) {
            case EXECUTE_FEATURE:
                onExecuteFeature(message);
                break;
            case CUCUMBER_OPTIONS:
                onCucumberOptions(message);
                break;
            case NO_MORE_FEATURES:
                logger.info("no more features");
                break;
            case SHUTDOWN:
                logger.info("Shutdown received...");
                client.shutdown();
                break;
        }
    }

    private void onCucumberOptions(Message message) {
        ArrayList<String> args = message.getData();
        try {
            initCucumber(args);
        } catch (InitializationError initializationError) {
            initializationError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(new Message(MessageID.REQUEST_FEATURE));
    }

    private void onExecuteFeature(Message message) throws InitializationError {
        Serializable uniqueID = message.getData();
        logger.info("Execute feature " + uniqueID);
        CucumberFeature cucumberFeature = getFeatureByID(uniqueID);
        if (cucumberFeature == null) {
            send(new Message(MessageID.UNKNOWN_FEATURE, uniqueID));
            return;
        }

        ReportAppender reportAppender = new ReportAppender(this);
        FeatureListener featureListener = getTestInstanceAs(FeatureListener.class);
        if (featureListener != null) {
            featureListener.onBeforeFeature(cucumberFeature.getGherkinFeature());
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
            } else if (cucumberTagStatement instanceof CucumberScenarioOutline) {
                CucumberScenarioOutline cucumberScenarioOutline = (CucumberScenarioOutline) cucumberTagStatement;

                for (CucumberExamples cucumberExamples : cucumberScenarioOutline.getCucumberExamplesList()) {

                    for (CucumberScenario cucumberScenario : cucumberExamples.createExampleScenarios()) {
                        ExecutionUnitRunner runner = new CucumberGridExecutionUnitRunner(this, runtime, cucumberScenario, jUnitReporter);
                        runner.run(currentNotifier);
                    }

                }
            }
        }
        currentNotifier.fireTestFinished(featureDescription);

        if (featureListener != null) {
            featureListener.onAfterFeature(cucumberFeature.getGherkinFeature(), reportAppender);
        }

        jUnitReporter.eof();


        logger.info("Requesting new feature");
        send(new Message(MessageID.REQUEST_FEATURE));
        logger.info("Feature requested");
    }

    @Override
    public void onDataReceived(Channel channel, Message data) {
        try {
            process(data);
        } catch (InitializationError e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    void send(Message msg) {
        client.send(msg);
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
            handleFailure(failure);
            sendMessage(MessageID.TEST_FAILURE, failure);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            handleFailure(failure);
            sendMessage(MessageID.TEST_ASSUMPTION_FAILURE, failure);
        }
    }

    private void handleFailureException(Throwable exception) {
        List<StackTraceElement> list = new ArrayList<>();
        boolean remove = false;

        int i = 0;
        for (StackTraceElement ste : exception.getStackTrace()) {
            if (ste.getClassName().startsWith("org.jboss.netty")) {
                remove = true;
                i++;
            } else {
                if (remove) {
                    StackTraceElement netty = new StackTraceElement("org.jboss.netty", "<supressed " + i + " exceptions>", null, 0);
                    list.add(netty);
                    remove = false;
                    i = 0;
                }
            }
            if (!remove) {
                list.add(ste);
            }
        }
        exception.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
        Throwable cause = exception.getCause();
        if (cause != null) {
            handleFailureException(cause);
        }
    }

    private void handleFailure(Failure failure) {
        Throwable exception = failure.getException();
        handleFailureException(exception);
    }
}
