package com.jd.workflow.console.entity;

import java.util.Map;

public interface IMethodInfo {
    public Long getKeyId();
    public Object getContentObject();
    public void setContentObject(Object content);
    public String getDocInfo();
    public void setDocInfo(String docInfo);
    public Integer getType();
    public void setDigest(String digest);

    public String getContent();
    public String getName();
    public String getDesc();
    public String getHttpMethod();

    public default void setDelta(Map<String,Object> delta){}
;}
