package com.jd.workflow.server.dto;

/**
 * 接口版本信息
 */
public class InterfaceVersionDto {
    /**
     * 版本id
     */
    Long id;
    /**
     * 版本号
     */
    String version;
    /**
     * 创建时间
     */
    String created;
    /**
     * 版本描述
     */
    String versionDesc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }
}
