package com.jd.workflow.console.entity.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/28
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/28 
 */
@Data
@TableName(value = "requirement_info_log",autoResultMap = true)
public class RequirementInfoLog extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 空间id
     */
    private Long requirementId;


    /**
     * 日志详情
     */
    @TableField(value = "`desc`")
    private String desc;
}
