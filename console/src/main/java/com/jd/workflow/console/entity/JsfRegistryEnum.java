package com.jd.workflow.console.entity;

public enum JsfRegistryEnum {
    test("test.i.jsf.jd.local","测试站"),
    online("i.jsf.jd.com","中国站"),
    id("id.jsf.jd.local","印尼站"),
    th("i.jsf.jd.th.local","泰国站");

    String address;
    String desc;

    JsfRegistryEnum(){}

    JsfRegistryEnum(String address,String desc) {
        this.address = address;
        this.desc = desc;
    }

    public String getAddress() {
        return address;
    }

    public String getDesc() {
        return desc;
    }
}
