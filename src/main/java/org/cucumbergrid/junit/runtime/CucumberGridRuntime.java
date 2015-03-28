package org.cucumbergrid.junit.runtime;

import cucumber.api.CucumberOptions;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.TagStatement;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CucumberGridRuntime {

    protected Class clazz;
    protected List<CucumberFeature> cucumberFeatures;
    private final Map<Step, Description> stepDescriptions = new HashMap<Step, Description>();

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
        List<CucumberTagStatement> featureElements = cucumberFeature.getFeatureElements();
        for (CucumberTagStatement cucumberTagStatement : featureElements) {
            if (cucumberTagStatement instanceof CucumberScenario) {
                Description scenarioDescription = getScenarioDescription((CucumberScenario)cucumberTagStatement);
                featureDescription.addChild(scenarioDescription);
            } else if (cucumberTagStatement instanceof CucumberScenarioOutline) {

            }
        }
        return featureDescription;
    }

    private Description getScenarioDescription(CucumberScenario cucumberScenario) {
        String name = cucumberScenario.getVisualName();
        Description description = Description.createSuiteDescription(name, cucumberScenario.getGherkinModel());

        if (cucumberScenario.getCucumberBackground() != null) {
            for (Step backgroundStep : cucumberScenario.getCucumberBackground().getSteps()) {
                // We need to make a copy of that step, so we have a unique one per scenario
                Step copy = new Step(
                        backgroundStep.getComments(),
                        backgroundStep.getKeyword(),
                        backgroundStep.getName(),
                        backgroundStep.getLine(),
                        backgroundStep.getRows(),
                        backgroundStep.getDocString()
                );
                description.addChild(getStepDescription(cucumberScenario, copy));
            }
        }

        for (Step step : cucumberScenario.getSteps()) {
            description.addChild(getStepDescription(cucumberScenario, step));
        }

        return description;
    }

    private Description getStepDescription(CucumberScenario cucumberScenario, Step step) {
        Description description = stepDescriptions.get(step);
        if (description == null) {
            description = Description.createTestDescription(cucumberScenario.getVisualName(), step.getKeyword() + step.getName(), step);
            stepDescriptions.put(step, description);
        }
        return description;
    }

    public abstract Description getDescription();

    public abstract void run(RunNotifier notifier);
}
