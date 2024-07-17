package com.jd.workflow.codegen;

import com.jd.workflow.codegen.model.FieldModel;
import com.jd.workflow.codegen.model.MethodModel;
import com.jd.workflow.codegen.model.type.EntityClassModel;
import com.jd.workflow.codegen.model.type.IClassModel;

import java.util.*;

public class GroupModels {
    Collection<EntityClassModel> models;
    Map<String, Set<String>> jsImports;
    Map<String,String> className2SavedPath = new HashMap<>();

    public String getSavedPath(){
        if(models.isEmpty()) return null;
        EntityClassModel classModel = models.iterator().next();
        return className2SavedPath.get(classModel.getClassName());
    }
    public Map<String, Set<String>> getJsImports(){
        if(jsImports != null) return jsImports;
        if(models.isEmpty()) return Collections.emptyMap();
        Map<String,Set<String>> result = new HashMap<>();
        String savedPath = getSavedPath();
        for (EntityClassModel model : models) {
            for (FieldModel field : model.getFields()) {
                if(field.getType() instanceof IClassModel){
                    MethodModel.getImport(savedPath,(IClassModel) field.getType(),result,className2SavedPath);
                }
            }
            for (int i = 0; i <model.getGenericTypes().size(); i++) {
                IClassModel genericType = model.getGenericTypes().get(i);
                if(model.getFormalParams().size()-1 >= i
                 && model.getFormalParams().get(i) != null
                ){
                    continue;
                }
                MethodModel.getImport(savedPath, genericType,result,className2SavedPath);
            }

        }
        jsImports = result;
        return result;
    }

    public GroupModels(Collection<EntityClassModel> models) {
        this.models = models;
    }

    public Collection<EntityClassModel> getModels() {
        return models;
    }


    public Map<String, String> getClassName2SavedPath() {
        return className2SavedPath;
    }

    public void setClassName2SavedPath(Map<String, String> className2SavedPath) {
        this.className2SavedPath = className2SavedPath;
    }
}
