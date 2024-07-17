package com.jd.workflow.console.entity.manage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

@Data
public class TopSupportInfo extends BaseEntityNoDelLogic {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口类型 1-interface 2-requirement
     */
    @TableField("`type`")
    private Integer type;
    /**
     * 关联id: type为1的时候为接口id type为2的时候为需求id
     */
    private Long relatedId;

    /**
     * 当前时间
     */
    Long currentTime;
    /**
     * 操作人
     */
    String operator;
}
