package com.jd.workflow.console.base.enums;

import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @description:断言操作符
 * @author: sunchao81
 * @Date: 2024-05-21
 */
@AllArgsConstructor
public enum AssertionOptEnum {
    /**
     *
     */
    lt("lt","小于","<"),
    /**
     *
     */
    le("le","小于等于","<="),
    /**
     *
     */
    eq("eq","等于","="),
    /**
     *
     */
    ne("ne","不等于","<>"),
    /**
     *
     */
    ge("ge","大于等于",">="),
    /**
     *
     */
    gt("gt","大于",">")

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

    @Getter
    @Setter
    private String symbol;

    /**
     *
     */
    public static List getOptList(){
        AssertionOptEnum[] values = AssertionOptEnum.values();
        List list = Lists.newArrayList();
        for (AssertionOptEnum assertionOptEnum : values) {
            Map map = MapUtil.newHashMap();
            map.put("value",assertionOptEnum.getCode());
            map.put("label",assertionOptEnum.getDescription());
            list.add(map);
        }
        return list;
    }

    @Override
    public String toString() {
        return String.format("{vaue: '%s', label: '%s'}", code,description);
    }

    /**
     *
     * @param code
     * @return
     */
    public static AssertionOptEnum getByCode(String code){
        for (AssertionOptEnum value : AssertionOptEnum.values()) {
            if(value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }
    
}
