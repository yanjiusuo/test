package com.jd.workflow.console.dto.doc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分组排序
 */
@Data
public class GroupSortModel extends TreeSortModel {

    private List<TreeSortModel> children = new ArrayList<>();
    /**
     * @hidden
     */
    @JsonIgnore
    private Long relatedId;

    //private int  count;

    public int getCount(){
        return allChildMethods().size();
    }

    public List<MethodSortModel> allChildMethods(){
        List<MethodSortModel> result = new ArrayList<>();
        collect(this,result);
        return result;
    }
    public List<GroupSortModel> allChildGroups(){
        List<GroupSortModel> result = new ArrayList<>();
        collectGroup(this,result);
        return result;
    }
    private void collectGroup(GroupSortModel group,List<GroupSortModel> result){
        for (TreeSortModel child : group.children) {
            if(child instanceof GroupSortModel){
                collectGroup((GroupSortModel) child,result);
            }else {

            }
        }
    }
    private void collect(GroupSortModel group,List<MethodSortModel> result){
        if(group ==null || group.children == null) return;
        for (TreeSortModel child : group.children) {
            if(child instanceof GroupSortModel){
                collect((GroupSortModel) child,result);
            }else {
                result.add((MethodSortModel)child);
            }
        }
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getType() {
        return TYPE_GROUP;
    }
    @JsonIgnore
    public List<MethodSortModel> childMethods(){
        if(children != null){
            return children.stream().filter(vs->vs instanceof MethodSortModel).map(vs->(MethodSortModel)vs).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    @JsonIgnore
    public List<GroupSortModel> childGroups(){
        if(children != null){
            return children.stream().filter(vs->vs instanceof GroupSortModel).map(vs->(GroupSortModel)vs).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


}
