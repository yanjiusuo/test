package com.jd.workflow.console.base.annotation;

import java.lang.annotation.*;

/**
 * 是否开启性能监控，开启的话会自动将方法调用情况注册,
 * 可用来注解controller类或者controller方法,方便开启ump监控
 * 实现参考 {@link com.jd.mpaas.console.web.interceptor.UmpInterceptor}
 *
 * @author wangjingfang3
 * {@link }
 * CallerInfo callerInfo = Profiler.registerInfo(name,profiler.enableHeartbeat(),profiler.enableTP());
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface UmpMonitor {
    public String name() default "";

    public boolean enableHeartbeat() default false;

    public boolean enableTP() default true;
}
