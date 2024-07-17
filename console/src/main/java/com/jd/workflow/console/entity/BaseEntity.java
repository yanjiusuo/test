package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjingfang3
 * @create 2021-07-28
 */
@Data
public class BaseEntity extends BaseEntityNoDelLogic {


    /**
     * 逻辑删除标示 0、删除 1、有效
     * link{@com.jd.workflow.console.base.enums.DataYnEnum}
     */
    @TableField("yn")
    //@TableLogic(value = "1", delval = "0")
    private Integer yn;



//    /**
//     * 逻辑删除：0未删除，1删除
//     */
//    @TableLogic(value = "0", delval = "1")
//    private String delFlag;
//
//    private String remarks;
//    /**
//     * 删除标记（0：正常；1：删除；）
//     */
//    public static final String DEL_FLAG_NORMAL = "0";
//    public static final String DEL_FLAG_DELETE = "1";
}
