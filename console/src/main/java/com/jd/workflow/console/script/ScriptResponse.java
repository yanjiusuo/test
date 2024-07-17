/*
 *
 */
package com.jd.workflow.console.script;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @param <T>
 */
@Data
public class ScriptResponse<T> implements Serializable {
    /**
     *
     */
    private int code;

    /**
     *
     */
    private boolean success;

    /**
     *
     */
    private String message;

    /**
     *
     */
    private String error;

    /**
     *
     */
    private T data;
}

