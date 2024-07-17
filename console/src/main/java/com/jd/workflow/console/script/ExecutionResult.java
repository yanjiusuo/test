package com.jd.workflow.console.script;

import lombok.Data;

@Data
public class ExecutionResult {

    /**
     * 标志位
     */
    private final String key;

    /**
     *
     */
    private final String output;
    /**
     *
     */
    private final Exception exception;

}
