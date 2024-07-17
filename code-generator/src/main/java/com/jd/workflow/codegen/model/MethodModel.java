package com.jd.workflow.codegen.model;

import com.jd.workflow.codegen.model.type.*;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class MethodModel {
    String methodName;
    String desc;
    String url;
    String httpMethod;
    List<Param> inputs = new ArrayList<>();
    Param returnType;
    private ApiModel apiModel;

    Map<String,String> className2SavedPath = new HashMap<>();

    public Set<String> getImports(){
        Set<String> result = new HashSet<>();
        for (Param input : inputs) {
            if(input.getType().getClassName().startsWith("java.lang.")) continue;
            result.add(input.getType().getClassName());
        }
        if(returnType !=null){
            result.add(returnType.getType().getClassName());
        }
        return result;
    }

    public static void getImport(String baseSavedPath, IClassModel type,Map<String,Set<String>> result,Map<String,String> className2SavedPath){
        if(type instanceof EntityClassModel){
            if(((EntityClassModel) type).isHidden()) return;
            String savedPath = className2SavedPath.get(type.getClassName());
            if(savedPath == null){
                throw new StdException("type.err_save_path_is_not_allow_null");
            }
            if(baseSavedPath.equals(savedPath)) return;
            String relativePath = StringHelper.relativizePath(baseSavedPath,savedPath );
            if(!relativePath.startsWith("/")){
                relativePath = "./"+relativePath;
            }
            if(relativePath.endsWith(".ts")){
                relativePath = relativePath.substring(0,relativePath.length()- ".ts".length());
            }
            Set<String> set = result.computeIfAbsent(relativePath, vs -> new HashSet<>());
            set.add(type.getName());
            for (IClassModel genericType : type.getGenericTypes()) {
                getImport(baseSavedPath,genericType,result,className2SavedPath);
            }


        }else if(type instanceof ArrayClassModel){
            List<IType> children = ((ArrayClassModel) type).getChildren();
            for (IType child : children) {
                if(child instanceof IClassModel){
                    getImport(baseSavedPath, (IClassModel) child,result,className2SavedPath);
                }

            }
        }else if(type instanceof ReferenceClassModel){
            for (IClassModel genericType : type.getGenericTypes()) {
                getImport(baseSavedPath,genericType,result,className2SavedPath);
            }
        }

    }
    /**
     * 获取js的导入对象列表
     * @return
     */
    public Map<String,Set<String>> getJsImports(){
        Map<String,Set<String>> result = new HashMap<>();
        if(returnType !=null){
            getImport(apiModel.getSavedPath(),returnType.getType(),result,className2SavedPath);
            /*if(classModel !=null){
                Set<String> paths = result.computeIfAbsent(classModel.getSavedPath(), vs -> new HashSet<>());
                paths.add(classModel.getName());
            }*/


        }
        for (Param input : inputs) {
           getImport(apiModel.getSavedPath(),input.getType(),result,className2SavedPath);
           /*if(type !=null){
               Set<String> paths = result.computeIfAbsent(type.getSavedPath(), vs -> new HashSet<>());
               paths.add(type.getName());
           }*/

        }
        return result;
    }
    public String getInputString(){
        return inputs.stream().map(vs->{
            return vs.getType().getReference()+" " + vs.getName();
        }).collect(Collectors.joining(","));
    }

    public List<Param> getInputs() {
        return inputs;
    }

    public void setInputs(List<Param> inputs) {
        this.inputs = inputs;
    }
}
