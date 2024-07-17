package com.jd.workflow.server.dto;

import java.io.Serializable;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/17
 */
public class BizLogicInfoDto implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 组件编号
     */
    private Long componentId;

    /**
     * 1-接口文档 2-调用示例 3-业务逻辑
     */
    private Integer type;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}