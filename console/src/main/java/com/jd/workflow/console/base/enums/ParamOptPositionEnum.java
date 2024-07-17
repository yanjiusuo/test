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
public enum ParamOptPositionEnum {
    /**
     *
     */
    preOpt("10","前置"),

    /**
     *
     */
    other("20","其他"),
    /**
     *
     */
    postOpt("30","后置")
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
    public static List<ParamOptPositionEnum> getOptList(){
        ParamOptPositionEnum[] values = ParamOptPositionEnum.values();
        return Lists.newArrayList(values);
    }


    public static List getPostOptList(){
        List<ParamOptPositionEnum> optList = getOptList();
        List list = Lists.newArrayList();
        for (ParamOptPositionEnum paramOptTypeEnum : optList) {
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
