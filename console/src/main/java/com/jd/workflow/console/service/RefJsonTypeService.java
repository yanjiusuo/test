package com.jd.workflow.console.service;

import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.service.model.IApiModelService;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.xml.schema.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RefJsonTypeService {
   // public static List<String> needCopyAttrs = Arrays.asList("desc","required","value","name","hidden","mock","className");
    public static List<String> needCopyAttrs = Arrays.asList("desc","required","value","name","hidden","mock","className");
    @Autowired
    IApiModelService apiModelService;

    public boolean initRefJsonType(JsonType jsonType,Long appId){
        List<String> refNames = collectRefNames(jsonType);
        if(!refNames.isEmpty()){
            List<ApiModel> apiModels = apiModelService.queryModels(refNames,appId);
            Map<String, JsonType> refMap = apiModels.stream().collect(Collectors.toMap(ApiModel::getName, ApiModel::getContent));
            fillJsonType(jsonType,refMap,new ArrayList<>());
            return true;
        }
        return false;
    }

    public boolean initMethodRefInfos(List<? extends IMethodInfo> methods,Long appId){
        if(methods.isEmpty() || appId == null) return false;
        List<String> allRefNames = new ArrayList<>();
        List<HttpMethodModel> httpMethods = new ArrayList<>();
        List<JsfStepMetadata> jsfMethods = new ArrayList<>();
        boolean hasRef = false;
        for (IMethodInfo method : methods) {
            if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                HttpMethodModel content = (HttpMethodModel) method.getContentObject();
               List<String> refNames = collectHttpRefs(content);
                if(!refNames.isEmpty()){
                    allRefNames.addAll(refNames);
                    httpMethods.add(content);
                    List<ApiModel> apiModels = apiModelService.queryModels(refNames,appId);
                    Map<String, JsonType> refMap = apiModels.stream().collect(Collectors.toMap(ApiModel::getName, ApiModel::getContent,(i1,i2)->i1));
                    initHttpRef(content,refMap);
                    hasRef = true;
                }
            }else if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                JsfStepMetadata content = (JsfStepMetadata) method.getContentObject();
                List<String> refNames = collectJsfRefs(content);
                if(!refNames.isEmpty()){
                    allRefNames.addAll(refNames);
                    jsfMethods.add(content);
                    hasRef = true;
                }
            }
            if(allRefNames.isEmpty()) return false;
            List<ApiModel> apiModels = apiModelService.queryModels(allRefNames,appId);
            Map<String, JsonType> refMap = apiModels.stream().collect(Collectors.toMap(ApiModel::getName, ApiModel::getContent,(i1,i2)->i1));
            for (HttpMethodModel httpMethod : httpMethods) {
                initHttpRef(httpMethod,refMap);
            }
            for (JsfStepMetadata jsfMethod : jsfMethods) {
                initJsfRef(jsfMethod,refMap);
            }

            return hasRef;
        }
        return false;
    }
    public boolean initMethodRefInfos(IMethodInfo method,Long appId){
        return initMethodRefInfos(Collections.singletonList(method),appId);
    }

    public void initHttpRef(HttpMethodModel method,Map<String,JsonType> refMap){
        if(method.getInput() != null){
            fillJsonList(method.getInput().getPath(),refMap,new ArrayList<>());
            fillJsonList(method.getInput().getHeaders(),refMap,new ArrayList<>());
            fillJsonList(method.getInput().getBody(),refMap,new ArrayList<>());
            fillJsonList(method.getInput().getParams(),refMap,new ArrayList<>());
        }
        if(method.getOutput() != null){
            fillJsonList(method.getOutput().getHeaders(),refMap,new ArrayList<>());
            fillJsonList(method.getOutput().getBody(),refMap,new ArrayList<>());
        }
    }
    public void initJsfRef(JsfStepMetadata jsfStepMetadata,Map<String,JsonType> refMap){
        fillJsonList(jsfStepMetadata.getInput(),refMap,new ArrayList<>());
        fillJsonType(jsfStepMetadata.getOutput(),refMap,new ArrayList<>());
    }
    public void fillJsonList(List<? extends JsonType> jsonTypes,Map<String,JsonType> refMap,List<String> currentStacks){
        if(jsonTypes == null) return;
        for (JsonType jsonType : jsonTypes) {
            fillJsonType(jsonType,refMap,currentStacks);

        }
    }
    public void fillJsonType(JsonType jsonType, Map<String,JsonType> refMap){
        fillJsonType(jsonType, refMap,new ArrayList<>());
    }
    public void fillJsonType(JsonType jsonType, Map<String,JsonType> refMap,List<String> currentStacks){
        if(jsonType instanceof RefObjectJsonType){
            String refName = ((RefObjectJsonType) jsonType).getRefName();
            if(StringUtils.isNotBlank(refName)){
                if(currentStacks.contains(refName)){ // 出现循环依赖链条
                    return;
                }
                currentStacks.add(refName);
                JsonType refJsonType = refMap.get(refName);
                if(refJsonType != null){

                    refJsonType.setClassName(refName);
                    refJsonType.setName(jsonType.getName());
                    //refJsonType.setDesc(jsonType.getDesc());
                    mergeRefJsonType( jsonType, refJsonType,refMap,currentStacks);
                }else{
                    List<JsonType> children = ((RefObjectJsonType) jsonType).getChildren();
                    if(children != null){
                        fillJsonList(children,refMap,currentStacks);
                    }
                }
                currentStacks.remove(currentStacks.size() - 1);
            }
        }else if(jsonType instanceof ComplexJsonType){
             fillJsonList(((ComplexJsonType) jsonType).getChildren(),refMap,currentStacks);
        }
    }
    public JsonType findByName(List<JsonType> jsonTypes,JsonType jsonType){
        for (JsonType type : jsonTypes) {
            if(type.getName().equals(jsonType.getName())){
                return type;
            }
        }
        return null;
    }
    public List<JsonType> mergeRefJsonTypeList(List<JsonType> originalChildren,List<JsonType> refChildren,Map<String,JsonType> refTypeMap,List<String> currentStacks){
        List<JsonType> children = new ArrayList<>();
        if(refChildren == null || refChildren.isEmpty()){
            fillJsonList(originalChildren,refTypeMap,currentStacks);
            return originalChildren;
        }

        for (JsonType child : refChildren) { // child和
            JsonType jsonType = findByName(originalChildren, child);
            if(jsonType == null){
                children.add(child);
                continue;
            }
            if(child instanceof SimpleJsonType){
                List<String> parentTypeName=new ArrayList<>();
                parentTypeName.addAll(refTypeMap.keySet());
                child.setParentTypeName(parentTypeName);
                children.add(child);
            }else{
                children.add(jsonType);
                fillJsonType(jsonType,refTypeMap,currentStacks);
            }


        }

        return children;
    }
    public void mergeRefJsonType(JsonType original, JsonType ref, Map<String,JsonType> refType,List<String> currentStacks){
        for (String needCopyAttr : needCopyAttrs) {
            if("desc".equals(needCopyAttr) && StringUtils.isNotBlank( original.getDesc())
                    || original.isRequired()
            ){
                continue;
            }
            BeanTool.setProp(original,needCopyAttr,BeanTool.getProp(ref,needCopyAttr));
            if(original instanceof RefObjectJsonType ){
                ((RefObjectJsonType) original).setRefName(ref.getClassName());
               // ((RefObjectJsonType) original).setRefType(ref.getType());
            }
        }

        if(original instanceof ComplexJsonType && ref instanceof ComplexJsonType){
             List<JsonType> children = mergeRefJsonTypeList(((ComplexJsonType)original).getChildren(),((ComplexJsonType) ref).getChildren(),refType,currentStacks);
            ((ComplexJsonType)original).setChildren(children);
        }




    }

    public List<String> collectJsfRefs(JsfStepMetadata stepMetadata){
        List<String> result = new ArrayList<>();
        if(stepMetadata == null) return result;
        collectRefNames(stepMetadata.getInput(),result);
        collectRefNames(stepMetadata.getOutput(),result);
        return result;
    }
    public List<String> collectHttpRefs(HttpMethodModel methodModel){
        List<String> result = new ArrayList<>();
        if(methodModel == null) return result;
        if(methodModel.getInput() != null){
            collectRefNames(methodModel.getInput().getHeaders(),result);
            collectRefNames(methodModel.getInput().getParams(),result);
            collectRefNames(methodModel.getInput().getPath(),result);
            collectRefNames(methodModel.getInput().getBody(),result);
        }
        if(methodModel.getOutput() != null){
            collectRefNames(methodModel.getOutput().getBody(),result);
            collectRefNames(methodModel.getOutput().getHeaders(),result);
        }
        return result;
    }
    public void collectRefNames(List<? extends JsonType> jsonTypes,List<String> result){
        if(jsonTypes == null) return ;
        for(JsonType jsonType : jsonTypes){
            collectRefNames(jsonType, result);
        }
    }
    public List<String> collectRefNames(List<JsonType> jsonTypes){
        if(jsonTypes == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for(JsonType jsonType : jsonTypes){
            collectRefNames(jsonType, result);
        }
        return result;
    }
    public List<String> collectRefNames(JsonType jsonType){
        if(jsonType == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        collectRefNames(jsonType, result);
        return result;
    }
    private void collectRefNames(JsonType jsonType, List<String> refNames){
        if(jsonType == null) return ;
        if(jsonType instanceof RefObjectJsonType && !StringUtils.isEmpty( ((RefObjectJsonType) jsonType).getRefName())){
            refNames.add(((RefObjectJsonType) jsonType).getRefName());
        }
        if(jsonType instanceof ComplexJsonType){
            ComplexJsonType complexJsonType = (ComplexJsonType) jsonType;
            if(complexJsonType.getChildren() != null){
                for(JsonType child : complexJsonType.getChildren()){
                    collectRefNames(child, refNames);
                }
            }
        }
    }
}
