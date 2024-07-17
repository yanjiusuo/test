package com.jd.workflow.console.jme;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 京东ME的accessToken对象
 * 参见：http://regist-open.timline.jd.com/?code=3y7PAiuVrnRehTyPgAgpyB6-rs7QsA8jWkR9blj4y9Q&state=Cu9h9SDl8-y9c_zr22uSwjuhG5lKJ05x7Rg-s3C9VJ0#/api-list
 * @author xiaobei
 * @date 2022-12-21 20:16
 */
@Getter
@Setter
@ToString
public class JdMeAccessToken {

    /**
     * 访问凭证
     */
    private String access_token;

    /**
     * 有效时间，单位：秒
     */
    private Integer effective_time;

    private Owner owner;
}

@Getter
@Setter
@ToString
class Owner {

    private String app;

    private String pin;
}
