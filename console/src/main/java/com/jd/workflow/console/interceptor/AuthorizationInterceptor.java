package com.jd.workflow.console.interceptor;


import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.enums.AuthorizationKeyTypeEnum;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：example
 * 类 名 称：AuthorizationInterceptor
 * 类 描 述：数据权限拦截器
 * 创建时间：2022-05-24 17:54
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Order(4)
public class AuthorizationInterceptor extends BaseInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authorization annotation = null;
        try {
            if (handler instanceof HandlerMethod) {
                annotation = (Authorization)((HandlerMethod)handler).getMethodAnnotation(Authorization.class);
                if (annotation != null) {
                    if(!checkPass(annotation, request)){
                        responsePermission(response);
                        return false;
                    }
                }
                return true;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("数据权限拦截异常:" ,e);
            //return false;
            throw e;
        }
    }

    /**
     * 返回没有权限
     * @param response
     * @throws IOException
     */
    private void responsePermission(HttpServletResponse response) throws IOException {
        log.info("AuthorizationInterceptor 没有权限进行该操作");
        ServiceErrorEnum error = ServiceErrorEnum.NO_OPERATION_PERMISSION;
        CommonResult commonResult = CommonResult.buildErrorCodeMsg(error.getCode(), error.getMsg());
        setHttpJsonBody(commonResult,response);
    }
    private Boolean checkPass(Authorization annotation, HttpServletRequest request) throws IOException {
        if (annotation != null) {
            String key = annotation.key();
            AuthorizationKeyTypeEnum authorizationKeyTypeEnum = annotation.keyType();
            if (StringUtils.isBlank(key) || authorizationKeyTypeEnum==null) {
                return true;
            }
            String authCode = null;
            if(annotation.parseType() == ParseType.PARAM){
                authCode = request.getParameter(key);
            }else{
                authCode = parseBody(read(new BufferedReader(new InputStreamReader(request.getInputStream()))),key);
            }
            if(authCode==null){
                return true;
            }
            String username = this.getUsername();
            if (username != null) {
                switch (authorizationKeyTypeEnum){
                    case INTERFACE:
                        return this.userPrivilegeHelper.hasInterfaceRole(Long.parseLong(authCode),username);
                    case METHOD:
                        return this.userPrivilegeHelper.hasPrivilegeByMethodId(Long.parseLong(authCode),username);
                    case RELATION:
                        return this.userPrivilegeHelper.hasPrivilegeByRelationId(Long.parseLong(authCode),username);
                    default:
                        break;
                }
            }
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception e) throws Exception {

    }

    private UserPrivilegeHelper userPrivilegeHelper;

    public AuthorizationInterceptor() {
    }
    @Autowired
    public void setUserPrivilegeHelper(UserPrivilegeHelper userPrivilegeHelper) {
        this.userPrivilegeHelper = userPrivilegeHelper;
    }

    private static String parseBody(String json,String key){
        Map object;
        if(isJsonArray(json)){
            List<Map> arrayObjs = JsonUtils.parseArray(json,Map.class);
            if(arrayObjs==null||arrayObjs.size()==0){
                object = null;
            }else{
                object = arrayObjs.get(0);
            }
        }else{
            object = JsonUtils.parse(json,Map.class);
        }
        if(object==null)return null;
        return getValueByKey(object,key);
    }


    private static boolean isJsonArray(String sourceJson){
        char first = sourceJson.charAt(0);
        char last = sourceJson.charAt(sourceJson.length() - 1);
        if (first == '[' && last == ']'){
            return true;
        }
        return false;
    }


    private static String getValueByKey(Map sourceJson, String key) {
        Object obj=sourceJson;
        for(String s : key.split("\\.")) {
            if(!s.isEmpty()) {
                if(!(s.contains("[") || s.contains("]"))) {
                    obj=((Map) obj).get(s);
                }else if(s.contains("[") || s.contains("]")) {
                    List mid = (List) ((Map) obj).get(s.split("\\[")[0]);
                    if(mid==null||mid.size()==0){
                        return null;
                    }
                    int idx = Integer.parseInt(s.split("\\[")[1].replaceAll("]", ""));
                    if(mid.size()<=idx){
                        return null;
                    }
                    obj = mid.get(idx);
                }
                if(obj==null){
                    return null;
                }
            }
        }
        return obj!=null?obj.toString():null;
    }

}