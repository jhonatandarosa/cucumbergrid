package org.cucumbergrid.junit.runtime.common;

import java.util.HashMap;
import java.util.Map;

import gherkin.formatter.model.Feature;

public class CucumberGridFeature extends Feature {

    private Feature feature;
    private Map<String, Object> extraInfo = new HashMap<>();
    private Map<String, Object> featureMap;

    public CucumberGridFeature(Feature feature) {
        super(feature.getComments(), feature.getTags(), feature.getKeyword(), feature.getName(), feature.getDescription(), feature.getLine(), feature.getId());
        this.feature = feature;
    }

    public void addReportInfo(String key, Object value) {
        if (featureMap == null) {
            extraInfo.put(key, value);
        } else {
            if (!featureMap.containsKey(key)) {
                featureMap.put(key, value);
            }
        }
    }

    @Override
    public Map<String, Object> toMap() {
        if (featureMap == null) {
            featureMap = super.toMap();
            for (String key : extraInfo.keySet()) {
                if (!featureMap.containsKey(key)) {
                    featureMap.put(key, extraInfo.get(key));
                }
            }
        }
        return featureMap;
    }
}
