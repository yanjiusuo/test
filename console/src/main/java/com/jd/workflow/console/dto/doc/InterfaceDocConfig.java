package com.jd.workflow.console.dto.doc;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY,property = "type",visible = true)
@JsonSubTypes({

        @JsonSubTypes.Type(value = JsfDocConfig.class, name = "jsf")})*/
@Data
public  class InterfaceDocConfig {
   /* public static String TYPE_JSF = "jsf";
    public static String TYPE_HTTP = "http";
    public abstract String getType();*/
    String pomConfig;
    String invokeConfig;
    String docType;
    /**
     * 变更通知状态：1-开启 非1为关闭
     */
    Integer noticeStatus;
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static void main(String[] args) {
        String configStr = "{\"pomConfig\":\"<dependency><groupId>com.jd.up.standardserve</groupId><artifactId>up-standardserve-service</artifactId><version>1.0.0-SNAPSHOT</version></dependency>\",\"invokeConfig\":null,\"type\":\"jsf\"}";
        final InterfaceDocConfig docConfig = JsonUtils.parse(configStr, InterfaceDocConfig.class);
    }
}
