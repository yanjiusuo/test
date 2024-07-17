package com.jd.workflow.flow.core.output;

import com.jd.workflow.flow.core.exception.WebServiceError;
import com.jd.workflow.flow.core.input.HttpInput;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class WebServiceOutput extends HttpOutput{

    WebServiceError error;
}
