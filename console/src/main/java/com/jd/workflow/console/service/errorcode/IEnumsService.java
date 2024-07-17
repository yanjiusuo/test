package com.jd.workflow.console.service.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.errorcode.DeleteEnumPropDTO;
import com.jd.workflow.console.dto.errorcode.EnumDTO;
import com.jd.workflow.console.entity.errorcode.Enums;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */
public interface IEnumsService extends IService<Enums> {

    /**
     * 获取错误码枚举id
     *
     * @param appId
     * @return
     */
    Long getErrorCodeEnumId(Long appId);

    /**
     * 获取所有枚举类型，包括：错误码+枚举
     *
     * @param appId
     * @return
     */
    List<EnumDTO> queryAllEnums(Long appId);


    /**
     * 通过类型，查询枚举列表
     *
     * @param appId
     * @param enumType
     * @return
     */
    List<EnumDTO> queryEnumsByType(Long appId, Integer enumType);

    /**
     * 删除枚举
     *
     * @param enumDTO
     * @return
     */
    Boolean deleteEnum(EnumDTO enumDTO);

    /**
     * 初始化http入参和出参中的枚举
     *
     * @param httpMethodModel
     */
    void initHttpEnums(HttpMethodModel httpMethodModel, Long interfaceId);

    /**
     * 初始化jsf入参和出参中的枚举
     *
     * @param contentJSFObject
     */
    void initJsfEnums(JsfStepMetadata contentJSFObject, Long interfaceId);

    /**
     * 保存枚举
     * @param enumDTO
     * @return
     */
    Boolean saveEnum(EnumDTO enumDTO);

    /**
     *
     * @param methodManageDTO
     */
    void initContentEnums(MethodManageDTO methodManageDTO);



}
