package com.jd.workflow.console.controller.plugin;

import com.jd.fastjson.JSON;
import com.jd.jsf.open.api.domain.Server;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.FlowDebugDto;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.NewJsfDebugDto;
import com.jd.workflow.console.dto.jsf.PluginCallDto;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.entity.debug.dto.PluginCallLog;
import com.jd.workflow.console.service.DebugService;
import com.jd.workflow.console.service.debug.FlowDebugLogService;
import com.jd.workflow.console.service.plugin.JsfOpenService;
import com.jd.workflow.console.service.plugin.PluginLoginService;
import com.jd.workflow.console.service.plugin.jsf.JsfInputInfo;
import com.jd.workflow.console.service.plugin.jsf.MethodResponse;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * jsf开放平台
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/jsfApi")
@UmpMonitor
public class JsfOpenController {
    @Autowired
    private JsfOpenService jsfOpenService;
    @Autowired
    DebugService debugService;

    @Autowired
    FlowDebugLogService flowDebugLogService;
    @RequestMapping("/queryAliasByInterfaceName")
    public CommonResult<List<String>> queryAliasByInterfaceName(String interfaceName){
        List<String> aliasList = jsfOpenService.queryAliasByInterfaceName(interfaceName);
        return CommonResult.buildSuccessResult(aliasList);
    }
    @RequestMapping("/queryMethodByAlias")
    public CommonResult<List<MethodResponse>> queryMethodByAlias(String interfaceName,
                                                                 @RequestParam(required = false)
                                                                 String alias,
                                                                 @RequestParam(required = false)String ipAndPort){
        List<MethodResponse> methodList = jsfOpenService.queryJsfMethods(interfaceName, alias, ipAndPort);
        return CommonResult.buildSuccessResult(methodList);
    }
    @RequestMapping("/queryProviders")
    public CommonResult<List<Server>> queryProviders(String interfaceName, String alias){
        List<Server> serverList = jsfOpenService.getValidProviders(interfaceName, alias);
        return CommonResult.buildSuccessResult(serverList);
    }
    @RequestMapping("/getInputJsfInputInfos")
    public CommonResult<JsfInputInfo> getInputJsfInputInfos(String interfaceName, String alias, String methodName){
        JsfInputInfo template = jsfOpenService.getInputJsfInputInfos(interfaceName, alias, methodName);
        return CommonResult.buildSuccessResult(template);
    }
    @RequestMapping("/getInputTemplate")
    public CommonResult<String> getInputTemplate(String interfaceName, String alias, String methodName){
        Object template = jsfOpenService.getInputTemplate(interfaceName, alias, methodName);
        return CommonResult.buildSuccessResult(JSON.toJSONString(template));
    }

    @RequestMapping("/queryAppAllJsfInterfaces")
    public CommonResult<List<String>> getAllJsfInterfaces(String appCode){
        List<String> appCodes = jsfOpenService.queryTestAndOnlineInterface(appCode);
        return CommonResult.buildSuccessResult(appCodes);
    }
    @RequestMapping("/callJsfInterface")
    public CommonResult<JsfOutputExt> callJsfInterface( @RequestBody PluginCallDto dto){
        Guard.notEmpty(dto.getUserToken(),"userToken不能为空");
        Guard.notEmpty(dto.getInterfaceName(),"interfaceName不能为空");
        Guard.notEmpty(dto.getMethodName(),"methodName不能为空");
        Guard.notEmpty(dto.getEnv(),"env不能为空");
        try {
            PluginLoginService.UserBaseInfo userInfo = PluginLoginService.getUserInfo(dto.getUserToken());
            UserInfoInSession userInfoInSession = new UserInfoInSession();
            userInfoInSession.setUserId(userInfo.getUserName());
            UserSessionLocal.setUser(userInfoInSession);
        } catch (Exception e) {
            throw new BizException("无效的userToken", e);
        }
        NewJsfDebugDto jsfDebugDto = new NewJsfDebugDto();
        BeanUtils.copyProperties(dto, jsfDebugDto);
        jsfDebugDto.setInputData(JsonUtils.parseArray(dto.getInputData(), Object.class));
        JsfOutputExt output = debugService.debugJsfNew(jsfDebugDto);
        return CommonResult.buildSuccessResult(output);
    }
    @RequestMapping("/saveDebugRecord")
    public CommonResult<Boolean> saveDebugRecord(@RequestBody List<PluginCallLog> logs){
        Guard.notEmpty(logs,"logs不能为空");

        for (PluginCallLog log : logs) {
            Guard.notEmpty(log.getUserToken(),"userToken不能为空");
            try {
                PluginLoginService.UserBaseInfo userInfo = PluginLoginService.getUserInfo(log.getUserToken());
                UserInfoInSession userInfoInSession = new UserInfoInSession();
                userInfoInSession.setUserId(userInfo.getUserName());
                UserSessionLocal.setUser(userInfoInSession);
            } catch (Exception e) {
                throw new BizException("无效的userToken", e);
            }
        }
        boolean result = flowDebugLogService.savePluginCallLogs(logs);
        return CommonResult.buildSuccessResult(result);
    }

}
