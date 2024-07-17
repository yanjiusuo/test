package com.jd.workflow.jsf.generic;

import com.jd.jsf.gd.filter.AbstractFilter;
import com.jd.jsf.gd.util.JSFContext;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：parent
 * 类 名 称：JsfConsumerConfig
 * 类 描 述：TODO
 * 创建时间：2022-06-28 10:15
 * 创 建 人：wangxiaofei8
 */
@Data
public class JsfConsumerConfig {

    private String interfaceClassName;
    private String serialization;
    private String protocol;
    private String alias;
    private Integer timeout;
    protected boolean generic = true;
    private List<JsfParameterConfig> jsfParameterConfigs = new ArrayList<>();
    private String index;

    public Map<String, String> toJsfParameterConfigMap(Map<String, String> parameters) {
        if (jsfParameterConfigs == null || jsfParameterConfigs.isEmpty()) {
            return parameters;
        }
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        for (JsfParameterConfig registry : jsfParameterConfigs) {
            if (registry.isHide()) {
                JSFContext.putGlobalVal('.' + registry.getKey(), registry.getValue());
                parameters.put('.' + registry.getKey(), registry.getValue());
            } else {
                JSFContext.putGlobalVal(registry.getKey(), registry.getValue());
            }
        }
        return parameters;
    }

    public String getConsumerName() {
        return this.interfaceClassName + "#"+ this.alias;
    }


}
