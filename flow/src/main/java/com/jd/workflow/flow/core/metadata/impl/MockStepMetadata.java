package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.metadata.StepMetadata;
import lombok.Data;

import java.util.Map;

/**
 *  mock步骤，可以跳过这些步骤
 */
@Data
public class MockStepMetadata extends StepMetadata {
    Map<String,Object> input;
    Map<String,Object> output;
}
