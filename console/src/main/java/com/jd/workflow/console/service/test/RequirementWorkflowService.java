package com.jd.workflow.console.service.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.cjg.flow.sdk.client.WorkFlowClient;
import com.jd.cjg.flow.sdk.model.dto.query.CommonQueryVO;
import com.jd.cjg.flow.sdk.model.dto.query.ProcessDetailQuery;
import com.jd.cjg.flow.sdk.model.dto.query.ProcessInstanceQuery;
import com.jd.cjg.flow.sdk.model.dto.submit.ProcessInstanceVO;
import com.jd.cjg.flow.sdk.model.dto.submit.WorkFlowInstanceVO;
import com.jd.cjg.flow.sdk.model.result.FlowResult;
import com.jd.cjg.flow.sdk.model.result.PageResult;
import com.jd.cjg.flow.sdk.model.vo.PersonVo;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.requirement.FlowInstanceVo;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.soap.common.exception.BizException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RequirementWorkflowService {
    public static final int source = 1;
    @Autowired
    WorkFlowClient cjgWorkflowService;

    public WorkFlowInstanceVO validateRequirementId(Long requirementId){
        CommonQueryVO query = new CommonQueryVO();
        query.setSource(source);
        query.setFlowId(requirementId);
        FlowResult<WorkFlowInstanceVO> result = cjgWorkflowService.getFlowInstanceInfo(query);
        if(result.checkSuccess()){
            return result.getModel();
        }
        return null;
    }

    /**
     * 角色类型，工作流内部（1：执行人；2：关注人；3：创建人）
     * @param flowId
     * @return
     */
    public Set<String> getPersonsOfFlow(Long flowId){

        CommonQueryVO query = new CommonQueryVO();
        query.setSource(source);
        query.setFlowId(flowId);
        FlowResult<List<PersonVo>> result = cjgWorkflowService.getPersonsOfFlow(query);
        if(!result.checkSuccess()){
            throw new BizException("获取流程成员失败："+result.getMessage());
        }
        Set<String> personVos = result.getModel().stream().map(item->item.getErp()).collect(Collectors.toSet());
        return personVos;
    }

    public FlowInstanceVo flowDetail(Long requirementId){
        WorkFlowInstanceVO vo = validateRequirementId(requirementId);
        if(vo == null){
            throw new BizException("该需求不存在");
        }
        FlowInstanceVo result = new FlowInstanceVo();
        BeanUtils.copyProperties(vo,result);
        result.setId(result.getFlowId());
        return result;
    }


    public IPage<FlowInstanceVo> queryRequirementList(String name, Integer current, Integer pageSize){
        ProcessInstanceQuery query = new ProcessInstanceQuery();
        query.setName(name);
        query.setPageSize(pageSize);
        query.setSource(source);
        query.setCurrent(current);
        FlowResult<PageResult<ProcessInstanceVO>> result = cjgWorkflowService.queryProcessInstance(query);
        if(!result.checkSuccess()){
            throw new BizException("获取需求失败："+result.getMessage());
        }
        PageResult<ProcessInstanceVO> model = result.getModel();
        List<FlowInstanceVo> flowVos = model.getData().stream().map(item -> {
            FlowInstanceVo vo = new FlowInstanceVo();
            BeanUtils.copyProperties(item,vo);
            vo.setId(vo.getFlowId());
            return vo;
        }).collect(Collectors.toList());
        Page<FlowInstanceVo> page = new Page<>();
        page.setTotal(model.getTotalCount());
        page.setCurrent(model.getCurrentPage());
        page.setSize(model.getPageSize());
        page.setRecords(flowVos);
        return page;
    }


}
