package com.jd.workflow.console.dto.env;

import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItem;
import lombok.Data;

import java.util.List;

/**
 * @author wufagang
 * @description
 * @date 2023年06月06日 10:53
 */
@Data
public class EnvConfigDto {
    /**
     * 应用编码
     */
    private String appCode;
    private Long appId;
    /**
     * 接口id
     */
    private Long interfaceManageId;

    /**
     * 需求id
     */
    private Long requirementId;
    /**
     * 环境名称
     */
    private String envName;
    /**
     * 配置id
     */
    private Long envConfigId;
    /**
     * 配置详情id
     */
    private Long envConfigItemId;
    /**
     * 配置信息
     */
    private EnvConfig envConfig;
    /**
     * 配置详情列表信息
     */
    List<EnvConfigItem> envConfigItemList;
}
