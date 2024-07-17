package com.jd.workflow.console.script;

public class ScriptExecutorFactory {
    public static ScriptExecutor createExecutor(ScriptType type) {
        switch (type) {
            case SHELL:
                return new ShellExecutor();
            default:
                throw new IllegalArgumentException("Unsupported script type: " + type);
        }
    }
}
