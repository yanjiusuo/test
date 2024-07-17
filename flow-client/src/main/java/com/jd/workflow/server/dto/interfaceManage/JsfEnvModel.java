package com.jd.workflow.server.dto.interfaceManage;


import java.util.List;

/**
 * 对应env变量的一个对象
 * {@link com.jd.workflow.console.entity.InterfaceManage.env}
 * @date: 2022/5/30 14:44
 * @author wubaizhao1
 */

public class JsfEnvModel {
    public JsfEnvModel(){}
    /**
     * 环境名称
     */
    String envName;
    /**
     * 基础url列表
     * @date: 2022/5/30 14:51
     * @author wubaizhao1
     */
    List<String> url;
    /**
     * 环境的类型
     */
    EnvTypeEnum type;

    /**
     *
     */
    String hostIp;

    List<JsfSimpleJsonType> headers;

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public List<String> getUrl() {
        return url;
    }

    public void setUrl(List<String> url) {
        this.url = url;
    }

    public EnvTypeEnum getType() {
        return type;
    }

    public void setType(EnvTypeEnum type) {
        this.type = type;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public List<JsfSimpleJsonType> getHeaders() {
        return headers;
    }

    public void setHeaders(List<JsfSimpleJsonType> headers) {
        this.headers = headers;
    }
}
