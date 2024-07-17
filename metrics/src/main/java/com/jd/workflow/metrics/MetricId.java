package com.jd.workflow.metrics;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MetricId {
    String id;
    List<Label> labels = new ArrayList<>();
    public MetricId id(String id){
        this.id = id;
        return this;
    }
    public MetricId labels(String key,String value){
        labels.add(Label.builder().key(key).value(value).build());
        return this;
    }
    public MetricId labelKeys(String ...keys){
        for (String key : keys) {
            labels.add(Label.builder().key(key).build());
        }
        return this;
    }
    public List<Label> labelWithValues(){
        return labels.stream().filter(vs-> !StringUtils.isBlank(vs.getValue())).collect(Collectors.toList());
    }

    public String[] labelKeys(){
        return labels.stream().filter(vs-> StringUtils.isBlank(vs.getValue())).map(vs->vs.getKey()).collect(Collectors.toList()).toArray(new String[0]);
    }
}
