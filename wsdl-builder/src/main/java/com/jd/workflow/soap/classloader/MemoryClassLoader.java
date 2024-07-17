package com.jd.workflow.soap.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载classLoader信息
 */
public class MemoryClassLoader extends URLClassLoader {
    Map<String, Class> classes = new HashMap();

    public MemoryClassLoader(URL[] urls) {
        super(urls, MemoryClassLoader.class.getClassLoader());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class ret = super.findClass(name);
        if(ret != null){
            classes.put(name,ret);
        }
        return ret;
    }

    public Map<String, Class> getClasses() {
        return classes;
    }
}
