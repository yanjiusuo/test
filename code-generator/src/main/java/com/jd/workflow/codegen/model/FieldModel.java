package com.jd.workflow.codegen.model;

import com.jd.workflow.codegen.model.type.IClassModel;
import com.jd.workflow.codegen.model.type.IType;
import com.jd.workflow.codegen.model.type.SimpleClassModel;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
public class FieldModel {
    String name;
    boolean required;
    String desc;
    IType type;

    public void setName(String name){
        this.name = name;
    }

    public Set<String> getImports(){
        if(!(type instanceof IClassModel)
         || type instanceof SimpleClassModel
        ) return Collections.emptySet();
        IClassModel model = (IClassModel) type;
        Set<String> result = new HashSet<>();
        result.add(model.getClassName());
        for (IClassModel genericType : model.getGenericTypes()) {
            result.add(genericType.getClassName());
        }
        return result;
    }
    public String getJsType(){
        if(type instanceof IClassModel){
            return ((IClassModel) type).getJsType();
        }
        return ((IType)type).getTypeName();
    }
    public String getReference(){
        String result = null;
        if(type.isTypeVariable()) {
          result=  type.getTypeName();
        } else {
            result = ((IClassModel)type).getReference();
        }
        if(StringUtils.isEmpty(result)) {
            return "";
        }
        return result;
    }
    public String getGetMethodName(){
        if(required && "java.lang.Boolean".equals(type.getTypeName())){
            return "is"+StringHelper.capitalize(name);
        }
        return "get"+StringHelper.capitalize(name);
    }
    public String getSetMethodName(){
        return "set"+StringHelper.capitalize(name);
    }
}
