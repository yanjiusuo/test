package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 项目名称：parent
 * 类 名 称：PublishCluster
 * 类 描 述：TODO
 * 创建时间：2022-12-27 15:39
 * 创 建 人：wangxiaofei8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "publish_cluster")
public class PublishCluster extends BaseEntity implements Serializable {

    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 集群code
     */
    private String clusterCode;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 集群域名
     */
    private String clusterDomain;

    /**
     * 状态 0 未启用 1 启用  默认启用
     */
    private Integer status;

    /**
     * 成员
     */
    private String members;

    /**
     * 应用描述
     */
    @TableField("`desc`")
    private String desc;
}
