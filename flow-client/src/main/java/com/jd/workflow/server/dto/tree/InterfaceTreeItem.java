package com.jd.workflow.server.dto.tree;

import java.io.File;
import java.util.List;

/**
 * 接口树节点
 */
public class InterfaceTreeItem {
    /**
     * 接口、方法或者文件夹id
     */
    private Long id;
    /**
     * 1-方法
     * 2-文件夹
     * 3-接口,
     * 最上级的type必须为3
     */
    private int type;
    /**
     * 名称
     */
    private String name;
    /**
     * 指定版本
     */
    private String version;
    /**
     * 英文名称
     */
    private String enName;

    /**
     * 接口分组类型：1-http分组 3-jsf分组
     */
    private Integer interfaceType;
    /**
     * 是否被删除
     */
    boolean deleted;

    /**
     * 子节点
     */
    List<InterfaceTreeItem> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<InterfaceTreeItem> getChildren() {
        return children;
    }

    public void setChildren(List<InterfaceTreeItem> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(Integer interfaceType) {
        this.interfaceType = interfaceType;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public static void main(String[] args) {
        boolean exists = new File("/D:\\github-git\\interface-transform\\http-auth-demo\\springmvc-demo-for-test\\src\\main\\resources\\spring.xml").exists();
        System.out.println(exists);
    }
}
