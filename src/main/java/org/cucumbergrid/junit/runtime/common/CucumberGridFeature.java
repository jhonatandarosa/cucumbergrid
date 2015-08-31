package org.cucumbergrid.junit.runtime.common;

import java.util.HashMap;
import java.util.Map;

import gherkin.formatter.model.Feature;

public class CucumberGridFeature extends Feature {

    private Feature feature;
    private Map<String, Object> featureMap = new HashMap<>();
    private boolean converted;

    public CucumberGridFeature(Feature feature) {
        super(feature.getComments(), feature.getTags(), feature.getKeyword(), feature.getName(), feature.getDescription(), feature.getLine(), feature.getId());
        this.feature = feature;
    }

    public void addReportInfo(String key, Object value) {
        if (!featureMap.containsKey(key)) {
            featureMap.put(key, value);
        }
    }

    @Override
    public Map<String, Object> toMap() {
        if (!converted) {
            featureMap.putAll(super.toMap());
            converted = true;
        }
        return featureMap;
    }
}
