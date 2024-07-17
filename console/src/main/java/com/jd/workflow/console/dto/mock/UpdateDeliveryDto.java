package com.jd.workflow.console.dto.mock;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateDeliveryDto {
    @NotNull(message = "接口id不可为空")
    Long interfaceId;
  /*  @NotNull(message = "是否透传不可为空")
    Boolean delivery;*/
    String deliverToken;
    String deliverAlias;
}
