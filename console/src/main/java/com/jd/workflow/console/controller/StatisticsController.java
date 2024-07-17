package com.jd.workflow.console.controller;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jd.rcenter.businessworks.plugins.PluginsClient;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.MeasureDataEnum;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.dashboard.InterfaceHealthDTO;
import com.jd.workflow.console.dto.dashboard.UserDashboardDTO;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.entity.MeasureData;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.console.service.statistics.StatisticsService;
import com.jd.workflow.soap.common.exception.BizException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@UmpMonitor
@Api(value = "用户统计", tags = "统计")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;

    @Autowired
    private IMeasureDataService measureDataService;

    @Autowired
    private IMethodManageService methodManageService;

    /**
     * @param
     * @return
     */
    @GetMapping("/getInterfaceStatistics")
    @ApiOperation(value = "数据概览")
    public CommonResult<UserDashboardDTO> getInterfaceStatistics() {
        log.info("StatisticsController getInterfaceStatistics  ");

        //3.service层
        UserDashboardDTO ref = statisticsService.getPersonInterfaceStatistics(UserSessionLocal.getUser().getUserId());
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }


    @GetMapping("/getHeathStatistics")
    @ApiOperation(value = "获取健康度")
    public CommonResult<InterfaceHealthDTO> getHeathStatistics() {
        log.info("StatisticsController getHeathStatistics  ");


        //3.service层
        InterfaceHealthDTO ref = statisticsService.getHeathStatistics(UserSessionLocal.getUser().getUserId());
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("/getMethodStatusStatistics")
    @ApiOperation(value = "获取接口状态统计")
    public CommonResult<InterfaceHealthDTO> getMethodStatusStatistics() {
        log.info("StatisticsController getHeathStatistics  ");


        //3.service层
        InterfaceHealthDTO ref = statisticsService.getMethodStatusStatistics(UserSessionLocal.getUser().getUserId());
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }


    @GetMapping("/getBanner")
    @ApiOperation(value = "获取banner")
    public CommonResult<String> getBanner() {
        log.info("StatisticsController getBanner");

        String result = getTableHeadInfo("japiconfig", "module", "banner");

        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/getAblity")
    @ApiOperation(value = "获取能力模块")
    public CommonResult<String> getAblity() {
        log.info("StatisticsController getAblity");

        String result = getTableHeadInfo("japiconfig", "module", "ablity");

        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/getVideo")
    @ApiOperation(value = "获取视频模块")
    public CommonResult<String> getVideo() {
        log.info("StatisticsController getVideo");

        String result = getTableHeadInfo("japiconfig", "module", "video");

        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/getGuide")
    @ApiOperation(value = "获取指南模块")
    public CommonResult<String> getGuide() {
        log.info("StatisticsController getGuide");

        String result = getTableHeadInfo("japiconfig", "module", "guide");

        return CommonResult.buildSuccessResult(result);
    }

    private String getTableHeadInfo(String headRuleKey, String headKey, String headValue) {
        log.info("getTableHeadInfo 入参：ruleKey：{},key:{},value:{}", headRuleKey, headKey, headValue);
        String data = null;
        try {
            PluginsClient.PluginParam pluginParam = new PluginsClient.PluginParam(headRuleKey);
            pluginParam.add(headKey, headValue);
            log.info("getTableHeadInfo 入参：pluginParam：{}", JSONObject.toJSONString(pluginParam));
            data = (String) PluginsClient.send(pluginParam);
        } catch (Exception e) {

            log.error("getTableHeadInfo 异常：", e);
        }
        log.info("getTableHeadInfo 出参：data：{}", data);
        return data;
    }


    /**
     * @param
     * @return
     */
    @GetMapping("/getDeptInterfaceStatistics")
    @ApiOperation(value = "部门数据概览")
    public CommonResult<UserDashboardDTO> getDeptInterfaceStatistics() {
        log.info("StatisticsController getDeptInterfaceStatistics  ");

        //3.service层
        UserDashboardDTO ref = statisticsService.getDeptInterfaceStatistics(UserSessionLocal.getUser().getUserId());
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param
     * @return
     */
    @GetMapping("/getAllInterfaceStatistics")
    @ApiOperation(value = "平台数据概览")
    public CommonResult<UserDashboardDTO> getAllInterfaceStatistics() {
        log.info("StatisticsController getAllInterfaceStatistics  ");

        //3.service层
        UserDashboardDTO ref = statisticsService.getAllInterfaceStatistics(UserSessionLocal.getUser().getUserId());
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param
     * @return
     */
    @GetMapping("/getUserInfo")
    @ApiOperation(value = "获取用户权限信息")
    public CommonResult<UserRoleDTO> getUserInfo() {
        log.info("StatisticsController getUserInfo  ");
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
        //4.出参
        return CommonResult.buildSuccessResult(userRoleDTO);
    }

    @GetMapping("/getDeptTrends")
    @ApiOperation(value = "获取部门最近动态")
    public CommonResult<List<String>> getDeptTrends(@RequestParam(required = false) Integer count) {
        log.info("StatisticsController getDeptTrends  ");
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
        StringBuilder deptBuilder = new StringBuilder();
        String[] depts = userRoleDTO.getDept().split("-");
        if (depts.length > 5) {
            deptBuilder.append(depts[0]);
            deptBuilder.append("-");
            deptBuilder.append(depts[1]);
            deptBuilder.append("-");
            deptBuilder.append(depts[2]);
            deptBuilder.append("-");
            deptBuilder.append(depts[3]);
            deptBuilder.append("-");
            deptBuilder.append(depts[4]);
        } else {
            if (!userRoleDTO.getJapiAdmin()) {
                deptBuilder.append(userRoleDTO.getDept());
            }
        }
        if (Objects.isNull(count)) {
            count = 5;
        }
        if (!userRoleDTO.getJapiAdmin() && !userRoleDTO.getJapiDepartment() && !userRoleDTO.getDeptLeader()) {
            throw new BizException("无权限查看");
        }

        List<MeasureData> measureDataList = measureDataService.queryDeptInfo(deptBuilder.toString(), count);

        List<String> resultList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(measureDataList)) {
            String txtFormat = "%s于%s进行了%s";
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (MeasureData measureData : measureDataList) {
                MeasureDataEnum measureDataEnum = MeasureDataEnum.getByCode(measureData.getType());
                if (Objects.nonNull(measureDataEnum)) {
                    resultList.add(String.format(txtFormat, measureData.getErp(), DATE_FORMAT.format(measureData.getCreated()), measureDataEnum.getDescription()));
                }
            }
        }


        //4.出参
        return CommonResult.buildSuccessResult(resultList);
    }

    @GetMapping("/getUserView")
    @ApiOperation(value = "获取用户最近几条浏览接口文档记录")
    public CommonResult<List<MethodManageDTO>> getUserView(@RequestParam(required = false) Integer count) {
        log.info("StatisticsController getUserView  ");
        if (Objects.isNull(count)) {
            count = 5;
        }
        List<MeasureData> measureDataList = measureDataService.queryUserView(UserSessionLocal.getUser().getUserId(), count);
        List<MethodManageDTO> resultList = Lists.newArrayList();

        if (CollectionUtils.isNotEmpty(measureDataList)) {
            for (MeasureData measureData : measureDataList) {
                MethodManageDTO methodManageDTO = methodManageService.getMetaMethodInfo(Long.parseLong(measureData.getNote()));
                resultList.add(methodManageDTO);
            }
        }


        //4.出参
        return CommonResult.buildSuccessResult(resultList);
    }


}
