package com.jd.workflow.console.entity.watch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

import java.sql.Date;

@Data
@TableName(value = "code_activity_statistic", autoResultMap = true)
public class CodeActivityStatistic extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 统计天数
     */
    private String statisticDay;

    private String type;
    /**
     * 用户erp
     */
    private String erp;
    /**
     *
     */
    private Long costTime;

}
