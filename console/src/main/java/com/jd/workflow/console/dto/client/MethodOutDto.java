package com.jd.workflow.console.dto.client;

import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

/***
 * 对外输出接口dto
 */
@Data
public class MethodOutDto extends BaseEntity {
    /**
     * 接口id
     */
    Long id;
    /**
     * 方法编码
     */
    String methodCode;
    /**
     * 方法名称
     */
    String name;
    /**
     * 单个的http方法
     */
    String httpMethod;
    /**
     * http方法列表，以，分割
     */
    String httpMethods;
    /**
     * 接口id
     */
    private Long interfaceId;
    /**
     *  请求路径
     */
    String path;
    /**
     * 方法描述
     */
    String desc;
    /**
     * 是否有效
     */
    Integer yn;
    /**
     * 文档链接
     */
    String docUrl;
    /**
     * 部门
     */
    String dept;
    /**
     * 应用id
     */
    Long appId;
    /**
     * 藏经阁应用编码
     */
    String appCode;
    /**
     * jdos应用编码
     */
    String jdosAppCode;
}
