package com.jd.workflow.jsf.enums;


public enum JsfRegistrySite {
    zh("中国站","i.jsf.jd.com"),
    id("印尼站","id.jsf.jd.local"),
    th("泰国站","th.jsf.jd.local");



    String desc;
    String address;
    JsfRegistrySite(String desc,String address) {

        this.desc = desc;
    }



    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    JsfRegistrySite(){}
    public static JsfRegistrySite from(String name){
        for (JsfRegistrySite value : values()) {
            if(name.equals(value.name())){
                return value;
            }
        }
        return null;
    }
}
