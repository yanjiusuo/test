package com.jd.workflow.flow.core.expr;

import org.mvel2.integration.PropertyHandler;
import org.mvel2.integration.VariableResolverFactory;

import java.util.HashMap;
import java.util.Map;

public class MapPropertyHandler implements PropertyHandler {
    @Override
    public Object getProperty(String name, Object contextObj, VariableResolverFactory variableFactory) {
        if(contextObj == null){
            return null;
        }
        Map map = (Map) contextObj;
        return map.get(name);
    }

    @Override
    public Object setProperty(String name, Object contextObj, VariableResolverFactory variableFactory, Object value) {
        if(contextObj == null){
            return null;
        }
        Map map = (Map) contextObj;
        map.put(name,value);
        return value;
    }

    public static void main(String[] args) {

    }
}
