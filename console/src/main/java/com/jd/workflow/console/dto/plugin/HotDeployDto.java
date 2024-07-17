package com.jd.workflow.console.dto.plugin;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.entity.plugin.dto.DeployFileStatisticInfo;
import lombok.Data;

@Data
public class HotDeployDto {
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
     * 部署文件数量
     */
    private Integer deployFileCount;
    /**
     * 本地ip地址
     */
    private String localIp;
    /**
     * 远程ip地址
     */
    private String remoteIp;
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

    String channel;

    /**
     * 是否成功：1-成功 0-否
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

    /**
     * 热更新文件
     */
    HotFiles hotFiles;

    /**
     * 桶名称
     */
    String bucketName;

    private  boolean onlyUpdateClass;

}
