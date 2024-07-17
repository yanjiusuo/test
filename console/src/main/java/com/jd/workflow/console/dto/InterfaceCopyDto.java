package com.jd.workflow.console.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InterfaceCopyDto {
    @NotNull(message = "原接口id不可为空")
    Long interfaceId;
    @NotNull(message = "name不可为空")
    String name;
    @NotNull(message = "负责人不可为空")
    String adminCode;
    @NotNull(message = "描述不可为空")
    String desc;
    @NotNull(message = "serviceCode不可为空")
    String serviceCode;
}
