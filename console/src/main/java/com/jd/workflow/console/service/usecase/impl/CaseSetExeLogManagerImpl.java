package com.jd.workflow.console.service.usecase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.usecase.CaseSetExeLogMapper;
import com.jd.workflow.console.entity.usecase.*;
import com.jd.workflow.console.service.usecase.CaseSetExeLogDetailService;
import com.jd.workflow.console.service.usecase.CaseSetExeLogManager;
import org.junit.experimental.theories.Theory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description: 用例集执行记录表 服务实现类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Service
public class CaseSetExeLogManagerImpl extends ServiceImpl<CaseSetExeLogMapper, CaseSetExeLog> implements CaseSetExeLogManager {

    @Autowired
    private CaseSetExeLogDetailService caseSetExeLogDetailService;

    @Transactional
    @Override
    public void delById(Long id) {
        CaseSetExeLog caseSetExeLog = new CaseSetExeLog();
        caseSetExeLog.setId(id);
        caseSetExeLog.setYn(DataYnEnum.INVALID.getCode());
        updateById(caseSetExeLog);
        caseSetExeLogDetailService.delByCaseSetExeLogId(id);
    }
}

