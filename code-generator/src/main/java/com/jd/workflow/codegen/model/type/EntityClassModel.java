package com.jd.workflow.codegen.model.type;


import com.jd.workflow.codegen.model.ApiModel;
import com.jd.workflow.codegen.model.FieldModel;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class EntityClassModel extends IClassModel {
    IClassModel superClass;
    List<String> interfaces = new LinkedList<>();
    Set<String> imports = new HashSet<>();
    List<FieldModel> fields = new ArrayList<>();
    boolean hidden;
    private ApiModel apiModel;

    public void addField(FieldModel field){
        fields.add(field);
        imports.addAll(field.getImports());
    }
    @Override
    public IClassModel clone() {
        EntityClassModel classModel = new EntityClassModel();
        classModel.setClassName(getClassName());
        classModel.setFormalParams(new ArrayList<>(formalParams));
        classModel.setGenericTypes(new ArrayList<>(genericTypes));
        for (FieldModel field : fields) {
            classModel.addField(field);
        }
        return classModel;
    }
    public FieldModel getField(String name){
        for (FieldModel field : fields) {
            if(name.equals(field.getName())) return field;
        }
        return null;
    }
    public ApiModel getApiModel() {
        return apiModel;
    }

    public void setApiModel(ApiModel apiModel) {
        this.apiModel = apiModel;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @Override
    public String getJsType() {
        String name = StringHelper.lastPart(getClassName(),'.');
        if(genericTypes.isEmpty()){
            return name;
        }
        name+='<';
        for (IClassModel genericType : genericTypes) {
            name+= genericType.getJsType()+",";
        }
        name = name.substring(0,name.length() - 1);
        name+=">";
        return name;
    }
}
