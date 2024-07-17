package com.jd.workflow.console.config.dao;

/**
 * 标记是否是导入
 */
public class MetaContextHelper {
    private static final ThreadLocal<Boolean> skipModify = new ThreadLocal<>();

    /**
     * 设置用户信息
     */
    public static void skipModify(boolean skip) {
        skipModify.set(skip);
    }

    /**
     * 获取登录用户信息
     */
    public static Boolean isSkipModify() {
        return skipModify.get();
    }

    /**
     * 清除缓存信息
     */
    public static void clearModifyState() {
        skipModify.remove();
    }
}
