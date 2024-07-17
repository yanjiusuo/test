package com.jd.workflow.console.controller.debug;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.controller.DebugController;
import com.jd.workflow.console.dto.jsf.JarJsfDebugDto;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.jsf.input.JsfOutput;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/forwardJsf")
public class ForwardJsfController {
    @Autowired
    JsfJarDebugController jsfJarDebugController;
    @Autowired
    DebugController debugController;
    private void initErp(HttpServletRequest request){
        String erp = request.getHeader("erp");
        if(erp == null){
            throw new BizException("erp不可为空");
        }
        UserInfoInSession userInfoInSession = new UserInfoInSession();
        userInfoInSession.setUserId(erp);
        userInfoInSession.setUserName(erp);
        UserSessionLocal.setUser(userInfoInSession);
    }
    @RequestMapping("reParseJsfJar")
    public CommonResult<Boolean> reParseJsfJar(@RequestBody MavenJarLocation location,HttpServletRequest req){
        initErp(req);
          return jsfJarDebugController.reParseJsfJar(location);
    }
    @PostMapping(value = "/jsfJarDebug")
    @ResponseBody
    public CommonResult<Object> jsfJarDebug(@RequestBody @Valid JarJsfDebugDto dto, HttpServletRequest req) {
        initErp(req);
        return debugController.jsfJarDebug(dto,req);
    }
}
