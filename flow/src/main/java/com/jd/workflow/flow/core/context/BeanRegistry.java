package com.jd.workflow.flow.core.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface BeanRegistry {
  /*  Map<String,Object> beanInstances = new HashMap<>();
    Set<Class> utilsClass = new HashSet<>();*/

    public Object get(String name);
}
