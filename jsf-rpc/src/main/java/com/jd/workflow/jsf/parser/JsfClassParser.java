package com.jd.workflow.jsf.parser;

import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.parser.ClassParser;
import java.lang.reflect.Method;

public class JsfClassParser {
    private static JsfStepMetadata toJsfStepMetadata(MethodMetadata method){
        JsfStepMetadata jsfStepMetadata = new JsfStepMetadata();
        jsfStepMetadata.setInterfaceName(method.getInterfaceName());
        jsfStepMetadata.setMethodName(method.getMethodName());
        jsfStepMetadata.setInput(method.getInput());
        jsfStepMetadata.setOutput(method.getOutput());
        jsfStepMetadata.setExceptions(method.getExceptions());
        return jsfStepMetadata;
    }
    public static JsfStepMetadata buildMethodInfo(Method method){
         return toJsfStepMetadata(ClassParser.buildMethodInfo(method));
    }
    public static JsfStepMetadata buildMethodInfo(Class clazz, String methodName){
        return toJsfStepMetadata(ClassParser.buildMethodInfo(clazz, methodName));
    }
}
