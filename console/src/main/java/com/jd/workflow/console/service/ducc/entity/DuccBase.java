package com.jd.workflow.console.service.ducc.entity;

import java.util.Date;

/**
 * DuccNode
 *
 * @author wangxianghui6
 * @date 2022/3/1 3:51 PM
 */
public class DuccBase {
    private Long id;
    private DuccCode createBy;
    private DuccCode updateBy;
    private Date createTime;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DuccCode getCreateBy() {
        return createBy;
    }

    public void setCreateBy(DuccCode createBy) {
        this.createBy = createBy;
    }

    public DuccCode getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(DuccCode updateBy) {
        this.updateBy = updateBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
