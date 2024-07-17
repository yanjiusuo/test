package com.jd.workflow.console.dto.role;

import java.util.List;

/**
 * 资源树
 */
public class ResourceTree {

    private Long id;                                //编号
    private String name = "";                            //资源名称
    private String type;  //资源类型
    private String url;                             //资源路径
    private String permission;                      //权限字符串
    private Long parentId;                          //父编号
    private String parentIds = "";                       //父编号列表
    private Boolean available = Boolean.FALSE;
    private String icon;
    private int sort;
    private String desc;

    private Boolean hidden = Boolean.FALSE;

    private List<ResourceTree> children;

    private Boolean publicInChannel;
    private Integer useType;
    private Boolean canDelete;

    public Boolean getPublicInChannel() {
        return publicInChannel;
    }

    public void setPublicInChannel(Boolean publicInChannel) {
        this.publicInChannel = publicInChannel;
    }

    public Integer getUseType() {
        return useType;
    }

    public void setUseType(Integer useType) {
        this.useType = useType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }


    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public List<ResourceTree> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceTree> children) {
        this.children = children;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    /**
     * 资源类型枚举类
     */
    public enum ResourceType {
        /**
         *
         */
        menu("菜单"),
        /**
         *
         */
        button("按钮");

        private final String info;

        ResourceType(String info) {
            this.info = info;
        }

        public String getInfo() {
            return info;
        }
    }
}

