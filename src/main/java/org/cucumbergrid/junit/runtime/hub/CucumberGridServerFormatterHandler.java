package org.cucumbergrid.junit.runtime.hub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import org.cucumbergrid.junit.runtime.common.FormatMessage;
import org.cucumbergrid.junit.runtime.common.FormatMessageID;

public class CucumberGridServerFormatterHandler {

    private CucumberGridReporter jUnitReporter;
    private Map<String, List<FormatMessage>> messages = new HashMap<>();

    public CucumberGridServerFormatterHandler(CucumberGridReporter jUnitReporter) {
        this.jUnitReporter = jUnitReporter;
    }

    public boolean hasUnprocessedMessages() {
        return !messages.isEmpty();
    }

    /**
     * @param token a uniquely identifier to node that sent the message
     * @param message the message
     */
    public void onFormatMessage(String token, FormatMessage message) {
        getMessages(token).add(message);
        if (message.getID() == FormatMessageID.EOF) {
            flushMessages(token);
        }
    }

    /**
     * @param token a uniquely identifier to node that sent the message
     * @return the messages associated with the given {@code token}
     */
    private List<FormatMessage> getMessages(String token) {
        List<FormatMessage> formatMessages = messages.get(token);
        if (formatMessages == null) {
            formatMessages = new ArrayList<>();
            messages.put(token, formatMessages);
        }
        return formatMessages;
    }

    private synchronized void flushMessages(String token) {
        List<FormatMessage> formatMessages = getMessages(token);
        for (FormatMessage message : formatMessages) {
            process(message);
        }
        messages.remove(token);
    }

    private void process(FormatMessage message) {
        switch (message.getID()) {
            // Formatter interface
            case SYNTAX_ERROR:
                onSyntaxError(message);
                break;
            case URI:
                onUri(message);
                break;
            case FEATURE:
                onFeature(message);
                break;
            case SCENARIO_OUTLINE:
                onScenarioOutline(message);
                break;
            case EXAMPLES:
                onExamples(message);
                break;
            case START_SCENARIO_LIFE_CYCLE:
                onStartOfScenarioLifeCycle(message);
                break;
            case BACKGROUND:
                onBackground(message);
                break;
            case SCENARIO:
                onScenario(message);
                break;
            case STEP:
                onStep(message);
                break;
            case END_SCENARIO_LIFE_CYCLE:
                onEndOfScenarioLifeCycle(message);
                break;
            case EOF:
                onEOF(message);
                break;

            // Reporter interface
            case BEFORE:
                onBefore(message);
                break;
            case RESULT:
                onResult(message);
                break;
            case AFTER:
                onAfter(message);
                break;
            case MATCH:
                onMatch(message);
                break;
            case EMBEDDING:
                onEmbedding(message);
                break;
            case WRITE:
                onWrite(message);
                break;
        }
    }

    /////////////////////
    // Reportar interface
    private void onBefore(FormatMessage message) {
        Match match = message.getData(0);
        Result result = message.getData(1);
        jUnitReporter.before(match, result);
    }

    private void onResult(FormatMessage message) {
        Result result = message.getData(0);
        jUnitReporter.result(result);
    }

    private void onAfter(FormatMessage message) {
        Match match = message.getData(0);
        Result result = message.getData(1);
        jUnitReporter.after(match, result);
    }

    private void onMatch(FormatMessage message) {
        Match match = message.getData(0);
        jUnitReporter.match(match);
    }

    private void onEmbedding(FormatMessage message) {
        String mimeType = message.getData(0);
        byte[] data = message.getData(1);
        jUnitReporter.embedding(mimeType, data);
    }

    private void onWrite(FormatMessage message) {
        String text = message.getData(0);
        jUnitReporter.write(text);
    }

    ///////////////////////
    // Formatter interface
    private void onSyntaxError(FormatMessage message) {
        String state = message.getData(0);
        String event = message.getData(1);
        String[] legalEvents = message.getData(2);
        String uri = message.getData(3);
        Integer line = message.getData(4);
        jUnitReporter.syntaxError(state, event, Arrays.asList(legalEvents), uri, line);
    }

    private void onUri(FormatMessage message) {
        String uri = message.getData(0);
        jUnitReporter.uri(uri);
    }

    private void onFeature(FormatMessage message) {
        Feature feature = message.getData(0);
        jUnitReporter.feature(feature);
    }

    private void onScenarioOutline(FormatMessage message) {
        ScenarioOutline scenarioOutline = message.getData(0);
        jUnitReporter.scenarioOutline(scenarioOutline);
    }

    private void onExamples(FormatMessage message) {
        Examples examples = message.getData(0);
        jUnitReporter.examples(examples);
    }

    private void onStartOfScenarioLifeCycle(FormatMessage message) {
        Scenario scenario = message.getData(0);
        jUnitReporter.startOfScenarioLifeCycle(scenario);
    }

    private void onBackground(FormatMessage message) {
        Background background = message.getData(0);
        jUnitReporter.background(background);
    }

    private void onScenario(FormatMessage message) {
        Scenario scenario = message.getData(0);
        jUnitReporter.scenario(scenario);
    }

    private void onStep(FormatMessage message) {
        Step step = message.getData(0);
        jUnitReporter.step(step);
    }

    private void onEndOfScenarioLifeCycle(FormatMessage message) {
        Scenario scenario = message.getData(0);
        jUnitReporter.endOfScenarioLifeCycle(scenario);
    }

    private void onEOF(FormatMessage message) {
        jUnitReporter.eof();
    }

}