package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.PublishRecordDto;
import lombok.Data;

/**
 * 所有在线的发布记录：存放所有已发布的记录信息
 * 健康想要去掉ducc，发布后直接存到该库里即可
 */
@Data
@TableName(autoResultMap = true)
public class RouteConfigRecord extends BaseEntityNoDelLogic{

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 服务路径，与methodId无关了
     */
    private String methodId;
    /**
     * 当前版本号，每次+1
      */
    private Long version;
    @TableField(typeHandler= JacksonTypeHandler.class)
    private PublishRecordDto config;

}
