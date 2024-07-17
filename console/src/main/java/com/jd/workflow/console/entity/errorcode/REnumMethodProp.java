package com.jd.workflow.console.entity.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

/**
 * @description: 枚举和接口字段关联关系表
 * @author: chenyufeng18
 * @Date: 2023/8/4 
 */
@Data
@TableName(value = "r_enum_method_prop",autoResultMap = true)
public class REnumMethodProp extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 接口入参或出参的属性名称
     */
    private String propName;

    /**
     * 枚举id
     */
    private Long enumId;

    /**
     * 属性类型
     */
    private String propType;

    /**
     * 属性说明
     */
    private String propDesc;
}
