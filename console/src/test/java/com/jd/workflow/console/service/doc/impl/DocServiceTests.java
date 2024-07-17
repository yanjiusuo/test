package com.jd.workflow.console.service.doc.impl;

import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.entity.MethodManage;

import java.util.ArrayList;
import java.util.List;

public class DocServiceTests {
    static MethodManage newMethod(Long id){
        com.jd.workflow.console.entity.MethodManage methodManage = new MethodManage();
        methodManage.setId(id);
        return methodManage;
    }
    static GroupSortModel newGroup(Long groupId){
        GroupSortModel sortModel = new GroupSortModel();
        sortModel.setId(groupId);
        return sortModel;
    }
    static MethodSortModel newMethodModel(Long methodId){
        MethodSortModel sortModel = new MethodSortModel();
        sortModel.setId(methodId);
        return sortModel;
    }
    public static void main(String[] args) {
        DocReportServiceImpl service = new DocReportServiceImpl();
        GroupSortModel groupSortModel = newGroup(1L);
        groupSortModel.getChildren().add(newMethodModel(1L));
        groupSortModel.getChildren().add(newMethodModel(2L));
        groupSortModel.getChildren().add(newMethodModel(3L));

        List<MethodManage> methods = new ArrayList<>();
        methods.add(newMethod(1L));
        methods.add(newMethod(2L));
        methods.add(newMethod(4L));

        final boolean result = service.isAllMethodInGroup(groupSortModel, methods);
        System.out.println(result);
    }
}
