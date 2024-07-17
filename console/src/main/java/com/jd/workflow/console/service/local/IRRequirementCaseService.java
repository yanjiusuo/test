package com.jd.workflow.console.service.local;

import com.jd.workflow.console.entity.local.LocalTestRecord;
import com.jd.workflow.console.entity.local.RRequirementCase;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 需求空间与用例关系表 服务类
 * </p>
 *
 * @author sunchao81
 * @since 2024-07-02
 */
public interface IRRequirementCaseService extends IService<RRequirementCase> {

    /**
     *
     * @param requirementId
     * @param record
     */
    void bindRequirementCase(Long requirementId, LocalTestRecord record);
}
