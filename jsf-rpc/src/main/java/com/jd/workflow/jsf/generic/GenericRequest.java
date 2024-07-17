package com.jd.workflow.jsf.generic;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 项目名称：parent
 * 类 名 称：GenericRequest
 * 类 描 述：方法请求参数
 * 创建时间：2022-06-28 09:45
 * 创 建 人：wangxiaofei8
 */
@Data
public class GenericRequest {

    /**
     * JSF方法名稱
     */
    private String method;

    /**
     * 方法参数列表
     */
    private String[] parameterTypes;

    /**
     * 参数对象
     */
    private Object[] args;


    public static GenericRequest buildRequest(String method,String parameterTypeStr, String parameterNameStr, Map<String,Object> paramValues) {
        GenericRequest request = new GenericRequest();
        request.setMethod(method);
        if (StringUtils.isBlank(parameterTypeStr)) {
            request.setParameterTypes(new String[] {});
        }else {
            request.setParameterTypes(StringUtils.split(parameterTypeStr, ","));
        }
        if (StringUtils.isBlank(parameterNameStr)) {
            request.setArgs(new Object[]{});
        }else {
            String[] webParmers = StringUtils.split(parameterNameStr, ",");
            Object[] args = new Object[webParmers.length];
            for (int i = 0; i < webParmers.length; i++) {
                args[i] = paramValues.get(webParmers[i]);
            }
            request.setArgs(args);
        }
        return request;
    }

}
