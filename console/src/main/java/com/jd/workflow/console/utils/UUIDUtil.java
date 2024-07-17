package com.jd.workflow.console.utils;

import java.util.UUID;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/29
 */
public class UUIDUtil {
    public UUIDUtil() {
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }
}
