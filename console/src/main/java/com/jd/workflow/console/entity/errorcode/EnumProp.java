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
 * @description: 枚举值
 * @author: chenyufeng18
 * @Date: 2023/8/4 
 */
@Data
@TableName(value = "enum_prop",autoResultMap = true)
public class EnumProp  extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    /**
     * 枚举表主键
     */
    private  Long enumId;

    /**
     * 编码
     */
    private String propCode;

    /**
     * 中文名
     */
    private String propName;
    /**
     * 描述
     */
    private String propDesc;

    /**
     * 解决方案描述
     */
    private String propSolution;

}
