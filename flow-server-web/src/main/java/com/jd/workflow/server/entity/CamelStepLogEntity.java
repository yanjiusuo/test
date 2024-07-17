package com.jd.workflow.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目名称：example
 * 类 名 称：CamelStepLog
 * 类 描 述：camel调用日志
 * 创建时间：2022-06-07 15:42
 * 创 建 人：wangxiaofei8
 */
@Data
@TableName("camel_step_log")
public class CamelStepLogEntity implements Serializable {
    public CamelStepLogEntity(){}
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    private String businessId;


    private String methodId;


    private Integer logLevel;


    private String version;


    private String logContent;


    private Date created;




}
