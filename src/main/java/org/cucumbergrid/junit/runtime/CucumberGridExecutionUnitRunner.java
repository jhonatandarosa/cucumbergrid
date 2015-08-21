package org.cucumbergrid.junit.runtime;

import java.util.ArrayList;
import java.util.List;

import cucumber.runtime.Runtime;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

public class CucumberGridExecutionUnitRunner extends ExecutionUnitRunner {

    private CucumberGridRuntime cucumberGridRuntime;
    private CucumberScenario cucumberScenario;
    private List<Step> runnerSteps = new ArrayList<Step>();

    public CucumberGridExecutionUnitRunner(CucumberGridRuntime gridRuntime, Runtime runtime, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(runtime, cucumberScenario, jUnitReporter);
        this.cucumberGridRuntime = gridRuntime;
        this.cucumberScenario = cucumberScenario;

        createRunnerSteps();
    }

    private void createRunnerSteps() {
        if (cucumberScenario.getCucumberBackground() != null) {
            for (Step backgroundStep : cucumberScenario.getCucumberBackground().getSteps()) {
                // We need to make a copy of that step, so we have a unique one per scenario
                Step copy = new Step(
                        backgroundStep.getComments(),
                        backgroundStep.getKeyword(),
                        backgroundStep.getName(),
                        backgroundStep.getLine(),
                        backgroundStep.getRows(),
                        backgroundStep.getDocString()
                );
                runnerSteps.add(copy);
            }
        }

        for (Step step : getChildren()) {
            runnerSteps.add(step);
        }
    }

    @Override
    public List<Step> getRunnerSteps() {
        return runnerSteps;
    }

    @Override
    protected Description describeChild(Step step) {
        return cucumberGridRuntime.getDescription(cucumberScenario, step);
    }

    @Override
    public Description getDescription() {
        return cucumberGridRuntime.getDescription(cucumberScenario);
    }
}
