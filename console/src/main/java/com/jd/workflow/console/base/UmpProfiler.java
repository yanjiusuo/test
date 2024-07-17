package com.jd.workflow.console.base;

import com.jd.ump.profiler.CallerInfo;
import com.jd.ump.profiler.proxy.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class UmpProfiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmpProfiler.class);

    public UmpProfiler() {
    }

    public static CallerInfo register(String key) {
        return register(key, false, true);
    }

    public static CallerInfo register(String key, boolean enableHeartbeat, boolean enableTP) {
        try {
            if (!StringUtils.isEmpty(key)) {
                return Profiler.registerInfo(key, enableHeartbeat, enableTP);
            }
        } catch (Exception var4) {
            LOGGER.error("ump error", var4);
        }

        return null;
    }

    public static void success(CallerInfo info) {
        try {
            if (info != null) {
                Profiler.registerInfoEnd(info);
            }
        } catch (Exception var2) {
            LOGGER.error("ump error", var2);
        }

    }

    public static void error(CallerInfo info) {
        try {
            if (info != null) {
                Profiler.functionError(info);
            }
        } catch (Exception var2) {
            LOGGER.error("ump error", var2);
        }

    }

    public static void registerJVM(String key) {
        try {
            if (!StringUtils.isEmpty(key)) {
                Profiler.registerJVMInfo(key);
            }
        } catch (Exception var2) {
            LOGGER.error("ump error", var2);
        }

    }
}