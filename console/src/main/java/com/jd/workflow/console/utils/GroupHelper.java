package com.jd.workflow.console.utils;

import com.jd.workflow.console.dto.doc.GroupHttpData;

import java.util.List;

public class GroupHelper {
    public static <T> void  addGroupData(List<GroupHttpData<T>> list, GroupHttpData newGroup){
        GroupHttpData exist = null;
        for (GroupHttpData data : list) {
            if(data.getGroupDesc().equalsIgnoreCase(newGroup.getGroupDesc())){
                exist = data;
            }
        }
        if(exist == null){
            list.add(newGroup);
        }else{
            exist.getHttpData().addAll(newGroup.getHttpData());
        }
    }
}
