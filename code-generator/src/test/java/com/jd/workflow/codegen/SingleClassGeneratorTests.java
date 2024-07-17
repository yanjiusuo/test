package com.jd.workflow.codegen;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.codegen.dto.GeneratedCodeDto;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class SingleClassGeneratorTests extends BaseTestCase {
    @Test
    public void generateModelTests(){
        String content = getResourceContent("classpath:models/obj-type.json");
        ObjectJsonType jsonType = (ObjectJsonType) JsonUtils.parse(content, JsonType.class);
        SingleClassGenerator generator = new SingleClassGenerator();
        String javaCode = generator.generateModel(jsonType,"java");
        String typescriptCode = generator.generateModel(jsonType,"ts");
        System.out.println("==========java=================");
        System.out.println(javaCode);
        System.out.println("==========typescript=================");
        System.out.println(typescriptCode);
    }


    @Test
    public void generateSingleTypeScript(){
        String content = getResourceContent("classpath:models/test-http-model.json");
        HttpMethodModel methodModel = (HttpMethodModel) JsonUtils.parse(content, HttpMethodModel.class);
        SingleClassGenerator generator = new SingleClassGenerator();
        List<FileCode> result = generator.generateSingleMethodCode(methodModel,"ts");

        System.out.println("==========result=================");
        System.out.println(result);
        System.out.println("==========typescript=================");
        System.out.println(result);
    }

    @Test
    public void generateSingleJava(){
        String content = getResourceContent("classpath:models/test-http-model.json");
        HttpMethodModel methodModel = (HttpMethodModel) JsonUtils.parse(content, HttpMethodModel.class);
        SingleClassGenerator generator = new SingleClassGenerator();
        List<FileCode> result = generator.generateSingleMethodCode(methodModel,"java");

        System.out.println("==========result=================");
        System.out.println(result);
        System.out.println("==========typescript=================");
        System.out.println(result);
    }

    @Test
    public void testSimpleType(){
        String content = getResourceContent("classpath:models/test-simple-type.json");
        HttpMethodModel methodModel = (HttpMethodModel) JsonUtils.parse(content, HttpMethodModel.class);
        SingleClassGenerator generator = new SingleClassGenerator();
        List<FileCode> result = generator.generateSingleMethodCode(methodModel,"java");

        System.out.println("==========result=================");
        System.out.println(result);
        System.out.println("==========typescript=================");
        System.out.println(result);
    }
}
