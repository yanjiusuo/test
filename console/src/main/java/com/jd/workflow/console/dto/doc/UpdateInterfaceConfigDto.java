package com.jd.workflow.console.dto.doc;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateInterfaceConfigDto {
    @NotNull(message = "接口id不可为空")
    Long interfaceId;
    @NotNull(message = "字段名不可为空")
    String field;
    String fieldValue;
}
