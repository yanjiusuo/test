package com.jd.workflow.console.dto.test.deeptest;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

public class MemberUserInfo implements Serializable {
    private static final long serialVersionUID = 8750202175477872036L;

    private Long id;
    private String erp;
    private String name;
    private String email;
    private String mobile;
    private String headImg;
    private String organizationCode;
    private String organizationName;
    private String organizationFullPath;
    private String organizationFullName;

    @TableField(exist = false)
    private String role;

    /**
     * 0在职1离职
     */
    private int dimission ;//默认0

    /**
     * 职位
     */
    private String positionName;

    /**
     * 创建时间
     */
    private Date createTime;

    public int getDimission() {
        return dimission;
    }

    public void setDimission(int dimission) {
        this.dimission = dimission;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getErp() {
        return erp;
    }

    public void setErp(String erp) {
        this.erp = erp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationFullPath() {
        return organizationFullPath;
    }

    public void setOrganizationFullPath(String organizationFullPath) {
        this.organizationFullPath = organizationFullPath;
    }

    public String getOrganizationFullName() {
        return organizationFullName;
    }

    public void setOrganizationFullName(String organizationFullName) {
        this.organizationFullName = organizationFullName;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
