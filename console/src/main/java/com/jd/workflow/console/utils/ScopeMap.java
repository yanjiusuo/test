package com.jd.workflow.console.utils;

import java.util.HashMap;
import java.util.Map;

public class ScopeMap<K, V> {
    ScopeMap<K, V> parent;
    Map<K, V> container = new HashMap<>();

    public void put(K key, V value) {


        container.put(key, value);
    }

    public V get(K key) {
        if (container.containsKey(key)) {
            return container.get(key);
        }
        if (parent == null) return null;
        return parent.get(key);
    }
    public static ScopeMap<String,Object> of(Map<String,Object> map){
        ScopeMap<String,Object> scopeMap = new ScopeMap<>();
        scopeMap.container = map;
        return scopeMap;
    }
    public static <K, V> ScopeMap<K, V> of(Map<K, V> map,ScopeMap<K, V> parent) {
        ScopeMap<K, V> current = new ScopeMap<>();
        current.container = map;
        current.parent = parent;
        return current;
    }
}
