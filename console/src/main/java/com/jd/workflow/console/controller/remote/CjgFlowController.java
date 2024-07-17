package com.jd.workflow.console.controller.remote;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.cjg.flow.sdk.model.dto.submit.ProcessInstance;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.StatusResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.app.CjgFlowCreateResult;
import com.jd.workflow.console.dto.jingme.SendMsgParam;
import com.jd.workflow.console.dto.requirement.RequirementInfoDto;
import com.jd.workflow.console.dto.requirement.WfFlowVo;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.service.jingme.SendMsgService;
import com.jd.workflow.console.service.requirement.InterfaceSpaceService;
import com.jd.workflow.console.service.requirement.*;
import com.jd.workflow.console.service.requirement.RequirementStatisticDto;
import com.jd.workflow.console.service.test.RequirementWorkflowService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 需求接口相关
 *
 * @menu deeptest集成相关接口
 * @description 给deeptest提供的接口
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/wfFlow")
@UmpMonitor
public class CjgFlowController {
    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
    InterfaceSpaceService interfaceSpaceService;

    @Autowired
    private SendMsgService sendMsgService;

    /**
     * 分页查询需求列表
     *
     * @param current  当前页
     * @param pageSize 每页条数
     * @param sortBy   排序字段：created_at、modified_at
     * @param status   状态
     * @param name     需求名称
     * @return
     */
    @RequestMapping("list")
    public CommonResult<IPage<RequirementInfoDto>> pageList(Long current, Long pageSize, String sortBy, Integer status, String name, String departmentId) {

        IPage<RequirementInfoDto> ref = requirementInfoService.pageList(current, pageSize, name, status, sortBy, departmentId);

        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 分页查询需求列表
     *
     * @param id 需求id
     * @return
     */
    @RequestMapping("deleteLogical")
    public CommonResult<Boolean> deleteLogical(Long id) {
        //try{
        return CommonResult.buildSuccessResult(requirementInfoService.remove(id));
        /*}catch (Exception e){
            return processException(e);
        }*/
    }

    @RequestMapping("save")
    public CommonResult<CjgFlowCreateResult> save(@RequestBody WfFlowVo wfFlowVo) {
        Guard.notEmpty(wfFlowVo.getName(), "请求体不能为空");

        RequirementInfo exist = requirementInfoService.getById(wfFlowVo.getId());
        if (StringUtils.isBlank(exist.getRelatedRequirementCode())) {
            if (StringUtils.isNotBlank(wfFlowVo.getCode())) {
                List<RequirementInfo> infos = interfaceSpaceService.getRequirementByDemandCode(wfFlowVo.getCode());
                Guard.assertTrue(CollectionUtils.isEmpty(infos), "已经关联过该需求，请勿重复关联");
            }
        }
        ProcessInstance insertWfFlow = new ProcessInstance();
        insertWfFlow.setName(wfFlowVo.getName());
        insertWfFlow.setBusinessRequirement(wfFlowVo.getCode());
        insertWfFlow.setTemplateCode(wfFlowVo.getTemplateCode());
        insertWfFlow.setSource(RequirementWorkflowService.source);
        insertWfFlow.setCreateBy(UserSessionLocal.getUser().getUserId());
        insertWfFlow.setParentId(wfFlowVo.getParentId());

        final List<WfFlowVo.SaveNodeDetailVo> vos = wfFlowVo.getNodeDetailVos();
        Guard.notEmpty(vos, "nodeDetailVos不能为空");
        final WfFlowVo.SaveNodeDetailVo detailVo = vos.get(0);
        String extDataStr = detailVo.getExtDataStr();
        insertWfFlow.setRootNodeDataJson(extDataStr);
        Map map = JsonUtils.parse(extDataStr, Map.class);
        Map input = (Map) map.get("input");
        List<String> operators = (List<String>) input.get("operators");
        return CommonResult.buildSuccessResult(requirementInfoService.createRequirement(wfFlowVo.getId(), insertWfFlow, operators));
    }

    @RequestMapping("getById")
    public CommonResult<RequirementInfoDto> getById(Long id) {
        Guard.notNull(id, "id不能为空");
        return CommonResult.buildSuccessResult(requirementInfoService.getEntityById(id));
    }

    @RequestMapping("getIdByFlowId")
    public CommonResult<RequirementInfo> getIdByFlowId(Long id) {
        Guard.notNull(id, "id不能为空");
        return CommonResult.buildSuccessResult(requirementInfoService.getRequirementByFlowRelatedId(id));
    }

    @RequestMapping("exportRequirementData")
    public CommonResult<List<RequirementStatisticDto>> exportRequirementData() {

        return CommonResult.buildSuccessResult(requirementInfoService.exportRequirementData());
    }

    /**
     * 发送jingme通知消息
     *
     * @param sendMsgParam
     * @return
     */
    @RequestMapping(value = "sendJingmeMsg", method = RequestMethod.POST)
    public CommonResult<Boolean> sendJingmeMsg(@RequestBody SendMsgParam sendMsgParam) {

        if (CollectionUtils.isNotEmpty(sendMsgParam.getReceiveErps())) {
            for (String receiveErp : sendMsgParam.getReceiveErps()) {
                sendMsgService.sendUserJueMsg(receiveErp, sendMsgParam.getTemplateMsgDTO());
            }
        }
        return CommonResult.buildSuccessResult(true);

    }
}
