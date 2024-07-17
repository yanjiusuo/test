package com.jd.workflow.console.service.model;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.ModelGroupDTO;
import com.jd.workflow.console.dto.QueryModelsResponseDTO;
import com.jd.workflow.console.dto.model.RequireModelPageQuery;
import com.jd.workflow.console.entity.model.ApiModelGroup;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */
public interface IApiModelGroupService extends IService<ApiModelGroup> {

    /**
     * 添加分组
     *
     * @param appId
     * @param name
     * @param enName
     * @param parentId
     * @return
     */
    Long addGroup(Long appId, String name, String enName, Long parentId);

    /**
     * 修改目录名称
     *
     * @param modelGroupDTO
     * @return
     */
    boolean modifyGroupName(ModelGroupDTO modelGroupDTO);

    /**
     * @param appId
     * @param groupId
     * @return
     */
    boolean removeGroup(Long appId, Long groupId);

    /**
     * 获取应用下的模型目录树
     *
     * @param appId
     * @return
     */
    MethodGroupTreeDTO findMethodGroupTree(Long appId);

    /**
     * 获取应用下的模型目录树
     *
     * @param appId
     * @param modelName
     * @param compactPackage 是否折叠文件夹
     * @return
     */
    MethodGroupTreeDTO findMethodGroupTree(Long appId, String modelName,Boolean compactPackage);

    /**
     * 更新目录树
     *
     * @param dto
     * @return
     */
    Boolean modifyMethodGroupTree(MethodGroupTreeDTO dto);

    List<ApiModelGroup> getGroupsByAppId(Long appId);


    /**
     * 获取需求关联的所有模型的目录树
     *
     * @param requirementId
     * @return
     */
    MethodGroupTreeDTO findRequireModelChildTree(Long requirementId, String modelName);

    /***
     * 需求交付结束时，保存模型的快照
     * @param requirementId
     * @return
     */
    boolean saveRequireModelSnapshot(Long requirementId);


    /**
     * 查询需求下的模型
     * @param requireModelPageQuery
     * @return
     */
    Page<QueryModelsResponseDTO> findRequireModelPage(RequireModelPageQuery requireModelPageQuery);

}
