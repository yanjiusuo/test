package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.LoginDto;
import com.jd.workflow.console.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@Slf4j
@RequestMapping("/login")
@UmpMonitor
public class LoginController {
    @Autowired
    LoginService loginService;
    @PostMapping("/login")
    @ResponseBody
    public CommonResult<Boolean> login(HttpServletResponse response, @RequestBody @Valid LoginDto dto){
        log.info("user.login:user_info:name={}",dto.getUserName());
        boolean result = loginService.login(dto,response);
        if(result){
            return CommonResult.buildSuccessResult(result);
        }
        return CommonResult.error("用户名或者密码错误");
    }

}
