package com.jd.workflow.domain;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6 
 */
@Data
public class InterfaceInfo {
    private Long id;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     */

    private Integer type;

    /**
     * 接口名称
     */

    private String name;

    /**
     * 接口描述
     */

    private String desc;

    /**
     * 是否设置为demo   1-是  2-否
     */

    private Integer isPublic;

    /**
     * 是否有操作权限（不存入表中）  1-有  2-无
     */

    private Integer hasAuth;

    /**
     * 大json串
     * Map<String,EnvModel>
     */

    private String env;

    /**
     * 地址
     *
     * @date: 2022/5/17 14:44
     * @author wubaizhao1
     */

    private String path;

    /**
     * 租户id
     */

    private String tenantId;

    /**
     * 编排类型 0-默认 1-单节点 2-多节点
     *
     * @date: 2022/6/1 16:03
     * @author wubaizhao1
     */
    private Integer nodeType;
    /**
     * 服务编码
     *
     * @date: 2022/6/2 15:56
     * @author wubaizhao1
     */
    private String serviceCode;
    /**
     * 数据库无关字段----------------------------------
     */

    /**
     * 用户Code
     */

    private String userCode;

    /**
     * 用户名称
     */

    private String userName;


    /**
     * 关联记录id,目前用来保存mock记录关联的id。也可以是j-api的接口或者方法id
     */
    private Long relatedId;

    /**
     * 关联藏经阁appId
     */
    private String cjgAppId;
    /**
     * 关联藏经阁appName
     */
    private String cjgAppName;
    /**
     * 是否有权限编辑 false-不能
     *
     * @date: 2022/6/19 10:38
     * @author wubaizhao1
     */

    private Boolean editable;


    String groupId;

    String artifactId;


    String version;


    /**
     * 当前分组版本，默认当前时间戳
     */

    private String groupLastVersion;
    /**
     * 是否自动上报：1-是 0-否
     */
    Integer autoReport;

    Long appId;
    /**
     * 最新文档的版本
     */
    String latestDocVersion;

    /**
     * 藏经阁业务域的trace字段，规则为：-0--1--4--8- 这种，需要查询时，直接like前缀查询即可
     */
    String cjgBusinessDomainTrace;
    /**
     * 藏经阁业务域名称
     */

    String cjgBusinessDomainTraceName;

    /**
     * 文档信息
     */
    String docInfo;



    Date latestReportTime;

    /**
     * 应用名称:appId关联的应用名称
     */

    String appName;
    /**
     * 应用编码:appCode关联的应用编码
     */

    String appCode;
    /**
     * 是否鉴权接口，true:是 false: 否
     */

    Boolean hasLicense = false;


    Boolean needApply = false;
    /**
     * 关注状态：1-已关注 0-未关注
     */

    Integer followStatus  ;


    /**
     * 所属部门名称
     */

    private String deptName;
    /**
     * 接口级别
     */

    private Integer level;
    /**
     * 可见范围 0默认全部可见 1 应用成员可见
     */

    private Integer visibility;
}
