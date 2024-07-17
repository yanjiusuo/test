package com.jd.workflow.console.service.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.test.TestCasesExecuteInfoMapper;
import com.jd.workflow.console.entity.test.TestCasesExecuteInfo;
import com.jd.workflow.soap.common.util.StringHelper;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TestCasesExecuteInfoService extends ServiceImpl<TestCasesExecuteInfoMapper, TestCasesExecuteInfo> {
    public void executeCases(Long requirementId, Long flowStepId, List<Long> caseIds){
        TestCasesExecuteInfo executeInfo = new TestCasesExecuteInfo();
        executeInfo.setRelatedFlowStepId(flowStepId+"");
        executeInfo.setRelatedRequirementId(requirementId);
        save(executeInfo);

    }
    public TestCasesExecuteInfo getLatestExecuteInfo(Long stepId){
        LambdaQueryWrapper<TestCasesExecuteInfo> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(TestCasesExecuteInfo::getId);
        lqw.eq(TestCasesExecuteInfo::getRelatedFlowStepId,stepId);
        lqw.last("LIMIT 1");
        List<TestCasesExecuteInfo> list = list(lqw);
        if(!list.isEmpty()){
            return list.get(0);
        }
        return null;

    }
    public Long newExecuteInfo(Long requirementId,String env,Long stepId,List<Long> caseIds,Long deeptestId){
        TestCasesExecuteInfo t = new TestCasesExecuteInfo();
        t.setTestExecuteId(deeptestId+"");
        t.setEnv(env);
        t.setRelatedRequirementId(requirementId);
        t.setRelatedFlowStepId(stepId+"");
        t.setCaseIds(StringHelper.join(caseIds,","));
        save(t);
        return t.getId();
    }
    public int queryFlowStepExecuteInfo(Long requirementId,Long flowStepId){
        LambdaQueryWrapper<TestCasesExecuteInfo> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(TestCasesExecuteInfo::getId);
        List<TestCasesExecuteInfo> list = list(lqw);
        if(list.isEmpty()){
            return 0;
        }
        List<String> ids = StringHelper.split(list.get(0).getCaseIds(), ",");
        return ids.size();

    }
}
