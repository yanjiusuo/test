package com.jd.workflow.console.entity.watch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

/**
 * 消费活动统计点位。
 * 每次统计后记录下上次的点位
 */
@Data
@TableName(value = "code_activity_checkpoint", autoResultMap = true)
public class CodeActivityCheckpoint extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 点位类型
     */
    private String type;
    /**
     * 消费点位，默认是表主键id
     */
    String checkpoint;


}
