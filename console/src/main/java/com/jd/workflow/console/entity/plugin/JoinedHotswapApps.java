package com.jd.workflow.console.entity.plugin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

/**
 * 已经接入的jdos app应用
 */
@Data
@TableName(value = "joined_hotswap_apps",autoResultMap = true)
public class JoinedHotswapApps extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * jdos应用编码
     */
    String jdosAppCode;

    /**
     * 代码仓库
     */
    String codeRepository;

    String site;

}
