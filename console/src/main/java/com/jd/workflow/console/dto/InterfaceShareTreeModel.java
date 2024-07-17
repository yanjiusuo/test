package com.jd.workflow.console.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/4 18:14
 * @Description:
 */
@Data
public class InterfaceShareTreeModel {
    private List<TreeSortModel> treeItems = new ArrayList<>();

    @JsonIgnore
    public List<MethodSortModel> childMethods() {
        return (List<MethodSortModel>) treeItems.stream().filter(vs -> vs instanceof MethodSortModel).map(vs -> (MethodSortModel) vs).collect(Collectors.toList());
    }
    public void removeExist(List<TreeSortModel> treeItems) {
        List<MethodSortModel> allMethods = allMethods();
        Set<Long> methodIds = allMethods.stream().map(MethodSortModel::getId).collect(Collectors.toSet());
        removeExist(treeItems,methodIds);
        removeEmptyGroup(treeItems);
    }
    public void removeEmptyGroup(List<TreeSortModel> treeItems){
        treeItems.removeIf(treeItem -> {
            if(treeItem instanceof GroupSortModel){
                if(((GroupSortModel) treeItem).getCount()==0){
                    return true;
                }else{
                    removeEmptyGroup(((GroupSortModel) treeItem).getChildren());
                }
            }
            return false;
        });
    }

    private void removeExist(List<TreeSortModel> treeItems,Set<Long> methodIds){
        Iterator<TreeSortModel> iterator = treeItems.iterator();
        while (iterator.hasNext()) {
            TreeSortModel treeItem = iterator.next();
            if(treeItem instanceof MethodSortModel){
                if(methodIds.contains(treeItem.getId())){
                    iterator.remove();
                }
            }
            if(treeItem instanceof GroupSortModel){
                removeExist(((GroupSortModel) treeItem).getChildren(),methodIds);
            }
        }

    }

    public List<MethodSortModel>  allMethods(){
        List<MethodSortModel> result = new ArrayList<>();
        allMethods(result,treeItems);
        return result;
    }
    public void removeMethod(Long methodId){
        removeMethod(methodId,treeItems);
    }
    public void removeMethod(Long methodId,List<TreeSortModel> treeItems){
        if(treeItems == null) return;
        Iterator<TreeSortModel> iterator = treeItems.iterator();
        while (iterator.hasNext()){
            TreeSortModel treeItem = iterator.next();
            if( treeItem instanceof MethodSortModel && methodId.equals(treeItem.getId())){
                iterator.remove();
            }
            if(treeItem instanceof GroupSortModel){
                removeMethod(methodId,((GroupSortModel) treeItem).getChildren());
            }
        }
        for (TreeSortModel treeItem : treeItems) {

        }
    }
    private void allMethods(List<MethodSortModel> result,List<TreeSortModel> treeItems){
        if(treeItems == null) return;
        for (TreeSortModel treeItem : treeItems) {
            if(treeItem instanceof MethodSortModel){
                result.add((MethodSortModel) treeItem);
            }else if(treeItem instanceof GroupSortModel){
                allMethods(result,((GroupSortModel) treeItem).getChildren());
            }

        }
    }

    @JsonIgnore
    public List<GroupSortModel> childGroup() {
        return treeItems.stream().filter(vs -> vs instanceof GroupSortModel).map(vs -> (GroupSortModel) vs).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<InterfaceSortModel> childInterfaceJSFGroup() {
        return treeItems.stream().filter(vs -> vs instanceof InterfaceSortModel && Objects.equals(vs.getType(), InterfaceSortModel.TYPE_INTERFACE_GROUP_JSF))
                .map(vs ->{
                    vs.setType(InterfaceSortModel.TYPE_INTERFACE);
                    return (InterfaceSortModel) vs;
                }).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<InterfaceSortModel> childInterfaceHTTPGroup() {
        return treeItems.stream().filter(vs -> vs instanceof InterfaceSortModel && Objects.equals(vs.getType(), InterfaceSortModel.TYPE_INTERFACE_GROUP_HTTP))
                .map(vs ->{
                    vs.setType(InterfaceSortModel.TYPE_INTERFACE);
                    return (InterfaceSortModel) vs;
                }).collect(Collectors.toList());
    }
    private GroupSortModel findModel(Long groupId,List<TreeSortModel> models){
        for (TreeSortModel model : models) {
            if(model instanceof GroupSortModel ){
                if(groupId.equals(model.getId())){
                    return (GroupSortModel) model;
                }
                GroupSortModel found = findModel(groupId, ((GroupSortModel) model).getChildren());
                if(found != null) return found;
            }
        }
        return null;
    }
    public void mergeTreeItems(List<TreeSortModel> models){
        for (TreeSortModel model : models) {
            if(model instanceof MethodSortModel){
                treeItems.add(model);
            }else{
                GroupSortModel groupModel = findModel(model.getId(), treeItems);
                if(groupModel == null){
                    treeItems.add(model);
                }else{
                    mergeGroup(groupModel,((GroupSortModel)model).getChildren());
                }

            }
        }
    }
    private void mergeGroup(GroupSortModel sortModel,List<TreeSortModel> children){
        for (TreeSortModel child : children) {
            if(child instanceof MethodSortModel){
                sortModel.getChildren().add(child);
            }else{
                GroupSortModel found = findModel(child.getId(), sortModel.getChildren());
                if(found != null){
                    mergeGroup(found,((GroupSortModel)child).getChildren());
                }else{
                    sortModel.getChildren().add(child);
                }
            }
        }
    }
    public GroupSortModel findMethodParent(Long methodId) {
        return findMethodParent(methodId, treeItems, null);
    }

    private GroupSortModel findMethodParent(Long methodId, List<TreeSortModel> treeItems, GroupSortModel parent) {
        if (treeItems == null) return null;
        for (TreeSortModel treeItem : treeItems) {
            if (treeItem instanceof MethodSortModel) {
                if (treeItem.getId() != null && treeItem.getId().equals(methodId)) {
                    return parent;
                }

            } else {
                GroupSortModel groupSortModel = (GroupSortModel) treeItem;
                GroupSortModel ret = findMethodParent(methodId, groupSortModel.getChildren(), groupSortModel);
                if (ret != null) return ret;
            }
        }
        return null;
    }
}
