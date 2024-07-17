package com.jd.workflow.console.dto.debug;

import lombok.Data;

@Data
public class MavenLoadStatus {
    boolean isLoaded;
    boolean canReload;
    /**
     * 0:未加载 1:加载中 2:加载成功 3:加载失败
     */
    Integer loadStatus;
    /**
     * 加载失败原因
     */
    String failReason;
    /**
     * 操作人
     */
    String operator;
    /**
     * 加载耗时，单位：秒
     */
    Integer costTime;

    public MavenLoadStatus(boolean isLoaded, boolean canReload) {
        this.isLoaded = isLoaded;
        this.canReload = canReload;
    }
    public MavenLoadStatus() {
    }
}
