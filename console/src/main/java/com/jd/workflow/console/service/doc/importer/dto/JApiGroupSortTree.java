package com.jd.workflow.console.service.doc.importer.dto;

import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JApiGroupSortTree {
    List<ApiInfo> apiArrForSort;
    List<ApiGroup> apiGroupArrForSort;
    List<ApiGroupTreeNode> apiGroupSortTree;
    Long sortTreeVersion;
    @Data
    public static class ApiGroup{
        String groupName;
        Long groupID;
    }
    @Data
    public static class ApiInfo{
        Long apiID;
        String apiName;
        Integer apiStatus;
        String apiURI;
        String mockID;
    }
    // type=1表示group,2为method
    @Data
    public static class ApiGroupTreeNode{
        Long id;
        Integer type;
        List<ApiGroupTreeNode> sub;
        public TreeSortModel toTree(Map<Long, Long> japiGroupId2GroupId, Map<Long, Long> japiId2newId){
            if(type == 2){
                MethodSortModel sortModel = new MethodSortModel();
                sortModel.setId(japiId2newId.get(id));
                return sortModel;
            }else{
                GroupSortModel sortModel = new GroupSortModel();
                sortModel.setId(japiGroupId2GroupId.get(id));
                if(sub != null){
                    for (ApiGroupTreeNode child : sub) {
                        sortModel.getChildren().add(child.toTree(japiGroupId2GroupId, japiId2newId));
                    }
                }
                return sortModel;
            }
        }
    }
    public MethodGroupTreeModel toGroupTreeModel(Map<Long, Long> japiGroupId2GroupId, Map<Long, Long> japiId2newId){
        MethodGroupTreeModel treeModel = new MethodGroupTreeModel();
        for (ApiGroupTreeNode apiGroupTreeNode : getApiGroupSortTree()) {
            treeModel.getTreeItems().add(apiGroupTreeNode.toTree(japiGroupId2GroupId, japiId2newId));
        }
        return treeModel;
    }
}
