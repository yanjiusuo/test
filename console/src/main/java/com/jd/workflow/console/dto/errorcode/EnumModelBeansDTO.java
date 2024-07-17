package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/19
 */

import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.doc.InterfaceCountModel;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/19 
 */
@Data
public class EnumModelBeansDTO {

    /**
     * bean 树
     */
    private List<InterfaceCountModel> beansList;

    /**
     * model 树
     */
    private MethodGroupTreeDTO modelTree;

    /**
     *
     */
    private List<EnumDTO> allEnumList;

    /**
     * bean总数
     */
    private Integer beanCount;
    /**
     * 模型总数
     */
    private Integer modelCount;
    /**
     *  枚举总数
     */
    private Integer enumCount;
}
