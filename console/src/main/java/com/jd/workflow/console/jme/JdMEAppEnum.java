package com.jd.workflow.console.jme;

/**
 * 国内：ee, 泰国:th.ee, 印尼:id.ee, 赛夫:sf.ee
 * @author xiaobei
 * @date 2022-12-21 22:07
 */
public enum JdMEAppEnum {
    CN("ee", "国内"),
    TH("th.ee", "泰国"),
    ID("id.ee", "印尼"),
    SF("sf.ee", "赛夫"),
    ;

    JdMEAppEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
