package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.base.enums.MeasureDataEnum;
import lombok.Data;

/**
 * @author yza
 * @description
 * @date 2024/1/12
 */
@Data
@TableName(value = "measure_data", autoResultMap = true)
public class MeasureData extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 1.http接口上报 2.jsf接口上报 3.快捷调用（http） 4.接口文档详情 5.上报插件下载 6.mock(jsf默认模版) 7.mock(http默认模版) 8.快捷调用一键mock（jsf） 9.快捷调用一键mock（http）10.快捷调用（jsf）
     * @see MeasureDataEnum
     */
    private int type;

    /**
     * 数量
     */
    private int num;

    /**
     * 状态：1.成功  2.失败
     */
    private int status;

    /**
     * 备注
     */
    private String note;

    /**
     * 用户
     */
    private String erp;

    /**
     * 部门
     */
    private String department;

    /**
     * 所属子集团
     */
    private String dep0;

    /**
     * 一级部门
     */
    private String dep1;

    /**
     * 二级部门
     */
    private String dep2;

    /**
     * 三级部门
     */
    private String dep3;

}
