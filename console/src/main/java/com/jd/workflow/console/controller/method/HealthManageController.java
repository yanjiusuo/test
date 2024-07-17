package com.jd.workflow.console.controller.method;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.matrix.generic.spi.SPI;
import com.jd.matrix.generic.spi.func.SimpleSPIReducer;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.auth.InterfaceAuthFilter;
import com.jd.workflow.console.dto.manage.HealthCheckResultDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.impl.ScoreManageService;
import com.jd.workflow.console.service.manage.RankScoreService;
import com.jd.workflow.matrix.ext.spi.HealthCheckSPI;
import com.jd.workflow.matrix.ext.spi.ModelContentParseSPI;
import com.jd.workflow.method.MethodInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/healthManage")
@UmpMonitor
public class HealthManageController {

    @Autowired
    ScoreManageService scoreManageService;
    @Autowired
    RankScoreService rankScoreService;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    IMethodManageService methodManageService;

    /**
     * 健康度诊断
     * @param methodId 方法id
     * @return
     */
    @GetMapping(value = "/healthCheck")
    @ResponseBody
    public CommonResult<List<HealthCheckResultDto>> healthCheck(Long methodId) {
        List<HealthCheckResultDto> result = new ArrayList<>();
        /*{
            HealthCheckResultDto dto = new HealthCheckResultDto();
            dto.setResult(true);
            dto.setType("param");
            result.add(dto);
        }
        {
            HealthCheckResultDto dto = new HealthCheckResultDto();
            dto.setResult(true);
            dto.setType("docInfo");
            result.add(dto);
        }
        {
            HealthCheckResultDto dto = new HealthCheckResultDto();
            dto.setResult(true);
            dto.setType("inputExample");
            result.add(dto);
        }
        {
            HealthCheckResultDto dto = new HealthCheckResultDto();
            dto.setResult(true);
            dto.setType("outputExample");
            result.add(dto);
        }
        {
            HealthCheckResultDto dto = new HealthCheckResultDto();
            dto.setResult(true);
            dto.setType("mockTemplate");
            result.add(dto);
        }
        {
            HealthCheckResultDto dto = new HealthCheckResultDto();
            dto.setResult(true);
            dto.setType("debugHistory");
            result.add(dto);
        }*/
        MethodManageDTO methodManage = methodManageService.getEntityById(methodId);
        MethodInfo methodInfo = new MethodInfo();
        BeanUtils.copyProperties(methodManage,methodInfo);
        methodInfo.setId(methodId);
        if(methodManage.getDocConfig() != null){
            methodInfo.setInputExample(methodManage.getDocConfig().getInputExample());
            methodInfo.setOutputExample(methodManage.getDocConfig().getOutputExample());
        }
        SPI.of(HealthCheckSPI.class, spi -> {
            HealthCheckResultDto dto = new HealthCheckResultDto();
            dto.setResult(spi.isTypeValid(methodInfo));
            dto.setType(spi.getType());
            result.add(dto);
            return dto;
        }).filter(spec->{
            return spec.getGroup().equals("jd");
        }).callWithDetail();

        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 健康度诊断
     * @param methodId 方法id
     * @return
     */
    @GetMapping(value = "/checkAndUpdateScore")
    @ResponseBody
    public CommonResult<List<HealthCheckResultDto>> checkAndUpdateScore(Long methodId) {
        scoreManageService.updateMethodScore(methodId);
        return healthCheck(methodId);
    }

    @GetMapping(value = "/updateScore")
    @ResponseBody
    public CommonResult<Boolean> updateScore(Long appId) {
       /* AppInfo app = appInfoService.getById(appId);
        scoreManageService.initAppScore(app);*/
        rankScoreService.updateRankScores();
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 初始化应用分数
     * @hidden
     * @param appId
     * @return
     */
    @GetMapping(value = "/initAllAppScores")
    @ResponseBody
    public CommonResult<Boolean> initAllAppScores(Long appId) {
       /* AppInfo app = appInfoService.getById(appId);
        scoreManageService.initAppScore(app);*/
        scoreManageService.initAllAppScores();
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 获取方法分数排名
     * @param methodId 方法id
     * @return  返回排名后的分数排名百分比，最大100
     */
    @GetMapping("/getMethodRank")
    public CommonResult<Double> getMethodRank(Long methodId){
        return CommonResult.buildSuccessResult(rankScoreService.getMethodRank(methodId));
    }

    /**
     * 更新方法分数
     * @param methodId 方法id
     * @return
     */
    @GetMapping("/updateMethodRank")
    public CommonResult<Boolean> updateMethodRank(Long methodId){
        scoreManageService.updateMethodScore(methodId);
        return CommonResult.buildSuccessResult(true);
    }


}
