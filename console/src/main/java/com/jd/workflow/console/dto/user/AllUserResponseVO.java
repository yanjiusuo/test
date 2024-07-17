package com.jd.workflow.console.dto.user;

import com.jd.official.omdm.is.hr.vo.UserVo;

/**
 * Created by chenguoyou on 2017/1/13.
 */
public class AllUserResponseVO {

    private String appCode;
    private String resStatus;
    private String resMsg;
    private int resCount;

    private UserVo responsebody;

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

    public UserVo getResponsebody() {
        return responsebody;
    }

    public void setResponsebody(UserVo responsebody) {
        this.responsebody = responsebody;
    }
}
