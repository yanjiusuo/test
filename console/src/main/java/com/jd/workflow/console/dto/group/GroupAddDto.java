package com.jd.workflow.console.dto.group;

import lombok.Data;

@Data
public class GroupAddDto {
    /**
     * 接口id
     */
    Long interfaceId;
    /**
     * 父节点id
     */
    private Long parentId;

    /**
     * 分组名称
     */
    String name;
    /**
     * 英文名
     */
    String enName;
}
