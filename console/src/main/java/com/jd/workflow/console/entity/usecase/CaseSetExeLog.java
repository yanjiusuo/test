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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("case_set_exe_log")
public class CaseSetExeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用例集ID
     */
    @TableField("case_set_id")
    private Long caseSetId;

    /**
     * jsf别名
     */
    @TableField("jsf_alias")
    private String jsfAlias;

    /**
     * IP+端口
     */
    @TableField("ip")
    private String ip;

    /**
     * http环境
     */
    @TableField("http_env")
    private String httpEnv;

    /**
     * 用例总数
     */
    @TableField("case_total_no")
    private Integer caseTotalNo;

    /**
     * 用例执行成功数
     */
    @TableField("case_success_no")
    private Integer caseSuccessNo;

    /**
     * 用例执行失败数
     */
    @TableField("case_fail_no")
    private Integer caseFailNo;

    /**
     * 状态 1-用例待执行 2-用例执行中 3-覆盖率计算中 4-成功 5-失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 分支名
     */
    @TableField("branch_name")
    private String branchName;

    /**
     * 新增代码覆盖率
     */
    @TableField("new_code_coverage")
    private String newCodeCoverage;

    /**
     * 覆盖率计算开始时间
     */
    @TableField("coverage_start_time")
    private Date coverageStartTime;

    /**
     * 执行结束时间
     */
    @TableField("exe_end_time")
    private Date exeEndTime;

    /**
     * 备注
     */
    @TableField("remart")
    private String remark;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    @TableField("yn")
    private Integer yn;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField("created")
    private Date created;

    /**
     * bucketName
     */
    @TableField("bucket_name")
    private String bucketName;


    /**
     * coding地址
     */
    @TableField("coding_address")
    private String codingAddress;

    /**
     * 需求空间Id
     */
    @TableField("requirement_id")
    private Long requirementId;

    /**
     * 保存ip时会校验数据格式 和非空判断
     * @return
     */
    public String obtainIpData(){
        String[] split = ip.split(":");
        return split[0];
    }

}
