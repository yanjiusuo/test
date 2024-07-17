package com.jd.workflow.console.entity.usecase;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @description:
 * 用例集执行结果明细表
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("case_set_exe_log_detail")
public class CaseSetExeLogDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用例ID
     */
    @TableField("case_id")
    private Long caseId;

    /**
     * 用例集执行记录id
     */
    @TableField("case_set_exe_log_id")
    private Long caseSetExeLogId;

    /**
     * 用例执行结果id
     */
    @TableField("case_exe_result_id")
    private Long caseExeResultId;

    /**
     * 状态 1-成功 2-失败
     */
    @TableField("status")
    private Integer status;

//    /**
//     * 创建时间
//     */
//    @TableField("created")
//    private Date created;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    @TableField("yn")
    private Integer yn;

}
