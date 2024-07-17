package com.jd.workflow.console.controller.remote;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.jsf.JsfBaseInfoDto;
import com.jd.workflow.console.dto.manage.EnvConfigItemDto;
import com.jd.workflow.console.dto.manage.JsfOrHttpDemo;
import com.jd.workflow.console.dto.test.DeepTestBaseDto;
import com.jd.workflow.console.dto.test.MethodQueryDto;
import com.jd.workflow.console.dto.test.deeptest.step.StepCaseDetail;
import com.jd.workflow.console.entity.test.TestCaseGroup;
import com.jd.workflow.console.service.test.TestCaseGroupService;
import com.jd.workflow.console.service.test.TestRequirementInfoService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 需求接口相关
 *
 * @menu deeptest集成相关接口
 * @description 给deeptest提供的接口
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/testRelated")
@UmpMonitor
public class ToDeeptestController {
    @Autowired
    TestRequirementInfoService testRequirementInfoService;

    @Autowired
    TestCaseGroupService testCaseGroupService;

    /**
     * 获取jsf接口
     *
     * @param dto
     * @param search 搜索字符串
     * @return
     */
    @RequestMapping("/getJsfInterface")
    public CommonResult<List<JsfBaseInfoDto>> getJsfInterface(DeepTestBaseDto dto, String search) {
        dto.init();
        return CommonResult.buildSuccessResult(testRequirementInfoService.getJsfInterface(dto, search));
    }

    /**
     * 获取jsf方法
     *
     * @param dto
     * @param interfaceId jsf接口id
     * @return
     */
    @RequestMapping("/getJsfMethods")
    public CommonResult<List<JsfBaseInfoDto>> getJsfMethods(DeepTestBaseDto dto, Long interfaceId) {
        dto.init();
        return CommonResult.buildSuccessResult(testRequirementInfoService.getJsfMethods(dto, interfaceId));
    }

    /**
     * 获取http方法列表
     *
     * @param dto
     * @param search 搜索添加
     * @return
     */
    @RequestMapping("/queryHttpInterfaces")
    public CommonResult<List<MethodSortModel>> queryHttpInterfaces(DeepTestBaseDto dto, String search) {
        dto.init();
        return CommonResult.buildSuccessResult(testRequirementInfoService.queryHttpInterfaces(dto, search));
    }

    /**
     * 获取接口基本信息
     *
     * @param dto      环境id-联调平台上传
     * @param methodId 方法id
     * @return
     */
    @RequestMapping("/queryInterfaceDemoValue")
    public CommonResult<JsfOrHttpDemo> queryInterfaceDemoValue(DeepTestBaseDto dto, @RequestParam(required = false) String envConfigId, Long methodId) {
        dto.init();
        return CommonResult.buildSuccessResult(testRequirementInfoService.queryInterfaceDemoValue(dto, envConfigId, methodId));
    }

    /**
     * ；、
     * 查询接口环境列表
     *
     * @param dto
     * @param envConfigId
     * @return
     */
    @RequestMapping("/queryInterfaceEnv")
    public CommonResult<List<EnvConfigItemDto>> queryInterfaceEnv(DeepTestBaseDto dto, Long envConfigId) {
        dto.init();
        return CommonResult.buildSuccessResult(testRequirementInfoService.queryInterfaceEnv(dto, envConfigId));
    }

    /**
     * 查询接口是否有过修改
     *
     * @return
     */
    @GetMapping(value = "/queryInterfacesChanged")
    public CommonResult<Boolean> queryInterfacesChanged(@RequestParam String station, @RequestParam Long methodId) {
        DeepTestBaseDto deepTestBaseDto = new DeepTestBaseDto();
        deepTestBaseDto.setStation(station);
        TestCaseGroup testCaseGroup = testCaseGroupService.queryMethodTestCaseGroup(station, methodId);
        if (Objects.nonNull(testCaseGroup)) {
            deepTestBaseDto.setModuleId(testCaseGroup.getRelatedTestModuleId());
            deepTestBaseDto.setRootSuiteId(testCaseGroup.getRelatedTestCaseGroupId());
        }
        return CommonResult.buildSuccessResult(testCaseGroupService.queryInterfacesChanged(deepTestBaseDto));
    }

    /**
     * 查询接口用例集信息
     *
     * @param station
     * @param methodId
     * @return
     * @desc 不存在用例集，会初始化出来
     */
    @GetMapping("/queryMethodGroup")
    public CommonResult<DeepTestBaseDto> queryMethodGroup(@RequestParam String station, @RequestParam Long methodId) {
        DeepTestBaseDto deepTestBaseDto = new DeepTestBaseDto();
        deepTestBaseDto.setStation(station);

        TestCaseGroup testCaseGroup = testCaseGroupService.getMethod(station, methodId);
        if (Objects.nonNull(testCaseGroup)) {
            deepTestBaseDto.setModuleId(testCaseGroup.getRelatedTestModuleId());
            deepTestBaseDto.setRootSuiteId(testCaseGroup.getRelatedTestCaseGroupId());
        }
        return CommonResult.buildSuccessResult(deepTestBaseDto);
    }

    /**
     * 获取接口对应的测试用例数据
     *
     * @param dto
     * @return
     */
    @GetMapping("/queryInterfaceTestStep")
    public CommonResult<StepCaseDetail> queryInterfaceTestStep(DeepTestBaseDto dto) {
        StepCaseDetail stepCaseDetail = testCaseGroupService.queryInterfaceStepCaseDetail(dto);
        return CommonResult.buildSuccessResult(stepCaseDetail);
    }

    /**
     * 通过接口文档地址获取deeptest用例数据
     * @param methodQueryDto
     * @return
     */
    @PostMapping("/queryMethodTestStep")
    public CommonResult<StepCaseDetail> queryMethodTestStep(@RequestBody MethodQueryDto methodQueryDto) {

        Guard.notNull(methodQueryDto.getUrl(), "接口文档路径不能为空");
        return CommonResult.buildSuccessResult(testCaseGroupService.queryMethodTestStep(methodQueryDto));
    }

}

