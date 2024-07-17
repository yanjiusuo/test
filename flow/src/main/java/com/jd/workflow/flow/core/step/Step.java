package com.jd.workflow.flow.core.step;

import com.jd.workflow.flow.core.attr.AttributeSupport;
import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.input.Input;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.retry.ExecContext;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class  Step extends AttributeSupport {
    String id;
    String key;
    Input input;
    Output output;
    StepContext context;
    boolean invoked = false;
    boolean success = true;
    Long costTime;
    ExecContext execContext;
    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("id",id);
        map.put("key",key);
        map.put("input",input);
        map.put("output",output);
        map.put("invoked",invoked);
        map.put("success",success);
        map.put("costTime",costTime);
        if(!attrs.isEmpty()){
            map.put("attrs",attrs);
        }
        return map;
    }
}
