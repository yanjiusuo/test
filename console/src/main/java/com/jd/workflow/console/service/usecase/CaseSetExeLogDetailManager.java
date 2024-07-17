package com.jd.workflow.console.service.usecase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDetail;
import com.jd.workflow.console.entity.usecase.PageCaseSetExeLogDetailParam;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetExeLogDetailManager extends IService<CaseSetExeLogDetail> {

    /**
     * 保存用例执行记录 更新用例集成功失败数量
     * @param caseSetExeLogDetail
     */
    void saveCaseSetExeLogDetail(CaseSetExeLogDetail caseSetExeLogDetail);
}
