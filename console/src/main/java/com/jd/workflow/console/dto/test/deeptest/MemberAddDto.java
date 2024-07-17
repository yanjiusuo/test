package com.jd.workflow.console.dto.test.deeptest;

import lombok.Data;

@Data
public class MemberAddDto {
    Long id;
    Long moduleId;
    // 部门维度：2 用户维度=1
    int scope;
    String memberErp;
    String departName;
    /**
     * 1-管理员 2-成员
     */
    int roleId;
}
