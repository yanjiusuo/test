package com.jd.workflow.console.dto.manage;

import lombok.Data;

@Data
public class MethodRelatedDto {
    public MethodRelatedDto(){}
    public MethodRelatedDto(Long interfaceId, Integer type) {
        this.interfaceId = interfaceId;
        this.type = type;
    }

    /**
     * 方法所属分组
     */
    Long interfaceId;
    /**
     * 接口类型：{@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    Integer type;

    private String key;

    /**
     * 接口类型 {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    private Integer interfaceType;


}
