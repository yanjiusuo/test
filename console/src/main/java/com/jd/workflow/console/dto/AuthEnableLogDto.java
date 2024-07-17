package com.jd.workflow.console.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthEnableLogDto {
    /**
     * 主键
     */
   // @NotBlank(message = "id不能为空")
    private Long id;
    /**
     * 应用编码
     */
    //@NotBlank(message = "应用编码不能为空")
    private String appCode;
    /**
     * 是否启用日志
     */
    //@NotBlank(message = "是否启用日志不能为空")
    private Boolean enableAuditLog ;
}
