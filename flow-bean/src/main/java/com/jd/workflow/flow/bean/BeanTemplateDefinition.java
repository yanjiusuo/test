package com.jd.workflow.flow.bean;

import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BeanTemplateDefinition {
    /**
     * 初始化配置类
     */
    String initConfigClass;
    String type;
    List<MethodMetadata> methods=new ArrayList<>();

    public MethodMetadata getMethod(String methodName,String paramCount){
        for (MethodMetadata method : methods) {
            if(methodName.equals(method.getMethodName())
             && Variant.valueOf(paramCount).toInt() == method.getInput().size()
            ){
                return method;
            }
        }
        return null;
    }

}
