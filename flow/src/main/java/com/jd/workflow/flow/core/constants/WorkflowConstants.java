package com.jd.workflow.flow.core.constants;

public interface WorkflowConstants {
    public static final String MVEL_ENV_EXPR = "Object stepContext = exchange.getProperty(\"stepContext\");\n" +
            "        Object workflow = stepContext.buildEnv().get(\"workflow\");\n" +
            "        Map steps = stepContext.steps;";

    public static final int DEFAULT_CHILD_KEY = 0;

    public static void main(String[] args) {

    }


}
