package com.jd.workflow.flow.parser;

import com.jd.workflow.flow.core.definition.*;

import java.util.HashMap;
import java.util.Map;

public class StepParserRegistry {
    static Map<String, Class<? extends StepDefinition>> stepsParser = new HashMap<>();
    static{
        register("choice", ChoiceDefinition.class);
        register("choose", ChoiceDefinition.class);
        register("multicast", MulticastDefinition.class);
        register("subflow", SubflowDefinition.class);
        register("default", BeanStepDefinition.class);
    }
    public static void register(String type,Class<? extends StepDefinition> clazz){
        stepsParser.put(type, clazz);
    }
    public static Class<? extends StepDefinition>  getType(String type){
        return stepsParser.get(type);
    }
}
