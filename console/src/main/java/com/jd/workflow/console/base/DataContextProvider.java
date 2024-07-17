package com.jd.workflow.console.base;

import com.jd.workflow.console.dto.context.DataContext;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-28
 */
public class DataContextProvider {

    private static final ThreadLocal<DataContext> LOCAL = new ThreadLocal<>();

    /**
     * 获取信息
     */
    public static DataContext getContext() {
        DataContext dataContext = LOCAL.get();
        if(dataContext == null){
            dataContext = new DataContext();
            LOCAL.set(dataContext);
        }
        return dataContext;
    }

    /**
     * 清除缓存信息
     */
    public static void clearDataContext() {
        LOCAL.remove();
    }

}
