package com.jd.workflow.console.base;

import com.jd.workflow.console.base.enums.LoginTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @date 2019/8/3 下午5:07
 */
@Getter
@Setter
public class UserInfoInSession {

    private String userId;

    private String userName;
    /**
     * 对应租户编码
     */
    private String tenantId;
    private Long tenantKey;
    String dept;

    private LoginTypeEnum loginType;

    public UserInfoInSession() {
    }

    public UserInfoInSession(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

}
