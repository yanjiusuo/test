package com.jd.workflow.console.service.plugin.log;

import lombok.Data;

@Data
public class SingleDeployInfo {
    /**
     * 请求id
     */
    String reqId;
    /**
     * 开始时间
     */
    Long start;

    /**
     * 文件下载时间
     */
    Integer fileDownloadTime;
    /**
     * 结束时间
     */
    Long end;
    /**
     * 是否成功
     */
    boolean success = true;


    DeployLogInfo deployLogInfo;
}
