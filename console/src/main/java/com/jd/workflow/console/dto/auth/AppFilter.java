package com.jd.workflow.console.dto.auth;

import com.jd.workflow.console.base.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class AppFilter extends PageParam {


    /**
     * 应用名称或编码
     */
    private String nameOrCode;
    /**
     * 负责人--支持erp
     */
    @ApiModelProperty("负责人")
    String adminCode;


    /**
     * 藏经阁业务域的trace字段，规则为：-0--1--4--8- 这种
     */
    private String cjgBusinessDomainTrace;

    /**
     * 藏经阁产品域
     */
    private String cjgProductTrace;



    @ApiModelProperty("部门名称--中文")
    private String deptName;


    private Boolean hasLicense;


    /**
     * 是否关注：1-已关注 0-未关注 不传为全部
     */
    private Integer isFollow;

    /**
     * 1==产品域 2==业务域
     */
    private Integer queryType;






}
