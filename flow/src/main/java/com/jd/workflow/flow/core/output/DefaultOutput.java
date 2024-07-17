package com.jd.workflow.flow.core.output;

import java.util.Map;

public class DefaultOutput extends BaseOutput{
    Object body;

    @Override
    public Object getBody() {
        return body;
    }

    @Override
    public void setBody(Object body) {
        this.body = body;
    }
}
