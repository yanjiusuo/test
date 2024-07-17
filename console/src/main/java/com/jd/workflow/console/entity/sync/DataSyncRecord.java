package com.jd.workflow.console.entity.sync;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

/**
 * 不同平台的数据同步记录
 */
@Data
public class DataSyncRecord extends BaseEntityNoDelLogic {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    Integer success;

    String errorInfo;
    /**
     * 渠道：jddj、j-api等
     */
    String source;
    String sourceAppCode;
    String sourceEnv;
    String sourceGroup;
    Integer totalCost;
    String targetAppCode;
    String targetInterfaceId;
    String lastSyncVersion;
}
