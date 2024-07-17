package com.jd.workflow.console.service.usecase;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.usecase.*;
import com.jd.workflow.console.entity.usecase.enums.CaseSetExeLogStatusEnum;
import com.jd.workflow.server.dto.requirement.QueryRequirementCodeParam;

import java.util.List;

/**
 * @description:
 * 用例集执行记录表 服务类
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetExeLogService  extends IService<CaseSetExeLog> {

    /**
     * 创建执行批次记录
     * @param createParam
     * @return
     */
    Long createCaseSetExeLog(CreateCaseSetExeLogParam createParam);

    /**
     * 获取分页查询数据
     * @param pageParam
     * @return
     */
    Page<CaseSetExeLogDTO> pageList(PageCaseSetExeLogParam pageParam);

    /**
     * 获取批次执行记录详情数据
     * @param detailParam
     */
    CaseSetExeLogInfo detail(CaseSetExeLogDetailParam detailParam);

    /**
     * 分页查询 CaseSetExeLog 数据
     * @param pageParam
     * @return
     */
    Page<CaseSetExeLog> listCaseSetExeLogs(PageCaseSetExeLogParam pageParam);

    /**
     * 失败用例数加1
     * @param id
     * @return
     */
    boolean failNoAddOne(Long id);

    /**
     * 成功用例数加1
     * @param id
     * @return
     */
    boolean successNoAddOne(Long id);

    /**
     * 状态调整到 用例执行中 。前置状态可为null
     * @param id
     */
    boolean updateStatus(Long id, CaseSetExeLogStatusEnum preStatus,CaseSetExeLogStatusEnum resultStatus,String remark);

    /**
     * 重新执行用例集数据
     * @param id
     */
    Long reExecute(Long id);

    /**
     * 修改状态为成功
     * @param caseSetExeLog
     * @return
     */
    boolean update2Success(CaseSetExeLog caseSetExeLog);

    /**
     * 通过coding地址和分支名查询需求code列表
     * @param queryParam
     * @return
     */
    List<String> queryRequirementCodes(QueryRequirementCodeParam queryParam);
}
