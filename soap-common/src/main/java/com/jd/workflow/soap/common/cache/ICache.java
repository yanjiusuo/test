package com.jd.workflow.soap.common.cache;

import java.util.function.Function;

public interface ICache {

    public void hSet(String key, String field, Object obj,int seconds);
    public <T> T hGet(String key, String field);
    public <T> T hRemove(String key,String field);

}
