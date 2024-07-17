package com.jd.workflow.console.service.doc;

import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.MethodTagEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.doc.GroupHttpData;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.utils.GroupHelper;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.parser.SwaggerParser;
import com.jd.workflow.console.utils.DigestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class SwaggerParserService {
    static Map<Class<? extends Property>,String > propTypeMapping = new HashMap<>();
    static{

        propTypeMapping.put(ArrayProperty.class, "array");
        propTypeMapping.put(BinaryProperty.class, "string");
        propTypeMapping.put(DecimalProperty.class, "string");
        propTypeMapping.put(DoubleProperty.class, "double");
        propTypeMapping.put(FloatProperty.class, "float");
        propTypeMapping.put(LongProperty.class, "long");

        propTypeMapping.put(FileProperty.class, SimpleParamType.FILE.typeName());
    }



    /**
     * 去重，出入参一致的只保留一条呢
     * @param operationMap
     * @return
     */
    private Map<Operation,List<HttpMethod>> filterDuplicate(Map<HttpMethod, Operation> operationMap){
        Map<Operation,List<HttpMethod>> ret = new HashMap<>();
        if(operationMap.size() == 1){
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                ret.put(entry.getValue(),Collections.singletonList(entry.getKey()));
            }
        }else if(hasAllMethod(operationMap)){
            ret.put(operationMap.get(HttpMethod.GET),Arrays.asList(HttpMethod.values()));
        }else {
            Map<String/*sign*/,Operation> map = new HashMap<>();
            for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                String sign = getOperationSign(entry.getValue());
                Operation exist = map.get(sign);
                if(exist == null){
                    map.put(sign,entry.getValue());
                    List<HttpMethod> methods = ret.computeIfAbsent(entry.getValue(),vs->new ArrayList<>());
                    methods.add(entry.getKey());
                }else{
                    List<HttpMethod> methods = ret.get(exist);
                    methods.add(entry.getKey());
                }
            }
        }
        return ret;
    }

    /**
     * 获取operation对应的操作，用来去重
     * @param operation
     * @return
     */
    private String getOperationSign(Operation operation){
        Map<String,Object> map = new HashMap<>();
        //map.put("summary",operation.getSummary());
        map.put("description",operation.getDescription());
        map.put("schemes",operation.getSchemes());
        //map.put("consumes",operation.getConsumes());
        //map.put("produces",operation.getProduces());
        map.put("parameters",operation.getParameters());
        Response response = operation.getResponses().get("200");
        map.put("responses", response);
        return toJSONString(map);
    }
    public static String toJSONString(Object o) {
        return DigestUtils.toJSONString(o);

    }
    private boolean hasAllMethod(Map<HttpMethod, Operation> operationMap) {
        for (HttpMethod value : HttpMethod.values()) {
            if(!operationMap.containsKey(value)) return false;
        }
        return true;
    }
    private String getMethodCode(String path,Operation operation){
         String opName = StringHelper.lastPart(path, '/');
         if(StringHelper.isValidPropName(opName)){
             return opName;
         }
         return operation.getOperationId();
    }
    public static int getActualLength(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        return str.getBytes(Charset.forName("UTF-8")).length;
    }

    /**
     * 字符串太长存到数据库可能会异常，需要截断一下
     * @param str
     * @param size
     * @return
     */
    public static String truncateStr(String str,int size){
        if(StringUtils.isEmpty(str)) return str;
        if(str.length() < size /3 ) return str;
        int actualLength = getActualLength(str);
        if(actualLength < size) return str;

        for (int i = Math.min(actualLength,str.length()); i >= 0; i--) {
            String subStr = str.substring(0, i);
            if(getActualLength(subStr) <= size){
                return subStr;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("---------------------");
        for (int i = 0; i < 11; i++) {
            System.out.println(truncateStr("我很好",i));
        }

    }
    public static MethodManage fromHttpMethodModel(HttpMethodModel model){
        MethodManage methodManage = new MethodManage();
        methodManage.setHttpMethod(model.getInput().getMethod());
        methodManage.setPath(model.getInput().getUrl());
        methodManage.setMethodCode(model.getMethodCode());
        methodManage.setName(truncateStr(model.getSummary(),128));
        methodManage.setDesc(truncateStr(model.getDesc(),256));
        methodManage.setDocInfo(model.getDesc());



        if (model.getInput().getBody() != null) {
            List<JsonType> pp = model.getInput().getBody().stream().filter(i -> i.getName().equals("functionId")).collect(Collectors.toList());
            //标记color接口
            if (!CollectionUtils.isEmpty(pp)) {
                methodManage.setMethodTag(MethodTagEnum.COLOR.getCode());
                if(pp.get(0).getValue() != null){
                    methodManage.setFunctionId(pp.get(0).getValue().toString());
                }else{
                    methodManage.setFunctionId(null);
                }

            }
        }
        if (model.getInput().getParams() != null) {
            List<JsonType> param = model.getInput().getParams().stream().filter(i -> i.getName().equals("functionId")).collect(Collectors.toList());
            //标记color接口
            if (!CollectionUtils.isEmpty(param)) {
                methodManage.setMethodTag(MethodTagEnum.COLOR.getCode());
                methodManage.setFunctionId(param.get(0).getDesc());
                List<JsonType> body = new ArrayList<>();
                body.addAll(model.getInput().getParams());

                if(ObjectHelper.isEmpty(model.getInput().getBody())){
                    model.getInput().setBody(body);
                    model.getInput().getParams().clear();
                }
            }

        }
        //methodManage.setDesc(truncateStr(model.getDesc(),256));
        methodManage.setAuthKey(model.getAuthKeys());
        methodManage.setType(InterfaceTypeEnum.HTTP.getCode());
        methodManage.setContent(JsonUtils.toJSONString(model));
        methodManage.setDigest(DigestUtils.getHttpMethodMd5(methodManage,model));
        methodManage.setMergedContentDigest(methodManage.getDigest());
        return methodManage;
    }


    public List<GroupHttpData<MethodManage>> parseSwagger(String swaggerJson){
        SwaggerParser swaggerParser = new SwaggerParser();
        List<GroupHttpData<MethodManage>> groupMethods = new ArrayList<>();
        Swagger swagger = swaggerParser.parse(swaggerJson);
        if(swagger.getPaths() == null) return groupMethods;
        for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            Map<Operation, List<HttpMethod>> operationListMap = filterDuplicate(pathEntry.getValue().getOperationMap());
            for (Map.Entry<Operation, List<HttpMethod>> entry : operationListMap.entrySet()) {
                Operation operation = entry.getKey();
                String httpMethods = entry.getValue().stream().map(vs->vs.name()).collect(Collectors.joining(","));
                MethodManage methodManage = new MethodManage();
                methodManage.setHttpMethod(httpMethods);
                methodManage.setPath(pathEntry.getKey());

                methodManage.setMethodCode(getMethodCode(pathEntry.getKey(),operation));
                Object authKey = operation.getVendorExtensions().get("x-authKey");
                if(authKey != null){
                    String key = Variant.valueOf(authKey).toString();
                    List<String> keys = StringHelper.split(key, ",");
                    methodManage.setAuthKey(keys);
                }

                methodManage.setName(truncateStr(operation.getSummary(),128));
                //methodManage.setDesc(truncateStr(operation.getDescription(),256));
                methodManage.setDocInfo(operation.getDescription());

                methodManage.setType(InterfaceTypeEnum.HTTP.getCode());
                HttpMethodModel methodModel = new HttpMethodModel();
                HttpMethodModel.HttpMethodInput methodInput = new HttpMethodModel.HttpMethodInput();
                HttpMethodModel.HttpMethodOutput output = new HttpMethodModel.HttpMethodOutput();
                methodModel.setInput(methodInput);
                methodModel.setOutput(output);
                List<String> groupTags = operation.getTags();
                GroupHttpData groupHttpData = new GroupHttpData();
                groupHttpData.setGroupDesc(groupTags.get(0));
                List<MethodManage> methods = new ArrayList<>();
                groupHttpData.setHttpData(methods);
                methods.add(methodManage);
                GroupHelper.addGroupData(groupMethods,groupHttpData);
                for (Parameter parameter : operation.getParameters()) {
                    parameterToJsonType(parameter,methodInput,swagger);
                }
                Response response = operation.getResponses().get("200");
                if(response != null){
                    Model responseSchema = response.getResponseSchema();
                    BuilderJsonType jsonType = new BuilderJsonType();
                    processModel(jsonType,responseSchema,swagger.getDefinitions(),new HashSet<>());
                    jsonType.setName("root");
                     Object returnInfo = response.getVendorExtensions().get("x-returnInfo");
                    if(returnInfo != null){
                        jsonType.setDesc(Variant.valueOf(returnInfo).toString());
                    }
                    output.setBody(Collections.singletonList(jsonType.toJsonType()));
                }

                methodManage.setContent(JsonUtils.toJSONString(methodModel));
                methodManage.setDigest(DigestUtils.getHttpMethodMd5(methodManage,methodModel));
                methodManage.setMergedContentDigest(methodManage.getDigest());
            }

        }
        return groupMethods;
    }
    private ClassMetadata getClassMetadata(List<ClassMetadata> classes,String name){
        for (ClassMetadata metadata : classes) {
            if(name.equalsIgnoreCase(metadata.getClassName())) return metadata;
        }
        return null;
    }

    /**
     * 解析信鸽api的方法描述：ServiceResponse getUserLabelPortraitData(java.lang.Long)
     * @return
     */
    private List<String> parseMethodTypesByDesc(String methodDesc){
        List<String> result = new ArrayList<>();
        if(methodDesc.indexOf("(") != -1
         && methodDesc.lastIndexOf(")") == methodDesc.length())
        {
           String parameterStr= methodDesc.substring(methodDesc.indexOf("(")+1,methodDesc.lastIndexOf(")") -1);
           String[] parameterTypes = StringUtils.split(parameterStr,',');
            for (String parameterType : parameterTypes) {

                    result.add(TypeUtils.getPrimitiveType(parameterType));

            }
        }
        return result;
    }

    /**
     * 解析信鸽api的json信息
     * @param swaggerJson
     * @return
     */
    public List<ClassMetadata> parseJsfMetadata(String swaggerJson){
        SwaggerParser swaggerParser = new SwaggerParser();
        Map<String, List<ClassMetadata>> groupMethods = new HashMap<>();
        Swagger swagger = swaggerParser.parse(swaggerJson);

        List<ClassMetadata> classes = new ArrayList<>();
        for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            if(!path.startsWith("/jsf")){
                continue;
            }
            String[] strs = StringUtils.split(path, '/');
            String className = strs[1];
            String methodName = strs[2];
            Operation operation = pathEntry.getValue().getPost();

            ClassMetadata classMetadata = getClassMetadata(classes, className);
            if(classMetadata == null){
                classMetadata = new ClassMetadata();
                if(!ObjectHelper.isEmpty(operation.getTags())){
                    classMetadata.setDesc(operation.getTags().get(0));
                }
                classMetadata.setClassName(className);
                classes.add(classMetadata);
            }


            MethodMetadata methodMetadata = new MethodMetadata();
            classMetadata.getMethods().add(methodMetadata);
            methodMetadata.setMethodName(methodName);
            methodMetadata.setInterfaceName(className);
            methodMetadata.setDesc(operation.getSummary());
            methodMetadata.setCnName(operation.getSummary());
            List<JsonType> input = new ArrayList<>();
            methodMetadata.setInput(input);
            List<String> types = parseMethodTypesByDesc(operation.getDescription());
            for (Parameter parameter : operation.getParameters()) {
                input.add(jsfParameterToJsonType(parameter,swagger));
            }
            if(types.size() == operation.getParameters().size()){
                for (int i = 0; i < types.size(); i++) {
                    if(types.get(i) == null) continue;
                     JsonType jsonType = input.get(i);
                     if(jsonType instanceof SimpleJsonType){
                         ((SimpleJsonType) jsonType).setType(types.get(i));
                     }
                }
            }
            Response response = operation.getResponses().get("200");
            if(response != null){
                Model responseSchema = response.getResponseSchema();
                BuilderJsonType jsonType = new BuilderJsonType();
                processModel(jsonType,responseSchema,swagger.getDefinitions(),new HashSet<>());
                jsonType.setName("root");
                methodMetadata.setOutput(jsonType.toJsonType());
            }

        }


        return classes;
    }

    private JsonType parameterToJsonType(Parameter parameter,HttpMethodModel.HttpMethodInput methodInput,Swagger swagger){
        BuilderJsonType jsonType = new BuilderJsonType();
        jsonType.setName(parameter.getName());
        String desc = parameter.getDescription();
        if(desc != null && desc.equals(parameter.getName())){
            desc = null;
        }
        jsonType.setDesc(desc);
        jsonType.setRequired(parameter.getRequired());
        if(parameter instanceof AbstractSerializableParameter){
            AbstractSerializableParameter param = (AbstractSerializableParameter) parameter;
            String type = param.getType();
            if(StringUtils.isEmpty( type )){
                if(param.getItems()!=null ){
                    type = "object";
                }
            }
            jsonType.setType(convertToJavaType(type,param.getFormat(),swagger.getDefinitions()));
        }
        if(parameter instanceof BodyParameter){
            methodInput.setReqType(ReqType.json.name());
            BodyParameter bodyParameter = (BodyParameter) parameter;


            processModel(jsonType, bodyParameter.getSchema(), swagger.getDefinitions(),new HashSet<>());
            if(methodInput.getBody() == null){
                methodInput.setBody(new ArrayList<>());
            }
            jsonType.setName("root");
            methodInput.getBody().add(jsonType.toJsonType());
        }else if(parameter instanceof FormParameter){
            methodInput.setReqType(ReqType.form.name());
            if(methodInput.getBody() == null){
                methodInput.setBody(new ArrayList<>());
            }
            methodInput.getBody().add(jsonType);
        }else if(parameter instanceof CookieParameter){ // 忽略cookie param

        }else if(parameter instanceof HeaderParameter){
            if(methodInput.getHeaders() == null){
                methodInput.setHeaders(new ArrayList<>());
            }
            methodInput.getHeaders().add( jsonType.toJsonType());
        }else if(parameter instanceof PathParameter){
            if(methodInput.getPath() == null){
                methodInput.setPath(new ArrayList<>());
            }
            methodInput.getPath().add((SimpleJsonType) jsonType.toJsonType());
        }else if(parameter instanceof QueryParameter){
            if(methodInput.getParams() == null){
                methodInput.setParams(new ArrayList<>());
            }
            JsonType paramType = jsonType.toJsonType();
            methodInput.getParams().add( paramType);

        }else if(parameter instanceof RefParameter){

        }
        return jsonType;
    }
    private JsonType jsfParameterToJsonType(Parameter parameter,Swagger swagger){
        BuilderJsonType jsonType = new BuilderJsonType();
        jsonType.setName(parameter.getName());
        String desc = parameter.getDescription();
        if(desc != null && desc.equals(parameter.getName())){
            desc = null;
        }
        jsonType.setDesc(desc);
        jsonType.setRequired(parameter.getRequired());
        if(parameter instanceof AbstractSerializableParameter){
            AbstractSerializableParameter param = (AbstractSerializableParameter) parameter;
            String type = param.getType();
            if(StringUtils.isEmpty( type )){
                if(param.getItems()!=null ){
                    type = "object";
                }
            }
            jsonType.setType(convertToJavaType(type,param.getFormat(),swagger.getDefinitions()));
        }
        if(parameter instanceof BodyParameter){

            BodyParameter bodyParameter = (BodyParameter) parameter;


            processModel(jsonType, bodyParameter.getSchema(), swagger.getDefinitions(),new HashSet<>());
            String name = StringUtils.isEmpty(parameter.getName()) ? "root": parameter.getName();
            jsonType.setName(  name);

        }else if(parameter instanceof FormParameter){

        }else if(parameter instanceof CookieParameter){ // 忽略cookie param

        }else if(parameter instanceof HeaderParameter){

        }else if(parameter instanceof PathParameter){

        }else if(parameter instanceof QueryParameter){


        }else if(parameter instanceof RefParameter){

        }
        return jsonType.toJsonType();
    }
    private void processModel(BuilderJsonType current,Model model,Map<String,Model> modelMap,Set<Model> added){
        if(model instanceof RefModel){
            Model actualModel = modelMap.get(((RefModel) model).getSimpleRef());
            if(!added.contains(actualModel)){
                added.add(actualModel);
                processModel(current,actualModel,modelMap,added);
                added.remove(actualModel);
            }else{
                if(current.getType() == null){
                    current.setType(getModelType(actualModel,modelMap,new HashSet<>()));
                }
            }

            return;
        }
        if(model instanceof ModelImpl){
            ModelImpl modelImpl = (ModelImpl) model;
            current.setType(convertToJavaType(modelImpl.getType(),
                    modelImpl.getFormat(),modelMap));
            processModel(current,model.getProperties(),modelMap,added);
        }else if(model instanceof ComposedModel){ // 复合model暂时忽略child，只考虑普通属性
            ComposedModel composedModel = (ComposedModel) model;
            current.setType("object");
            processModel(current,composedModel.getProperties(),modelMap,added);
        }else if(model instanceof ArrayModel){
            current.setType("array");
            ArrayModel arrayModel = (ArrayModel) model;
            Map<String, Property> props = new HashMap<>();
            props.put("$$0",arrayModel.getItems());
            processModel(current,props,modelMap,added);
        }
    }

    /**
     *
     * @param model
     * @param modelMap
     * @param alreadyProcessed 防止递归引用，加一个判别条件
     * @return
     */
    private String getModelType(Model model,Map<String,Model> modelMap,Set<Model> alreadyProcessed){
        if(model instanceof RefModel){
            Model actualModel = modelMap.get(((RefModel) model).getSimpleRef());
            if(alreadyProcessed.contains(actualModel)){
                return "object";
            }
            return getModelType(model,modelMap,alreadyProcessed);
        }
        if(model instanceof ModelImpl){
            ModelImpl modelImpl = (ModelImpl) model;
            return convertToJavaType(modelImpl.getType(),
                    modelImpl.getFormat(),modelMap);
        }else if(model instanceof ComposedModel){ // 复合model暂时忽略child，只考虑普通属性
            return "object";
        }else if(model instanceof ArrayModel){
            return "array";
        }
        return "object";
    }

    /**
     *
     * @param parent
     * @param properties
     * @param modelMap
     * @param alreadyProcessed 解决死循环的问题
     */
    private void processModel(BuilderJsonType parent,Map<String,Property> properties,Map<String,Model> modelMap,Set<Model> alreadyProcessed){
        if(properties == null) return;
        for (Map.Entry<String, Property> propertyEntry : properties.entrySet()) {
            Property property = propertyEntry.getValue();

            BuilderJsonType child = new BuilderJsonType();
            child.setName(propertyEntry.getKey());
            child.setType(convertToJavaType(property, modelMap));
            child.setDesc(property.getDescription());
            child.setRequired(property.getRequired());
            parent.addChild(child);

            if(property instanceof RefProperty){
                Model actualModel = modelMap.get(((RefProperty) property).getSimpleRef());
                if(!alreadyProcessed.contains(actualModel)){
                    alreadyProcessed.add(actualModel);
                    processModel(child,actualModel,modelMap,alreadyProcessed);
                    alreadyProcessed.remove(actualModel);
                }
                continue;
            }

            if(property instanceof ObjectProperty){
                ObjectProperty objectProperty = (ObjectProperty) property;
                processModel(child,objectProperty.getProperties(),modelMap,alreadyProcessed);
            }else if(property instanceof ArrayProperty){
                ArrayProperty arrayModel = (ArrayProperty) property;

                Map<String,Property> props = new HashMap<>();
                props.put("$$0",arrayModel.getItems());
                processModel(child,props,modelMap,alreadyProcessed);
            }
        }
    }
    private String convertToJavaType(Property property,Map<String,Model> modelMap){
        if(property instanceof RefProperty){
            Model actualModel = modelMap.get(((RefProperty) property).getSimpleRef());
            return getModelType(actualModel,modelMap,new HashSet<>());
        }
        String specialType =  propTypeMapping.get(property.getClass());
        String type = property.getType();

        if(specialType != null){
            return specialType;
        }else if("array".equals(type) || "object".equals(type)) {
            return type;
        }else if(SimpleParamType.from(type) != null){
            return type;
        }
        return "string";
        //throw new StdException("found not support type:"+type);
    }

    private String convertToJavaType(String swaggerType, String format,Map<String,Model> models){
        Property property = PropertyBuilder.build(swaggerType,format,null);
        return convertToJavaType(property,models);
    }


}
