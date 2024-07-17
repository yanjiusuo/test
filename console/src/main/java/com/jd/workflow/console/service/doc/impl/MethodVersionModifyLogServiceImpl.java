package com.jd.workflow.console.service.doc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.doc.InterfaceVersionMapper;
import com.jd.workflow.console.dao.mapper.doc.MethodVersionModifyLogMapper;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.IMethodVersionModifyLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class MethodVersionModifyLogServiceImpl extends ServiceImpl<MethodVersionModifyLogMapper, MethodVersionModifyLog> implements IMethodVersionModifyLogService {
    @Override
    public void removeByInterfaceId(Long interfaceId) {
        LambdaQueryWrapper<MethodVersionModifyLog> modifyLog = new LambdaQueryWrapper<>();
        modifyLog.in(MethodVersionModifyLog::getInterfaceId, Collections.singletonList(interfaceId));
        remove(modifyLog);
    }

    public void removeModifyLog(Long id){
        MethodVersionModifyLog modifyLog = getById(id);
        removeById(modifyLog);
    }

    @Override
    public void removeByMethodIds(List<Long> methodIds) {
        if(methodIds.isEmpty()) return;
        LambdaQueryWrapper<MethodVersionModifyLog> modifyLog = new LambdaQueryWrapper<>();
        modifyLog.in(MethodVersionModifyLog::getMethodId, methodIds);
        remove(modifyLog);
    }

    @Override
    public List<MethodVersionModifyLog> listMethodVersions(Long methodId) {
        LambdaQueryWrapper<MethodVersionModifyLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodVersionModifyLog::getMethodId, methodId);
        lqw.eq(MethodVersionModifyLog::getYn, 1);
        lqw.select(MethodVersionModifyLog::getId,MethodVersionModifyLog::getInterfaceId,MethodVersionModifyLog::getMethodId,MethodVersionModifyLog::getVersion);
        return list(lqw);
    }
}
