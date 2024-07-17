package com.jd.workflow.console.service.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.test.TestProjectInfoMapper;
import com.jd.workflow.console.entity.test.TestProjectInfo;
import org.springframework.stereotype.Service;

@Service
public class TestProjectInfoService extends ServiceImpl<TestProjectInfoMapper, TestProjectInfo> {
    public TestProjectInfo getProject(String jagleProjectId,String env){
        LambdaQueryWrapper<TestProjectInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TestProjectInfo::getEnv,env);
        lqw.eq(TestProjectInfo::getRelatedProjectId,jagleProjectId);
        return getOne(lqw);
    }
}
