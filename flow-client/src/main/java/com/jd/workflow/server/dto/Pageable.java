package com.jd.workflow.server.dto;

import java.util.List;

/**
 * 分页
 * @param <T>
 */
public class Pageable<T>{
    /**
     * 总数
     */
    int total;
    /**
     * 分页数据
     */
    List<T> data;
    int current;
    int size;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
