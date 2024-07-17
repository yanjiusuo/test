package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口关注列表
 */
@Data
@TableName(value = "interface_follow_list",autoResultMap = true)
public class InterfaceFollowList extends BaseEntityNoDelLogic{

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 接口id
     */
    private Long interfaceId;
    /**
     * 方法id
     */
    private Long methodId;
    /**
     * erp列表
     */
    private String erp;
}
