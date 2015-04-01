package org.cucumbergrid.junit.runtime.common;

public enum FormatMessageID {
    // Formatter interface
    URI,
    FEATURE,
    SCENARIO_OUTLINE,
    EXAMPLES,
    START_SCENARIO_LIFE_CYCLE,
    BACKGROUND,
    SCENARIO,
    STEP,
    END_SCENARIO_LIFE_CYCLE,
    EOF,
    SYNTAX_ERROR,

    // Reporter interface
    BEFORE,
    RESULT,
    AFTER,
    MATCH,
    EMBEDDING,
    WRITE
}
