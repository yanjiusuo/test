package com.jd.workflow.console.controller.plugin;


import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.plugin.HotDeployDto;
import com.jd.workflow.console.dto.plugin.PluginConfig;
import com.jd.workflow.console.dto.plugin.jdos.JdosGroup;
import com.jd.workflow.console.dto.plugin.jdos.JdosPod;
import com.jd.workflow.console.dto.plugin.jdos.JdosSystemApps;
import com.jd.workflow.console.service.plugin.HotswapDeployInfoService;
import com.jd.workflow.console.service.plugin.PluginLoginService;
import com.jd.workflow.console.service.plugin.PluginUpdateService;
import com.jd.workflow.console.service.plugin.jdos.JdosJcd;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * idea插件更新
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/pluginJdos")
@UmpMonitor
public class PluginJdosController {
    
     @Resource
     JdosJcd jdosJcd;

    @RequestMapping("/getSystemApps")
    public CommonResult<List<JdosSystemApps>> getSystemApps(String erp){
        List<JdosSystemApps> systemApps = jdosJcd.getSystemApps(erp);
        return CommonResult.buildSuccessResult(systemApps);
    }

    @RequestMapping("/getGroups")
    public CommonResult<List<JdosGroup>> getGroups(String appCode){
        List<JdosGroup> groups = jdosJcd.getGroups(appCode);
        return CommonResult.buildSuccessResult(groups);
    }

    @RequestMapping("/getIps")
    public CommonResult<List<JdosPod>> getIps(String appCode,String groupName){
        List<JdosPod> ips = jdosJcd.getIps(appCode, groupName);
        return CommonResult.buildSuccessResult(ips);
    }

}
