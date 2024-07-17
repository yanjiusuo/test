package com.jd.workflow.console.dto.jsf;

import com.jd.jsf.gd.util.NetUtils;
import com.jd.jsf.open.api.vo.Request;
import com.jd.jsf.open.api.vo.request.QueryInterfaceRequest;
import com.jd.jsf.open.api.vo.request.QueryMethodInfoRequest;
import com.jd.jsf.open.api.vo.request.QueryProviderRequest;
import com.jd.workflow.soap.common.exception.StdException;
import org.springframework.stereotype.Component;

/**
 * @author wufagang
 * @description
 * @date 2023年06月20日 14:05
 */
@Component
public class JSFArgBuilder {
    private static final String appKey = "jdos_data-flow";//Your appName in Jsf admin
    private static final String token = "abc31"; //Your appName  token 如果线上环境可以在管理端app管理页面中点击修改按钮进行手动添加
    private static final String operator = "wangjingfang3";

    public static <T extends Request> T buildBaseReq(Class<T> clazz){
        try {
            T req = clazz.newInstance();
            req.setAppKey(appKey);
            req.setOperator(operator);
            req.setTimeStamp(System.currentTimeMillis());
            req.setSign(req.sign(token));
            req.setClientIp(NetUtils.getLocalHost());
            return req;
        } catch (Exception e) {
            throw StdException.adapt(e);
        }
    }
    public static QueryInterfaceRequest buildQueryInterfaceRequest(){
        QueryInterfaceRequest req = new QueryInterfaceRequest();
        req.setAppKey(appKey);
        req.setOperator(operator);
        req.setTimeStamp(System.currentTimeMillis());
        req.setSign(req.sign(token));
        req.setClientIp(NetUtils.getLocalHost());
        return req;
    }

    public static QueryProviderRequest buildQueryProviderRequest(){
        QueryProviderRequest req = new QueryProviderRequest();
        req.setAppKey(appKey);
        req.setOperator(operator);
        req.setTimeStamp(System.currentTimeMillis());
        req.setSign(req.sign(token));
        req.setClientIp(NetUtils.getLocalHost());
        return req;
    }

    /**
     * 构建设计请求数据
     * @param req
     * @param <T>
     * @return
     */
    public static <T> T buildSetRequestInfo(Request req){
        req.setAppKey(appKey);
        req.setOperator(operator);
        req.setTimeStamp(System.currentTimeMillis());
        req.setSign(req.sign(token));
        req.setClientIp(NetUtils.getLocalHost());
        return (T) req;
    }

}
