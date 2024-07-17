package com.jd.workflow.console.interceptor;

import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SelfLoginInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SelfLoginInterceptor.class);

    @Autowired
    LoginService loginService;
    @Value("${interceptor.loginType}")
    private Integer loginType;


    public SelfLoginInterceptor() {
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        if(!LoginTypeEnum.SELF.getCode().equals(loginType)){
            return true;
        }
        UserInfo userInfo = loginService.getLoginUserInfo(request);
        if(userInfo != null){
            UserInfoInSession user = new UserInfoInSession(userInfo.getUserCode(), userInfo.getUserName());
            user.setTenantId("0");
            UserSessionLocal.setUser(user);
            LoginService.setLoginUser(request,user);
            return true;
        }
        if (this.isAjaxRequest(request)) {
            response.sendError(401);
        }else{
            response.sendRedirect("/pages/login/login.html");
        }
        return false;
    }
    protected boolean isAjaxRequest(HttpServletRequest request) {
        boolean isAjaxReuest = false;

            isAjaxReuest = request.getHeader("X-Requested-With") != null;


        return isAjaxReuest;
    }

}
