package com.jd.workflow.flow.parser;

import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.exception.StepParseException;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 步骤拓扑图
 * @author wangjingfang3
 */
@Data
public class StepTopoGraph {
    List<StepDefinition> definitions;
    List<Node> nodes;
    public void init(){
        List<Node> nodes = new ArrayList<>();
        for (StepDefinition definition : definitions) {
            nodes.add(Node.from(definition));
        }
        this.nodes = nodes;

    }

    /**
     * 校验node在不同分支下是否唯一
     * 基本思路：遍历每个节点，判断当前节点在父节点所有子节点里是否存在
     *
     * @return
     */
    public Map<String,Set<String>> validateUnique(){
        Map<String,Set<String>> parentPath2Ids = new HashMap<>();
        validateUnique(nodes,parentPath2Ids);
        return parentPath2Ids;
    }
    private void validateUnique(List<Node> nodes,Map<String/* nodePath */,Set<String> /*nodeIds*/> parentPath2Ids){
        for (Node node : nodes) {
            validateUnique(node,parentPath2Ids);
        }
    }

    /**
     计算出每个node前驱节点列表，然后判断是否在前驱里面
     * @param node
     * @param map
     */
    private void validateUnique(Node node,Map<String/* nodePath */,Set<String> /*nodeIds*/> map){
        map.computeIfAbsent(node.parentPath(), vs->{
            return new HashSet<>();
        });
        if(map.get(node.parentPath()).contains(node.getNodeId())){
            throw new StepParseException("workflow.err_found_duplicate_stepId").id(node.getNodeId());
        }
        addNode(node,map);
        for (Map.Entry<Integer, List<Node>> entry : node.getChildren().entrySet()) {
            validateUnique(entry.getValue(),map);
        }
    }
    private void addNode(Node node,Map<String/* nodePath */,Set<String> /*nodeIds*/> map){
        String path = node.parentPath();
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if(path.startsWith(entry.getKey())){
                entry.getValue().add(node.getNodeId());
            }
        }
    }



    @Data
    public static class Node{
        String nodeId;
        Integer index;
        StepDefinition definition;
        Node parentNode;
        String parentPath;
        public String parentPath(){
            if(this.parentPath != null){
                return parentPath;
            }
            if(parentNode == null) {
                this.parentPath = "";
                return this.parentPath;
            }
            if(index != null){
                this.parentPath = parentNode.parentPath()+"_"+parentNode.getNodeId()+"_"+index;
            }else{
                this.parentPath = parentNode.parentPath()+"_"+parentNode.getNodeId();
            }

            return  this.parentPath;

        }

        Map<Integer, List<Node>> children = new HashMap<>();
        public static Node from(StepDefinition definition){
            Node node = new Node();
            node.definition = definition;
            node.nodeId = definition.getId();
            Map<Integer, List<Node>> children  = new HashMap<>();
            Map<Integer, List<StepDefinition>> definitionChildren  = definition.getChildren();

            for (Map.Entry<Integer, List<StepDefinition>> entry : definitionChildren.entrySet()) {
                List<Node> nodes = new ArrayList<>();
                for (StepDefinition stepDefinition : entry.getValue()) {
                    nodes.add(from(stepDefinition));
                }
                children.put(entry.getKey(),nodes);
            }
            node.setChildren(children);
            return node;
        }
        public void setChildren(Map<Integer, List<Node>> children){
            this.children = children;
            for (Map.Entry<Integer, List<Node>> entry : children.entrySet()) {
                for (Node node : entry.getValue()) {
                    node.parentNode = this;
                    node.index = entry.getKey();
                }
            }
        }

        @Override
        public String toString() {
            return "Node{" +
                    "nodeId='" + nodeId + '\'' +
                    '}';
        }
    }
}
