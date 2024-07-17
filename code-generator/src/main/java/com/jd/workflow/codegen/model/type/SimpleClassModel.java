package com.jd.workflow.codegen.model.type;

import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class SimpleClassModel extends IClassModel {


    @Override
    public IClassModel clone() {
        SimpleClassModel classModel = new SimpleClassModel();
        classModel.setClassName(getClassName());
        classModel.setFormalParams(new ArrayList<>(formalParams));
        classModel.setGenericTypes(new ArrayList<>(genericTypes));
        return classModel;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }
}
