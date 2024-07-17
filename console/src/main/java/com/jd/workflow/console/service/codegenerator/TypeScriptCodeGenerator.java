package com.jd.workflow.console.service.codegenerator;

import com.jd.workflow.codegen.SingleClassGenerator;
import com.jd.workflow.console.service.ICodeGenerator;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.springframework.stereotype.Service;

@Service
public class TypeScriptCodeGenerator implements ICodeGenerator {
    @Override
    public String getType() {
        return "ts";
    }

    @Override
    public String generateEntityModel(JsonType jsonType) {
        SingleClassGenerator generator = new SingleClassGenerator();
        return generator.generateModel((ObjectJsonType) jsonType,"ts");
    }
}
