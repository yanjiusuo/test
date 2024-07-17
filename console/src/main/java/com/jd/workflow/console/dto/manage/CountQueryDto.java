package com.jd.workflow.console.dto.manage;

import lombok.Data;

@Data
public class CountQueryDto {
    public CountQueryDto(Long appId,String currentUser) {
        this.appId = appId;
        this.currentUser = currentUser;
    }
    public CountQueryDto(){}

    Long appId;
    String currentUser;
}
