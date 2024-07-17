package com.jd.workflow.codegen.model.type;

import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ArrayClassModel extends IClassModel {
    List<IType> children = new ArrayList<>();

    @Override
    public IClassModel clone() {
        ArrayClassModel classModel = new ArrayClassModel();
        classModel.setClassName(getClassName());
        classModel.setFormalParams(new ArrayList<>(formalParams));
        classModel.setGenericTypes(new ArrayList<>(genericTypes));
        return classModel;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public String getJsType() {
        if(children.isEmpty()){
            return "[]";
        }else if(children.size() == 1){
            return children.get(0).getJsType()+"[]";
        }else{
            return "any[]";
        }

    }
    public String getReference(){
        String name = "List";

        name+='<';
        for (IType genericType : children) {
            name+= genericType.getReference()+",";
        }
        name = name.substring(0,name.length() - 1);
        name+=">";
        return name;
    }
    public String getFullReference(){
        String name = "java.util.List";
        if(children.isEmpty()){
            return name;
        }
        name+='<';
        for (IType genericType : children) {
            if(genericType instanceof IClassModel){
                name+= ((IClassModel)genericType).getFullReference()+",";
            }else{
                name += genericType.getReference()+",";
            }

        }
        name = name.substring(0,name.length() - 1);
        name+=">";
        return name;
    }
/*
    @Override
    public String getTypeName() {
        return List.class.getSimpleName();
    }*/
}
