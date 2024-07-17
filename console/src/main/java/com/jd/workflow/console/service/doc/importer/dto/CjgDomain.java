package com.jd.workflow.console.service.doc.importer.dto;

import lombok.Data;

import java.util.List;

@Data
public class CjgDomain {
    Long id;
    String trace;
    String traceName;
    String description;
    String code;
    String dataSource;
    List<CjgDomain> childs;
    public CjgDomain findByCode(String code){
        return findByCode(code,this);
    }
    private CjgDomain findByCode(String code,CjgDomain root){
        if(code.equals(root.getCode())){
            return root;
        }
        if(root.getChilds() != null){
            for (CjgDomain child : root.getChilds()) {
                CjgDomain found = findByCode(code, child);
                if(found != null)return found;
            }
        }
        return null;
    }
}
