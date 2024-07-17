package com.jd.workflow.server.dto.interfaceManage;

import java.io.Serializable;
import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：InterfaceMethodGroup
 * 类 描 述：接口下方法分组（二级目录）
 * 创建时间：2022-11-08 16:17
 * 创 建 人：wangxiaofei8
 */
public class JsfInterfaceMethodGroup  implements Serializable {


    /**
     * 接口id
     */
    private Long interfaceId;
    /**
     * 分组名称
     */
    private String name;
    /**
     * 分组英文名
     */
    private String enName;
    /**
     * 分组类型：1 应用 2 需求 3 工作流
     */
    private Integer type;
    /**
     * 父节点id
     */
    private Long parentId;


    /**
     * 子目录
     */
    private List<JsfInterfaceMethodGroup> childDic;

    /**
     * 子方法
     */
    private List<JsfMethodManage> thirdMethods;


    public Long getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(Long interfaceId) {
        this.interfaceId = interfaceId;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<JsfInterfaceMethodGroup> getChildDic() {
        return childDic;
    }

    public void setChildDic(List<JsfInterfaceMethodGroup> childDic) {
        this.childDic = childDic;
    }

    public List<JsfMethodManage> getThirdMethods() {
        return thirdMethods;
    }

    public void setThirdMethods(List<JsfMethodManage> thirdMethods) {
        this.thirdMethods = thirdMethods;
    }
}
