package com.jd.workflow.console.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("jsf_alias")
public class JsfAlias extends BaseEntity implements Serializable {

    //主键
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    //所属接口ID
    @TableField("interface_id")
    Long interfaceId;

    //别名
    @TableField("alias")
    String alias;

    //站点
    @TableField("site")
    String site;

    //环境
    @TableField("env")
    String env;

}
