package com.jd.workflow.console.service.usecase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.usecase.CaseSetExeLog;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDTO;
import com.jd.workflow.console.entity.usecase.CreateCaseSetExeLogParam;
import com.jd.workflow.console.entity.usecase.PageCaseSetExeLogParam;

/**
 * @description:
 * 用例集执行记录表 服务类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetExeLogManager extends IService<CaseSetExeLog> {

    /**
     * 通过用例执行记录id 删除数据
     * @param id
     */
    void delById(Long id);

}
