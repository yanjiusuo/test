package com.jd.workflow.console.dto.usecase;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @description: 用例集表
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Data
public class CaseSetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * Japi应用ID
     */
    private Long appId;

    /**
     * 需求空间Id
     */
    private Long requirementId;

    /**
     * 执行次数
     */
    private Integer exeCount;

    /**
     *目前用例情况 0-只有jsf用例 1-只有http用例 2-两个都有
     */
    @TableField("case_type")
    private Integer caseType;

    /**
     * 选择用例和接口数据
     */
    private String selectedData;

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
     * 修改者
     */
    private String modifier;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 接口类全名
     */
    private String serviceCode;
}
