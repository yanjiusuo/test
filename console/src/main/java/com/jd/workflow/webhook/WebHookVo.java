package com.jd.workflow.webhook;

import lombok.Data;

/**
 * {
 * "after": "d79f6d1f7e47e4f69eb1c8cd57251ccba000154e",
 * "checkout_sha": "d79f6d1f7e47e4f69eb1c8cd57251ccba000154e",
 * "object_kind": "push",
 * "project": {
 * "avatar_url": "",
 * "default_branch": "master",
 * "description": "在线联调demo应用",
 * "git_http_url": "https://coding.jd.com/transform/japi-demo.git",
 * "git_ssh_url": "git@coding.jd.com:transform/japi-demo.git",
 * "homepage": "https://coding.jd.com/transform/japi-demo",
 * "http_url": "https://coding.jd.com/transform/japi-demo.git",
 * "id": 738813,
 * "name": "japi-demo",
 * "namespace": "transform",
 * "path_with_namespace": "transform/japi-demo",
 * "ssh_url": "git@coding.jd.com:transform/japi-demo.git",
 * "url": "https://coding.jd.com/transform/japi-demo",
 * "visibility_level": 0,
 * "web_url": "https://coding.jd.com/transform/japi-demo"
 * },
 * "project_id": 738813,
 * "ref": "master",
 * "repository": {
 * "description": "在线联调demo应用",
 * "git_http_url": "https://coding.jd.com/transform/japi-demo.git",
 * "git_ssh_url": "git@coding.jd.com:transform/japi-demo.git",
 * "homepage": "https://coding.jd.com/transform/japi-demo",
 * "name": "japi-demo",
 * "url": "git@coding.jd.com:transform/japi-demo.git",
 * "visibility_level": 0
 * },
 * "user_email": "zhangqian61@jd.com",
 * "user_id": 17586,
 * "user_name": "张骞",
 * "user_username": "zhangqian346"
 * }
 */
@Data
public class WebHookVo {

    private String checkout_sha;
    private String object_kind;
    private Project project;
    private long project_id;
    private String ref;
    private String user_email;
    private int user_id;
    private String user_name;
    private String user_username;
    private String profile;
    private String compilePath;
    private String appCode;
    /**
     *  0=测试 1=部署记录上报 2=手动上报
     */

    private Integer type;
    /**
     * 流程id
     */
    private String flowId;

    private String dept;

    private String language;

}
