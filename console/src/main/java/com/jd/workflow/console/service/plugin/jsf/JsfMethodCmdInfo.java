package com.jd.workflow.console.service.plugin.jsf;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
public class JsfMethodCmdInfo {
    String error;
    String methodName;
    String returnType;
    List<Map<String,Object>> parameters;
    @JSONField(unwrapped = true, serialize = false, deserialize = false)
    Map<String,Object> extAttrs;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtAttrs() {
        return extAttrs;
    }

    public void addExtraAttr(String key, Object value) {
        if (extAttrs == null) {
            extAttrs = new HashMap<>();
        }
        extAttrs.put(key, value);
    }

    @JsonAnySetter
    public void setExtAttrs(Map<String, Object> extAttrs) {
        this.extAttrs = extAttrs;
    }

}
