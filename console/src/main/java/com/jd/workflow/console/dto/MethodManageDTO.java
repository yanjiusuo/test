package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.workflow.console.base.PageParam;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.soap.common.exception.BizException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author wubaizhao1
 * @date: 2022/5/16 18:38
 */
@Data
public class MethodManageDTO extends PageParam implements IMethodInfo {
    /**
     * 主键
     */
    private String id;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     */
    private Integer type;
    /**
     * 方法编码
     */
    private String methodCode;

    /**
     * 方法编码
     */
    private String interfaceName;


    /**
     * 方法名称
     */
    private String name;

    /**
     * 方法描述
     * ws 自动生成的时候，为服务名
     */
    private String desc;

    /**
     * 是否有操作权限（不存入表中）  1-有  2-无
     */
    private Integer hasAuth;
    /**
     * 是否demo 1-是 0-否
     */
    private Integer isPublic;
    /**
     * @hidden
     */
    @JsonIgnore
    private String digest;
    /**
     * 请求方式 get post put 等
     */
    private String httpMethod;

    /**
     * 所属的接口id
     */
    private Long interfaceId;

    /**
     * 输出时为空，入参用
     * 方法内容 json信息 [大字段]
     * HTTP的 {@link HttpMethodModel}
     * WS的 {@link WebServiceMethod}
     */
    private String content;
    /**
     * 输出时不为空，出参用
     * HTTP的 {@link HttpMethodModel}
     * WS的 {@link WebServiceMethod}
     */
    private Object contentObject;
    /**
     * 修改的差量信息
     */
    private Map<String, Object> _delta;
    /**
     * 方法路径
     */
    private String path;

    /**
     * 父方法id
     */
    private Long parentId;

    /**
     * 是否发布 0-未发布 1-已发布
     */
    private Integer published;

    /**
     * 调用地址 发布后才能调用
     */
//	private String endpointUrl;

    /**
     * 调用环境 http转webservice才有调用环境
     */
    private String callEnv;

    /**
     * 参数个数 webservice方法存放
     */
    private Integer paramCount;

    /**
     * 额外配置
     */
    private String extConfig;
    /**
     * 文档配置，包括出入参示例
     */
    private MethodDocConfig docConfig;

    private String docInfo;
    /**
     * 分组id
     */
    private Long groupId;
    /**
     * 上报状态
     */
    Integer reportStatus;
    /**
     * 是否自动上报：1-是 0-否
     */
    Integer autoReport;
    /**
     * 接口状态
     */
    private Integer status;
    // http mock路径

    private String httpMockPath;
    // easy的mock别名

    private String jsfMockAlias;
    /**
     * 所属应用编码
     */
    private String appCode;
    private Long appId;
    /**
     * 所属应用名称
     */
    private String appName;
    private String key;

    private Set<String> tags;
    /**
     * 健康度，0-100分之间
     */
    private double score;
    /**
     * 版本号
     *
     * @hidden
     */
    private String version;
    /**
     * color接口必传字段
     */
    String functionId;

    /**
     * color环境信息 {"pro":"api.jd.com","pre":"pre-api.jd.com"}
     */
    Map<String, String> zoneInfo;
    /**
     * 应用管理员
     */
    String erp;
    /**
     * 接口是否有鉴权
     */
    Boolean hasLicense = false;
    /**
     * 关注状态：1-已关注 0-未关注
     */
    Integer followStatus;


    /**
     * 应用负责人
     */
    private String appOwner;

    private String cloudFilePath;
    /**
     * 云文档标签
     */
    private String cloudFileTags;

    /**
     * 接口说明markDown
     */
    private String interfaceText;
    /**
     * 调用示例markDown
     */
    private String explanationText;
    /**
     * 业务逻辑markDown
     */
    private String bizLogicText;


    /**
     * @return
     * @hidden
     */
    @JsonIgnore
    @Override
    public Long getKeyId() {
        return Long.valueOf(id);
    }

    @Override
    public void setDelta(Map<String, Object> delta) {
        this._delta = delta;
    }

    public static MethodManageDTO from(MethodManage methodManage) {
        MethodManageDTO method = new MethodManageDTO();
        try {
            BeanUtils.copyProperties(method, methodManage);
        } catch (Exception e) {
            throw new BizException("method.err_copy_prop", e);
        }
        method.setType(methodManage.getType());
        return method;
    }

    /**
     *
     */
    @TableField("creator")
    private String creator;
    /**
     *
     */
    @TableField("modifier")
    private String modifier;
    /**
     *
     */
    @ApiModelProperty(value = "创建时间")
    @TableField("created")
    private Date created;
    /**
     *
     */
    @ApiModelProperty(value = "修改时间")
    @TableField("modified")
    private Date modified;

}
