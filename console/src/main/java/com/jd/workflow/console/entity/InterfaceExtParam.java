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
@TableName("interface_ext")
public class InterfaceExtParam extends BaseEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    @TableField("`interface_id`")
    Long interfaceId;

    @TableField("content")
    String content;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     * link{@com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    @TableField("`type`")
    private Integer type;

}
