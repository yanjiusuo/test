package com.jd.workflow.console.dto.role;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>UserEntity: chenguoyou
 * <p>Date: 2017-9-14
 * <p>Version: 1.0
 */
public class Role implements Serializable {
    private static final long serialVersionUID = -4141544257436164385L;

    private Long id;            //编号
    //private String role;        //角色标识 程序中判断使用,如"admin"
    private String roleName;    //角色名，同role 做为一个冗余字段存在

    private String description; //角色描述,UI界面显示使用
    private List<Long> resourceIds; //拥有的资源
    private Boolean available = Boolean.FALSE; //是否可用,如果不可用将不会添加给用户


    private int maxPage;            //可创建的最多大屏数目

    private List<Long> compIds;     //组件ids

    private List<Long> templateIds; //模版ids

    private String createUser;

    private Timestamp createTime;

    private Long tenantId;

    private Boolean publicInChannel;

    private Integer useType;

    private Integer userNum;    //当前角色下的用户数

    /**
     * 修改者编号
     */
    private String modifyBy;
    /**
     * 修改日期
     */
    private Date modifyAt;

    private List<ResourceTree> menuTrees;

    //是否租户下的主角色
    private Integer isMainRole = 0;

    public Role() {
    }

    public Role(String roleName, String description, Boolean available) {
        this.roleName = roleName;
        this.description = description;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public Boolean getPublicInChannel() {
        return publicInChannel;
    }

    public List<ResourceTree> getMenuTrees() {
        return menuTrees;
    }

    public void setMenuTrees(List<ResourceTree> menuTrees) {
        this.menuTrees = menuTrees;
    }

    public void setPublicInChannel(Boolean publicInChannel) {
        this.publicInChannel = publicInChannel;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserNum() {
        return userNum;
    }

    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }

    public Integer getIsMainRole() {
        return isMainRole;
    }

    public void setIsMainRole(Integer isMainRole) {
        this.isMainRole = isMainRole;
    }

    /*public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }*/

    public String getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }

    public Date getModifyAt() {
        return modifyAt;
    }

    public void setModifyAt(Date modifyAt) {
        this.modifyAt = modifyAt;
    }

    public Integer getUseType() {
        return useType;
    }

    public void setUseType(Integer useType) {
        this.useType = useType;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getResourceIds() {
        if (resourceIds == null) {
            resourceIds = new ArrayList<Long>();
        }
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public String getResourceIdsStr() {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for (Long resourceId : resourceIds) {
            s.append(resourceId);
            s.append(",");
        }
        return s.toString();
    }

    public void setResourceIdsStr(String resourceIdsStr) {
        if (StringUtils.isEmpty(resourceIdsStr)) {
            return;
        }
        String[] resourceIdStrs = resourceIdsStr.split(",");
        for (String resourceIdStr : resourceIdStrs) {
            if (StringUtils.isEmpty(resourceIdStr)) {
                continue;
            }
            getResourceIds().add(Long.valueOf(resourceIdStr));
        }
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public List<Long> getCompIds() {
        return compIds;
    }

    public void setCompIds(List<Long> compIds) {
        this.compIds = compIds;
    }

    public List<Long> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(List<Long> templateIds) {
        this.templateIds = templateIds;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        Role role = (Role) o;

        return id != null ? id.equals(role.id) : role.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RoleEntity{" +
                "id=" + id +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                //", resourceIds=" + resourceIds +
                ", available=" + available +
                '}';
    }
}
