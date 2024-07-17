package com.jd.workflow.flow.parser;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.junit.Test;

import java.util.*;

public class StepTopoGraphTests extends BaseTestCase {
    /**
        3--5

     1
        3--5

     2  7

     */
    public List<StepTopoGraph.Node> nodes1(){
        StepTopoGraph.Node node1 = newNode("1");
        StepTopoGraph.Node node2 = newNode("2");
        StepTopoGraph.Node node3 = newNode("3");
        StepTopoGraph.Node node33 = newNode("3");
        StepTopoGraph.Node node55 = newNode("5");
        StepTopoGraph.Node node5 = newNode("5");
        StepTopoGraph.Node node7 = newNode("7");

        Map<Integer,List<StepTopoGraph.Node>> node1Children = new LinkedHashMap<>();

        List<StepTopoGraph.Node> node1ChildList = new ArrayList<>();
        node1ChildList.add(node3);
        node1ChildList.add(node5);

        node1Children.put(1,node1ChildList);

        List<StepTopoGraph.Node> node11ChildList = new ArrayList<>();
        node11ChildList.add(node33);
        node11ChildList.add(node55);
        node1Children.put(2,node11ChildList);



        node1.setChildren(node1Children);


        Map<Integer,List<StepTopoGraph.Node>> node2Children = new LinkedHashMap<>();

        List<StepTopoGraph.Node> node2ChildList = new ArrayList<>();
        node2ChildList.add(node7);
        node2Children.put(1,node2ChildList);
        node2.setChildren(node2Children);

        List<StepTopoGraph.Node> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);
        return nodes;

    }

    /**
     *  1
        3
        3
     * @return
     */
    public List<StepTopoGraph.Node> nodes2(){
        StepTopoGraph.Node node1 = newNode("1");
        StepTopoGraph.Node node2 = newNode("2");
        StepTopoGraph.Node node3 = newNode("2");


        List<StepTopoGraph.Node> nodes = new ArrayList<>();
        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        return nodes;

    }
    private StepTopoGraph.Node newNode(String nodeId){
        StepTopoGraph.Node node = new StepTopoGraph.Node();
        node.setNodeId(nodeId);

        return node;
    }

     @Test public void testBranch(){
        StepTopoGraph graph = new StepTopoGraph();
        graph.setNodes(nodes1());
        Map<String, Set<String>> map = graph.validateUnique();
        System.out.println(JsonUtils.toJSONString(map));
    }

     @Test public void testDuplicate(){
        try {
            StepTopoGraph graph = new StepTopoGraph();
            graph.setNodes(nodes2());
            Map<String, Set<String>> map = graph.validateUnique();
            System.out.println(JsonUtils.toJSONString(map));
        }catch (Exception e){
            assertTrue(e instanceof StepParseException);
        }

    }
}
