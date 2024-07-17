package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * java bean 模板接口
 */
@Data
public class JavaBeanTemplate extends BaseEntity{
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 初始配置信息
     */
    private String initConfig;
    /**
     * 类型信息：ducc、jsf等
     */
    private String type;

    private String methodConfig;
}
