package com.jd.workflow.console.service.param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.console.dto.requirement.ParamBuilderAddParam;
import com.jd.workflow.console.dto.requirement.ParamBuilderParam;
import com.jd.workflow.console.entity.param.ParamBuilder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.param.ParamBuilderRecord;

import java.util.List;

/**
 * <p>
 * 入参构建器 服务类
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
public interface IParamBuilderService extends IService<ParamBuilder> {

    /**
     * 执行构造器
     * 1、生成调用记录，待执行状态
     * 2、执行任务，调用接口，回填返回结果
     *
     * @param id
     */
    void run(Long id, String cookie);

    /**
     * 批量执行，置状态为待执行，异步执行
     * @param ids
     */
    void batchRun(String ids, String cookie);

    /**
     *
     * @param param
     * @return
     */
    Page listPage(ParamBuilderParam param);

    /**
     *
     * @param param
     * @return
     */
    boolean saveCase(ParamBuilderAddParam param, String cookie);

    /**
     *
     * @param id
     * @return
     */
    boolean logicDelete(Long id);

    /**
     *
     * @param id
     * @return
     */
    long copy(Long id);

    /**
     * 根据方法id集合批量获取用例
     * @param methodIds
     * @return
     */
    List<ParamBuilder> listByMethodIds(String methodIds);

    /**
     *
     * @param id
     * @param ip
     * @param alias
     * @param dto
     * @return
     */
    ParamBuilderRecord run(Long id, String ip, String alias, HttpDebugDto dto);

    /**
     * 更新
     * @param param
     * @param cookie
     * @return
     */
    boolean update(ParamBuilderAddParam param, String cookie);

}
