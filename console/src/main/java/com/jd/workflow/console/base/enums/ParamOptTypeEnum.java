package com.jd.workflow.console.base.enums;

import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @description: 操作类型 用于前置后置
 * @author: sunchao81
 * @Date: 2024-05-21
 */
@AllArgsConstructor
public enum ParamOptTypeEnum {
    /**
     *
     */
    wlTool("wlTool","物料工具"),

    /**
     *
     */
    other("other","接口及其他"),

    /**
     *
     */
    assertion("assertion","断言")

    ;

    /**
     *
     */
    @Getter
    @Setter
    private String code;
    /**
     *
     */
    @Getter
    @Setter
    private String description;

    /**
     *
     */
    public static List<ParamOptTypeEnum> getOptList(){
        ParamOptTypeEnum[] values = ParamOptTypeEnum.values();
        return Lists.newArrayList(values);
    }

    /**
     *
     * @return
     */
    public static List getPreOptList(){
        List<ParamOptTypeEnum> optList = getOptList();
        optList.removeAll(Lists.newArrayList(ParamOptTypeEnum.assertion));
        optList.removeAll(Lists.newArrayList(ParamOptTypeEnum.other));
        List list = Lists.newArrayList();
        for (ParamOptTypeEnum paramOptTypeEnum : optList) {
            Map map = MapUtil.newHashMap();
            map.put("value",paramOptTypeEnum.getCode());
            map.put("label",paramOptTypeEnum.getDescription());
            list.add(map);
        }
        return list;
    }

    public static List getPostOptList(){
        List<ParamOptTypeEnum> optList = getOptList();
        optList.removeAll(Lists.newArrayList(ParamOptTypeEnum.other));
        List list = Lists.newArrayList();
        for (ParamOptTypeEnum paramOptTypeEnum : optList) {
            Map map = MapUtil.newHashMap();
            map.put("value",paramOptTypeEnum.getCode());
            map.put("label",paramOptTypeEnum.getDescription());
            list.add(map);
        }
        return list;
    }

    @Override
    public String toString() {
        return String.format("{vaue: '%s', label: '%s'}", code,description);
    }

}
