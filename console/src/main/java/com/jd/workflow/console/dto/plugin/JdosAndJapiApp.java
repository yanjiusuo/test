package com.jd.workflow.console.dto.plugin;

import lombok.Data;

@Data
public class JdosAndJapiApp {
    public static Integer TYPE_JDOS = 1;
    public static Integer TYPE_JAPI = 2;
    String code;
    String name;
    // 1-jdos 2-japi应用
    Integer type;
    String relatedJdosAppCode;

    String appSecret;
}
