package org.cucumbergrid.junit.runtime.node;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import org.cucumbergrid.junit.runtime.common.*;
import org.cucumbergrid.junit.runtime.node.CucumberGridClient;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class CucumberGridRemoteFormatter implements Formatter {

    private CucumberGridClient client;

    public CucumberGridRemoteFormatter(CucumberGridClient client) {
        this.client = client;
    }

    private void send(FormatMessageID messageID, Serializable... data) {
        FormatMessage formatMsg = new FormatMessage(messageID, data);
        Message msg = new Message(MessageID.FORMAT, formatMsg);
        try {
            client.send(IOUtils.serialize(msg));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        send(FormatMessageID.SYNTAX_ERROR, state, event, legalEvents.toArray(new String[legalEvents.size()]), uri, line);
    }

    @Override
    public void uri(String uri) {
        send(FormatMessageID.URI, uri);
    }

    @Override
    public void feature(Feature feature) {
        send(FormatMessageID.FEATURE, feature);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        send(FormatMessageID.SCENARIO_OUTLINE, scenarioOutline);
    }

    @Override
    public void examples(Examples examples) {
        send(FormatMessageID.EXAMPLES, examples);
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        send(FormatMessageID.START_SCENARIO_LIFE_CYCLE, scenario);
    }

    @Override
    public void background(Background background) {
        send(FormatMessageID.BACKGROUND, background);
    }

    @Override
    public void scenario(Scenario scenario) {
        send(FormatMessageID.SCENARIO, scenario);
    }

    @Override
    public void step(Step step) {
        send(FormatMessageID.STEP, step);
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        send(FormatMessageID.END_SCENARIO_LIFE_CYCLE, scenario);
    }

    @Override
    public void done() {

    }

    @Override
    public void close() {

    }

    @Override
    public void eof() {
        send(FormatMessageID.EOF);
    }
}
