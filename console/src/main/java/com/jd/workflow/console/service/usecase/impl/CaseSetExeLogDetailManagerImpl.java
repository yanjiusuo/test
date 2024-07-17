package com.jd.workflow.console.service.usecase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.usecase.CaseSetExeLogDetailMapper;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDetail;
import com.jd.workflow.console.entity.usecase.PageCaseSetExeLogDetailParam;
import com.jd.workflow.console.service.usecase.CaseSetExeLogDetailManager;
import com.jd.workflow.console.service.usecase.CaseSetExeLogDetailService;
import com.jd.workflow.console.service.usecase.CaseSetExeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * 用例集执行结果明细表 服务实现类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Service
public class CaseSetExeLogDetailManagerImpl extends ServiceImpl<CaseSetExeLogDetailMapper, CaseSetExeLogDetail> implements CaseSetExeLogDetailManager {

    @Autowired
    private CaseSetExeLogService caseSetExeLogService;

    @Transactional
    @Override
    public void saveCaseSetExeLogDetail(CaseSetExeLogDetail caseSetExeLogDetail) {
        boolean save = save(caseSetExeLogDetail);
        boolean updateResult = false;
        if(caseSetExeLogDetail.getStatus()==1){
            updateResult = caseSetExeLogService.successNoAddOne(caseSetExeLogDetail.getCaseSetExeLogId());
        }else{
            updateResult = caseSetExeLogService.failNoAddOne(caseSetExeLogDetail.getCaseSetExeLogId());
        }
    }
}
