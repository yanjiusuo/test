package com.jd.workflow.console.interceptor;

import com.jd.ump.profiler.CallerInfo;
import com.jd.ump.profiler.proxy.Profiler;
import com.jd.ump.profiler.util.StringUtil;
import com.jd.workflow.console.base.UmpProfiler;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.soap.common.exception.BizException;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ump默认的注解需要每个方法都写一个注解，
 * 更好的方法是一个controller里写一个注解,通过拦截器拦截处理，减少非必要的注解
 *
 * @author wangjingfang3
 */
public class UmpInterceptor implements HandlerInterceptor {

    public UmpInterceptor(){

    }

    private String umpAppName;

    private String systemKey;
    private String jvmKey;
    @PostConstruct
    public void init(){
            if (!StringUtil.isEmpty(this.systemKey)) {
            Profiler.InitHeartBeats(umpAppName+"."+this.systemKey);
        }

        if (!StringUtil.isEmpty(this.jvmKey)) {
            Profiler.registerJVMInfo(umpAppName+"."+this.jvmKey);
        }
    }
    public void setUmpAppName(String umpAppName) {
        this.umpAppName = umpAppName;
    }

    public void setSystemKey(String systemKey) {
        this.systemKey = systemKey;
    }

    public void setJvmKey(String jvmKey) {
        this.jvmKey = jvmKey;
    }

    static String ATTR_KEY_UMP = "profilerCallInfo";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            UmpMonitor profiler = method.getMethodAnnotation(UmpMonitor.class);
            if (profiler == null) {
                profiler = method.getBeanType().getAnnotation(UmpMonitor.class);
            }
            if (profiler != null) {
                String name = profiler.name();
                if (StringUtils.isEmpty(name)) {
                    name = umpAppName + ".web.controller." + method.getBeanType().getSimpleName() + "." + method.getMethod().getName();
                }
                CallerInfo callerInfo = UmpProfiler.register(name, profiler.enableHeartbeat(), profiler.enableTP());
                request.setAttribute(ATTR_KEY_UMP, callerInfo);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CallerInfo profilerCallInfo = (CallerInfo) request.getAttribute(ATTR_KEY_UMP);
        if (profilerCallInfo != null) {
            if (ex != null && !(ex instanceof BizException)) {
                UmpProfiler.error(profilerCallInfo);
            } else {
                UmpProfiler.success(profilerCallInfo);
            }
        }
    }
}
