package com.jd.workflow.console.service.model;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/18
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.javaparser.resolution.types.ResolvedType;
import com.jd.workflow.console.dto.ApiModelDTO;
import com.jd.workflow.console.dto.QueryModelsResponseDTO;
import com.jd.workflow.console.dto.model.ApiModelPageQuery;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/18 
 */
public interface IApiModelService extends IService<ApiModel> {

    /**
     * 添加模型
     * @param apiModelDTO
     * @return
     */
    Long addModel(ApiModelDTO apiModelDTO);

    /**
     * 编辑模型
     * @param apiModelDTO
     * @return
     */
    boolean editModel(ApiModelDTO apiModelDTO);


    /**
     *  删除模型
     * @param apiModelDTO
     * @return
     */
    boolean removeModel(ApiModelDTO apiModelDTO);

    /**
     * 获取模型
     * @param id
     * @return
     */
    ApiModelDTO getModelById(Long id,Long requireId);

    /**
     *
     * @param apiModelPageQuery
     * @return
     */
    Page<QueryModelsResponseDTO> queryModels(ApiModelPageQuery apiModelPageQuery);

    /**
     * 解析sql语句，返回字段
     * @param sql
     * @return
     */
    ObjectJsonType parseSql(String sql);
    ObjectJsonType parseJavaBean(String javaBean);
    String generateCode(Long modelId,String type);
    String generateCode(JsonType objectJsonType,String name,String type);

    boolean removeDuplicatedModel();

    public List<ApiModel> queryModels(List<String> names,Long  appId);

    public List<ApiModel> getModelsByAppId(Long appId);

    /**
     * 查询应用下model总数
     * @param appId
     * @return
     */
    int queryModelCount(Long appId);

    ApiModelDTO getByRefName(Long appId,String refName);

    void initTypeInfoAndChild(ResolvedType resolvedType, String type, BuilderJsonType jsonType);

    /**
     *  上面方法 initTypeInfoAndChild 会导致 date 类型 编程String类型  类型不准
     * @param resolvedType
     * @param type
     * @param jsonType
     */
    void initParserTypeInfoAndChild(ResolvedType resolvedType, String type, BuilderJsonType jsonType);
}
