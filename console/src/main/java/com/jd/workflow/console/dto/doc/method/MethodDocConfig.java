package com.jd.workflow.console.dto.doc.method;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import lombok.Data;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY,property = "type",visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpMethodDocConfig.class, name = "http"),
        @JsonSubTypes.Type(value = JsfMethodDocConfig.class, name = "jsf")
})*/
@Data
public  class MethodDocConfig {
    public static String TYPE_JSF = "jsf";
    public static String TYPE_HTTP = "http";
    //public abstract String getType();
    /**
     * 文档类型：md、html
     */
    String docType;
    /**
     * 入参示例
     */
    String inputExample;
    /**
     * 出参示例
     */
    String outputExample;
    private String inputTypeScript;
    private String outputTypeScript;
    /**
     * 沒有setType方法反序列化会报错
     * @param type
     */
    public  void setType(String type){}
    public  String getType(){
        return null;
    }
}
