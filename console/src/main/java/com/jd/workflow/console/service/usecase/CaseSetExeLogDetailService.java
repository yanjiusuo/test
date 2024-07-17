package com.jd.workflow.console.service.usecase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordDTO;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDTO;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDetail;
import com.jd.workflow.console.entity.usecase.PageCaseSetExeLogDetailParam;
import com.jd.workflow.console.entity.usecase.PageCaseSetExeLogParam;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetExeLogDetailService extends IService<CaseSetExeLogDetail> {
    /**
     * 分页查询-用例集执行记录
     * @param pageParam
     * @return
     */
    Page<ParamBuilderRecordDTO> pageList(PageCaseSetExeLogDetailParam pageParam);

    /**
     * 通过用例集执行记录id 删除明细数据
     * @param caseSetExeLogId
     */
    void delByCaseSetExeLogId(Long caseSetExeLogId);
}
