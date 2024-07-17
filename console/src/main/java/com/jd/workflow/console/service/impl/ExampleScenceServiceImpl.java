package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.ExampleScenceMapper;
import com.jd.workflow.console.entity.ExampleScence;
import com.jd.workflow.console.service.ExampleScenceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ExampleScenceServiceImpl extends ServiceImpl<ExampleScenceMapper, ExampleScence> implements ExampleScenceService {


    @Override
    public ExampleScence getEntity(String id) {
        return this.getById(id);
    }

    @Override
    public Boolean remove(Long id) {
        LambdaUpdateWrapper<ExampleScence> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(ExampleScence::getYn, 0);
        wrapper.eq(ExampleScence::getId, id);
        return update(wrapper);
    }

    @Override
    public Boolean removeHard(Long id) {
        return removeById(id);
    }

    @Override
    public Map<Long, Boolean> removeAndadd(List<ExampleScence> scences) {
        LambdaUpdateWrapper<ExampleScence> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ExampleScence::getMethodId, scences.get(0).getMethodId());
        wrapper.set(ExampleScence::getYn, 0);
        update(wrapper);
        Map<Long,Boolean> result=new HashMap<>();
        for (ExampleScence scence : scences) {
            scence.setId(null);
            Boolean ss = save(scence);
            result.put(scence.getId(), ss);
        }
        return result;
    }
    public List<ExampleScence> getAllScene(List<Long> methodIds){
        if(CollectionUtils.isEmpty(methodIds)){
            return null;
        }
        LambdaQueryWrapper<ExampleScence> wrapper=new LambdaQueryWrapper<>();
        wrapper.in(ExampleScence::getMethodId,methodIds);
        wrapper.eq(ExampleScence::getYn,1);
        return list(wrapper);
    }
    @Override
    public Map<Long, Boolean> add(List<ExampleScence> scences) {
        Map<Long,Boolean> result=new HashMap<>();
        for (ExampleScence scence : scences) {
            Boolean ss = save(scence);
            result.put(scence.getId(), ss);
        }
        return result;
    }

    @Override
    public List<ExampleScence> listByMethodId(Long methodId) {
        LambdaQueryWrapper<ExampleScence> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ExampleScence::getMethodId,methodId);
        wrapper.eq(ExampleScence::getYn,1);
        return list(wrapper);
    }
}
