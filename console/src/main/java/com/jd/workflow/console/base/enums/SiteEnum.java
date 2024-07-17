package com.jd.workflow.console.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 *  站点(数据中心)枚举类
 * @author chenzhenhua12
 * @since 2018/12/07
 */
public enum SiteEnum {
    China("中国", "China"),
    Thailand("泰国", "Thailand"),
    Indonesia("印尼", "Indonesia"),
    Sam("山姆", "Sam"),
    Haidian("海店", "Haidian"),
    Nongpi("农批", "Nongpi"),
    Cbb2b("跨境B2B","Cbb2b");

    // 成员变量
    private String name;
    private String code;
    // 构造方法
    private SiteEnum(String name, String  code) {
        this.name = name;
        this. code =  code;
    }
    // 普通方法 
    public static String getName(String code) {
        if(StringUtils.isBlank(code)){
            return null;
        }

        for (SiteEnum c : SiteEnum.values()) {
            if (code.equals(c.getCode())) {
                return c.name;
            }
        }
        return null;
    }

    /**
     * 转化枚举
     */
    public static SiteEnum getEnumByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }

        for (SiteEnum c : SiteEnum.values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }

        return null;
    }

    // get set 方法 
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
