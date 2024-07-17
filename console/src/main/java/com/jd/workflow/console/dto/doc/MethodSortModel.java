package com.jd.workflow.console.dto.doc;

import lombok.Data;

/**
 * 方法排序
 */
@Data
public class MethodSortModel extends TreeSortModel {

    public MethodSortModel() {
    }
    public MethodSortModel(Long id) {
       this.setId(id);
    }
    //@Override
    public String getType() {
        return   TYPE_METHOD;

    }
}
