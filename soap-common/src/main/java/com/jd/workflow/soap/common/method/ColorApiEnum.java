package com.jd.workflow.soap.common.method;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum ColorApiEnum {

    API_ADD("api_add", "API发布"),
    API_DELETE("api_delete", "API删除"),
    API_OFFLINE("api_offline", "API下线"),
    API_ONLINE("api_online", "API上线");

    private String type;



    private String desc;
}
