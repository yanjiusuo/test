package com.jd.workflow.console.service.remote.api.dto.jagile;

import java.io.Serializable;
import java.util.List;

/**
 * @author yangzongbin
 * @data 2022/8/31
 * @desc
 */
public class Paging<T> implements Serializable {


    private static final long serialVersionUID = -7864295884467967634L;

    private Integer pageNum;
    private Integer pageSize;
    private Integer records;
    private List<T> list;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getRecords() {
        return records;
    }

    public void setRecords(Integer records) {
        this.records = records;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
