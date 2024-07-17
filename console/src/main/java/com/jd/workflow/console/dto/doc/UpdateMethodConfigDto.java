package com.jd.workflow.console.dto.doc;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateMethodConfigDto {
    @NotNull(message = "方法id不可为空")
    Long methodId;
    @NotNull(message = "字段名不可为空")
    String field;
    String fieldValue;
}
