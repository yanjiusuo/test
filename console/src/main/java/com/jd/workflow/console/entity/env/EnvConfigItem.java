package com.jd.workflow.console.entity.env;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.*;

import java.beans.Transient;
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
@TableName(value = "env_config_item")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnvConfigItem extends BaseEntity implements Serializable {

    /**
     * 分组id主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 环境配置id
     */
    private Long envConfigId;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * url
     */
    private String url;
    /**
     * 自动生成标志
     */
    private Integer defaultFlag ;
    /**
     * 接口类型
     */
    private Integer interfaceType ;
    /**
     * 请求方式
     */
//    private String req_type ;
    /**
     * configJson
     */
    private String configJson;

    /**
     * 是否选中， 如果有true表示选中，如果没有可以随机选中一个作为默认值
     */
    @TableField(exist = false)
    private Boolean selected ;


}
