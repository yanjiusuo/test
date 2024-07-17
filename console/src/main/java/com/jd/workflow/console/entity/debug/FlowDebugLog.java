package com.jd.workflow.console.entity.debug;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

/**
 * 流程调试日志
 */
@Data
public class FlowDebugLog extends BaseEntityNoDelLogic {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 是否执行成功
     */
    private Integer success;

    /**
     * 方法id
     */
    private String methodId;
    /**
     *环境：local、China、test
     */
    private String site;
    private String envName;

    private String logContent;

    private String digest;
    @TableField(value = "`desc`")
    private String desc;

    private Integer yn;
    /**
     * 1==置顶
     */
    private Integer topFlag;
    /**
     * 1=color
     */
    private Integer methodTag;

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
