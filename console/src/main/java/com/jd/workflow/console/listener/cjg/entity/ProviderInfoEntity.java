package com.jd.workflow.console.listener.cjg.entity;

import lombok.ToString;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.sql.Timestamp;


@ToString
public class ProviderInfoEntity implements Serializable {
    private int id;
    private String erp;
    private Integer componentId;

    /**
     * 角色类型,1负责人、2开发人员、3产品经理、4架构师、5 测试人员、6、在kg_personnel_relation中被使用_此处暂不使用、7测试相关人员
     */
    private Integer roleType;

    private String createBy;
    private Timestamp createAt;
    private String modifyBy;
    private Timestamp modifyAt;
    private Byte yn;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getErp() {
        return erp;
    }

    public void setErp(String erp) {
        this.erp = erp;
    }


    public Integer getComponentId() {
        return componentId;
    }

    public void setComponentId(Integer componentId) {
        this.componentId = componentId;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }


    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }


    public String getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }


    public Timestamp getModifyAt() {
        return modifyAt;
    }

    public void setModifyAt(Timestamp modifyAt) {
        this.modifyAt = modifyAt;
    }


    public Byte getYn() {
        return yn;
    }

    public void setYn(Byte yn) {
        this.yn = yn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProviderInfoEntity that = (ProviderInfoEntity) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(erp, that.erp)
                .append(componentId,that.componentId)
                .append(createBy, that.createBy)
                .append(createAt, that.createAt)
                .append(modifyBy, that.modifyBy)
                .append(modifyAt, that.modifyAt)
                .append(yn, that.yn)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(erp)
                .append(componentId)
                .append(createBy)
                .append(createAt)
                .append(modifyBy)
                .append(modifyAt)
                .append(yn)
                .toHashCode();
    }
}
