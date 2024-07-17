package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/7
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("coding_app_relation")
public class CodingAppRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口全名
     */
    private String codePath;


    /**
     * 应用名称
     */
    private String appCode;

    /**
     * 部门信息
     */
    private String dept;


    private String erp;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    @TableField("yn")
    private Integer yn;

    /**
     * 创建时间
     */
    @TableField("created")
    private LocalDateTime created;

    /**
     * 修改时间
     */
    @TableField("modified")
    private LocalDateTime modified;

    private String creator;

    private String modifier;


    public CodingAppRelation() {
        this.yn = 1;
    }
}
