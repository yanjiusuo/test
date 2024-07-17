package com.jd.workflow.console.dto;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/19
 */

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

/**
 * @description: 模型
 * @author: chenyufeng18
 * @Date: 2023/7/19 
 */
@Data
public class ApiModelDTO {

    Long id;
    String name; // 类名
    /**
     * 应用id
     */
    Long appId;
    /**
     * 描述
     */
    String desc;
    /**
     * 是否自动上报
     */
    Integer autoReport;

    /**
     * 模型主体数据
     */
    JsonType content; // 自动上报的内容


    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 0 是模型，1是文件夹
     */
    private Integer type;

    /**
     * 增加包路径
     */
    private String packagePath;

}
