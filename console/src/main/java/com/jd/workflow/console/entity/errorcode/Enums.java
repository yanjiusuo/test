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
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4 
 */
@Data
@TableName(value = "enum",autoResultMap = true)
public class Enums  extends BaseEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 枚举类型：0 错误码，1 枚举
     */
    private Integer enumType;

    /**
     * 编码
     */
    private String  enumCode;

    /**
     * 中文名称
     */
    private String enumName;

    /**
     * 说明文案
     */
    private String enumDesc;

    /**
     * 应用id
     */
    private Long  appId;

    /**
     * 包路径
     */
    private String packagePath;
}
