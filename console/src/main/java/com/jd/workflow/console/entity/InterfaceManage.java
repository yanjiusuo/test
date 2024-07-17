package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.SimpleAppInfo;
import com.jd.workflow.console.dto.doc.InterfaceDocConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * 接口管理
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "interface_manage", autoResultMap = true)
public class InterfaceManage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     * {@link InterfaceTypeEnum}
     */
    @TableField("`type`")
    private Integer type;

    /**
     * 接口名称
     */
    @TableField("`name`")
    private String name;

    /**
     * 接口描述
     */
    @TableField("`desc`")
    private String desc;

    /**
     * 是否设置为demo   1-是  2-否
     */
    @TableField("`is_public`")
    private Integer isPublic;

    /**
     * 是否有操作权限（不存入表中）  1-有  2-无
     */
    @TableField(exist = false)
    private Integer hasAuth;

    /**
     * 大json串
     * {@link EnvModel}
     * Map<String,EnvModel>
     */
    @TableField("env")
    private String env;

    /**
     * 地址
     *
     * @date: 2022/5/17 14:44
     * @author wubaizhao1
     */
    @TableField("path")
    private String path;

    /**
     * 租户id
     */
    @TableField("tenant_id")
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
    @TableField(exist = false)
    private String userCode;

    /**
     * 用户名称
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 环境 输出的时候赋值
     * List<EnvModel>
     */
    @TableField(exist = false)
    private List<EnvModel> envList;
    /**
     * 关联记录id,目前用来保存mock记录关联的id。也可以是j-api的接口或者方法id
     */
    private Long relatedId;
    /**
     * 配置信息
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map config;
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
    @TableField(exist = false)
    private Boolean editable;

    @TableField(exist = false)
    String groupId;

    @TableField(exist = false)
    String artifactId;

    @TableField(exist = false)
    String version;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private MethodGroupTreeModel sortGroupTree;
    /**
     * 当前分组版本，默认当前时间戳
     */
    @TableField
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
     * 产品路径
     */
    String cjgProductTrace;
    /**
     * 藏经阁业务域名称
     */
    @TableField(exist = false)
    String cjgBusinessDomainTraceName;

    /**
     * 文档信息
     */
    String docInfo;
    @TableField(typeHandler = JacksonTypeHandler.class)
    InterfaceDocConfig docConfig;

    @TableField(exist = false)
    Date latestReportTime;

    /**
     * 应用名称:appId关联的应用名称
     */
    @TableField(exist = false)
    String appName;
    /**
     * 应用编码:appCode关联的应用编码
     */
    @TableField(exist = false)
    String appCode;
    /**
     * 是否鉴权接口，true:是 false: 否
     */
    @TableField(exist = false)
    Boolean hasLicense = false;

    @TableField(exist = false)
    Boolean needApply = false;
    /**
     * 关注状态：1-已关注 0-未关注
     */
    @TableField(exist = false)
    Integer followStatus;
    /**
     * 健康度：0-100
     */
    private double score;

    /**
     * 所属部门名称
     */
    @TableField("`dept_name`")
    private String deptName;
    /**
     * 接口级别
     */
    @TableField("`level`")
    private Integer level;
    /**
     * 可见范围 0默认全部可见 1 应用成员可见
     */
    @TableField("`visibility`")
    private Integer visibility;
    /**
     * 云文档路径
     */
    private String cloudFilePath;
    /**
     * 云文档标签
     */
    private String cloudFileTags;

    /**
     * 1 存在关联文档（云文档或者上传） 0不存在文档
     */
    private Integer unionFile;

    /**
     * 简单应用信息
     */
    @TableField(exist = false)
    private SimpleAppInfo simpleAppInfo;

    /**
     * 接口文档说明 markDown
     */
    @TableField(exist = false)
    private String interfaceText ;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceCode() {
        return this.serviceCode;
    }

    public void init() {
        if (InterfaceTypeEnum.JSF.getCode().equals(getType()) && StringUtils.isNotBlank(getPath())) {
            final String[] strs = StringUtils.split(path, ":");
            groupId = strs[0];
            artifactId = strs[1];
            version = strs[2];
        }
    }
}
