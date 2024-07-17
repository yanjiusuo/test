package com.jd.workflow.console.entity.debug.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class PluginCallLog {
    /**
     * 是否执行成功
     */
    private Integer success;


    /**
     *环境：local、China、test
     */
    private String site;
    private String envName;

    private String logContent;

    @TableField(value = "`desc`")
    private String desc;

   private String userToken;

    private String channel;
    /**
     * jsf对应接口
     */
    private String interfaceName;
    /**
     * 类型 {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    private Integer type;
    /**
     * 方法路径
     */
    private String methodPath;
    private String jdosAppCode;
    private String codeRepository;
    private String branch;
}
