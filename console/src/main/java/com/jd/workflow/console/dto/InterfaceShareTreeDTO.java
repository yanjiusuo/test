package com.jd.workflow.console.dto;

import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/4 17:47
 * @Description:
 */
@Data
public class InterfaceShareTreeDTO {

    /**
     * 分享分组id
     */
    private Long shareGroupId;

    /**
     * 结构树最新版本号
     */
    private String groupLastVersion;

    private InterfaceShareTreeModel treeModel;
    /**
     * 插入接口
     *
     * @param parentId
     * @param newInterfaceId
     */
   /* public void insertInterface(Long parentId, Long newInterfaceId) {
        insertTreeItem(parentId, newInterfaceId, TreeSortModel.TYPE_INTERFACE);
    }*/

    /**
     * 插入方法分组
     *
     * @param parentId
     * @param newGroupId
     */
    public void insertGroup(Long parentId, Long newGroupId, String name) {
        insertTreeItem(parentId, newGroupId, TreeSortModel.TYPE_GROUP, name);
    }

    /**
     * 插入方法
     *
     * @param parentId
     * @param
     */
    private void insertTreeItem(Long parentId, Long newGroupId, String type, String name) {
        if (parentId == null) {
            List<TreeSortModel> treeItems = treeModel.getTreeItems();
            if (CollectionUtils.isEmpty(treeItems)) {
                treeItems = new ArrayList<>();
                treeModel.setTreeItems(treeItems);
            }
            addModel(newGroupId, type, name, treeItems);
        } else {
            // 获取parentModel
            TreeSortModel parentModel = findParentModel(treeModel.getTreeItems(), parentId);
            if (Objects.nonNull(parentModel)) {
                //添加分组信息
                insertTreeItemToInterface(parentModel, newGroupId, type, name);
            } else {
                throw new BizException("parentId 不存在!");
            }

        }
    }


    /**
     * 获取parentModel
     *
     * @param treeItems
     * @param parentId
     * @return
     */
    private TreeSortModel findParentModel(List<TreeSortModel> treeItems, Long parentId) {
        for (TreeSortModel model : treeItems) {
            if (Objects.equals(model.getId(), parentId)) {
                return model;
            }
            List<TreeSortModel> children = null;
            if (model instanceof GroupSortModel) {
                GroupSortModel groupSortModel = ((GroupSortModel) model);
                children = groupSortModel.getChildren();
            }
            if (model instanceof InterfaceSortModel) {
                InterfaceSortModel interfaceSortModel = ((InterfaceSortModel) model);
                children = interfaceSortModel.getChildren();

            }
            if (CollectionUtils.isNotEmpty(children)) {
                TreeSortModel parentModel = findParentModel(children, parentId);
                if (Objects.nonNull(parentModel)) {
                    return parentModel;
                }
            }
        }
        return null;

    }


    /**
     * 新增具体分组信息
     *
     * @param parentModel 父model
     * @param newGroupId  新分组id
     * @param type        分组类型
     * @param name        分组名称
     */
    private void insertTreeItemToInterface(TreeSortModel parentModel, Long newGroupId, String type, String name) {
        List<TreeSortModel> children = new ArrayList<>();
        if (parentModel instanceof GroupSortModel) {
            GroupSortModel groupSortModel = ((GroupSortModel) parentModel);
            children = groupSortModel.getChildren();
            if (CollectionUtils.isEmpty(children)) {
                children = new ArrayList<>();
                groupSortModel.setChildren(children);
            }
        }
        if (parentModel instanceof InterfaceSortModel) {
            InterfaceSortModel interfaceSortModel = ((InterfaceSortModel) parentModel);
            children = interfaceSortModel.getChildren();
            if (CollectionUtils.isEmpty(children)) {
                children = new ArrayList<>();
                interfaceSortModel.setChildren(children);
            }

        }
        addModel(newGroupId, type, name, children);
    }

    /**
     * 添加分组信息
     *
     * @param newGroupId
     * @param type
     * @param name
     * @param children
     */
    private void addModel(Long newGroupId, String type, String name, List<TreeSortModel> children) {
        if (TreeSortModel.TYPE_GROUP.equals(type)) {
            GroupSortModel model = new GroupSortModel();
            model.setId(newGroupId);
            model.setName(name);
            model.setType(type);
            children.add(model);
        } else if (TreeSortModel.TYPE_METHOD.equals(type)) {
            MethodSortModel model = new MethodSortModel();
            model.setId(newGroupId);
            model.setName(name);
            model.setType(type);
            children.add(model);
        } else {
            InterfaceSortModel model = new InterfaceSortModel();
            model.setId(newGroupId);
            model.setName(name);
            model.setType(type);
            children.add(model);
        }
    }


    /**
     * 修改分组名称
     *
     * @param groupId
     * @param name
     * @param parentId
     */
    public void updateGroupName(Long groupId, String name, Long parentId) {
        TreeSortModel childrenModel = null;
        if (parentId == null) {
            List<TreeSortModel> treeItems = treeModel.getTreeItems();
            childrenModel = getChildModel(treeItems, groupId);
        } else {
            // 获取parentModel
            TreeSortModel parentModel = findParentModel(treeModel.getTreeItems(), parentId);
            if (Objects.nonNull(parentModel)) {
                if (parentModel instanceof GroupSortModel) {
                    GroupSortModel groupSortModel = ((GroupSortModel) parentModel);
                    childrenModel = getChildModel(groupSortModel.getChildren(), groupId);
                }
                if (parentModel instanceof InterfaceSortModel) {
                    InterfaceSortModel interfaceModel = ((InterfaceSortModel) parentModel);
                    childrenModel = getChildModel(interfaceModel.getChildren(), groupId);
                }
            } else {
                throw new BizException("parentId 不存在!");
            }

        }
        updateModel(childrenModel, name);
    }


    /**
     * 获取匹配子model
     *
     * @param childrenModel model集合
     * @param groupId       分组id
     * @return
     */
    private TreeSortModel getChildModel(List<TreeSortModel> childrenModel, Long groupId) {
        if (CollectionUtils.isEmpty(childrenModel)) {
            return null;
        }
        for (TreeSortModel treeSortModel : childrenModel) {
            // 获取与之匹配的子model
            if (Objects.equals(groupId, treeSortModel.getId())) {
                return treeSortModel;
            }
            if (treeSortModel instanceof GroupSortModel) {
                GroupSortModel groupSortModel = ((GroupSortModel) treeSortModel);
                TreeSortModel childModel = getChildModel(groupSortModel.getChildren(), groupId);
                if (Objects.nonNull(childModel)) {
                    return childModel;
                }

            }
            if (treeSortModel instanceof InterfaceSortModel) {
                InterfaceSortModel interfaceModel = ((InterfaceSortModel) treeSortModel);
                TreeSortModel childModel = getChildModel(interfaceModel.getChildren(), groupId);
                if (Objects.nonNull(childModel)) {
                    return childModel;
                }
            }
        }
        return null;
    }

    /**
     * 更新model信息
     *
     * @param childrenModel
     * @param name
     */
    private void updateModel(TreeSortModel childrenModel, String name) {
        if (Objects.nonNull(childrenModel)) {
            childrenModel.setName(name);
        } else {
            throw new BizException("groupId is error 分组id不存在!");
        }
    }

    public boolean removeGroup(Long groupId) {
        return removeGroup(groupId, treeModel.getTreeItems());
    }

    private boolean removeGroup(Long groupId, List<TreeSortModel> treeModels) {
        if (groupId == null || treeModels == null) return false;
        for (int i = 0; i < treeModels.size(); i++) {
            TreeSortModel treeItem = treeModels.get(i);
            if (treeItem instanceof GroupSortModel) {
                if (groupId.equals(treeItem.getId())) {
                    treeModels.remove(i);
                    return true;
                }
                GroupSortModel groupSortModel = (GroupSortModel) treeItem;
                boolean hasRemoved = removeGroup(groupId, groupSortModel.getChildren());
                if (hasRemoved) return true;
            }
        }
        return false;

    }

    public void removeMethod(Long methodId, Long parentId){
        removeGroup(methodId,parentId);
    }

    /**
     * 删除分组信息
     *
     * @param groupId
     * @param parentId
     */
    public void removeGroup(Long groupId, Long parentId) {
        List<TreeSortModel> childrenList = null;
        TreeSortModel childrenModel = null;
        if (parentId == null) {
            childrenList = treeModel.getTreeItems();
            childrenModel = getChildModel(childrenList, groupId);
        } else {
            // 获取parentModel
            TreeSortModel parentModel = findParentModel(treeModel.getTreeItems(), parentId);
            if (Objects.nonNull(parentModel)) {
                if (parentModel instanceof GroupSortModel) {
                    GroupSortModel groupSortModel = ((GroupSortModel) parentModel);
                    childrenList = groupSortModel.getChildren();
                    childrenModel = getChildModel(childrenList, groupId);
                }
                if (parentModel instanceof InterfaceSortModel) {
                    InterfaceSortModel interfaceModel = ((InterfaceSortModel) parentModel);
                    childrenList = interfaceModel.getChildren();
                    childrenModel = getChildModel(childrenList, groupId);
                }
            } else {
                throw new BizException("parentId 不存在!");
            }
        }
        if (CollectionUtils.isNotEmpty(childrenList) && Objects.nonNull(childrenModel)) {
            childrenList.remove(childrenModel);
        } else {
            throw new BizException("请检查parentId 与 groupId !");
        }
    }
}
