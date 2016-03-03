package org.cucumbergrid.junit.runtime;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Step;
import java.io.Serializable;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

public abstract class CucumberGridRuntime {

    protected Description description;
    protected Class clazz;
    protected Object testInstance;
    protected List<CucumberFeature> cucumberFeatures;
    private Map<Serializable, Description> descriptionMap = new HashMap<>();
    protected Logger logger = Logger.getLogger(getClass().getName());
    private Map<Serializable, CucumberFeature> featureByID;


    public CucumberGridRuntime(Class clazz) {
        this.clazz = clazz;

        try {
            testInstance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating test class " + clazz, e);
        }
    }

    public <T> T getTestInstanceAs(Class<T> clazz) {
        if (clazz.isInstance(testInstance)) {
            return (T)testInstance;
        }
        return null;
    }

    protected void loadFeatures(CucumberGridRuntimeOptionsFactory factory) {
        ClassLoader classLoader = clazz.getClassLoader();

        RuntimeOptions runtimeOptions = factory.create();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        Collections.sort(cucumberFeatures, new Comparator<CucumberFeature>() {
            @Override
            public int compare(CucumberFeature o1, CucumberFeature o2) {
                return Integer.compare(o1.getFeatureElements().size(), o2.getFeatureElements().size()) * -1;
            }
        });

        description = getDescription();
        featureByID = new HashMap<>();
        for (CucumberFeature feature : cucumberFeatures) {
            featureByID.put(CucumberUtils.getUniqueID(feature), feature);
        }
    }

    protected CucumberFeature getFeatureByID(Serializable uniqueID) {
        return featureByID.get(uniqueID);
    }

    protected Description getDescription(CucumberFeature cucumberFeature) {
        String uniqueID = CucumberUtils.getUniqueID(cucumberFeature);
        Description description = descriptionMap.get(uniqueID);
        if (description == null) {
            Feature feature = cucumberFeature.getGherkinFeature();
            String name = feature.getKeyword() + ": " + feature.getName();

            description = Description.createSuiteDescription(name, uniqueID);
            List<CucumberTagStatement> featureElements = cucumberFeature.getFeatureElements();
            for (CucumberTagStatement cucumberTagStatement : featureElements) {
                if (cucumberTagStatement instanceof CucumberScenario) {
                    Description scenarioDescription = getDescription((CucumberScenario) cucumberTagStatement);
                    description.addChild(scenarioDescription);
                } else if (cucumberTagStatement instanceof CucumberScenarioOutline) {
                    logger.warning("scenario outline " + cucumberTagStatement);
                }
            }

            descriptionMap.put(uniqueID, description);
        }
        return description;
    }

    protected Description getDescription(CucumberScenario cucumberScenario) {
        String uniqueID = CucumberUtils.getUniqueID(cucumberScenario);
        Description description = descriptionMap.get(uniqueID);
        if (description == null) {
            String name = cucumberScenario.getVisualName();
            description = Description.createSuiteDescription(name, uniqueID);

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
                    description.addChild(getDescription(cucumberScenario, copy));
                }
            }

            for (Step step : cucumberScenario.getSteps()) {
                description.addChild(getDescription(cucumberScenario, step));
            }
            descriptionMap.put(uniqueID, description);
        }

        return description;
    }

    protected Description getDescription(CucumberScenario cucumberScenario, Step step) {
        String uniqueID = CucumberUtils.getUniqueID(cucumberScenario, step);
        Description description = descriptionMap.get(uniqueID);
        if (description == null) {
            description = Description.createTestDescription(cucumberScenario.getVisualName(), step.getKeyword() + step.getName(), uniqueID);
            descriptionMap.put(uniqueID, description);
        }
        return description;
    }

    protected Description getDescriptionByUniqueID(Serializable uniqueID) {
        Description description = null;
        if (descriptionMap.containsKey(uniqueID)) {
            description = descriptionMap.get(uniqueID);
            if (description == null) {
                logger.warning("Description with id " + uniqueID + " is null!");
            }
        } else {
            logger.warning("Description with id " + uniqueID + " not found!");
        }

        return description;
    }

    public abstract Description getDescription();

    public abstract void run(RunNotifier notifier);
}
