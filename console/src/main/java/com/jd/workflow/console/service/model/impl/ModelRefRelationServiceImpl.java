package com.jd.workflow.console.service.model.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.ModelRefRelationMapper;
import com.jd.workflow.console.entity.model.ModelRefRelation;
import com.jd.workflow.console.service.model.IModelRefRelationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class ModelRefRelationServiceImpl extends ServiceImpl<ModelRefRelationMapper, ModelRefRelation> implements IModelRefRelationService {
    public List<ModelRefRelation> listByNames(Long appId,List<String> names,Integer type){
        if(names == null || names.size() == 0){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ModelRefRelation> lqw = new LambdaQueryWrapper<>();
        lqw.in(ModelRefRelation::getModelName, names);
        lqw.eq(type != null, ModelRefRelation::getType, type);
        lqw.eq(ModelRefRelation::getAppId, appId);
        lqw.eq(ModelRefRelation::getYn,1);
        return this.list(lqw);
    }

    public void mergeModelRelation(Long appId,List<String> modelNames,Integer type, Long modelId) {
        List<ModelRefRelation> modelRelations = listByNames(appId, modelNames,type);
        Map<String, List<ModelRefRelation>> name2Models = modelRelations.stream().collect(Collectors.groupingBy(ModelRefRelation::getModelName));
        for (String modelName: modelNames) {
            if(name2Models.containsKey(modelName)){
                List<ModelRefRelation> relations = name2Models.get(modelName);
                for (ModelRefRelation relation : relations) {
                    if(relation.getRelatedIds() == null){
                        relation.setRelatedIds(new HashSet<>());
                    }
                    relation.getRelatedIds().add(modelId);
                    this.updateById(relation);
                }
            }else{
                ModelRefRelation relation = new ModelRefRelation();
                relation.setAppId(appId);
                relation.setModelName(modelName);
                relation.setType(type);
                if(relation.getRelatedIds() == null){
                    relation.setRelatedIds(new HashSet<>());
                }
                relation.getRelatedIds().add(modelId);
                this.save(relation);
            }
        }
    }


    public void removeModelRelation(Long appId,List<String> modelNames,Integer type, Long modelId) {
        List<ModelRefRelation> modelRelations = listByNames(appId, modelNames,type);
        Map<String, List<ModelRefRelation>> name2Models = modelRelations.stream().collect(Collectors.groupingBy(ModelRefRelation::getModelName));
        for (String modelName: modelNames) {
            if(name2Models.containsKey(modelName)){
                List<ModelRefRelation> relations = name2Models.get(modelName);
                for (ModelRefRelation relation : relations) {
                    if(relation.getRelatedIds() == null){
                        relation.setRelatedIds(new HashSet<>());
                    }
                    relation.getRelatedIds().remove(modelId);
                    this.updateById(relation);
                }
            }
        }
    }
    @Override
    public void mergeModelRelation(Long appId,List<String> modelName, Long modelId) {
        mergeModelRelation(appId,modelName,ModelRefRelation.TYPE_MODEL,modelId);

    }

    @Override
    public void removeModelRef(Long appId, List<String> modelName, Long modelId) {
        removeModelRelation(appId,modelName,ModelRefRelation.TYPE_MODEL,modelId);
    }

    @Override
    public void removeMethodRef(Long appId, List<String> modelName, Long modelId) {
        removeModelRelation(appId,modelName,ModelRefRelation.TYPE_INTERFACE,modelId);
    }

    @Override
    public void mergeMethodRef(Long appId,List<String> modelName, Long methodId) {
        mergeModelRelation(appId,modelName,ModelRefRelation.TYPE_INTERFACE,methodId);
    }

    @Override
    public void removeModel(Long appId, String modelName) {
        List<ModelRefRelation> relations = listByNames(appId,Collections.singletonList(modelName),null);
        for (ModelRefRelation relation : relations) {
            relation.setYn(1);
            this.updateById(relation);
        }
    }

    @Override
    public void merge(List<String> beforeRefNames, List<String> afterRefNames, Long appId, Long modelId, Integer type) {
        if(beforeRefNames == null){
            beforeRefNames = new ArrayList<>();
        }
        if(afterRefNames == null){
            afterRefNames = new ArrayList<>();
        }
        for (String beforeRefName : beforeRefNames) {
            if(!afterRefNames.contains(beforeRefName)){
                removeModelRelation(appId,Collections.singletonList(beforeRefName),type,modelId);
            }
        }
        for (String afterRefName : afterRefNames) {
            if(!beforeRefNames.contains(afterRefName)){
                mergeModelRelation(appId,Collections.singletonList(afterRefName),type,modelId);
            }
        }
    }

}
