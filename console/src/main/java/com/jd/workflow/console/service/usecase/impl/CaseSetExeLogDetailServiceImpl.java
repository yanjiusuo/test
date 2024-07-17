package com.jd.workflow.console.service.usecase.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.usecase.CaseSetExeLogDetailMapper;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordDTO;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDTO;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDetail;
import com.jd.workflow.console.entity.usecase.PageCaseSetExeLogDetailParam;
import com.jd.workflow.console.service.param.IParamBuilderRecordService;
import com.jd.workflow.console.service.usecase.CaseSetExeLogDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class CaseSetExeLogDetailServiceImpl extends ServiceImpl<CaseSetExeLogDetailMapper, CaseSetExeLogDetail> implements CaseSetExeLogDetailService {

    @Autowired
    private IParamBuilderRecordService paramBuilderRecordService;

    @Override
    public Page<ParamBuilderRecordDTO> pageList(PageCaseSetExeLogDetailParam pageParam) {
        LambdaQueryWrapper<CaseSetExeLogDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CaseSetExeLogDetail::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(CaseSetExeLogDetail::getCaseSetExeLogId, pageParam.getCaseSetExeLogId());
        lqw.orderByDesc(CaseSetExeLogDetail::getId);
        Page<CaseSetExeLogDetail> page = this.page(new Page<>(pageParam.getCurrent(), pageParam.getPageSize()), lqw);
        Page<ParamBuilderRecordDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        //获取当前页用用例结果Id
        List<Long> resultIds = page.getRecords().stream().map(logDetail -> {
            return logDetail.getCaseExeResultId();
        }).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(resultIds)){
            List<ParamBuilderRecordDTO> paramBuilderRecordDTOS = paramBuilderRecordService.getByIds(resultIds);
            result.setRecords(paramBuilderRecordDTOS);
        }else{
            // 应该不用
            result.setRecords(new ArrayList<>());
        }
        return result;
    }

    @Override
    public void delByCaseSetExeLogId(Long caseSetExeLogId) {
        LambdaQueryWrapper<CaseSetExeLogDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CaseSetExeLogDetail::getCaseSetExeLogId, caseSetExeLogId);
        CaseSetExeLogDetail caseSetExeLogDetail = new CaseSetExeLogDetail();
        caseSetExeLogDetail.setYn(DataYnEnum.INVALID.getCode());
        update(caseSetExeLogDetail, lqw);
    }
}
