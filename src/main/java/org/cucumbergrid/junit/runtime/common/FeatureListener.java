package org.cucumbergrid.junit.runtime.common;

import gherkin.formatter.model.Feature;
import org.cucumbergrid.junit.runtime.node.ReportAppender;

public interface FeatureListener {

    void onBeforeFeature(Feature feature);

    void onAfterFeature(Feature feature, ReportAppender appender);
}
