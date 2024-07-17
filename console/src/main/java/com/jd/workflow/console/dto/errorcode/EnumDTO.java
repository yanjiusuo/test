package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7 
 */
@Data
public class EnumDTO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 枚举类型：0 错误码，1 枚举
     */
    private Integer enumType;

    /**
     * 编码
     */
    private String  enumCode;

    /**
     * 中文名称
     */
    private String enumName;

    /**
     * 说明文案
     */
    private String enumDesc;

    /**
     * 应用id
     */
    private Long  appId;


    /**
     * 包路径
     */
    private String packagePath;
}
