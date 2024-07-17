package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */

import com.jd.workflow.console.dto.MethodPropDTO;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7 
 */
@Data
public class BindPropParam {
    /**
     * 类型：0 为错误码，1为枚举
     */
    private Integer enumType;
    /**
     * 枚举id，错误码时不需要填
     */
    private Long id;
    /**
     * 应用id
     */
    private Long appId;
    /**
     * 属性名称，准确名称，不能模糊。
     */
    private String prop;

    /**
     * 批量绑定属性
     */
    private List<MethodPropDTO> bindProps;

}
