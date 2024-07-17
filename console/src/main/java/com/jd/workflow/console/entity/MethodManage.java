package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.WebServiceMethod;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "method_manage",autoResultMap = true)
public class MethodManage extends BaseEntity implements Serializable,IMethodInfo {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 接口类型 1-http、2-webservice、3-jsf 10-编排
	 * link{@com.jd.workflow.console.base.enums.InterfaceTypeEnum}
	 */
	@TableField("`type`")
	private Integer type;

	/**
	 * 方法名称
	 */
	@TableField("`name`")
	private String name;
	/**
	 * 方法编码- 英文名称
	 */
	private String methodCode;

	/**
	 * 方法描述
	 */
	@TableField("`desc`")
	private String desc;

	/**
	 * 是否有操作权限（不存入表中）  1-有  2-无
	 */
	@TableField(exist = false)
	private Integer hasAuth;

	/**
	 * 请求方式 GET POST PUT等
	 */
	@TableField("http_method")
	private String httpMethod;

	/**
	 * 所属的接口id
	 */
	@TableField("interface_id")
	private Long interfaceId;

	/**
	 * 方法内容 json信息 [大字段]
	 * HTTP的 {@link HttpMethodModel}
	 * WS的 {@link WebServiceMethod}
	 * JSF的：{@link com.jd.workflow.jsf.metadata.JsfStepMetadata}
	 */
	@TableField("content")
	private String content;
	/**
	 * @hidden
	 */
	@JsonIgnore
	@TableField(exist = false)
	private Object contentObject;

	/**
	 * 方法路径
	 */
	private String path;

	/**
	 * 父方法id
	 */
	private Long parentId;

	/**
	 * 生成的wsdl地址
	 */
	//private String wsdlUrl;

	/**
	 * 是否发布 0-未发布 1-已发布
	 */
	private Integer published;

	/**
	 * 调用地址 发布后才能调用
	 */
	//private String endpointUrl;

	/**
	 * 调用环境 http转webservice才有调用环境
	 */
	private String callEnv;

	/**
	 * 参数个数 webservice方法存放
	 */
	private Integer paramCount;

	/**
	 * 接口状态
	 */
	private Integer status;

	/**
	 * 额外配置
	 */
	private String extConfig;
	/**
	 * 关联id,目前用来存导入记录的id
	 */
	private Long relatedId;
	// http mock路径
	@TableField(exist = false)
	private String httpMockPath;
	// easy的mock别名
	@TableField(exist = false)
	private String jsfMockAlias;
	/**
	 * 鉴权标识,目前只有一个
	 */
	@TableField(exist = false)
	private List<String> authKey;

	@TableField(exist = false)
	private String groupName;

	@TableField(exist = false)
	private Long appId;

	@TableField(exist = false)
	private Set<String> tags;

	/**
	 * 方法标签 1=colorApi  不用了这个值
	 */
	private Integer methodTag;

	private String functionId;
	/**
	 * 数字签名，对应content部分的签名。该字段主要用来判断方法上报的时候是否有更新
	 * 需要注意的是，自动上报的接口返回给前端的是content合并MethodModifyDeltaInfo后的结果，该字段仍然是未合并前content的签名。
	 */
	private String digest;
	/**
	 * 合并后的content的数字签名(自动上报的方法里会引用模型以及手动维护信息，需要有个字段来存储)，版本快照比对的时候使用
	 */
	private String mergedContentDigest;
	/**
	 * 接口上报同步状态:1-不同步(默认) 0-同步
	 */
	private Integer reportSyncStatus;

	/**
	 * 文档描述
	 */
	String docInfo;
	/**
	 * 健康度：0-100
	 */
	double score;

	String zoneInfo;
	/**
	 * color接口状态
	 */
	String colorApiStatus;

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
	 * 文档描述
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	MethodDocConfig docConfig;

	public MethodManage clone(){
		MethodManage target = new MethodManage();
		BeanUtils.copyProperties(this,target);
		return target;
	}
	/**
	 * @hidden
	 * @return
	 */
	@JsonIgnore
	@Override
	public Long getKeyId() {
		return id;
	}

	@TableField(exist = false)
	private String key;
	public void initKey(){
		key = TreeSortModel.getKey(TreeSortModel.TYPE_METHOD,interfaceId,type,id);
	}


	public String getName(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getMethodCode(){
		return this.methodCode;
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
