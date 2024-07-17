package com.jd.workflow.console.filter;

import com.alibaba.fastjson.JSON;
import com.jd.jsf.gd.filter.AbstractFilter;
import com.jd.jsf.gd.msg.RequestMessage;
import com.jd.jsf.gd.msg.ResponseMessage;
import com.jd.jsf.gd.util.RpcContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsfLogFilter extends AbstractFilter {


/* @Resource
    DuccConfig duccConfig;*/
    /**
     * ducc打印日志开关
     *
     * @author xieguangyao3
     * @date 2021/12/02
     */
    private static final String PRINT_LOG_SWITCH = "print.log.switch";

    @Override
    public ResponseMessage invoke(RequestMessage request) {
        // 本端是服务消费者
        if (RpcContext.getContext().isConsumerSide()) {
            // 添加监控和日志
            return invokeConsumer(request);
        }
        // 本端是服务提供者
        return this.getNext().invoke(request);
    }

    private ResponseMessage invokeConsumer(RequestMessage request) {
        // 创建监控KEY
        String umpKey = String.join(".", "jsf", "consumer", request.getClassName(), request.getMethodName());
        // 开启监控
        // CallerInfo callerInfo = umpHelper.registerInfo(umpKey);
        // 调用计时开始
        long invokeStart = System.currentTimeMillis();
        // 请求JSF-CONSUMER
        ResponseMessage response = this.getNext().invoke(request);
        // 调用计时结束
        long invokeEnd = System.currentTimeMillis();
        // 处理请求结果
        if (response.isError()) {
            // 请求报错-记录UMP方法可用率
            // Profiler.functionError(callerInfo);
            // 打印异常信息
            if (log.isErrorEnabled()) {
                log.error("Request Exception:UMP-Key={}, Alias={}, RequestParam={}, Consume={}ms, Msg={}",
                        umpKey, request.getAlias(), JSON.toJSONString(request.getInvocationBody().getArgs()), invokeEnd - invokeStart, response.getException().getMessage());
            }

        } else {
            // 请求成功-打印出入参和调用耗时
            if (getPrintLog()) {
                log.info("Request Complete:UMP-Key={}, Alias={}, RequestParam={}, ResponseParam={}, Consume={}ms",
                        umpKey, request.getAlias(), JSON.toJSONString(request.getInvocationBody().getArgs()), JSON.toJSONString(response.getResponse()), invokeEnd - invokeStart);
            }
        }
        // 移除监控
        //Profiler.registerInfoEnd(callerInfo);
        // 返回结果
        return response;
    }

    /**
     * 获取日志打印开关
     *
     * @return
     */
    private Boolean getPrintLog() {
        return true;//duccConfig.getSwitchWithDefault(PRINT_LOG_SWITCH, false);
    }
}