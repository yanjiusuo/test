package com.jd.workflow.console.dto.jsf;

import com.jd.workflow.flow.core.output.HttpOutput;
import lombok.Data;

@Data
public class HttpDebugData {
    HttpDebugDto input;
    HttpOutput output;
}
