package org.cucumbergrid.junit.runner;

import cucumber.api.CucumberOptions;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.junit.Assertions;
import cucumber.runtime.model.CucumberFeature;
import org.cucumbergrid.junit.runtime.CucumberGridRuntime;
import org.cucumbergrid.junit.runtime.hub.CucumberGridHubRuntime;
import org.cucumbergrid.junit.runtime.node.CucumberGridNodeRuntime;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

public class CucumberGrid extends Runner {

    private CucumberGridRuntime gridRuntime;

    /**
     * Constructor called by JUnit.
     *
     * @param clazz the class with the @RunWith annotation.
     * @throws IOException                         if there is a problem
     * @throws InitializationError if there is another problem
     */
    public CucumberGrid(Class clazz) throws InitializationError, IOException {
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        Annotation gridHub = clazz.getDeclaredAnnotation(CucumberGridHub.class);
        Annotation gridNode = clazz.getDeclaredAnnotation(CucumberGridNode.class);

        if (gridHub == null && gridNode == null) {
            throw new InitializationError("Tests ran with CucumberGrid must specify either @CucumberGridHub or @CucumberGridNode");
        } else if (gridHub != null && gridNode != null) {
            throw new InitializationError("Tests ran with CucumberGrid must be either @CucumberGridHub or @CucumberGridNode, not both!");
        }

        if (gridHub != null) {
            gridRuntime = new CucumberGridHubRuntime(clazz);
        } else {
            gridRuntime = new CucumberGridNodeRuntime(clazz);
        }
    }

    @Override
    public Description getDescription() {
        return gridRuntime.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        gridRuntime.run(notifier);
    }
}
