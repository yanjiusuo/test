package com.jd.workflow.console.controller.test;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.test.RequirementInterfaceQueryDto;
import com.jd.workflow.console.dto.test.SmokeExecuteDto;
import com.jd.workflow.console.dto.test.SmokeExecuteResult;
import com.jd.workflow.console.dto.test.deeptest.SuiteDetail;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.service.group.FlowStepInterfaceGroupService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.group.impl.FlowStepGroupManageServiceImpl;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.service.test.IDeeptestRemoteCaller;
import com.jd.workflow.console.service.test.TestCasesExecuteInfoService;
import com.jd.workflow.console.service.test.TestRequirementInfoService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 接口自动化测试相关的接口
 * @menu 工作流相关接口
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/requirementFlow")
@UmpMonitor
public class DeepTestController {
    @Autowired
    FlowStepGroupManageServiceImpl flowStepGroupManageService;

    @Autowired
    FlowStepInterfaceGroupService flowStepInterfaceGroupService;

    @Autowired
    TestRequirementInfoService testRequirementInfoService;

    @Autowired
    TestCasesExecuteInfoService testCasesExecuteInfoService;
    @Autowired
    RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    RequirementInfoService requirementInfoService;

    /**
     * 获取需求应用列表
     * @param requirementId 需求id
     * @return
     */
    @RequestMapping("/getRequirementAppList")
    public CommonResult<List<AppInfoDTO>> getRequirementAppList(Long requirementId){
        requirementId = requirementInfoService.getRequirementByFlowRelatedId(requirementId).getId();
         return CommonResult.buildSuccessResult(requirementInterfaceGroupService.getRequirementInterfaceId(requirementId));
    }

    /**
     *  获取流程节点对应的接口列表
      * @param dto 需求id

     * @return
     */
    @RequestMapping("/getRequirementInterfaceList")
    public CommonResult<List<InterfaceManage>> getRequirementInterfaceList(RequirementInterfaceQueryDto dto
                                                                           ){

        Long requirementId = requirementInfoService.getRequirementByFlowRelatedId(dto.getRequirementId()).getId();
        dto.setRequirementId(requirementId);
        return CommonResult.buildSuccessResult(requirementInterfaceGroupService.getRequirementInterfaceList(dto));
    }
    private String transformEnv(String env){
        if("zh".equals(env) || "master".equals(env)){
            env = "China";
            return env;
        }
        return env;
    }
    /**
     * 获取选中的需求接口列表
     * @param requirementId 流程id
     * @param stepId 流程id
     * @return 选中后的借接口id列表
     */
    @RequestMapping("/getSelectedRequirementInterfaceList")
    public CommonResult<List<Long>> getSelectedRequirementInterfaceList(Long requirementId,Long stepId){
        RequirementInfo requirementInfo= requirementInfoService.getRequirementByFlowRelatedId(requirementId);
        if(Objects.nonNull(requirementInfo)) {
            requirementId = requirementInfo.getId();
            return CommonResult.buildSuccessResult(flowStepGroupManageService.getSelectedRequirementInterfaceList(requirementId, stepId));
        }
        else
        {
            return CommonResult.buildErrorCodeMsg(404,"找不到流程");
        }
    }

    /**
     * 获取需求方法数量
     * @param requirementId 流程id
     * @return 选中后的借接口id列表
     */
    @RequestMapping("/getRequirementMethodCount")
    public CommonResult<Integer> getRequirementMethodCount(Long requirementId){
        Guard.notEmpty(requirementId,"requirementId不可为空");
        Guard.assertTrue(requirementId>0,"无效的requirementId参数");
        RequirementInfo requirementInfo= requirementInfoService.getRequirementByFlowRelatedId(requirementId);
        if(Objects.nonNull(requirementInfo)) {
           return CommonResult.buildSuccessResult(requirementInfoService.getRequirementMethodCount(requirementInfo.getId()));
        }
        else
        {
            return CommonResult.buildErrorCodeMsg(404,"找不到流程");
        }
    }
    /**
     * 获取冒烟用例
     * @param env 环境
     * @param requirementId 需求id
     * @return
     */
    @RequestMapping("/getSmokeCase")
    public CommonResult<List<SuiteDetail>> getSmokeCase(String env, Long requirementId){
        requirementId = requirementInfoService.getRequirementByFlowRelatedId(requirementId).getId();
        env = transformEnv(env);
        return CommonResult.buildSuccessResult(testRequirementInfoService.getSmokeCase(env,requirementId));
    }
    @RequestMapping("/syncMembers")
    public CommonResult<Boolean> syncMembers(String erp,Long deeptestModuleId,String env){
        env = transformEnv(env);
        testRequirementInfoService.syncMembers(erp,deeptestModuleId,env);

        return CommonResult.buildSuccessResult(true);
    }
    /**
     * 执行冒烟用例
     * @param env 站点信息： test、zh
     * @param requirementId 需求id
     * @return
     */
    @RequestMapping("/executeSmokeCase")
    public CommonResult<Boolean> executeSmokeCase(String env, Long requirementId,Long stepId,
                                                            @RequestBody List<Long> caseIds){
        requirementId = requirementInfoService.getRequirementByFlowRelatedId(requirementId).getId();
        env = transformEnv(env);
        testRequirementInfoService.executeSmokeCase(env,requirementId,stepId,caseIds);
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * @title 查询冒烟用例结果
     * @description 查询冒烟用例结果，需要每隔2秒查询一次
     * @param dto 需求id
     * @return
     */
    @RequestMapping("/querySmokeCaseResult")
    public CommonResult<SmokeExecuteResult> querySmokeCaseResult(@RequestBody  SmokeExecuteDto dto){
        Long requirementId = requirementInfoService.getRequirementByFlowRelatedId(dto.getRequirementId()).getId();
        SmokeExecuteResult result = testRequirementInfoService.getExecuteResult(dto.getStepId());
        return CommonResult.buildSuccessResult(result);
    }



    /**
     * 设置选中的需求接口列表
     * @param requirementId 需求id
     * @param stepId 步骤id
     * @param interfaceIds 接口列表
     * @return 执行成功code为0，不成功code为非0
     */
    @RequestMapping("/setSelectedRequirementInterfaceList")
    public CommonResult<Boolean> setSelectedRequirementInterfaceList(Long requirementId, Long stepId, @RequestBody List<Long> interfaceIds){
        requirementId = requirementInfoService.getRequirementByFlowRelatedId(requirementId).getId();
        flowStepInterfaceGroupService.setSelectedRequirementInterfaceIds(requirementId,stepId,interfaceIds);
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * 获取流程步骤关联的测试用例数量
     * @param requirementId 需求id
     * @param stepId 流程步骤id
     * @return 测试用例的数量
     */
    @RequestMapping("/getFlowStepTestCaseCount")
    public CommonResult<Integer> getFlowStepTestCaseCount(Long requirementId,Long stepId){
        requirementId = requirementInfoService.getRequirementByFlowRelatedId(requirementId).getId();
        return CommonResult.buildSuccessResult(testRequirementInfoService.getSmokeCaseAllCount(requirementId));
    }



}
