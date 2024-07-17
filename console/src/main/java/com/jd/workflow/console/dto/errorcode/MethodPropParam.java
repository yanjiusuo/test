package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7 
 */
@Data
public class MethodPropParam extends PageParam {

    /**
     * 应用id
     */
    private Long appId;
    /**
     * 属性名称，模糊搜索
     */
    private String prop;

}
