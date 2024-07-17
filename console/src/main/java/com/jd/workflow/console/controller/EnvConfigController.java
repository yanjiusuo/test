package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.env.EnvConfigDto;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItem;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.env.IEnvConfigItemService;
import com.jd.workflow.console.service.env.IEnvConfigService;
import com.jd.workflow.console.service.env.impl.EnvConfigServiceImpl;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用管理
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/envConfig")
@UmpMonitor
@Api(value = "环境管理",tags="环境管理")
public class EnvConfigController {

    @Autowired
    private EnvConfigServiceImpl envConfigService;
    @Autowired
    private IEnvConfigItemService envConfigItemService;

    @Autowired
    IAppInfoService appInfoService;
    /**
     * 根据应用code或者需求空间获取环境列表
     */
    @GetMapping("/getEnvConfigList")
    public CommonResult<List<EnvConfig>> getEnvConfigList(@RequestParam(required = false) String appCode,
                                                          @RequestParam(required = false) Long appId,
                                                    @RequestParam(required = false) Long requirementId,
                                                    @RequestParam(required = false) String site){
        if(appCode == null && appId == null && requirementId == null){
            return CommonResult.buildSuccessResult(new ArrayList<>());
        }
        if(appId != null){
            appCode = appInfoService.getById(appId).getAppCode();
        }
        return CommonResult.buildSuccessResult(envConfigService.getEnvConfigList(appId,requirementId, site));
    }
    /**
     * 根据环境id获取详情
     */
    @GetMapping("/getEnvConfigItemList")
    public CommonResult<List<EnvConfigItem>> getEnvConfigItemList(@RequestParam(required = true) Long envConfigId){
        return CommonResult.buildSuccessResult(envConfigItemService.getEnvConfigItemList(envConfigId));
    }

    /**
     * 根据保存环境明细
     */
    @PostMapping("/saveEnvConfigItem")
    public CommonResult<Boolean> saveEnvConfigItem(
            @RequestBody List<EnvConfigDto> envConfigDto){
        for (EnvConfigDto configDto : envConfigDto) {
            if(configDto.getAppCode()!=null){
                configDto.setAppId(appInfoService.findApp(configDto.getAppCode()).getId());
            }
        }
        return CommonResult.buildSuccessResult(envConfigService.saveConfig(envConfigDto));
    }

    /**
     * 删除环境
     */
    @GetMapping("/delEnvConfig")
    public CommonResult<Boolean> delEnvConfig(@RequestParam(required = true) Long envConfigId){
        return CommonResult.buildSuccessResult(envConfigService.delConfig(envConfigId));
    }

    /**
     * 接口调试时获取相关Url数据
     */
    @GetMapping("/getEnvConfigItem")
    public CommonResult<List<EnvConfigItem>> getEnvConfigItem(@RequestBody EnvConfigDto envConfigDto){
        return CommonResult.buildSuccessResult(envConfigService.getEnvConfigItem(envConfigDto));
    }
    /**
     * 接口Url多个，且发生变化时调用
     */
    @GetMapping("/urlChange")
    public CommonResult<Boolean> urlChange(@RequestBody EnvConfigDto envConfigDto){
        envConfigService.urlChange(envConfigDto);
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * 初始化环境信息 上线前触发一次，后期不允许触发
     */
    @GetMapping("/initEnv")
    public CommonResult<Boolean> initEnv(String appCode){
        envConfigService.initEnv(appCode);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/updateAllMockUrls")
    public CommonResult<Boolean> updateAllMockUrls(){
        envConfigService.updateMockUrl();
        return CommonResult.buildSuccessResult(true);
    }


}
