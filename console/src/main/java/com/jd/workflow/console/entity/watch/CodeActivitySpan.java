package com.jd.workflow.console.entity.watch;

import com.jd.workflow.console.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.watch.dto.BuildTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@TableName(value = "code_activity_span", autoResultMap = true)
public class CodeActivitySpan extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 起始时间
     */
    private Timestamp startTime;
    /**
     * 格式化的统计日期
     */
    private String statisticDay;
    /**
     * 结束时间
     */
    private Timestamp endTime;
    /**
     * 耗费时间
     */
    private Long costTime;

    /**
     * erp
     */
    private String erp;
}
