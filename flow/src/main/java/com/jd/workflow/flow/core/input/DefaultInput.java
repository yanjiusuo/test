package com.jd.workflow.flow.core.input;

import java.util.Map;

public class DefaultInput extends BaseInput{
    Object body;

    public DefaultInput() {

    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}

