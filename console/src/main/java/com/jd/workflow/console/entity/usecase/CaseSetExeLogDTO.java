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
 * 用例集执行记录
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Data
@Accessors(chain = true)
public class CaseSetExeLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    private Long id;

    /**
     * 用例集ID
     */
    private Long caseSetId;

    /**
     * jsf别名
     */
    private String jsfAlias;

    /**
     * IP+端口
     */
    private String ip;


    /**
     * http环境
     */
    private String httpEnv;

    /**
     * 用例总数
     */
    private Integer caseTotalNo;

    /**
     * 用例执行成功数
     */
    private Integer caseSuccessNo;

    /**
     * 用例执行失败数
     */
    private Integer caseFailNo;

    /**
     * 状态 1-用例待执行 2-用例执行中 3-覆盖率计算中 4-成功 5-失败
     */
    private Integer status;

    /**
     * 分支名
     */
    private String branchName;

    /**
     * 新增代码行覆盖率
     */
    private String newCodeCoverage;

    /**
     * 覆盖率计算开始时间
     */
    private Date coverageStartTime;

    /**
     * 执行结束时间
     */
    private Date exeEndTime;

    /**
     * 备注
     */
    private String remart;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    private Integer yn;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 进度百分比
     */
    private String progressPercentage;

}
