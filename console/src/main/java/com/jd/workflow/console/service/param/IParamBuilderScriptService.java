package com.jd.workflow.console.service.param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.dto.requirement.ParamBuilderScriptParam;
import com.jd.workflow.console.entity.param.ParamBuilderScript;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 入参构建脚本 服务类
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-13
 */
public interface IParamBuilderScriptService extends IService<ParamBuilderScript> {

    /**
     * 列表分页
     * @param param
     * @return
     */
    Page listPage(ParamBuilderScriptParam param);

    /**
     * 保存
     * @return
     */
    boolean saveScript(ParamBuilderScript param);

    /**
     * 编辑
     * @param param
     * @return
     */
    boolean editScript(ParamBuilderScript param);

    /**
     * 复制
     * @param id
     * @return
     */
    ParamBuilderScript copy(Long id);

}
