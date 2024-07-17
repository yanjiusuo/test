package com.jd.workflow.console.dto.doc;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/14
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/14 
 */
@Data
public class EnumClassDTO {
    /**
     * 枚举类名称
     */
    private String enumClassName;

    /**
     * 枚举类描述
     */
    private String enumClassDesc;


    /**
     * code
     */
    private String code;
    /**
     * 枚举名称
     */
    private String name;
    /**
     * 枚举描述
     */
    private String desc;

    /**
     * 枚举类型，0 错误码，1是普通枚举
     */
    private Integer type;

    /**
     * 包路径
     */
    private String packagePath;
}
