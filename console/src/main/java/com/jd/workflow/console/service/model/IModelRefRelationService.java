package com.jd.workflow.console.service.model;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.ModelRefRelation;

import java.util.List;

public interface IModelRefRelationService extends IService<ModelRefRelation> {
    public void mergeModelRelation(Long appId,List<String> modelName, Long modelId);
    public void removeModelRef(Long appId,List<String> modelName, Long modelId);
    public void removeMethodRef(Long appId,List<String> modelName, Long modelId);


    public void mergeMethodRef(Long appId,List<String> modelName,Long methodId);

    public void removeModel(Long appId,String modelName);

    public void merge(List<String> beforeRefNames,List<String> afterRefNames,Long appId,Long modelId,Integer type);
}
