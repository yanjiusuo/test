package com.jd.workflow.console.service.param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordDTO;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordParam;
import com.jd.workflow.console.entity.param.ParamBuilderRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 入参构建器记录 服务类
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
public interface IParamBuilderRecordService extends IService<ParamBuilderRecord> {

    /**
     * 执行记录
     * @param param
     * @return
     */
    Page listPage(ParamBuilderRecordParam param);

    /**
     * 执行记录详情
     * @param id
     * @return
     */
    ParamBuilderRecord getOneById(Long id);

    /**
     * 批量通过用例执行记录id获取执行结果信息
     * @param ids
     * @return
     */
    List<ParamBuilderRecordDTO> getByIds(List<Long> ids);
}
