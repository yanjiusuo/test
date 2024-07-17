package com.jd.workflow.flow.core.bean;

import java.util.List;

public interface IBeanFactory<T,C> {
    public T init(C args );
    public default void destroy(T bean){

    }
}
