package com.jd.workflow.console.dto.requirement;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-11
 */
@Data
public class ParamBuilderScriptParam extends PageParam {


    /**
     * 工具名称
     */
    private String scriptName;

    /**
     * 工具内容
     */
    private String scriptContent;

    /**
     * 脚本来源 1、物料平台无返回值  2、物料平台有返回值
     */
    private Integer scriptSource;

    /**
     * 类型： 1.动态物料 2.静态物料
     */
    private Integer type;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建人
     */
    private String creator;
}
