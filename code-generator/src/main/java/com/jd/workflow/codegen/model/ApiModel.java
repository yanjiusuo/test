package com.jd.workflow.codegen.model;

import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;

import java.util.*;

@Data
public class ApiModel {
    /**
     * 分组名称
     */
    String name;
    String desc;
    String className;
    Set<String> imports = new HashSet<>();
    Map<String,Set<String>> jsImports = new HashMap<>();
    List<MethodModel> methods = new ArrayList<>();
    String savedPath;


    public String getPkgName(){
        if(className == null) return null;
        if(className.indexOf('.')>0){
            return className.substring(0,className.lastIndexOf('.'));
        }
        return null;

    }
    public String getName(){
        return StringHelper.lastPart(className,'.');
    }
    public void addMethod(MethodModel method){
        method.setApiModel(this);
       imports.addAll(method.getImports());
       methods.add(method);
    }
    public String getOriginalName(){
        String str = StringHelper.camelCase(name,'-',false);
        //str = StringHelper.camelCase(str,'_',false);
        if(str.endsWith("controller") || str.endsWith("Controller")){
            str = str.replace("controller","").replace("Controller","");
            return str;
        }

        return name;
    }
    public Map<String,Set<String>> getJsImports(){
        if(!jsImports.isEmpty()) return jsImports;
        Map<String,Set<String>> result = new HashMap<>();
        for (MethodModel method : methods) {
            Map<String, Set<String>> methodImports = method.getJsImports();
            for (Map.Entry<String, Set<String>> entry : methodImports.entrySet()) {
                result.computeIfAbsent(entry.getKey(),vs->new HashSet<>()).addAll(entry.getValue());
            }
        }
        jsImports = result;
        return result;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiModel apiModel = (ApiModel) o;
        return Objects.equals(className, apiModel.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }
    public String toString(){
        return className;
    }
}
