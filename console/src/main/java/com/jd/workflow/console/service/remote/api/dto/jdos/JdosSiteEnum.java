package com.jd.workflow.console.service.remote.api.dto.jdos;


import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum JdosSiteEnum {
    China(false),
    //测试站数据不需要
    Test(true),
    Thailand(false),
    Indonesia(false);

    //是否需要跳过
    @Getter
    private boolean isSkip;

    JdosSiteEnum(boolean isSkip) {
        this.isSkip = isSkip;
    }

    public static JdosSiteEnum transform(String site) {
        if (StringUtils.isBlank(site)) {
            return null;
        }

        if (site.equalsIgnoreCase("china")) {
            return JdosSiteEnum.China;
        }
        if (site.equalsIgnoreCase("test")) {
            return JdosSiteEnum.Test;
        }

        if (site.equalsIgnoreCase("th")) {
            return JdosSiteEnum.Thailand;
        }

        if (site.equalsIgnoreCase("id")) {
            return JdosSiteEnum.Indonesia;
        }

        return null;
    }

    public static JdosSiteEnum converter(String site) {
        if (StringUtils.isBlank(site)) {
            return null;
        }

        for(JdosSiteEnum jdosSiteEnum :JdosSiteEnum.values()) {
            if(jdosSiteEnum.name().equals(site)) {
                return jdosSiteEnum;
            }
        }

        return JdosSiteEnum.China;
    }
}
