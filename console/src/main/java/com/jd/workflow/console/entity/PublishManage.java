package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 项目名称：example
 * 类 名 称：PublishManage
 * 类 描 述：发布实体
 * 创建时间：2022-05-31 19:34
 * 创 建 人：wangxiaofei8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("publish_manage")
public class PublishManage extends BaseEntity implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口类型 1-发布生效、0-历史发布
     */
    private Integer isLatest;

    /**
     * 发布后调用地址
     *//*
    private String address;*/

    /**
     * 所属的方法id
     */
    private Long relatedMethodId;

    /**
     * 存放的位置 版本id
     */
    private Integer versionId;

    /**
     * 方法内容 json信息 [大字段]
     */
    private String content;


    private Long clusterId;




}
