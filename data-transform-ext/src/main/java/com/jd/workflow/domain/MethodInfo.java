package com.jd.workflow.domain;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6 
 */
@Data
public class MethodInfo {
    private Long id;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     * link{@com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */

    private Integer type;

    /**
     * 方法名称
     */

    private String name;
    /**
     * 方法编码- 英文名称
     */
    private String methodCode;

    /**
     * 方法描述
     */

    private String desc;

    /**
     * 是否有操作权限（不存入表中）  1-有  2-无
     */

    private Integer hasAuth;

    /**
     * 请求方式 GET POST PUT等
     */

    private String httpMethod;

    /**
     * 所属的接口id
     */

    private Long interfaceId;

    /**
     * 方法内容 json信息 [大字段]

     */

    private String content;
    /**
     * @hidden
     */

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

    private String httpMockPath;
    // easy的mock别名

    private String jsfMockAlias;
    /**
     * 鉴权标识,目前只有一个
     */

    private List<String> authKey;

    private String groupName;

    private Long appId;

    private String digest;
    /**
     * 接口上报同步状态:1-不同步(默认) 0-同步
     */
    private Integer reportSyncStatus;
    /**
     * 文档描述
     */
    String docInfo;
}
