package com.jd.workflow.soap.common.cache;

public interface ICacheExpireListener<T> {
    public void onExpire(T obj);
}
