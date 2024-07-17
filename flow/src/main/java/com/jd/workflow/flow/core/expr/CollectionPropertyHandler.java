package com.jd.workflow.flow.core.expr;

import org.mvel2.integration.PropertyHandler;
import org.mvel2.integration.VariableResolverFactory;

import java.util.List;

public class CollectionPropertyHandler implements PropertyHandler {
    @Override
    public Object getProperty(String name, Object contextObj, VariableResolverFactory variableFactory) {
        if(contextObj == null) return null;
        List list = (List) contextObj;
        int index = Integer.valueOf(name);
        return list.get(index);
    }

    @Override
    public Object setProperty(String name, Object contextObj, VariableResolverFactory variableFactory, Object value) {
        List list = (List) contextObj;
        int index = Integer.valueOf(name);
        list.set(index,value);
        return value;
    }
}
