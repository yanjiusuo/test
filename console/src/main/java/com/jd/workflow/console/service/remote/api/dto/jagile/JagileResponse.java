package com.jd.workflow.console.service.remote.api.dto.jagile;


import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;

import java.io.Serializable;

/**
 * @author yangzongbin
 * @data 2022/8/31
 * @desc jagile返回
 */

public class JagileResponse implements Serializable {

    private static final long serialVersionUID = -6586366009562811502L;

    private String code;
    private String message;
    private String reqId;
    private Boolean success;
    private Paging<JDosAppInfo> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Paging<JDosAppInfo> getData() {
        return data;
    }

    public void setData(Paging<JDosAppInfo> data) {
        this.data = data;
    }
}
