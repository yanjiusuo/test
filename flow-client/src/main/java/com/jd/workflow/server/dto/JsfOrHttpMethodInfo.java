package com.jd.workflow.server.dto;

import java.util.Map;

/**
 * jsf或者http方法信息
 */
public class JsfOrHttpMethodInfo {
   /**
    *  主键
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
     * 方法名称
     */
    private String name;



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
     * 方法内容 json信息 [大字段].
     * 普通字段描述为:{“type”:"object|array|float|number|boolean|string","desc":"描述","name":"字段名","required":true|false，mock:""}
     * 字段描述为JsonType <br/>
     *
     * http格式为：{input:{path:List<JsonType>,params:List<JsonType>,headers:List<JsonType>,body:List<JsonType>,reqType:""},output:{headers:List<JsonType>,body:List<JsonType>
     * jsf方法格式为：{input:List<JsonType>,output:JsonType}
     */
    private String content;

    /**
     * 方法路径
     */
    private String path;
    /**
     * 方法描述：md、html
     */
    String docType;
    /**
     * 文档信息
      */
    private String docInfo;
    /**
     * 入参示例
     */
    String inputExample;
    /**
     * 出差示例
     */
    String outputExample;

    /**
     * 接口状态
     */
    private Integer status;

 /**
  * 逻辑删除标示 0、删除 1、有效
  */
 //@TableLogic(value = "1", delval = "0")
  private Integer yn;


 public String getId() {
  return id;
 }

 public void setId(String id) {
  this.id = id;
 }

 public Integer getType() {
  return type;
 }

 public void setType(Integer type) {
  this.type = type;
 }

 public String getMethodCode() {
  return methodCode;
 }

 public void setMethodCode(String methodCode) {
  this.methodCode = methodCode;
 }

 public String getName() {
  return name;
 }

 public void setName(String name) {
  this.name = name;
 }

 public String getHttpMethod() {
  return httpMethod;
 }

 public void setHttpMethod(String httpMethod) {
  this.httpMethod = httpMethod;
 }

 public Long getInterfaceId() {
  return interfaceId;
 }

 public void setInterfaceId(Long interfaceId) {
  this.interfaceId = interfaceId;
 }

 public String getContent() {
  return content;
 }

 public void setContent(String content) {
  this.content = content;
 }

 public String getPath() {
  return path;
 }

 public void setPath(String path) {
  this.path = path;
 }

 public String getDocType() {
  return docType;
 }

 public void setDocType(String docType) {
  this.docType = docType;
 }

 public String getDocInfo() {
  return docInfo;
 }

 public void setDocInfo(String docInfo) {
  this.docInfo = docInfo;
 }

 public String getInputExample() {
  return inputExample;
 }

 public void setInputExample(String inputExample) {
  this.inputExample = inputExample;
 }

 public String getOutputExample() {
  return outputExample;
 }

 public void setOutputExample(String outputExample) {
  this.outputExample = outputExample;
 }

 public Integer getStatus() {
  return status;
 }

 public void setStatus(Integer status) {
  this.status = status;
 }

 public Integer getYn() {
  return yn;
 }

 public void setYn(Integer yn) {
  this.yn = yn;
 }
}
