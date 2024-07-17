package com.jd.workflow.console.entity.env;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName(value = "env_config_item_interface")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnvConfigItemInterface extends BaseEntity implements Serializable {

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
     * 环境配置id
     */
    private Long envConfigItemId;
    /**
     * 接口id
     */
    private Long interfaceManageId;

//    /**
//     * 应用id
//     */
//    private Long relId;
//    /**
//     * 环境类型，0表示应用，1表示需求
//     */
//    private Integer envType;

}
