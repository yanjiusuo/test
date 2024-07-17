package com.jd.workflow.console.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 方法分组树
 */
@Data
public class MethodGroupTreeModel {

    //private List<MethodSortModel> methodsOfNoGroup;

    private List<TreeSortModel> treeItems = new ArrayList<>();

    public void removeExist(List<TreeSortModel> treeItems) {
        List<MethodSortModel> allMethods = allMethods();
        Set<Long> methodIds = allMethods.stream().map(MethodSortModel::getId).collect(Collectors.toSet());
        removeExist(treeItems, methodIds);
        removeEmptyGroup(treeItems);
    }
    private static String getKey(TreeSortModel sortModel){
        return sortModel.getKey();
    }
    private static void findDuplicated(List<TreeSortModel> treeItems,Set<String> keys,List<TreeSortModel> duplicated){
        for (TreeSortModel treeItem : treeItems) {
            String key = getKey(treeItem);
            if(keys.contains(key)) {
                duplicated.add(treeItem);
                continue;
            }
            keys.add(key);

            if(treeItem instanceof GroupSortModel){
                GroupSortModel group = (GroupSortModel) treeItem;
                if(group.getChildren() == null) continue;
                findDuplicated(group.getChildren(),keys,duplicated);
            }
        }
    }
    public List<TreeSortModel> findDuplicated(){
        initKeys();
        Set<String> processed = new HashSet<>();
        List<TreeSortModel> duplicated = new ArrayList<>();
        findDuplicated(treeItems,processed,duplicated);
        return duplicated;
    }
    public void removeDuplicated(){
        initKeys();
        Set<String> processed = new HashSet<>();
        List<TreeSortModel> duplicated = new ArrayList<>();
        removeDuplicated(treeItems,processed);
    }
    private static void removeDuplicated(List<TreeSortModel> treeItems,Set<String> keys){
        Iterator<TreeSortModel> iterator = treeItems.iterator();
        while (iterator.hasNext()) {
            TreeSortModel treeItem = iterator.next();
            if (keys.contains(treeItem.getKey())) {
                iterator.remove();
            } else {
                keys.add(treeItem.getKey());
                if (treeItem instanceof GroupSortModel) {
                    removeDuplicated(((GroupSortModel) treeItem).getChildren(), keys);
                }
            }
        }
    }
    private static void removeItems(List<TreeSortModel> treeItems, List<TreeSortModel> removed) {
        Iterator<TreeSortModel> iterator = treeItems.iterator();
        while (iterator.hasNext()) {
            TreeSortModel treeItem = iterator.next();
            if (removed.contains(treeItem)) {
                iterator.remove();
            } else {
                if (treeItem instanceof GroupSortModel) {
                    removeItems(((GroupSortModel) treeItem).getChildren(), removed);
                }
            }
        }
    }

    public void removeEmptyGroup(List<TreeSortModel> treeItems) {
        treeItems.removeIf(treeItem -> {
            if (treeItem instanceof GroupSortModel) {
                if (((GroupSortModel) treeItem).getCount() == 0) {
                    return true;
                } else {
                    removeEmptyGroup(((GroupSortModel) treeItem).getChildren());
                }
            }
            return false;
        });
    }

    private void removeExist(List<TreeSortModel> treeItems, Set<Long> methodIds) {
        Iterator<TreeSortModel> iterator = treeItems.iterator();
        while (iterator.hasNext()) {
            TreeSortModel treeItem = iterator.next();
            if (treeItem instanceof MethodSortModel) {
                if (methodIds.contains(treeItem.getId())) {
                    iterator.remove();
                }
            }
            if (treeItem instanceof GroupSortModel) {
                removeExist(((GroupSortModel) treeItem).getChildren(), methodIds);
            }
        }

    }

    public void initKeys() {
        initKeys(treeItems);
    }

    private void initKeys(List<TreeSortModel> treeItems) {
        for (TreeSortModel treeItem : treeItems) {
            treeItem.initKey();
            if (treeItem instanceof GroupSortModel) {
                initKeys(((GroupSortModel) treeItem).getChildren());
            }
        }
    }

    public void setAppId(Long appId) {
        setAppId(treeItems, appId);
    }

    private void setAppId(List<TreeSortModel> treeItems, Long appId) {
        if (treeItems == null) return;

        for (TreeSortModel treeItem : treeItems) {
            treeItem.setAppId(appId);
            if (treeItem instanceof GroupSortModel) {
                setAppId(((GroupSortModel) treeItem).getChildren(), appId);
            }
        }
    }

    @JsonIgnore
    public List<MethodSortModel> childMethods() {


        return (List<MethodSortModel>) treeItems.stream().filter(vs -> vs instanceof MethodSortModel).map(vs -> (MethodSortModel) vs).collect(Collectors.toList());
    }

    public boolean containsInterface() {
        return containsInterface(treeItems);
    }

    private boolean containsInterface(List<TreeSortModel> treeItems) {
        if (treeItems == null) return false;
        for (TreeSortModel treeItem : treeItems) {
            if (treeItem instanceof InterfaceSortModel) return true;
            if (treeItem instanceof GroupSortModel) {
                return containsInterface(((GroupSortModel) treeItem).getChildren());
            }
        }
        return false;
    }

    public List<MethodSortModel> allMethods() {
        List<MethodSortModel> result = new ArrayList<>();
        for (TreeSortModel treeItem : treeItems) {
            if (treeItem instanceof GroupSortModel) {
                result.addAll(((GroupSortModel) treeItem).allChildMethods());
            } else {
                result.add((MethodSortModel) treeItem);
            }
        }
        return result;
    }

    public boolean removeGroup(Long id, Long parentId) {
        return removeGroup(id, treeItems, null, parentId);
    }

    private boolean removeGroup(Long id, List<TreeSortModel> treeItems, GroupSortModel parent, Long parentId) {
        Iterator<TreeSortModel> iterator = treeItems.iterator();
        while (iterator.hasNext()) {
            TreeSortModel treeItem = iterator.next();
            if (treeItem instanceof GroupSortModel) {
                if (id.equals(treeItem.getId())) {
                    if (parentId == null ||
                            parent != null && parentId != null && parentId.equals(parent.getId())) {
                        iterator.remove();
                        return true;
                    }
                }
                boolean result = removeGroup(id, ((GroupSortModel) treeItem).getChildren(), (GroupSortModel) treeItem, parentId);
                if (result) return result;
            }
        }
        return false;
    }

    public boolean removeMethod(Long id, Long parentId) {
        return removeMethod(id, treeItems, null, parentId);
    }

    private boolean removeMethod(Long id, List<TreeSortModel> treeItems, GroupSortModel parent, Long parentId) {
        Iterator<TreeSortModel> iterator = treeItems.iterator();
        while (iterator.hasNext()) {
            TreeSortModel treeItem = iterator.next();
            if (treeItem instanceof MethodSortModel) {
                if (id.equals(treeItem.getId())) {
                    if (parentId == null || parentId != null && parent != null && parentId.equals(parent.getId())) {
                        iterator.remove();
                        return true;
                    }
                }
            } else {
                boolean result = removeMethod(id, ((GroupSortModel) treeItem).getChildren(), (GroupSortModel) treeItem, parentId);
                if (result) return result;
            }
        }
        return false;
    }

    public List<GroupSortModel> allGroups() {
        List<GroupSortModel> result = new ArrayList<>();
        allGroups(result, treeItems);
        return result;
    }

    private void allGroups(List<GroupSortModel> groups, List<TreeSortModel> treeItems) {
        if (treeItems == null) return;
        for (TreeSortModel treeItem : treeItems) {
            if (treeItem instanceof GroupSortModel) {
                groups.add((GroupSortModel) treeItem);
                allGroups(groups, ((GroupSortModel) treeItem).getChildren());
            }
        }
    }

    @JsonIgnore
    public List<GroupSortModel> childGroup() {
        return treeItems.stream().filter(vs -> vs instanceof GroupSortModel).map(vs -> (GroupSortModel) vs).collect(Collectors.toList());
    }

    public List<GroupSortModel> mergeGroupByRelatedId(List<TreeSortModel> models) {
        List<GroupSortModel> allChildGroups = new ArrayList<>();
        mergeGroupByRelatedId(models, treeItems, allChildGroups);
        return allChildGroups;
    }

    public static boolean find(List<TreeSortModel> models, Long id) {
        if (models == null) return false;
        for (TreeSortModel model : models) {
            if (id.equals(model.getId())) return true;
            if (model instanceof GroupSortModel) {
                boolean result = find(((GroupSortModel) model).getChildren(), id);
                if (result) return true;
            }
        }
        return false;
    }

    public void mergeGroupByRelatedId(List<TreeSortModel> models, List<TreeSortModel> target, List<GroupSortModel> allChildGroups) {

        for (TreeSortModel model : models) {
            if (model instanceof MethodSortModel) {
                boolean exist = find(target, model.getId());/*target.stream().filter(vs -> {
                    return vs instanceof MethodSortModel && vs.getId().equals(model.getId());
                }).findAny();*/
                if (!exist) {
                    target.add(model);
                }

            } else {
                GroupSortModel relatedGroup = findGroupByRelatedId(model.getId());
                if (relatedGroup != null) {
                    mergeGroupByRelatedId(((GroupSortModel) model).getChildren(), relatedGroup.getChildren(), allChildGroups);
                } else {
                    allChildGroups.add((GroupSortModel) model);
                    allChildGroups.addAll(((GroupSortModel) model).allChildGroups());
                    target.add(model);
                }
            }
        }
    }

    public GroupSortModel findMethodParent(Long methodId) {
        return findMethodParent(methodId, treeItems, null);
    }

    public GroupSortModel findGroup(String groupName) {
        return findGroup(groupName, treeItems);
    }

    public GroupSortModel findGroup(Long id) {
        return findGroupById(id, treeItems);
    }

    public GroupSortModel findGroupByRelatedId(Long id) {
        return findGroupByRelatedId(id, treeItems);
    }

    private GroupSortModel findGroupByRelatedId(Long id, List<TreeSortModel> items) {
        for (TreeSortModel item : items) {
            if (item instanceof GroupSortModel) {
                if (id.equals(((GroupSortModel) item).getRelatedId())) {
                    return (GroupSortModel) item;
                }
                findGroupByRelatedId(id, ((GroupSortModel) item).getChildren());
            }
        }
        return null;
    }

    private GroupSortModel findGroupById(Long id, List<TreeSortModel> items) {
        if (items == null) return null;
        for (TreeSortModel treeItem : items) {
            if (treeItem instanceof GroupSortModel) {
                if (id.equals(treeItem.getId())) {
                    return (GroupSortModel) treeItem;
                }
                GroupSortModel group = (GroupSortModel) treeItem;
                GroupSortModel ret = findGroupById(id, group.getChildren());
                if (ret != null) return ret;

            }

        }
        return null;
    }

    private GroupSortModel findGroup(String groupName, List<TreeSortModel> items) {
        if (items == null) return null;
        for (TreeSortModel treeItem : items) {
            if (treeItem instanceof GroupSortModel) {
                if (groupName.equals(treeItem.getName())) {
                    return (GroupSortModel) treeItem;
                }
                GroupSortModel group = (GroupSortModel) treeItem;
                GroupSortModel ret = findGroup(groupName, group.getChildren());
                if (ret != null) return ret;

            }

        }
        return null;
    }

    private GroupSortModel findMethodParent(Long methodId, List<TreeSortModel> treeItems, GroupSortModel parent) {
        if (treeItems == null) return null;

        for (TreeSortModel treeItem : treeItems) {
            if (treeItem instanceof MethodSortModel

            ) {
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

    public void compactGroup(List<TreeSortModel> treeItems) {
        treeItems.stream().forEach(treeItem ->{
            if(treeItem instanceof GroupSortModel){
                GroupSortModel model = (GroupSortModel) treeItem;
                mergerChild(model.getChildren(), (GroupSortModel) treeItem);
            }
        });

    }
    private void mergerChild(List<TreeSortModel> children,GroupSortModel parent) {
        if (children.size() == 1) {

            if(children.get(0) instanceof GroupSortModel) {
                GroupSortModel child = (GroupSortModel) children.get(0);
                parent.setEnName(parent.getEnName() + "/" + child.getEnName());
                parent.setName(parent.getName() + "/" + child.getName());
                parent.setId(child.getId());
                parent.setRelatedId(child.getRelatedId());
                parent.setChildren(child.getChildren());
                mergerChild(child.getChildren(), parent);
            }
        } else if (children.size() > 1) {
            children.stream().forEach(treeItem -> {
                if (treeItem instanceof GroupSortModel) {
                    mergerChild(((GroupSortModel) treeItem).getChildren(), (GroupSortModel) treeItem);
                }
            });
        }
    }
}
