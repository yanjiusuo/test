package com.jd.workflow.console.dto.flow.param;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/27 14:54
 * @Description:
 */
@Data
public class GroupMemberDTO {
    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 编码
     */
    private String userCode;
}
