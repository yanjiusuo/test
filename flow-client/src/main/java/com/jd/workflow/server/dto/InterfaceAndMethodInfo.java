package com.jd.workflow.server.dto;

public class InterfaceAndMethodInfo {
 /**
  * id
  */
 private Long id;
 /**
  * 应用id
  */
 private Long appId;
 /**
  * 接口id
  */
 private Long interfaceId;
 /**
  * 接口名称
  */
 String interfaceName;
 /**
  * 英文名称
  */
 String enName;
 /**
  * 中文名称
  */
 String cnName;
 /**
  * url地址
  */
 String url;
 /**
  * http方法
  */
 String httpMethod;
 /**
  * 接口路径
  */
 String path;

 public Long getId() {
  return id;
 }

 public void setId(Long id) {
  this.id = id;
 }

 public String getInterfaceName() {
  return interfaceName;
 }

 public void setInterfaceName(String interfaceName) {
  this.interfaceName = interfaceName;
 }

 public String getEnName() {
  return enName;
 }

 public void setEnName(String enName) {
  this.enName = enName;
 }

 public String getCnName() {
  return cnName;
 }

 public void setCnName(String cnName) {
  this.cnName = cnName;
 }

 public String getUrl() {
  return url;
 }

 public void setUrl(String url) {
  this.url = url;
 }

 public String getHttpMethod() {
  return httpMethod;
 }

 public void setHttpMethod(String httpMethod) {
  this.httpMethod = httpMethod;
 }

 public String getPath() {
  return path;
 }

 public void setPath(String path) {
  this.path = path;
 }

 public Long getAppId() {
  return appId;
 }

 public void setAppId(Long appId) {
  this.appId = appId;
 }

 public Long getInterfaceId() {
  return interfaceId;
 }

 public void setInterfaceId(Long interfaceId) {
  this.interfaceId = interfaceId;
 }
}
