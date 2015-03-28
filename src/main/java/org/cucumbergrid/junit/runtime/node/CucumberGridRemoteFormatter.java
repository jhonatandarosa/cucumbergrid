package org.cucumbergrid.junit.runtime.node;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import org.cucumbergrid.junit.runtime.node.CucumberGridClient;

import java.util.List;

public class CucumberGridRemoteFormatter implements Formatter {

    private CucumberGridClient client;

    public CucumberGridRemoteFormatter(CucumberGridClient client) {
        this.client = client;
    }
    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {

    }

    @Override
    public void uri(String uri) {

    }

    @Override
    public void feature(Feature feature) {

    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {

    }

    @Override
    public void examples(Examples examples) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void background(Background background) {

    }

    @Override
    public void scenario(Scenario scenario) {

    }

    @Override
    public void step(Step step) {

    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void done() {

    }

    @Override
    public void close() {

    }

    @Override
    public void eof() {

    }
}
