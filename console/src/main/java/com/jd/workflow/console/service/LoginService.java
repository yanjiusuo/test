package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import com.jd.workflow.console.dto.LoginDto;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.soap.common.cache.ICache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Random;
import java.util.UUID;

@Service
public class LoginService {
    public static String COOKIE_NAME = "user_ticket";
    public static String LOGIN_USER_INFO = "logined_user_info";
    public static String LOGIN_CACHE_KEY_PREFIX = "__login_user_info";
    /**
     * 2小时过期
     */
    @Value("${login.cookie_expire_time:7200}")
    int expireSeconds = 60*60*2;

    @Autowired
    IUserInfoService userService;


    @Autowired
    ICache cache;


    public boolean login(LoginDto dto, HttpServletResponse response){
        UserInfo user = userService.login(dto);

        if(user!= null){
            String ticket = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(COOKIE_NAME,ticket);
            cookie.setPath("/");
            cookie.setMaxAge(expireSeconds);
            UserInfo cacheUser = new UserInfo();
            cacheUser.setId(user.getId());
            cacheUser.setUserCode(user.getUserCode());
            cacheUser.setUserName(user.getUserName());
            cacheUser.setDept(user.getDept());
            cache.hSet(LOGIN_CACHE_KEY_PREFIX,ticket,cacheUser,expireSeconds);
            response.addCookie(cookie);
            return true;
        }
        return false;
    }
    public UserInfo getLoginUserInfo(HttpServletRequest request){
        if(request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if(COOKIE_NAME.equals(cookie.getName())){
                String value = cookie.getValue();
                UserInfo userInfo = cache.hGet(LOGIN_CACHE_KEY_PREFIX, value);
                if(userInfo != null){
                    return userInfo;
                }
                return null;
            }
        }
        return null;
    }
    public static UserInfoInSession getLoginUser(HttpServletRequest request){
        return (UserInfoInSession) request.getAttribute(LOGIN_USER_INFO);
    }
    public static void setLoginUser(HttpServletRequest request,UserInfoInSession user){
         request.setAttribute(LOGIN_USER_INFO,user);
    }
}
