package com.jd.workflow.console.service.debug;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.debug.FlowDebugLogMapper;
import com.jd.workflow.console.dao.mapper.debug.StepDebugLogMapper;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.entity.debug.StepDebugLog;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StepDebugLogService extends ServiceImpl<StepDebugLogMapper, StepDebugLog> {

    public Page<StepDebugLog> pageList(String stepId,String methodId,long current,long size){
        final LambdaQueryWrapper<StepDebugLog> lqw = new LambdaQueryWrapper();
        lqw.eq(StepDebugLog::getStepId,stepId);
        lqw.eq(StepDebugLog::getMethodId,methodId); // 请求方法id
        return page(new Page<>(current,size),lqw);
    }



    public void saveLog(StepContext stepContext, Long methodId,Long flowId){

        List<Map<String,Object>> stepData = (List<Map<String,Object>>) stepContext.toLog().get("steps");
        List<StepDebugLog> logs = new ArrayList<>();
        for (Map<String, Object> stepItem : stepData) {
            StepDebugLog log = new StepDebugLog();
            String id = (String) stepItem.get("id");
            boolean success = (boolean) stepItem.get("success");
            log.setStepId(id);
            log.setFlowId(flowId);
            log.setSuccess(success ? 1 : 0);
            log.setLogContent(JsonUtils.toJSONString(stepData));
            log.setMethodId(methodId+"");
            logs.add(log);
        }

        saveBatch(logs);
    }

    public void updateLog(StepContext stepContext, Long methodId,Long flowId){

        final LambdaQueryWrapper<StepDebugLog> lqw = new LambdaQueryWrapper();
        lqw.eq(StepDebugLog::getFlowId,flowId);
        List<StepDebugLog> list = this.list(lqw);
        Map<String,Long> stepIdToPkId = null;
        if(CollectionUtils.isNotEmpty(list)){
            stepIdToPkId = list.stream().collect(Collectors.toMap(StepDebugLog::getStepId, StepDebugLog::getId));
        }else{
            stepIdToPkId = new HashMap<>();
        }
        List<Map<String,Object>> stepData = (List<Map<String,Object>>) stepContext.toLog().get("steps");
        List<StepDebugLog> logs = new ArrayList<>();
        List<StepDebugLog> updatelogs = new ArrayList<>();
        for (Map<String, Object> stepItem : stepData) {
            StepDebugLog log = new StepDebugLog();
            String id = (String) stepItem.get("id");
            boolean success = (boolean) stepItem.get("success");
            log.setStepId(id);
            log.setFlowId(flowId);
            log.setSuccess(success ? 1 : 0);
            log.setLogContent(JsonUtils.toJSONString(stepData));
            log.setMethodId(methodId+"");
            if(stepIdToPkId.containsKey(id)){
                log.setId(stepIdToPkId.get(id));
                updatelogs.add(log);
            }else{
                logs.add(log);
            }
        }
        if(logs.size()>0){
            saveBatch(logs);
        }
        if(updatelogs.size()>0){
            updateBatchById(updatelogs);
        }
    }
}
