package com.jd.workflow.console.dto;

import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法分组树
 */
@Data
public class MethodGroupTreeDTO {
    /**
     * 接口id
     */
    private Long interfaceId;
    /**
     * 分组版本
     */
    private String groupLastVersion;
    /**
     * 树节点信息
     */
    private MethodGroupTreeModel treeModel;

    public void setInterfaceId(Long interfaceId){
        this.interfaceId = interfaceId;
        if(treeModel != null){
            setInterfaceId(treeModel.getTreeItems(),interfaceId);
        }
    }
    private void setInterfaceId(List<TreeSortModel> trees,Long interfaceId){
        if(trees == null) return;
        for (TreeSortModel tree : trees) {
            tree.setInterfaceId(interfaceId);
            if(tree instanceof GroupSortModel){
                GroupSortModel group = (GroupSortModel) tree;
                setInterfaceId(group.getChildren(),interfaceId);
            }
        }
    }

    public void insertGroup(Long parentId,Long newGroupId){
        insertTreeItem(parentId,newGroupId,TreeSortModel.TYPE_GROUP);
    }
    public void insertMethod(Long parentId,Long newGroupId){
        insertTreeItem(parentId,newGroupId,TreeSortModel.TYPE_METHOD);
    }

    public void clearChildrenTreeModel(List<TreeSortModel> treeItems){
        for (TreeSortModel treeItem : treeItems) {
            if(treeItem instanceof GroupSortModel){
                GroupSortModel group = (GroupSortModel) treeItem;
                if(group.getChildren()== null || group.getChildren().isEmpty()){
                    group.setChildren(null);
                }else{
                    clearChildrenTreeModel(group.getChildren());
                }
            }
        }
    }
    private void insertTreeItem(Long parentId,Long newGroupId,String type){
        if(parentId == null){
            TreeSortModel model = null;
            if(TreeSortModel.TYPE_GROUP.equals(type)){
                model = new GroupSortModel();
            }else{
                model = new MethodSortModel();
            }
            model.setId(newGroupId);
            for (int i = treeModel.getTreeItems().size()-1; i >=0 ; i--) {
                final TreeSortModel treeItem = treeModel.getTreeItems().get(i);
                if(treeItem instanceof GroupSortModel) {
                    treeModel.getTreeItems().add(i,model);
                    return;
                }
            }
            treeModel.getTreeItems().add(0,model);
        }else {
            for (TreeSortModel treeItem : treeModel.getTreeItems()) {
                if(!(treeItem instanceof GroupSortModel)) continue;
                GroupSortModel groupSortModel = (GroupSortModel) treeItem;
                insertTreeItemToParent(groupSortModel,parentId,newGroupId,type);
            }
        }
    }
    public boolean removeGroup(Long groupId){
        return removeGroup(groupId,treeModel.getTreeItems());
    }
    private boolean removeGroup(Long groupId, List<TreeSortModel> treeModels){
        if(groupId == null || treeModels == null) return false;
        for (int i = 0; i < treeModels.size(); i++) {
            TreeSortModel treeItem = treeModels.get(i);
            if(treeItem instanceof GroupSortModel){
                if(groupId.equals(treeItem.getId())){
                    treeModels.remove(i);
                    return true;
                }
                GroupSortModel groupSortModel = (GroupSortModel) treeItem;
                boolean hasRemoved = removeGroup(groupId,groupSortModel.getChildren());
                if(hasRemoved) return true;
            }
        }
        return false;

    }
    private void insertTreeItemToParent(GroupSortModel groupSortModel, Long parentId, Long newGroupId,String type) {
        if (parentId.equals(groupSortModel.getId())) {

            if (groupSortModel.getChildren() == null) {
                groupSortModel.setChildren(new ArrayList<>());
            }
            if(TreeSortModel.TYPE_GROUP.equals(type)){
                GroupSortModel model = new GroupSortModel();
                model.setId(newGroupId);
                groupSortModel.getChildren().add(model);
            }else{
                MethodSortModel model = new MethodSortModel();
                model.setId(newGroupId);
                groupSortModel.getChildren().add(model);
            }

        } else {
            for (GroupSortModel childGroup : groupSortModel.childGroups()) {
                insertTreeItemToParent(childGroup, parentId, newGroupId,type);
            }
        }
    }
}
