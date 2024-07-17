package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import lombok.Data;


public interface ITreeEntitySupport {
    public Long getId();
    public void setSortGroupTree(MethodGroupTreeModel sortGroupTree);
    public MethodGroupTreeModel getSortGroupTree();

    public void setGroupLastVersion(String groupLastVersion);
    public String getGroupLastVersion();
    public int getInterfaceType();
    public Long  getInterfaceId();
    default MethodGroupTreeDTO toGroupDto(){
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
        dto.setGroupLastVersion(getGroupLastVersion());
        dto.setTreeModel(getSortGroupTree());
        return dto;
    }
}
