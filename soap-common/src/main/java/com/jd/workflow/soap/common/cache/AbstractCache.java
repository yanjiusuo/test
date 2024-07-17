package com.jd.workflow.soap.common.cache;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCache implements ICache{
    public static String DEFAULT_CACHE_KEY = "__default__key";
    public static String PREFIX  = "_flow_";

    public String cachedKey(String key,String field){
        if(StringUtils.isEmpty(key)){
            key = DEFAULT_CACHE_KEY;
        }
        return PREFIX+":"+key+":"+field;
    }
}
