package com.jd.workflow.flow.core.attr;

import com.jd.workflow.flow.core.input.Input;
import com.jd.workflow.flow.core.output.Output;

public interface ExchangeSupport {
    Input getInput();
    Output getOutput();
}
