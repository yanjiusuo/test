package com.jd.workflow.console.dto.user;

/**
 * Created by chenguoyou on 2017/1/13.
 */
public class ResponseVO {

    private String appCode;
    private String resStatus;
    private String resMsg;
    private int resCount;

    private ResponseBodyVO responsebody;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getResStatus() {
        return resStatus;
    }

    public void setResStatus(String resStatus) {
        this.resStatus = resStatus;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public int getResCount() {
        return resCount;
    }

    public void setResCount(int resCount) {
        this.resCount = resCount;
    }

    public ResponseBodyVO getResponsebody() {
        return responsebody;
    }

    public void setResponsebody(ResponseBodyVO responsebody) {
        this.responsebody = responsebody;
    }
}
