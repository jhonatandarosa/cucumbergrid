package org.cucumbergrid.junit.runtime;

import cucumber.runtime.Runtime;
import cucumber.runtime.junit.ExecutionUnitRunner;
import cucumber.runtime.junit.JUnitReporter;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.Step;
import org.cucumbergrid.junit.runtime.CucumberUtils;
import org.cucumbergrid.junit.runtime.node.CucumberGridNodeRuntime;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

public class CucumberGridExecutionUnitRunner extends ExecutionUnitRunner {

    private CucumberGridRuntime cucumberGridRuntime;
    private CucumberScenario cucumberScenario;
//    private Description description;

    public CucumberGridExecutionUnitRunner(CucumberGridRuntime gridRuntime, Runtime runtime, CucumberScenario cucumberScenario, JUnitReporter jUnitReporter) throws InitializationError {
        super(runtime, cucumberScenario, jUnitReporter);
        this.cucumberGridRuntime = gridRuntime;
        this.cucumberScenario = cucumberScenario;
    }

    @Override
    protected Description describeChild(Step step) {
        return cucumberGridRuntime.getDescription(cucumberScenario, step);
    }

    @Override
    public Description getDescription() {
        return cucumberGridRuntime.getDescription(cucumberScenario);
        /*
        if (description == null) {
            description = Description.createSuiteDescription(getName(), CucumberUtils.getUniqueID(cucumberScenario));

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
                    description.addChild(describeChild(copy));
                }
            }

            for (Step step : getChildren()) {
                description.addChild(describeChild(step));
            }
        }
        return description;*/
    }
}
