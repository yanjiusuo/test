package com.jd.workflow.console.dto.mock;

import lombok.Data;

import java.util.Map;

@Data
public class HttpDemoValue {
    Map<String,Object> inputHeaders;
    Map<String,Object> inputParams;
    Map<String,Object> inputPath;
    Object inputBody;
    Map<String,Object> outputHeaders;
    Object outputBody;
}
