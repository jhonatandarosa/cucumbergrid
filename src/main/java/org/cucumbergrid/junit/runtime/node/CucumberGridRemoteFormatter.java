package org.cucumbergrid.junit.runtime.node;

import cucumber.runtime.StepDefinitionMatch;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.cucumbergrid.junit.runtime.common.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class CucumberGridRemoteFormatter implements Formatter, Reporter {

    private CucumberGridClient client;

    public CucumberGridRemoteFormatter(CucumberGridClient client) {
        this.client = client;
    }

    private void send(FormatMessageID messageID, Serializable... data) {
        FormatMessage formatMsg = new FormatMessage(messageID, data);
        Message msg = new Message(MessageID.FORMAT, formatMsg);
        try {
            client.send(IOUtils.serialize(msg));
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
        // nothing to report
    }

    @Override
    public void close() {
        // nothing to report
    }

    @Override
    public void eof() {
        send(FormatMessageID.EOF);
    }

    @Override
    public void before(Match match, Result result) {
        match = handleMatch(match);
        send(FormatMessageID.BEFORE, match, result);
    }

    @Override
    public void result(Result result) {
        send(FormatMessageID.RESULT, result);
    }

    @Override
    public void after(Match match, Result result) {
        match = handleMatch(match);
        send(FormatMessageID.AFTER, match, result);
    }

    @Override
    public void match(Match match) {
        match = handleMatch(match);
        send(FormatMessageID.MATCH, match);
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        send(FormatMessageID.EMBEDDING, mimeType, data);
    }

    @Override
    public void write(String text) {
        send(FormatMessageID.WRITE, text);
    }

    private Match handleMatch(Match match) {
        if (match instanceof StepDefinitionMatch) {
            match = new Match(match.getArguments(), match.getLocation());

        }
        return match;
    }
}
