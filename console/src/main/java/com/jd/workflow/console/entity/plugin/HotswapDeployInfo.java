package com.jd.workflow.console.entity.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.plugin.dto.DeployFileStatisticInfo;
import lombok.Data;

/**
 * 插件安装
 */
@TableName(value = "hotswap_deploy_info",autoResultMap = true)
@Data
public class HotswapDeployInfo extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 部署总耗时
     */
    private Integer deployTotalCostTime;
    /**
     * 编译耗时：毫秒
     */
    private Integer compileCostTime;
    /**
     * 远程部署耗时
     */
    private Integer  remoteDeployCostTime;
    /**
     * 本地ip地址
     */
    private String localIp;
    /**
     * 远程ip地址
     */
    private String remoteIp;
    /**
     * joycoder、cjg
     */
    private String channel;

    /**
     * 部署文件数量
     */
    private Integer deployFileCount;
    /**
     * 部署erp
     */
    private String deployErp;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private DeployFileStatisticInfo deployFileStatisticInfo;
    /**
     * 部署请求id
     */
    String reqId;
    /**
     * 代码仓库
     */
    String codeRepository;
    /**
     * 代码分支
     */
    String codeBranch;
    /**
     * jdos应用编码
     */
    String jdosAppCode;

    /**
     * 是否成功：1-成功 2-是否
     */
    Integer succeed;
    /**
     * 失败原因
     */
    String failReason;

    /**
     * 环境编码
     */
    String envCode;
}
