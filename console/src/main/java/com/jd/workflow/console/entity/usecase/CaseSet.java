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
 * 用例集表
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("case_set")
public class CaseSet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * Japi应用ID
     */
    @TableField("app_id")
    private Long appId;

    /**
     * 需求空间Id
     */
    @TableField("requirement_id")
    private Long requirementId;

    /**
     * 执行次数
     */
    @TableField("exe_count")
    private Integer exeCount;


    /**
     * 选择用例和接口数据
     */
    @TableField("selected_data")
    private String selectedData;

    /**
     *目前用例情况 0-只有jsf用例 1-只有http用例 2-两个都有
     */
    @TableField("case_type")
    private Integer caseType;

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
     * 修改者
     */
    @TableField("modifier")
    private String modifier;

    /**
     * 修改时间
     */
    @TableField("modified")
    private Date modified;


}
