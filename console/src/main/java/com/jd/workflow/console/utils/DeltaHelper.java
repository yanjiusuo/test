package com.jd.workflow.console.utils;

import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.method.HttpDeltaInfo;
import com.jd.workflow.console.entity.method.JsfDeltaInfo;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.type.ObjectTypes;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.schema.*;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用来计算json type的差量。
 * 1.
 */
public class DeltaHelper {
    public static final String DELETED_FLAG = "_del";
    public static final String ADD_FLAG = "_add";
    public static final String DELTA_ATTRS = "_delta";
    public static List<String> needDeltaAttrs = Arrays.asList("desc","required","value","name","type","hidden","mock","enumId","constraint");

    static Map<String, PropertyDescriptor> propDescriptors = new HashMap<>();
    static {
        try {

            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] propertyDescriptors = propertyUtilsBean.getPropertyDescriptors(JsonType.class);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if(needDeltaAttrs.contains(propertyDescriptor.getName())){
                    propDescriptors.put(propertyDescriptor.getName(),propertyDescriptor);
                }
            }

        } catch (Exception e) {
            throw StdException.adapt(e);
        }
    }

    public static BuilderJsonType deepClone(JsonType jsonType,boolean containsExt){
        if(jsonType == null) return null;
        return BuilderJsonType.fromJsonType(jsonType,containsExt);
    }
    private static List<BuilderJsonType> cloneGeneric(List<? extends JsonType> types,boolean containsExt){
        if(types == null)  return new ArrayList<>();
        List< BuilderJsonType> result = new ArrayList<>();
        for (JsonType type : types) {
            result.add(BuilderJsonType.fromJsonType(type,containsExt));
        }
        return result;
    }
    private static List<BuilderJsonType> clone(List< JsonType> types,boolean containsExt){
        if(types == null)  return new ArrayList<>();
        List< BuilderJsonType> result = new ArrayList<>();
        for (JsonType type : types) {
            result.add(BuilderJsonType.fromJsonType(type,containsExt));
        }
        return result;

    }

    public static List<JsonType> deltaList(List<? extends JsonType> beforeList, List<? extends JsonType> afterList,boolean onlyDeltaRef){
        List<BuilderJsonType> type1 = cloneGeneric(beforeList,false);
        List<BuilderJsonType> type2 = cloneGeneric(afterList,false);
        if(type1 == null) type1 = new ArrayList<>();
        if(type2 == null) type2 = new ArrayList<>();
        for (BuilderJsonType type : type1) {
            clearGenericTypes(type);
        }
        for (BuilderJsonType type : type2) {
            clearGenericTypes(type);
        }
        return internalDeltaList(type1, type2,onlyDeltaRef,false).stream().map(BuilderJsonType::toJsonType).collect(Collectors.toList());
    }
    /**
     * 获取json对象差分
     * @param beforeList
     * @param afterList
     * @return
     */
    private static List<BuilderJsonType> internalDeltaList(List<BuilderJsonType> beforeList, List<BuilderJsonType> afterList,boolean onlyDeltaRef,boolean parentIsRef){
        List<BuilderJsonType> result = new ArrayList<>();
        if(beforeList == null) beforeList = new ArrayList<>();
        if(afterList == null) afterList = new ArrayList<>();
        boolean needDelta = !onlyDeltaRef  || parentIsRef;
        if(CollectionUtils.isNotEmpty(beforeList)){
            for (BuilderJsonType before : beforeList) {
                BuilderJsonType after = (BuilderJsonType) find(afterList, before);
                if(after == null){
                    if(needDelta){
                        before.getExtAttrs().put(DELETED_FLAG,true);
                        result.add(before);
                    }
                }else{
                    BuilderJsonType delta = internalDeltaJsonType(before, after,onlyDeltaRef,parentIsRef);
                    if(delta != null){
                        result.add(delta);
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(afterList)) {
            for (BuilderJsonType after : afterList) {
                BuilderJsonType before = (BuilderJsonType) find(beforeList, after);
                if (before == null) {
                    if (needDelta) {
                        after.getExtAttrs().put(ADD_FLAG, true);
                        result.add(after);
                    }

                }
            }
        }
        return result;
    }

    public static JsonType deltaJsonType(JsonType before, JsonType after,boolean onlyDeltaRef){
        BuilderJsonType type1 = deepClone(before,false);
        BuilderJsonType type2 = deepClone(after,false);
        clearJsonTypeDeltaAttrs(type1);
        clearGenericTypes(type1);
        clearGenericTypes(type2);
        BuilderJsonType result = internalDeltaJsonType(type1, type2, onlyDeltaRef, false);
        if(result == null) return result;
        return result.toJsonType();
    }
    private  static void clearJsonTypeDeltaAttrs(BuilderJsonType jsonType){
        if(jsonType == null) return;
        clearExtAttrs(jsonType);
        if(jsonType.getChildren() != null){
            List<BuilderJsonType> children = children( jsonType);
            for (BuilderJsonType child : children) {
                clearJsonTypeDeltaAttrs(child);
            }
        }
    }
    private static boolean needDeltaChildren(BuilderJsonType type1,BuilderJsonType type2){
        if(type1.getType().equals(type2.getType())){
            return true;
        }

        if(type1.getType().equals("object") && type2.getType().equals("ref")
                || type1.getType().equals("ref") && type2.getType().equals("object")){
            return true;
        }

        return false;
    }
    private static BuilderJsonType internalDeltaJsonType(BuilderJsonType before, BuilderJsonType after,boolean onlyDeltaRefType,boolean parentIsRef){

        if(before == null){
            after.getExtAttrs().put(ADD_FLAG,true);
            return after;
        }
        if(after == null){
            before.getExtAttrs().put(DELETED_FLAG,true);
            return before;
        }
        Map<String, Object> deltaAttrs = getDeltaAttrs(before, after);

        if(onlyDeltaRefType && !parentIsRef){
            deltaAttrs = new HashMap<>();
        }

        if(!needDeltaChildren(before,after)){ // 类型不一致直接返回后者即可
            if(!deltaAttrs.isEmpty()){
                after.getExtAttrs().put(DELTA_ATTRS,deltaAttrs);
            }
            return after;
        }



        BuilderJsonType result = before;
        if(!deltaAttrs.isEmpty()){
            before.getExtAttrs().put(DELTA_ATTRS,deltaAttrs);
        }
        boolean isRef = "ref".equals(before.getType());
        List<BuilderJsonType> beforeChildren = children(before);
        List<BuilderJsonType> afterChildren = children(after);

        List<BuilderJsonType> deltaChild = internalDeltaList(beforeChildren,afterChildren,onlyDeltaRefType,isRef);
        result.setChildren(deltaChild);
        if(deltaAttrs.isEmpty() && deltaChild.isEmpty()) return null;
        return result;

    }
    public static boolean isAllAttributeSame(JsonType before,JsonType after){
        return getDeltaAttrs(before, after).isEmpty();
    }

    public static Map<String,Object> getDeltaAttrs(JsonType before, JsonType after){
        Map<String,Object> map = new HashMap<>();
        for (String needDeltaAttr : needDeltaAttrs) {
            Object beforeVal = BeanTool.getProp(before,needDeltaAttr);
            Object afterVal = BeanTool.getProp(after,needDeltaAttr);
            if(!equals(beforeVal,afterVal)){
                map.put(needDeltaAttr,afterVal);
            }
        }
        return map;
    }

    public static List<BuilderJsonType> children(BuilderJsonType type){
        if(type.getChildren() == null) return new ArrayList<>();
        return type.getChildren();
    }
    public static JsonType find(List<? extends JsonType> types,JsonType found){
        if(null==found||found.getName() == null) return null;
        for (JsonType type : types) {
            if(found.getName().equals(type.getName())) return type;
        }
        return null;
    }
    private static void remove(List<? extends JsonType> types,JsonType found){
        if(found.getName() == null) return ;
        Iterator<? extends JsonType> iterator = types.iterator();
        while (iterator.hasNext()){
            JsonType type = iterator.next();
            if(found.getName().equals(type.getName())){
                iterator.remove();
            }
        }

    }
    public static JsonType mergeDelta(JsonType jsonType,JsonType delta){
        if(delta !=null ){
            delta = delta.clone();
        }
        BuilderJsonType result = internalMergeDelta(BuilderJsonType.fromJsonType(jsonType,false), BuilderJsonType.fromJsonType(delta,true));
        if(result == null) return result;
        return result.toJsonType();
    }
    private static void clearGenericTypes(BuilderJsonType jsonType){
        if(jsonType == null) return;
        if(jsonType.getGenericTypes() != null){
            jsonType.getGenericTypes().clear();
        }
        if(jsonType.getChildren() != null){
            for (BuilderJsonType child : children(jsonType)) {
                clearGenericTypes(child);
            }
        }
    }
    public static void clearExtAttrs(JsonType jsonType){
        if(jsonType == null) return;
        //clearGenericTypes(jsonType);
        Map<String,Object> extAttrs = jsonType.getExtAttrs();
        extAttrs.remove(DELETED_FLAG);
        extAttrs.remove(ADD_FLAG);
        extAttrs.remove(DELTA_ATTRS);
    }
    /**
     * 合并json差分信息
     * @param jsonType 待合并的信息
     * @param delta 差分信息
     * @return
     */
    private static BuilderJsonType internalMergeDelta(BuilderJsonType jsonType,BuilderJsonType delta){
        if(jsonType == null) {
            //clearExtAttrs(delta);
            return delta;
        }
        if(delta == null) return jsonType;
        if(delta.getExtAttrs().containsKey(DELETED_FLAG)) return null;
        Map<String,Object> deltaAttrs = (Map<String, Object>) delta.getExtAttrs().get(DELTA_ATTRS);
        boolean needDeltaChildren = needDeltaChildren(jsonType,delta);
        if(deltaAttrs != null){ // 这里可能会改变jsonType的type,因此需要先判别一下是否需要deltaChildren
            for (Map.Entry<String, Object> entry : deltaAttrs.entrySet()) {
                Object value = entry.getValue();
                if(propDescriptors.containsKey(entry.getKey())){
                    if(value instanceof Map || value instanceof Collection){
                        value = JsonUtils.cast(value,propDescriptors.get(entry.getKey()).getPropertyType());
                    }else{
                        value = ObjectTypes.convert(value,propDescriptors.get(entry.getKey()).getPropertyType());
                    }

                }
                BeanTool.setProp(jsonType,entry.getKey(),value);
            }

        }
        jsonType.getExtAttrs().putAll(delta.getExtAttrs());
        if(!needDeltaChildren){ // 节点不一致，直接返回后者
            //clearExtAttrs(delta);
            jsonType.setChildren(delta.getChildren());
            return jsonType;
        }


        List<BuilderJsonType> beforeChildren = children( jsonType);
        List<BuilderJsonType> deltaChildren = children( delta);


        jsonType.setChildren(internalMergeDeltaList(beforeChildren,deltaChildren));
        return jsonType;
    }
    public static List<JsonType> mergeDeltaList(List< JsonType> beforeChildren, List<JsonType> deltaChildren){
        return internalMergeDeltaList(clone(beforeChildren,false),clone(deltaChildren,true)).stream().map(jsonType->jsonType.toJsonType()).collect(Collectors.toList());
    }
    public static List<SimpleJsonType> mergeSimpleDeltaList(List< SimpleJsonType> before, List<SimpleJsonType> delta){
        List<JsonType> beforeChildren =new ArrayList<>();
        List<JsonType> deltaChildren =new ArrayList<>();
        if(CollectionUtils.isNotEmpty(before)){
            beforeChildren = before.stream().map(jsonType -> (JsonType) jsonType).collect(Collectors.toList());
        }
        if(CollectionUtils.isNotEmpty(delta)){
            deltaChildren = delta.stream().map(jsonType -> (JsonType) jsonType).collect(Collectors.toList());
        }
        return internalMergeDeltaList(clone(beforeChildren,false),clone(deltaChildren,true)).stream().map(jsonType->(SimpleJsonType)jsonType.toJsonType()).collect(Collectors.toList());
    }
    private  static List<BuilderJsonType> internalMergeDeltaList(List< BuilderJsonType> beforeChildren, List<BuilderJsonType> deltaChildren){
        if(beforeChildren == null) beforeChildren = new ArrayList<>();
        if(deltaChildren == null) deltaChildren = new ArrayList<>();

        fillParentType(deltaChildren,null);
        for (BuilderJsonType deltaChild : deltaChildren) {
            if(deltaChild.getExtAttrs().containsKey(DELETED_FLAG)){
                remove(beforeChildren,deltaChild);
                continue;
            }
            BuilderJsonType match = (BuilderJsonType) find(beforeChildren, deltaChild);
            if(match == null){
                //if(deltaChild.getExtAttrs().containsKey(ADD_FLAG)) { // 未找到，说明是新增加的
                //learExtAttrs(deltaChild);

                beforeChildren.add(deltaChild);
                //}
            }else{ // 找到了，说明是需要合并
                BuilderJsonType merged = internalMergeDelta(match, deltaChild);
                beforeChildren.set(beforeChildren.indexOf(match),merged);

            }
        }
        return beforeChildren;
    }


    public static void fillParentType(List<BuilderJsonType> body, List<String> parentType) {
        if (org.springframework.util.CollectionUtils.isEmpty(body)) {
            return;
        }
        if(null==parentType){
            parentType=new ArrayList<>();
        }
        for (BuilderJsonType jsonType : body) {
            jsonType.setParentTypeName(parentType);
            List<String> parentTypes= new ArrayList<>();
            parentTypes.addAll(parentType);
            if (jsonType.getType().equals("ref")) {
                List<BuilderJsonType> child = jsonType.getChildren();
                parentTypes.add(jsonType.getRefName());
                fillParentType(child, parentTypes);
            }
            if (jsonType.getType().equals("object") || jsonType.getType().equals("array")) {
                List<BuilderJsonType> child = jsonType.getChildren();
                fillParentType(child, parentTypes);
            }
        }
    }

    public static boolean equals(Object obj1,Object obj2){
        if(ObjectHelper.isEmpty(obj1) && ObjectHelper.isEmpty(obj2) ) return true;
        if(obj1 == null){
            if(Boolean.FALSE.equals(obj2)) return true;
        }
        if(obj2 == null){
            if(Boolean.FALSE.equals(obj1)) return true;
        }
        if(obj1  instanceof String && obj2 instanceof String){
            return ((String) obj1).trim().equals(((String) obj2).trim());
        }
        return ObjectHelper.equals(obj1,obj2);
    }
    public static HttpDeltaInfo deltaHttpMethod(MethodManageDTO beforeMethod, MethodManageDTO afterMethod,boolean onlyDeltaRef){

        HttpDeltaInfo deltaInfo = new HttpDeltaInfo();
        if(!onlyDeltaRef){
            if(!equals(beforeMethod.getName(),afterMethod.getName())){
                deltaInfo.getDeltaAttrs().put("name",afterMethod.getName());
            }
            if(!equals(beforeMethod.getMethodCode(),afterMethod.getMethodCode())){
                deltaInfo.getDeltaAttrs().put("methodCode",afterMethod.getMethodCode());
            }
            if(!equals(beforeMethod.getDocInfo(),afterMethod.getDocInfo())){
                deltaInfo.getDeltaAttrs().put("docInfo",afterMethod.getDocInfo());
            }
        }

        HttpMethodModel before = (HttpMethodModel) beforeMethod.getContentObject();
        HttpMethodModel after = (HttpMethodModel) afterMethod.getContentObject();
        boolean inputEmpty = false;
        {
            HttpMethodModel.HttpMethodInput input = new HttpMethodModel.HttpMethodInput();
            if(!ObjectHelper.equals(before.getInput().getReqType(),after.getInput().getReqType())){
                input.setReqType(after.getInput().getReqType());
            }
            input.setParams(deltaList(before.getInput().getParams(),after.getInput().getParams(),onlyDeltaRef));
            input.setHeaders(deltaList(before.getInput().getHeaders(),after.getInput().getHeaders(),onlyDeltaRef));
            input.setBody(deltaList(before.getInput().getBody(),after.getInput().getBody(),onlyDeltaRef));
            input.setPath(deltaList(before.getInput().getPath(),after.getInput().getPath(),onlyDeltaRef).stream().map(i->(SimpleJsonType)i).collect(Collectors.toList()));
            inputEmpty = ObjectHelper.isEmpty(input.getParams()) && ObjectHelper.isEmpty(input.getHeaders()) && ObjectHelper.isEmpty(input.getBody()) && ObjectHelper.isEmpty(input.getReqType());
            deltaInfo.setInput(input);
        }
        boolean outputEmpty = false;
        {
            HttpMethodModel.HttpMethodOutput output = new HttpMethodModel.HttpMethodOutput();
            output.setHeaders(deltaList(before.getOutput().getHeaders(),after.getOutput().getHeaders(),onlyDeltaRef));
            output.setBody(deltaList(before.getOutput().getBody(),after.getOutput().getBody(),onlyDeltaRef));
            outputEmpty = ObjectHelper.isEmpty(output.getHeaders()) && ObjectHelper.isEmpty(output.getBody());
            deltaInfo.setOutput(output);
        }
        if(deltaInfo.getDeltaAttrs().isEmpty() && inputEmpty && outputEmpty) return null;

        return deltaInfo;
    }
    public static void mergeHttpMethod(IMethodInfo method,HttpDeltaInfo deltaInfo){
        for (Map.Entry<String, Object> entry : deltaInfo.getDeltaAttrs().entrySet()) {
            BeanTool.setProp(method,entry.getKey(),entry.getValue());
        }
        method.setDelta(deltaInfo.getDeltaAttrs());
        HttpMethodModel methodModel = (HttpMethodModel) method.getContentObject();
        if(methodModel == null) return;
        HttpMethodModel.HttpMethodInput input = methodModel.getInput();
        HttpMethodModel.HttpMethodOutput output = methodModel.getOutput();

        if(deltaInfo.getInput() ==null) deltaInfo.setInput(new HttpMethodModel.HttpMethodInput());
        if(deltaInfo.getOutput() ==null) deltaInfo.setOutput(new HttpMethodModel.HttpMethodOutput());
        input.setHeaders(mergeDeltaList(input.getHeaders(),deltaInfo.getInput().getHeaders()));
        input.setParams(mergeDeltaList(input.getParams(),deltaInfo.getInput().getParams()));
        input.setPath(mergeSimpleDeltaList(input.getPath(),deltaInfo.getInput().getPath()));
        input.setBody(mergeDeltaList(input.getBody(),deltaInfo.getInput().getBody()));
        if(!StringUtils.isEmpty(deltaInfo.getInput().getReqType())){
            input.setReqType(deltaInfo.getInput().getReqType());
        }
        output.setHeaders(mergeDeltaList(output.getHeaders(),deltaInfo.getOutput().getHeaders()));
        output.setBody(mergeDeltaList(output.getBody(),deltaInfo.getOutput().getBody()));

    }
    public static void mergeJsfMethod(IMethodInfo method, JsfDeltaInfo deltaInfo){
        for (Map.Entry<String, Object> entry : deltaInfo.getDeltaAttrs().entrySet()) {
            BeanTool.setProp(method,entry.getKey(),entry.getValue());
        }
        method.setDelta(deltaInfo.getDeltaAttrs());
        JsfStepMetadata methodModel = (JsfStepMetadata) method.getContentObject();
        if(methodModel == null) return;
        if(deltaInfo.getInput() != null){
            methodModel.setInput(mergeDeltaList(transform(methodModel.getInput()),transform(deltaInfo.getInput())));
        }

        methodModel.setOutput(mergeDelta(methodModel.getOutput(),deltaInfo.getOutput()));


    }
    public  static List<JsonType> transform(List<? extends JsonType> list){
        List<JsonType> result = new ArrayList<>();
        if(list == null) return result;
        result.addAll(list);
        return result;
    }

    public static JsfDeltaInfo deltaJsfMethod(IMethodInfo beforeMethod, MethodManageDTO afterMethod,boolean onlyDeltaRef){
        JsfDeltaInfo deltaInfo = new JsfDeltaInfo();
        if(!onlyDeltaRef){
            if(!equals(beforeMethod.getDocInfo(),afterMethod.getDocInfo())){
                deltaInfo.getDeltaAttrs().put("docInfo",afterMethod.getDocInfo());
            }
            if(!equals(beforeMethod.getName(),afterMethod.getName())){
                deltaInfo.getDeltaAttrs().put("name",afterMethod.getName());
            }
        }

        JsfStepMetadata before = (JsfStepMetadata) beforeMethod.getContentObject();
        JsfStepMetadata after = (JsfStepMetadata) afterMethod.getContentObject();

        deltaInfo.setInput(deltaList(before.getInput(),after.getInput(),onlyDeltaRef));
        deltaInfo.setOutput(deltaJsonType(before.getOutput(),after.getOutput(),onlyDeltaRef));
        if(deltaInfo.getDeltaAttrs().isEmpty() && ObjectHelper.isEmpty(deltaInfo.getInput()) && ObjectHelper.isEmpty(deltaInfo.getOutput())) return null;
        return deltaInfo;
    }

}