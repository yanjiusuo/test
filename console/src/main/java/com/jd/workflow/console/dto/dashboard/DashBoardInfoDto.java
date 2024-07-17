package com.jd.workflow.console.dto.dashboard;

import lombok.Data;

/**
 * @Author yuanshuaiming
 * @Date 2023/7/6 4:29 下午
 * @Version 1.0
 */
@Data
public class DashBoardInfoDto {
    public DashBoardInfoDto() {
        this.yesterday = new SubCountDetail();
        this.today = new SubCountDetail();
    }

    public DashBoardInfoDto(Integer cjgAppId, Integer appId, String appCode, String appName, Long interfaceTotalCount) {
        this.cjgAppId = cjgAppId;
        this.appId = appId;
        this.appCode = appCode;
        this.appName = appName;
        this.interfaceTotalCount = interfaceTotalCount;
        this.yesterday = new SubCountDetail();
        this.today = new SubCountDetail();
    }

    /**
     * 应用id
     */
    Integer cjgAppId;
    Integer appId;
    String appCode;
    String appName;
    /**
     * jsf和http接口总数
     */
    Long interfaceTotalCount;
    SubCountDetail yesterday;
    SubCountDetail today;
}
