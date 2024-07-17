package com.jd.workflow.server.dto.interfaceManage;


/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY,property = "type",visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpMethodDocConfig.class, name = "http"),
        @JsonSubTypes.Type(value = JsfMethodDocConfig.class, name = "jsf")
})*/
public  class JsfMethodDocConfig {
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


    public static String getTypeJsf() {
        return TYPE_JSF;
    }

    public static void setTypeJsf(String typeJsf) {
        TYPE_JSF = typeJsf;
    }

    public static String getTypeHttp() {
        return TYPE_HTTP;
    }

    public static void setTypeHttp(String typeHttp) {
        TYPE_HTTP = typeHttp;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
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

    public String getInputTypeScript() {
        return inputTypeScript;
    }

    public void setInputTypeScript(String inputTypeScript) {
        this.inputTypeScript = inputTypeScript;
    }

    public String getOutputTypeScript() {
        return outputTypeScript;
    }

    public void setOutputTypeScript(String outputTypeScript) {
        this.outputTypeScript = outputTypeScript;
    }
}
