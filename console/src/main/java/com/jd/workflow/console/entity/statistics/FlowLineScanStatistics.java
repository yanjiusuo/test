package com.jd.workflow.console.entity.statistics;

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
 * <p>
 * 统计应用，coding维度流水线扫描数据
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-07-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("flow_line_scan_statistics")
public class FlowLineScanStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 流水线执行记录Id
     */
    @TableField("line_id")
    private Long lineId;

    /**
     * 流水线执行记录状态
     */
    @TableField("line_status")
    private Integer lineStatus;

    /**
     * jdos应用code
     */
    @TableField("app_code")
    private String appCode;

    /**
     * coding地址后缀
     */
    @TableField("code_address")
    private String codeAddress;

    /**
     * 逻辑删除标示 1-app 2-coding
     */
    @TableField("type")
    private Integer type;

    /**
     * 扫描出Http类数量
     */
    @TableField("scan_http_no")
    private Integer scanHttpNo;

    /**
     * 扫描出JSF接口数量
     */
    @TableField("scan_jsf_no")
    private Integer scanJsfNo;

    /**
     * JSF接口数量
     */
    @TableField("jsf_no")
    private Integer jsfNo;

    /**
     * 部门名称
     */
    @TableField("dept")
    private String dept;

    /**
     * 上线分支
     */
    @TableField("branch")
    private String branch;

    /**
     * 季度数
     */
    @TableField("quarter_no")
    private Integer quarterNo;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    @TableField("yn")
    private Integer yn;

    /**
     * 创建时间
     */
    @TableField("created")
    private Date created;

    /**
     * 修改时间
     */
    @TableField("modified")
    private Date modified;


}
