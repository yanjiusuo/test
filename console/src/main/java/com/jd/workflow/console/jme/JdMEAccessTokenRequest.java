package com.jd.workflow.console.jme;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 京东ME获取 access_token 所需请求参数
 * @author xiaobei
 * @date 2022-12-21 21:31
 */
@Getter
@Setter
@ToString
public class JdMEAccessTokenRequest {

    /**
     * 应用Key (应用基础信息里查看)
     */
    private String app_key;

    /**
     * 应用秘钥 (应用基础信息里查看)
     */
    private String app_secret;
}
