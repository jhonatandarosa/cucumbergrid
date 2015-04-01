package org.cucumbergrid.junit.runtime.hub;

import cucumber.api.PendingException;
import cucumber.runtime.junit.ExecutionUnitRunner;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.Runtime.isPending;

public class CucumberGridReporter implements Reporter, Formatter {
    private final List<Step> steps = new ArrayList<Step>();

    private final Reporter reporter;
    private final Formatter formatter;
    private final boolean strict;

    public CucumberGridReporter(Reporter reporter, Formatter formatter, boolean strict) {
        this.reporter = reporter;
        this.formatter = formatter;
        this.strict = strict;
    }

    public void match(Match match) {
        reporter.match(match);
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        reporter.embedding(mimeType, data);
    }

    @Override
    public void write(String text) {
        reporter.write(text);
    }

    public void result(Result result) {
        reporter.result(result);
    }

    @Override
    public void before(Match match, Result result) {
        reporter.before(match, result);
    }

    @Override
    public void after(Match match, Result result) {
        reporter.after(match, result);
    }

    @Override
    public void uri(String uri) {
        formatter.uri(uri);
    }

    @Override
    public void feature(gherkin.formatter.model.Feature feature) {
        formatter.feature(feature);
    }

    @Override
    public void background(Background background) {
        formatter.background(background);
    }

    @Override
    public void scenario(Scenario scenario) {
        formatter.scenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        formatter.scenarioOutline(scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        formatter.examples(examples);
    }

    @Override
    public void step(Step step) {
        steps.add(step);
        formatter.step(step);
    }

    @Override
    public void eof() {
        formatter.eof();
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        formatter.syntaxError(state, event, legalEvents, uri, line);
    }

    @Override
    public void done() {
        formatter.done();
    }

    @Override
    public void close() {
        formatter.close();
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        formatter.startOfScenarioLifeCycle(scenario);
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        formatter.endOfScenarioLifeCycle(scenario);
    }
}
