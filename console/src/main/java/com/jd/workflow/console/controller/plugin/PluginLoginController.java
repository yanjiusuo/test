package com.jd.workflow.console.controller.plugin;


import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.plugin.PluginConfig;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.console.service.plugin.PluginLoginService;
import com.jd.workflow.console.service.plugin.PluginUpdateService;
import com.jd.workflow.soap.common.exception.StdException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * idea插件更新
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/pluginSupport")
@UmpMonitor
public class PluginLoginController {
     @Autowired
     PluginUpdateService pluginUpdateService;

    @Autowired
    PluginLoginService pluginLoginService;
    @RequestMapping("/loginSuccess")
    public String loginSuccess(){
        return "login success";
    }

    @RequestMapping("/login")
    public void login(String reqId,HttpServletResponse response){
        log.info("login reqId:{}",reqId);
        String loginedUser = pluginLoginService.getUser(reqId);
        if(StringUtils.isBlank(loginedUser)){
            pluginLoginService.setUser(reqId, UserSessionLocal.getUser().getUserId());
        }
        try {
            response.sendRedirect("/pluginSupport/loginSuccess");
        } catch (IOException e) {
            throw StdException.adapt(e);
        }
    }

}
