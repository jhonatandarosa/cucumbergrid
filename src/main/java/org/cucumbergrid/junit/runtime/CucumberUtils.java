package org.cucumbergrid.junit.runtime;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.junit.runner.Description;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import gherkin.formatter.model.Step;

public class CucumberUtils {

    private static Field fUniqueId;

    private static String normalizeId(String id) {
        return id.replaceAll("[^\\d\\w]", "");
    }

    public static String getUniqueID(CucumberFeature feature) {
        return normalizeId(feature.getGherkinFeature().getId());
    }

    public static String getUniqueID(CucumberScenario cucumberScenario) {
        return normalizeId(cucumberScenario.getGherkinModel().getId());
    }

    public static String getUniqueID(CucumberScenarioOutline cucumberScenarioOutline) {
        return normalizeId(cucumberScenarioOutline.getGherkinModel().getId());
    }

    public static String getUniqueID(CucumberScenario cucumberScenario, Step step) {
        String uniqueID = getUniqueID(cucumberScenario);
        uniqueID += ";" + step.getKeyword() + step.getName();
        return normalizeId(uniqueID);
    }

    public static Serializable getDescriptionUniqueID(Description description) {
        if (fUniqueId == null) {
            try {
                fUniqueId = Description.class.getDeclaredField("fUniqueId");
                fUniqueId.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        try {
            return (Serializable) fUniqueId.get(description);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
