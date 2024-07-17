package com.jd.workflow.server.dto.app;
import java.util.*;
/**
 * 应用信息新增、展示
 */
public class JsfAppInfo {

    /**
     * appId
     */
    private Long id;

    /**
     * 应用code
     */
    private String appCode;

    /**s
     * 应用名称
     */
    private String appName;
    /**
     * 应用类型 "pqt"藏经阁应用 jdos应用默认为空
     */
    private String appType;

    /**
     * 调用级别 0 接口 1方法
     */
    private String authLevel;

    /**
     * 站点信息 "cn"
     */
    private String site;

    /**
     * 应用描述
     */
    private String desc;

    /**
     * 负责人
     */
    private List<String> owner;


    /**
     * 应用成员
     */
    private List<String> member;
    /**
     *
     */
    private List<String> jdosMembers;

    /**
     * 产品负责人
     */
    private List<String> productor;

    /**
     * 测试负责人
     */
    private List<String> tester;

    /**
     * 测试成员
     */
    private List<String> testMember;

    /**
     * pqtMember成员
     */
    private List<String> pqtMember;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 操作人
     */
    private String modifier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAuthLevel() {
        return authLevel;
    }

    public void setAuthLevel(String authLevel) {
        this.authLevel = authLevel;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getOwner() {
        return owner;
    }

    public void setOwner(List<String> owner) {
        this.owner = owner;
    }

    public List<String> getMember() {
        return member;
    }

    public void setMember(List<String> member) {
        this.member = member;
    }

    public List<String> getJdosMembers() {
        return jdosMembers;
    }

    public void setJdosMembers(List<String> jdosMembers) {
        this.jdosMembers = jdosMembers;
    }

    public List<String> getProductor() {
        return productor;
    }

    public void setProductor(List<String> productor) {
        this.productor = productor;
    }

    public List<String> getTester() {
        return tester;
    }

    public void setTester(List<String> tester) {
        this.tester = tester;
    }

    public List<String> getTestMember() {
        return testMember;
    }

    public void setTestMember(List<String> testMember) {
        this.testMember = testMember;
    }

    public List<String> getPqtMember() {
        return pqtMember;
    }

    public void setPqtMember(List<String> pqtMember) {
        this.pqtMember = pqtMember;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
}
