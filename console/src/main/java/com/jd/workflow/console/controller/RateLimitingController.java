package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingChangeStatusDTO;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingQueryDTO;
import com.jd.workflow.console.entity.RateLimitingRules;
import com.jd.workflow.console.entity.RateLimitingRulesConfig;
import com.jd.workflow.console.entity.RateLimitingRulesOperateLog;
import com.jd.workflow.console.service.RateLimitingRulesConfigService;
import com.jd.workflow.console.service.RateLimitingRulesOperateLogService;
import com.jd.workflow.console.service.RateLimitingRulesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rateLimiting")
public class RateLimitingController {

    @Autowired
    RateLimitingRulesService rateLimitingRulesService;

    @Autowired
    RateLimitingRulesConfigService configService;

    @Autowired
    RateLimitingRulesOperateLogService rateLimitingRulesOperateLogService;

    @RequestMapping("/addRules")
    public CommonResult<?> addRules(@RequestBody List<RateLimitingRules> list){
        String erp = UserSessionLocal.getUser().getUserId();
        rateLimitingRulesService.addRules(list, erp);
        return CommonResult.buildSuccessResult("成功");
    }


    @RequestMapping("/updateRules")
    public CommonResult<?> updateRules(@RequestBody List<RateLimitingRules> list){
        String erp = UserSessionLocal.getUser().getUserId();
        rateLimitingRulesService.updateRules(list, erp);
        return CommonResult.buildSuccessResult("成功");
    }


    @RequestMapping("/deleteRules")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<?> deleteRules(@RequestBody List<Long> ids){
        String erp = UserSessionLocal.getUser().getUserId();
        rateLimitingRulesService.deleteRules(ids, erp);
        return CommonResult.buildSuccessResult("成功");
    }

    @RequestMapping("/listRules")
    public CommonResult<?> listRules(@RequestBody RateLimitingQueryDTO queryDto){
        queryDto.setErp(UserSessionLocal.getUser().getUserId());
        log.info("listRues入参："+queryDto);
        Page<RateLimitingRules> list;
        try {
            list = rateLimitingRulesService.listRules(queryDto);
        }catch (Exception e){
            e.printStackTrace();
            return CommonResult.buildErrorCodeMsg(500, e.getMessage());
        }
        return CommonResult.buildSuccessResult(list);
    }

    @RequestMapping("/publish")
    public CommonResult<?> publish(@RequestBody RateLimitingChangeStatusDTO statusDTO){
        String erp = UserSessionLocal.getUser().getUserId();
        rateLimitingRulesService.changeStatus(statusDTO, erp);
        return CommonResult.buildSuccessResult("成功");
    }


    @RequestMapping("/setGlobalSettings")
    public CommonResult<?> setGlobalSettings(@RequestBody RateLimitingRulesConfig config){
        try {
            String erp = UserSessionLocal.getUser().getUserId();
            rateLimitingRulesService.globalSettings(config, erp);
        }catch (Exception e){
            return CommonResult.error(e.getMessage());
        }
        return CommonResult.buildSuccessResult("成功");
    }


    @RequestMapping("/getGlobalSettings")
    public CommonResult<?> getGlobalSettings(String appProvider){
        try {
            String erp = UserSessionLocal.getUser().getUserId();
            RateLimitingRulesConfig config = rateLimitingRulesService.getGlobalSettings(appProvider, erp);
            return CommonResult.buildSuccessResult(config);
        }catch (Exception e){
            return CommonResult.error(e.getMessage());
        }
    }

    @RequestMapping("/loglist")
    public CommonResult<?> loglist(@RequestBody RateLimitingQueryDTO queryDto){
        String erp = UserSessionLocal.getUser().getUserId();
        Page<RateLimitingRulesOperateLog> pageResult = rateLimitingRulesOperateLogService.loglist(queryDto, erp);
        return CommonResult.buildSuccessResult(pageResult);
    }
}
