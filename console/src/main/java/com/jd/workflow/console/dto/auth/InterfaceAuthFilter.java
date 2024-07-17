package com.jd.workflow.console.dto.auth;

import com.jd.workflow.console.base.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InterfaceAuthFilter extends PageParam {
    /**
     * 接口类型：1-http 2-webservice 3-jsf
     */
    @ApiModelProperty("接口类型：1-http 2-webservice 3-jsf")
    private Integer type;
    /**
     * 是否鉴权接口：true 过滤出只鉴权接口 false 只显示普通接口
     */
    private Boolean hasLicense;

    /**
     * 接口名称
     */
    @ApiModelProperty("接口名称，用来模糊搜索")
    private String name;

    /**
     * 应用名称或编码
     */
    private String nameOrCode;
    /**
     * 负责人
     */
    @ApiModelProperty("负责人")
    String adminCode;

    /**
     * 是否只查看自己的接口：1-是 0-否 ，接口市场传 0
     */
    @ApiModelProperty("是否只查看自己的接口：1-是 0-否,默认为0 ，接口市场传 0")
    private Integer onlySelf;
    @ApiModelProperty("应用ids")
    private List<Long> appIds;
    /**
     * 应用id
     */
    private Long appId;
    /**
     * 是否关注：1-已关注 0-未关注 不传为全部
     */
    private Integer isFollow;
    /**
     * 藏经阁业务域的trace字段，规则为：-0--1--4--8- 这种
     */
    private String cjgBusinessDomainTrace;

    @ApiModelProperty("部门名称")
    private String deptName;

    @ApiModelProperty(hidden = true)
    private Boolean nullDept;
    /**
     * 接口级别
     */
    private Integer level;


    /**
     * 藏经阁产品域
     */
    private String cjgProductTrace;

    /**
     * 健康度-低值
     */
    private Double scoreMin;
    /**
     * 健康度-高值
     */
    private Double scoreMax;
    /**
     * 查询类别 1=产品域 2业务域
     */
    private Integer queryType;




}
