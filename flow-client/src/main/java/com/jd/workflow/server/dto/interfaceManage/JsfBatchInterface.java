package com.jd.workflow.server.dto.interfaceManage;


import java.util.List;

public class JsfBatchInterface{
    /**
     * 一级目录
     */
    private JsfInterfaceManage firstDic;
    /**
     * 二级目录
     */
    private List<JsfInterfaceMethodGroup> secondDic;
    /**
     * 二级方法
     */
    private List<JsfMethodManage> secondMethods;

    public JsfBatchInterface() {
    }



    public JsfInterfaceManage getFirstDic() {
        return firstDic;
    }

    public void setFirstDic(JsfInterfaceManage firstDic) {
        this.firstDic = firstDic;
    }

    public List<JsfInterfaceMethodGroup> getSecondDic() {
        return secondDic;
    }

    public void setSecondDic(List<JsfInterfaceMethodGroup> secondDic) {
        this.secondDic = secondDic;
    }

    public List<JsfMethodManage> getSecondMethods() {
        return secondMethods;
    }

    public void setSecondMethods(List<JsfMethodManage> secondMethods) {
        this.secondMethods = secondMethods;
    }
}
