package com.jd.workflow.console.entity.env;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.*;

import java.io.Serializable;

/**
 * 项目名称：parent
 * 类 名 称：EnvConfig
 * 类 描 述：应用
 * 创建时间：2022-11-16 14:44
 * 创 建 人：wangxiaofei8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "env_config")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvConfig extends BaseEntity implements Serializable {

    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 站点信息
     */
    private String envName;
    /**
     * 站点信息:test、China
     */
    private String site;
    /**
     * 应用id
     */
    private Long appId;
    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 需求id
     */
    private Long requirementId;
    /**
     * 默认生成标志，0表示默认生成，1表示手工维护
     */
    private Integer defaultFlag ;
    /**
     * mock环境标志，0表示非mock，1表示mock
     */
    private Integer mockFlag;
    /**
     * 环境类型，0表示应用，1表示需求
     */
    private Integer envType;
    /**
     * 当envType=1时，存对应应用环境的id
     */
    private Long appEnvId;


}
