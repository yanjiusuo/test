package com.jd.workflow.console.service.remote.api.dto.jdos;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public enum JagileJdosSiteEnum {


    JDD("中国站","JDD"),
    JDT("科技站", "JDT"),
    JDTTEST("科技测试站", "JDTTEST"),
    JDDTEST("中国测试站", "JDDTEST"),
    TH("泰国站", "TH"),
    YN("印尼站", "YN");

    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String code;

    JagileJdosSiteEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static JagileJdosSiteEnum getJagileSite(JdosSiteEnum jdosSiteEnum) {
        if(jdosSiteEnum == null) {
            return JagileJdosSiteEnum.JDD;
        }

        if(JdosSiteEnum.China.equals(jdosSiteEnum)) {
            return JagileJdosSiteEnum.JDD;
        }
        if(JdosSiteEnum.Thailand.equals(jdosSiteEnum)) {
            return JagileJdosSiteEnum.TH;
        }
        if(JdosSiteEnum.Indonesia.equals(jdosSiteEnum)) {
            return JagileJdosSiteEnum.YN;
        }
        if(JdosSiteEnum.Test.equals(jdosSiteEnum)) {
            return JagileJdosSiteEnum.JDDTEST;
        }

        return JagileJdosSiteEnum.JDD;
    }

    public static JagileJdosSiteEnum getJagileSite(String site) {
        JdosSiteEnum jdosSiteEnum = JdosSiteEnum.converter(site);

        return getJagileSite(jdosSiteEnum);
    }

    public static String getCodeBySite(String site){
        if(StringUtils.isBlank(site)||"cn".equals(site)) {
            return JagileJdosSiteEnum.JDD.getCode();
        }
        if("kj".equals(site)) {
            return JagileJdosSiteEnum.JDT.getCode();
        }
        return JagileJdosSiteEnum.JDD.getCode();
    }

}
