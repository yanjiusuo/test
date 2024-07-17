package com.jd.workflow.console.service.ducc.enums;

/**
 * ProfileEnum
 *
 * @author wangxianghui6
 * @date 2022/3/1 5:15 PM
 */
public enum ProfileEnum {
    /**
     *
     */
    DEV("dev", "开发"),
    /**
     *
     */
    TEST("test", "测试"),
    /**
     *
     */
    PRE("pre", "预发"),
    /**
     *
     */
    PRD("prd", "线上");

    private final String name;
    private final String desc;

    ProfileEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }
    public static ProfileEnum getValueByName(String name) {
        for (ProfileEnum anEnum : ProfileEnum.values()) {
            if (anEnum.getName().equals(name)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getDesc() {
        return desc;
    }
    public String getName() {
        return name;
    }

}
