package com.jd.workflow.console.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UpdateBusinessDomainDto {
    /**
     * 接口id列表
     */
    @NotEmpty(message = "接口id不能为空")
    List<Long> interfaceIds;
    /**
     * 藏经阁业务域标识
     */
    @NotNull(message = "藏经阁业务域标识不可为空")
    String cjgBusinessDomainTrace;
}
