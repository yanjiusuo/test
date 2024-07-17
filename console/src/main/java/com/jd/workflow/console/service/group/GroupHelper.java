package com.jd.workflow.console.service.group;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceMethodGroup;
import com.jd.workflow.console.entity.MethodManage;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupHelper {
    public static <T> Map<Long,List<T>> groupBy(List<T> list, Function<T,Long> classifier){
        if(list.isEmpty()) return Collections.emptyMap();
        return list.stream().collect(Collectors.groupingBy(classifier));
    }

    public static List<InterfaceCountModel> toInterfaceSortModels(List<InterfaceManage> pageResult, Integer type,
                                                                  List<AppInterfaceCount> counts){
        Map<Long, List<AppInterfaceCount>> id2Interfaces = counts.stream().collect(Collectors.groupingBy(AppInterfaceCount::getInterfaceId));

        return pageResult.stream().map(vs->{
            InterfaceCountModel sortModel = new InterfaceCountModel();
            List<AppInterfaceCount> list = id2Interfaces.get(vs.getId());
            if(list != null){
                sortModel.setCount(list.get(0).getCount());
            }
            sortModel.setId(vs.getId());
            sortModel.setName(vs.getName());
            sortModel.setInterfaceType(type);
            sortModel.setAppId(vs.getAppId());
            sortModel.setChildren(null);
            sortModel.setEnName(vs.getServiceCode());
            return sortModel;
        }).collect(Collectors.toList());
    }
    public static List<TreeSortModel> buildGroupSearchTree(List<InterfaceManage> appInterfaces,
                                                           List<InterfaceMethodGroup> groups,
                                                           List<MethodManage> methods,
                                                           String search,
                                                           List<String> tagName
                                                           ){
        if(appInterfaces.isEmpty()) return Collections.emptyList();
        Map<Long,List<InterfaceManage>> id2Interfaces = groupBy(appInterfaces,InterfaceManage::getId);
        Map<Long,List<InterfaceMethodGroup>> id2Groups = groupBy(groups,InterfaceMethodGroup::getInterfaceId);
        Map<Long,List<MethodManage>> id2Methods = groupBy(methods,MethodManage::getInterfaceId);

        return appInterfaces.stream().map(vs->{
            InterfaceSortModel sortModel = new InterfaceSortModel();
            sortModel.setEnName(vs.getServiceCode());
            sortModel.setId(vs.getId());
            sortModel.setName(vs.getName());

            List<InterfaceMethodGroup> interfaceMethodGroups = id2Groups.get(vs.getId());
            List<MethodManage> methodManages = id2Methods.get(vs.getId());
           //tag标签过滤 没有命中tag的 不反回对应文件夹
            if(CollectionUtils.isNotEmpty(tagName)){
                if(CollectionUtils.isNotEmpty(methodManages)){
                    sortModel.getChildren().addAll(methodManages.stream().map(item->toSortModel(item)).collect(Collectors.toList()));
//                    if(interfaceMethodGroups !=null){
//                        sortModel.getChildren().addAll(interfaceMethodGroups.stream().map(item->toGroup(item)).collect(Collectors.toList()));
//                    }
                }else{
                    return new InterfaceSortModel();
                }
            }else{
                if(interfaceMethodGroups !=null){
                    sortModel.getChildren().addAll(interfaceMethodGroups.stream().map(item->toGroup(item)).collect(Collectors.toList()));
                }
                if(CollectionUtils.isNotEmpty(methodManages)){
                    sortModel.getChildren().addAll(methodManages.stream().map(item->toSortModel(item)).collect(Collectors.toList()));
                }
            }

            return sortModel;
        }).filter(vs->{
            return StringUtils.isNotBlank(vs.getName()) && vs.getName().contains(search) || StringUtils.isNotBlank(vs.getEnName()) && vs.getEnName().contains(search)
                    || !vs.getChildren().isEmpty();
        }).collect(Collectors.toList());

    }
    private static GroupSortModel toGroup(InterfaceMethodGroup group){
        GroupSortModel sortModel = new GroupSortModel();
        sortModel.setId(group.getId());
        sortModel.setName(group.getName());
        return sortModel;
    }

    /**
     *
     * @param method
     * @return
     */
    public static TreeSortModel toSortModel(MethodManage method){
        MethodSortModel sortModel = new MethodSortModel();
        sortModel.setId(method.getId());
        sortModel.setName(method.getName());
        sortModel.setEnName(method.getMethodCode());
        sortModel.setPath(method.getPath());
        sortModel.setInterfaceType(method.getType());

        return sortModel;
    }
}
