package com.jd.workflow.console.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginDto {
    @NotNull(message = "用户名不可为空")
    String userName;
    @NotNull(message = "密码不可为空")
    String password;
}
