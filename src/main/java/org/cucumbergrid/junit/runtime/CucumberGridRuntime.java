package org.cucumbergrid.junit.runtime;

import cucumber.api.CucumberOptions;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.model.Feature;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.List;

public abstract class CucumberGridRuntime {

    protected Class clazz;
    protected List<CucumberFeature> cucumberFeatures;

    public CucumberGridRuntime(Class clazz) {
        this.clazz = clazz;
        ClassLoader classLoader = clazz.getClassLoader();

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
    }

    protected Description getFeatureDescription(CucumberFeature cucumberFeature) {
        Feature feature = cucumberFeature.getGherkinFeature();
        String name = feature.getKeyword() + ": " + feature.getName();
        Description featureDescription = Description.createSuiteDescription(name, feature.getId());
        return featureDescription;
    }

    public abstract Description getDescription();

    public abstract void run(RunNotifier notifier);
}
