package org.cucumbergrid.junit.runtime;

import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;

import java.io.Serializable;
import java.lang.reflect.Field;

public class CucumberUtils {

    private static Field fUniqueId;
    public static String getUniqueID(CucumberFeature feature) {
        return feature.getGherkinFeature().getId();
    }

    public static String getUniqueID(CucumberScenario cucumberScenario) {
        return cucumberScenario.getGherkinModel().getId();
    }

    public static String getUniqueID(CucumberScenario cucumberScenario, Step step) {
        String uniqueID = getUniqueID(cucumberScenario);
        uniqueID += ";" + step.getKeyword() + step.getName();
        return uniqueID;
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
