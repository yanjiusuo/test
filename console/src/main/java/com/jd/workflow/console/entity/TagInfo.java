package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "tag_info",autoResultMap = true)
public class TagInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    private Long appId;


    private String name;

    private Integer yn;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 操作人
     */
    private String modifier;

    private String creator;
}
