package org.cucumbergrid.junit.runtime.common;

import java.util.HashMap;
import java.util.Map;

import gherkin.formatter.model.Feature;

/**
 * @author Jhonatan da Rosa <br>
 *         Dígitro - 15/07/15 <br>
 *         <a href="mailto:jhonatan.rosa@digitro.com.br">jhonatan.rosa@digitro.com.br</a>
 */
public class CucumberGridFeature extends Feature {

    private Feature feature;
    private Map<String, Object> extraInfo = new HashMap<>();

    public CucumberGridFeature(Feature feature) {
        super(feature.getComments(), feature.getTags(), feature.getKeyword(), feature.getName(), feature.getDescription(), feature.getLine(), feature.getId());
        this.feature = feature;
    }

    public void addReportInfo(String key, Object value) {
        extraInfo.put(key, value);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        for (String key : extraInfo.keySet()) {
            if (!map.containsKey(key)) {
                map.put(key, extraInfo.get(key));
            }
        }
        return map;
    }
}
