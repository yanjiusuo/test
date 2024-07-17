package com.jd.workflow.soap.common.xml.schema;/**
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
public class JsonEnumPropDTO {
    /**
     * 主键
     */
    private Long id;


    /**
     * 枚举表主键
     */
    private Long enumId;

    /**
     * 编码
     */
    private String propCode;

    /**
     * 中文名
     */
    private String propName;
    /**
     * 描述
     */
    private String propDesc;

    /**
     * 解决方案描述
     */
    private String propSolution;


}
