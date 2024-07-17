package com.jd.workflow.console.controller;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.measure.RequirementMeasureDataDTO;
import com.jd.workflow.console.dto.measure.UserMeasureDataDTO;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.soap.common.exception.BizException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author yza
 * @description
 * @date 2024/1/15
 */
@RestController
@Slf4j
@RequestMapping("/measureData")
@Api(tags = "度量数据")
public class MeasureDataController {

    @Autowired
    private IMeasureDataService measureDataService;

    @Autowired
    private RequirementInterfaceGroupService requirementInterfaceGroupService;

    /**
     * 【指标度量】获取用户明细
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    @GetMapping("/getUserMeasureData")
    @ApiOperation(value = "获取用户明细")
    public CommonResult<List<UserMeasureDataDTO>> getUserMeasureData(String department, String timeStart, String timeEnd, String erp) {
        CommonResult<List<UserMeasureDataDTO>> result = new CommonResult<>();
        result.setData(measureDataService.queryUserMeasureData(department, timeStart, timeEnd, erp));
        // EasyBI接入API要求code必须为200
        result.setCode(200);
        result.setMessage("");
        return result;
    }

    /**
     * 【指标度量】获取空间明细
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    @GetMapping("/getRequirementMeasureData")
    @ApiOperation(value = "获取空间明细")
    public CommonResult<List<RequirementMeasureDataDTO>> getRequirementMeasureData(String department, String timeStart,
                                                         String timeEnd, String requirementName, String requirementCode, String creator) {
        CommonResult<List<RequirementMeasureDataDTO>> result = new CommonResult<>();
        result.setData(measureDataService.queryRequirementMeasureData(department, timeStart, timeEnd, requirementName, requirementCode, creator));
        // EasyBI接入API要求code必须为200
        result.setCode(200);
        result.setMessage("");
        return result;
    }

    /**
     * 【指标度量】获取空间数
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    @GetMapping("/getRequirementCount")
    @ApiOperation(value = "获取空间数")
    public CommonResult<List<Map<String, Integer>>> getRequirementCount(String department, String timeStart, String timeEnd) {
        CommonResult<List<Map<String, Integer>>> result = new CommonResult<>();
        List<RequirementMeasureDataDTO> requireList = measureDataService.queryRequirementMeasureData(department, timeStart, timeEnd,
                "", "", "");
        HashMap<String, Integer> map = new HashMap<>(8);
        map.put("count", requireList.size());
        List<Map<String, Integer>> list = new ArrayList<>();
        list.add(map);
        result.setData(list);
        // EasyBI接入API要求code必须为200
        result.setCode(200);
        result.setMessage("");
        return result;
    }

    /**
     * 【指标度量】空间聚合接口数
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    @GetMapping("/getRequirementInterfaceCount")
    public CommonResult<List<Map<String, Integer>>> getRequirementInterfaceCount(String department, String timeStart, String timeEnd) {
        CommonResult<List<Map<String, Integer>>> result = new CommonResult<>();
        Integer count = requirementInterfaceGroupService.getRequirementInterfaceCount(department, timeStart, timeEnd);
        HashMap<String, Integer> map = new HashMap<>(8);
        map.put("count", count);
        List<Map<String, Integer>> list = new ArrayList<>();
        list.add(map);
        result.setData(list);
        // EasyBI接入API要求code必须为200
        result.setCode(200);
        result.setMessage("");
        return result;

    }

    /**
     * 更新部门数据
     * @return
     */
    @GetMapping("/refreshMeasureDataDept")
    public CommonResult<Boolean> refreshMeasureDataDept() {
        if(!"yanzengan".equals(UserSessionLocal.getUser().getUserId())) {
            throw new BizException("无权限");
        }
        return CommonResult.buildSuccessResult(measureDataService.refreshMeasureDataDept());
    }

}
