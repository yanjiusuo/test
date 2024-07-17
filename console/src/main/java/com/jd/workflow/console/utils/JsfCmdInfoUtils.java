package com.jd.workflow.console.utils;

import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class JsfCmdInfoUtils {
    /**
     *{
     *     "methodName": "queryPaymentInfoByOrderId",
     *     "returnType": "com.jd.ept.common.domain.EptRemoteResult<java.util.List<com.jd.ept.order.vo.imid.response.EptOrderPaymentVo>>",
     *     "EptRemoteResult": {
     *         "code": "int",
     *         "isSuccess": "boolean",
     *         "message": "java.lang.String",
     *         "model": "java.lang.Object"
     *     },
     *     "EptOrderPaymentVo": {
     *         "orderId": "java.lang.Long",
     *         "currencyBuy": "java.lang.String",
     *         "payId": "java.lang.String",
     *         "payType": "java.lang.String",
     *         "payEnum": "java.lang.String",
     *         "payTime": "java.util.Date"
     *     },
     *     "parameters": [{
     *             "param1": "java.lang.Long"
     *         }
     *     ]
     * }
     * @return
     */

    public static List<BuilderJsonType> parseJsfInfoCmdInputParam(List<Map<String,Object>> inputParams){
        List<BuilderJsonType> result = new ArrayList<>();
        for (int i = 0; i < inputParams.size(); i++) {

            Map<String,Object> param = inputParams.get(i);
            String paramType = param.get("param"+(i+1)).toString();
            BuilderJsonType builderJsonType = buildParamJsonType(ScopeMap.of(param),paramType,new ArrayList<>());
            builderJsonType.setName("param"+(1+i));
            result.add(builderJsonType);

        }
        return result;
    }

    public static BuilderJsonType buildParamJsonType(ScopeMap<String,Object> context, String type, List<String> processedTypes){
        type = type.trim();
        if(processedTypes.contains(type)){
            BuilderJsonType builderJsonType = new BuilderJsonType();
            builderJsonType.setType("object");
            builderJsonType.setClassName(type);
            return builderJsonType;
        }
        processedTypes.add(type);
        String primitiveType = TypeUtils.getPrimitiveType(type);
        BuilderJsonType builderJsonType = new BuilderJsonType();
        if(primitiveType != null){ // 简单类型

            builderJsonType.setType(primitiveType);
            if(type.indexOf(".") == -1){
                builderJsonType.setRequired(true);
                builderJsonType.setClassName(TypeUtils.getWrapClassName(type));
            }else{
                builderJsonType.setClassName(type);
            }

        }else if(type.length() == 1){ // 泛型类型
            builderJsonType.setType("object");
            builderJsonType.setClassName("java.lang.Object");
            builderJsonType.setTypeVariableName(type);
        }else if(TypeUtils.getSimpleType(type) != null) {
            builderJsonType.setType(TypeUtils.getSimpleType(type));
            builderJsonType.setClassName(type);
        }else{
            if(type.indexOf("<") != -1) { // 泛型
                builderJsonType = initReferenceType(context,ClassReference.parse(type),processedTypes);
            }else if(type.indexOf("[]") != -1){ // 数组
                builderJsonType = parseArrayType(context,type,processedTypes);
            }else if("java.lang.Object".equals(type) || type.contains("?") ) {
                builderJsonType.setType("object");
                builderJsonType.setClassName(type);
            }else if(isListType(type)){
                builderJsonType = new BuilderJsonType();
                builderJsonType.setType("array");
                builderJsonType.setClassName(type);
            }else if(isMapType(type)){
                builderJsonType = new BuilderJsonType();
                builderJsonType.setType("map");
                builderJsonType.setClassName(type);
            }else if(type.indexOf("java.") == 0) {
                builderJsonType.setType("object");
                builderJsonType.setClassName(type);
            }else{
                builderJsonType.setType("object");
                builderJsonType.setClassName(type);
                Object ac = context.get(getSimpleName(type));
                builderJsonType.setClassName(maybeSubClass(type,context));
                if(ac != null){

                    builderJsonType.setChildren(new ArrayList<>());
                    if(ac instanceof Map){
                        Map<String,Object> map = (Map<String,Object>)ac;

                        for(Map.Entry<String,Object> entry : map.entrySet()){
                            if(entry.getValue() instanceof String) {
                                BuilderJsonType child = buildParamJsonType(ScopeMap.of(map,context),entry.getValue().toString(),processedTypes);
                                child.setName(entry.getKey());
                                builderJsonType.getChildren().add(child);
                            }

                        }
                    }
                }

            }
        }
        processedTypes.remove(processedTypes.size()-1);
        return builderJsonType;
    }
    public static String maybeSubClass(String className,ScopeMap map){
        if(className.indexOf("$") != -1){
            return className;
        }
        List<String> types = StringHelper.split(className, ".");
        String str = "";
        for (int i = types.size() - 1; i >= 0 ; i--) {
            String type = types.get(i);
            if(firstLetterUpperCase(type)){
                Object o = map.get(type);
                //if(o != null && o instanceof Map){
                     if(StringUtils.isNotBlank(str)){
                         str =  type+"$"+str;
                     }else{
                        str = type;
                     }
                     continue;
                //}
            }
            if(StringHelper.isEmpty(str)){
                return className;
            }
            return StringHelper.join(types.subList(0,i+1),".")+"."+str;

        }
        return className;
    }

    private static boolean firstLetterUpperCase(String s) {
        return Character.isUpperCase(s.charAt(0));
    }

    private static String getSimpleName(String type){
        if(type.indexOf("$") != -1){
            return type.substring(type.lastIndexOf("$")+1);
        }
        if(type.indexOf(".") != -1){
            return type.substring(type.lastIndexOf(".")+1);
        }
        return type;
    }

    private static BuilderJsonType parseArrayType(ScopeMap<String, Object> context, String type, List<String> processedTypes) {
        BuilderJsonType builderJsonType = new BuilderJsonType();
        builderJsonType.setType("array");
        builderJsonType.setClassName(type);
        String componentType = type.substring(0,type.lastIndexOf("["));
        builderJsonType.setChildren(new ArrayList<>());
        BuilderJsonType childType = buildParamJsonType(context, componentType, processedTypes);
        childType.setName("$$0");
        builderJsonType.getChildren().add(childType);
        return builderJsonType;
    }

    private static BuilderJsonType initReferenceType(ScopeMap<String,Object> context,ClassReference reference,List<String> processedTypes){
        BuilderJsonType  builderJsonType = buildParamJsonType(context, reference.getClassName(),processedTypes);
        if(builderJsonType == null){
            return null;
        }
        if("array".equals(builderJsonType.getType())){
            builderJsonType.setChildren(new ArrayList<>());
            for(ClassReference child : reference.getChildren()){
                BuilderJsonType root = initReferenceType(context, child, processedTypes);
                root.setName("$$0");
                builderJsonType.getChildren().add(root);
            }
        }else if("map".equals(builderJsonType.getType())){
            builderJsonType.setChildren(new ArrayList<>());
            int i = 0;
            for(ClassReference child : reference.getChildren()){
                BuilderJsonType type = initReferenceType(context, child, processedTypes);
                if(i == 0){
                    type.setName("key");
                }else{
                    type.setName("value");
                }
                builderJsonType.getChildren().add(type);
                i++;
            }
        }else if("object".equals(builderJsonType.getType())){
            // 对象类型，说明
            List<BuilderJsonType> child = findClassTypeIsObjectChild(builderJsonType);

            if(reference.getChildren().size() == 1
                    && child.size() == 1
            ){ // 处理泛型参数
                BuilderJsonType referenceType = initReferenceType(context, reference.getChildren().get(0), processedTypes);
                BuilderJsonType childType = child.get(0);

                childType.setClassName(referenceType.getClassName());
                childType.setType(referenceType.getType());
                childType.setChildren(referenceType.getChildren());
            }
        }
        return builderJsonType;
    }

    /**
     * 找到子节点的className为java.lang.Object的节点
     * @param builderJsonType
     * @return
     */
    private static List<BuilderJsonType> findClassTypeIsObjectChild(BuilderJsonType builderJsonType){
        List<BuilderJsonType> result = new ArrayList<>();
        for (BuilderJsonType child : builderJsonType.getChildren()) {
            findClassTypeIsObjectChild(child,result);
        }
        if(result.size() > 1){
            List<BuilderJsonType> typeVars = result.stream().filter(item -> StringUtils.isNotEmpty(item.getTypeVariableName())).collect(Collectors.toList());
            if(typeVars.size() == 1){
                return typeVars;
            }
        }
        return result;
    }
    private static void findClassTypeIsObjectChild(BuilderJsonType builderJsonType, List<BuilderJsonType> result){
        if( "java.lang.Object".equals(builderJsonType.getClassName())){
            result.add(builderJsonType);
        }
        for (BuilderJsonType child : builderJsonType.getChildren()) {
            findClassTypeIsObjectChild(child,result);
        }
    }
    private static boolean isListType(String type){
        try{
            if(type.startsWith("java.")){
                Class<?> clazz = Class.forName(type);
                return Collection.class.isAssignableFrom(clazz) ||  Collection.class.equals(clazz);
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
    private static boolean isMapType(String type){
        try{
            if(type.startsWith("java.")){
                Class<?> clazz = Class.forName(type);
                return Map.class.isAssignableFrom(clazz) || Map.class.equals(clazz);
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }

}
