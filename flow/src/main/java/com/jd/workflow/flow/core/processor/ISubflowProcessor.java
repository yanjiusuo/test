package com.jd.workflow.flow.core.processor;

import com.jd.workflow.flow.core.input.Input;
import com.jd.workflow.flow.core.output.Output;

public interface ISubflowProcessor {
    public Output execSubflow(String subflowId, Input input);
}
