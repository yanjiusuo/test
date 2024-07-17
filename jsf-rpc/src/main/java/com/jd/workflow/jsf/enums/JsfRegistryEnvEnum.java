package com.jd.workflow.jsf.enums;

public enum JsfRegistryEnvEnum {
    //test("test.i.jsf.jd.local","测试站"),
    // jsf的注册中心要唯一，测试环境可以通过配置host实现
    local("i.jsf.jd.com","本地"),
    test("i.jsf.jd.com","测试站"),
    pre("i.jsf.jd.com","预发"),
    online("i.jsf.jd.com","线上");

    String address;
    String desc;

    JsfRegistryEnvEnum(String address,String desc) {
        this.address = address;
        this.desc = desc;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    JsfRegistryEnvEnum(){}
    public static JsfRegistryEnvEnum from(String name){
        for (JsfRegistryEnvEnum value : values()) {
            if(name.equals(value.name())){
                return value;
            }
        }
        return null;
    }
}
