package com.jd.workflow.console.script;

import java.util.Map;

import java.util.concurrent.Future;

public interface ScriptExecutor {
    /**
     *
     * @param key
     * @param scriptContent
     * @param parameters
     * @return
     */
    Future<ExecutionResult> executeAsync(String key,String scriptContent, Map<String, String> parameters);

    /**
     *
     * @param scriptContent
     * @param parameters
     */
    String execute(String scriptContent, Map<String, String> parameters);
}
