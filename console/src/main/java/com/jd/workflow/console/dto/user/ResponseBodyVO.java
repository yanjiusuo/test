package com.jd.workflow.console.dto.user;

import com.jd.official.omdm.is.hr.vo.UserVo;

import java.util.ArrayList;

/**
 * Created by chenguoyou on 2017/1/13.
 */
public class ResponseBodyVO {
    private int totalCount;
    private int pageNo;
    private int pageSize;
    ArrayList<UserVo> userVoList;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public ArrayList<UserVo> getUserVoList() {
        return userVoList;
    }

    public void setUserVoList(ArrayList<UserVo> userVoList) {
        this.userVoList = userVoList;
    }
}
