package com.jd.workflow.console.service;

import com.jd.workflow.soap.common.xml.schema.JsonType;

public interface ICodeGenerator {
    public String getType();
    public String generateEntityModel(JsonType jsonType);
}
