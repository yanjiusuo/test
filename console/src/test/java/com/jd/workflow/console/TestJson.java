package com.jd.workflow.console;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import com.jayway.jsonpath.JsonPath;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.JsonPathUtil;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.jsf.input.JsfOutput;
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import titan.profiler.shade.com.google.common.collect.Lists;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-23
 */
public class TestJson {


    public static void main(String[] args) {
//        test1();
        try {
//            Object o = test2();
            Map<String, List<Tuple2>> map = parseJsonToMap(getJson());
            System.out.println();
//            System.out.println(JSONUtil.toJsonStr(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Object test2() throws IOException {
        Map<String, Object> resultMap1 = JsonPath.read(getJson(), "$");
        Map<String, Object> jsonPathMap = extractLeafNodes(resultMap1, "");
        Object o = jsonPathMap.get("$.body.resultInfo.domainCollectionDTOs[17].childList[0].info.domainMap.area.areaList.v");
        return o;
    }

    /**
     *
     * @param jsonString
     * @return
     */
    public static Map<String, List<Tuple2>> parseJsonToMap(String jsonString){
        List<Tuple3<String, String, Object>> tuple3s = JsonPathUtil.parseJsonToList(jsonString, "");
        Map<String, List<Tuple2>> map = Maps.newHashMap();
        for (Tuple3<String, String, Object> tuple3 : tuple3s) {
            if("childEmpty".equals(tuple3.getV1())){
                System.out.println();
            }
            if(CollectionUtils.isEmpty(map.get(tuple3.getV1()))){
                Tuple2 tuple2 = new Tuple2(tuple3.getV2(), tuple3.getV3());
                List<Tuple2> list = new ArrayList<>();
                list.add(tuple2);
                map.put(tuple3.getV1(), list);
            }else {
                map.get(tuple3.getV1()).add(new Tuple2(tuple3.getV2(),tuple3.getV3()));
            }
        }
        return map;
    }

    /**
     * 解析JSON字符串为List,下一步处理Map的key相同时覆盖策略
     *
     * @param jsonString JSON字符串
     * @return 解析后的Map
     */
    public static List<Tuple3<String,String,Object>> parseJsonToList(String jsonString, String path) {
        Map<String, Object> map = JSONUtil.parseObj(jsonString);
        List<Tuple3<String,String,Object>> list = com.google.common.collect.Lists.newArrayList();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String keyPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                list.addAll(parseJsonToList(JSONUtil.toJsonStr(value),keyPath));
            }else if (value instanceof List) {
                for (int i = 0; i < ((List<?>) value).size(); i++) {
                    if(((List<?>) value).get(i) instanceof Map){
                        list.addAll(parseJsonToList(JSONUtil.toJsonStr(((List<?>) value).get(i)),keyPath));
                    }
                }
            }
            list.add(new Tuple3(entry.getKey(),"$."+keyPath, value));
        }
        return list;
    }

    /**
     * 解析到叶子节点
     * jsonPath 使用的是 net.minidev
     * @param map
     * @param path
     */
    public static Map<String,Object > extractLeafNodes(Map<String, Object> map, String path) {
        Map<String,Object > leafNodes = MapUtil.newHashMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String keyPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                leafNodes.putAll(extractLeafNodes((Map<String, Object>) value, keyPath));
            } else if (value instanceof List) {
                for (int i = 0; i < ((List<?>) value).size(); i++) {
                    if(((List<?>) value).get(i) instanceof String || ((List<?>) value).get(i) instanceof Integer){
                        leafNodes.put("$."+keyPath,value);
                    }else {
                        try{
                            Map<String, Object> temp = (Map<String, Object>) ((List<?>) value).get(i);
                            leafNodes.putAll(extractLeafNodes(temp, keyPath + "[" + i + "]"));
                        }catch (Exception e){
                            System.out.println(((List<?>) value).get(i));
                        }
                    }
                }
            } else {
                leafNodes.put("$."+keyPath,value);
            }
        }
        return leafNodes;
    }

    /**
     *
     */
    private static void test1() {
        try {
            CommonResult<JsfOutputExt> bean = JSONUtil.toBean(getJson(), new TypeReference<CommonResult<JsfOutputExt>>() {
            },true);
            JsfOutput output = new JsfOutput();
            BeanUtils.copyProperties(bean.getData(), output);
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * scanner.useDelimiter("\\Z"); //json对象
     * @return
     * @throws IOException
     */
    static String getJson() throws IOException {
        File jsonFile = new File("console/src/test/java/com/jd/workflow/console/testCase1.json");
        FileReader reader = new FileReader(jsonFile);
        List<String> allObjects = Lists.newArrayList();
        try (Scanner scanner = new Scanner(reader)) {
            scanner.useDelimiter("\\Z");
            while (scanner.hasNext()) {
                String chunk = scanner.next();
                List<String> objectsInChunk = Lists.newArrayList(chunk);
                allObjects.addAll(objectsInChunk);
            }
        }
        reader.close();
        return allObjects.get(0);
    }
}

