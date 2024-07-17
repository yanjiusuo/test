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
public class BaseEntityNoDelLogic implements Serializable {



    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    /**
     * 修改者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date created;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date modified;



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
