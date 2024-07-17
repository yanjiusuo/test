package com.jd.workflow.console.service.doc.impl;

import com.jd.workflow.console.entity.MethodManage;
import lombok.Data;

import java.util.*;

@Data
public class DocUpdateData {
    List<MethodManage> added;
    List<MethodManage> updated;
    Map<Long,MethodManage> updatedMap;
    List<MethodManage> removed;
    List<MethodManage> addAndUpdated = new ArrayList<>();
    public DocUpdateData(List<MethodManage> added, List<MethodManage> updated, List<MethodManage> removed) {
        this.added = added;
        this.updated = updated;
        updatedMap = new HashMap<>();
        for (MethodManage methodManage : updated) {
            updatedMap.put(methodManage.getId(),methodManage);
        }
        this.removed = removed;
        addAndUpdated.addAll(added);
        addAndUpdated.addAll(updated);
    }
    public MethodManage getUpdatedMethod(Long id){
        return updatedMap.get(id);
    }
}
