package com.jd.workflow.console.base;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Sets;
import com.jd.common.util.StringUtils;
import groovy.lang.Tuple3;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-24
 */
public class JsonPathUtil {

    /**
     * 解析JSON字符串为List,下一步处理Map的key相同时覆盖策略
     * @param jsonString
     * @param path
     * @return key、jsonPath、value
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
                        list.addAll(parseJsonToList(JSONUtil.toJsonStr(((List<?>) value).get(i)),keyPath+ "[" + i + "]"));
                    }else {
                        list.add(new Tuple3(entry.getKey(),"$."+keyPath+ "[" + i + "]", value));
                    }
                }
            }
            list.add(new Tuple3(entry.getKey(),"$."+keyPath, value));
        }
        return list;
    }
}
