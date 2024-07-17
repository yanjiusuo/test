package com.jd.workflow.codegen;

import com.jd.workflow.codegen.model.*;
import com.jd.workflow.codegen.model.type.*;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.xml.schema.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApiModelHelper {
    static Map<SimpleParamType, String> simpleType2JsType = new HashMap();

    static {
        simpleType2JsType.put(SimpleParamType.LONG, "number");
        simpleType2JsType.put(SimpleParamType.DOUBLE, "number");
        simpleType2JsType.put(SimpleParamType.STRING, "string");
        simpleType2JsType.put(SimpleParamType.FLOAT, "number");
        simpleType2JsType.put(SimpleParamType.FILE, "File");
        simpleType2JsType.put(SimpleParamType.INTEGER, "number");
        simpleType2JsType.put(SimpleParamType.BOOLEAN, "boolean");
        simpleType2JsType.put(SimpleParamType._TEXT, "string");
        simpleType2JsType.put(SimpleParamType._ATTR, "string");
    }

    private static String getHttpMethod(String method) {
        if (StringHelper.isEmpty(method)) return "get";
        List<String> methods = StringHelper.split(method, ",");
        return methods.get(0).toLowerCase();
    }

    public static MethodModel toMethod(HttpMethodModel httpMethodModel, ApiModel apiModel, ClassModelCollector collector) {
        MethodModel method = new MethodModel();
        method.setApiModel(apiModel);
        String methodCode = httpMethodModel.getMethodCode();
        method.setUrl(httpMethodModel.getInput().getUrl());
        method.setDesc(httpMethodModel.getDesc());
        if (methodCode == null) {
            methodCode = StringHelper.lastPart(httpMethodModel.getInput().getUrl(), '/');
        }
        method.setHttpMethod(getHttpMethod(httpMethodModel.getInput().getMethod()));
        method.setMethodName(methodCode);
        method.setInputs(newParams(httpMethodModel, method, collector));
        method.setReturnType(newBodyParam(method, httpMethodModel.getOutput().getBody(), collector));
        return method;
    }

    private static Param newParam(MethodModel methodModel, List<? extends JsonType> jsonTypes, String paramPrefix, ClassModelCollector collector, boolean hidden) {
        if (ObjectHelper.isEmpty(jsonTypes)) return null;
        Param param = new Param();
        /*if (jsonTypes.size() == 1 && jsonTypes.get(0) instanceof SimpleJsonType) {
            JsonType jsonType = jsonTypes.get(0);

            param.setName(jsonType.getName());
            param.setType(jsonType2ClassModel(jsonType,collector));
        } else {*/
        String simpleClassName = StringHelper.capitalize(methodModel.getMethodName()) + paramPrefix;
        String className = methodModel.getApiModel().getPkgName() + "." + simpleClassName;
        if (StringHelper.isEmpty(methodModel.getApiModel().getPkgName())) {
            className = simpleClassName;
        }
        param.setName(StringHelper.decapitalize(simpleClassName));
        EntityClassModel classModel = new EntityClassModel();
        classModel.setHidden(hidden);
        classModel.setClassName(className);
        GenericClassModel genericClassModel = new GenericClassModel(classModel);
        genericClassModel.addImplClassModel(classModel);
        collector.add(className, genericClassModel);
        for (JsonType jsonType : jsonTypes) {
            ((EntityClassModel) classModel).addField(jsonType2FieldModel(jsonType, collector, methodModel));
        }
        classModel.setJsType(classModel.getName());
        param.setType(classModel);
        //}
        return param;
    }

    private static FieldModel jsonType2FieldModel(JsonType jsonType, ClassModelCollector collector, MethodModel methodModel) {
        FieldModel field = new FieldModel();

        field.setRequired(jsonType.isRequired());
        if (StringHelper.isNotBlank(jsonType.getDesc())) {
            field.setDesc(jsonType.getDesc());
        }
        field.setName(jsonType.getName());

        if (jsonType.getTypeVariableName() != null) {
            TypeVariable typeVariable = new TypeVariable(jsonType.getTypeVariableName());
            field.setType(typeVariable);
            typeVariable.setBindType(jsonType2ClassModel(jsonType, collector, methodModel));
        } else {
            field.setType(jsonType2ClassModel(jsonType, collector, methodModel));
        }


        return field;

    }

    private static String getSimpleTypeClassName(SimpleJsonType jsonType) {
        if (StringHelper.isNotBlank(jsonType.getClassName())) return jsonType.getClassName();
        return SimpleParamType.from(jsonType.getType()).getType().getName();
    }

    private static String getJsType(SimpleParamType simpleParamType) {

        return simpleType2JsType.get(simpleParamType);
    }


    public static IClassModel makeNew(String className, ObjectJsonType jsonType, ClassModelCollector collector, MethodModel methodModel) {
        IClassModel classModel = null;
        if (className.startsWith("java.")) {
            classModel = new ReferenceClassModel();
        } else {
            classModel = new EntityClassModel();
            for (JsonType child : ((ObjectJsonType) jsonType).getChildren()) {
                ((EntityClassModel) classModel).addField(jsonType2FieldModel(child, collector, methodModel));
            }
        }

        classModel.setDesc(jsonType.getDesc());
        classModel.setClassName(className);
        classModel.setRelatedMethod(methodModel);
        return classModel;
    }

    private static IClassModel makeObjectModel(ObjectJsonType jsonType, ClassModelCollector collector, MethodModel methodModel) {

        IClassModel model = makeNew(getObjJsonType((ObjectJsonType) jsonType), (ObjectJsonType) jsonType, collector, methodModel);

        model.setJsType(model.getName());
        return model;
    }

    private static String getObjJsonType(ObjectJsonType jsonType) {
        String className = jsonType.getClassName();
        if (StringHelper.isEmpty(className)) {
            className = Object.class.getName();
        }
        return className;
    }

    private static IClassModel jsonType2ClassModel(JsonType jsonType, ClassModelCollector collector, MethodModel methodModel) {
        IClassModel result = null;
        if (jsonType instanceof SimpleJsonType) {
            SimpleClassModel classModel = new SimpleClassModel();
            classModel.setJsType(getJsType(SimpleParamType.from(jsonType.getType())));
            classModel.setClassName(getSimpleTypeClassName((SimpleJsonType) jsonType));
            classModel.setDesc(jsonType.getDesc());
            result = classModel;
        } else if (jsonType instanceof ObjectJsonType) {
            ObjectJsonType objJsonType = (ObjectJsonType) jsonType;
            IClassModel ret = null;
            String className = getObjJsonType((ObjectJsonType) jsonType);
            if (StringHelper.isEmpty(className)) {
                className = Object.class.getName();
            }
            GenericClassModel classModel = collector.get(className);
            if (classModel == null) {

                //  IClassModel genericModel = makeObjectModel((ObjectJsonType) jsonType,collector);// 这里实现类与非实现类区分下
                IClassModel model = makeObjectModel((ObjectJsonType) jsonType, collector, methodModel);

                classModel = new GenericClassModel(model);
                classModel.addImplClassModel(model);
                collector.add(className, classModel);

                ret = model;
            } else {
                IClassModel model = makeNew(className, (ObjectJsonType) jsonType, collector, methodModel);
                classModel.addImplClassModel(model);
                model.setJsType(model.getName());
                ret = model;
            }
            result = ret;
        } else if (jsonType instanceof ArrayJsonType) {
            ArrayJsonType arrayJsonType = (ArrayJsonType) jsonType;
            ArrayClassModel classModel = new ArrayClassModel();


            classModel.setJsType("Array");
            classModel.getFormalParams().add("T");

            IClassModel actual = classModel.clone();
            if (arrayJsonType.getChildren().size() == 1) {
                actual.getGenericTypes().add(jsonType2ClassModel(arrayJsonType.getChildren().get(0), collector, methodModel));
            }
            for (JsonType child : arrayJsonType.getChildren()) {
                classModel.getChildren().add(jsonType2ClassModel(child, collector, methodModel));
            }
            result = classModel;
            return result;
        } else if (jsonType instanceof StringJsonType) {
            SimpleClassModel classModel = new SimpleClassModel();
            classModel.setJsType("string");
            classModel.setClassName(String.class.getName());
            classModel.setDesc(jsonType.getDesc());
            result = classModel;
        }
        if (jsonType != null && !ObjectHelper.isEmpty(jsonType.getGenericTypes())) {
            for (JsonType genericType : jsonType.getGenericTypes()) {
                result.getFormalParams().add(genericType.getTypeVariableName());
                IClassModel classModel = jsonType2ClassModel(genericType, collector, methodModel);

                result.getGenericTypes().add(classModel);
            }
        }


        return result;
    }

    private static void addIfNotNull(List<Param> result, Param param) {
        if (param == null) return;
        result.add(param);
    }

    private static Param newBodyParam(MethodModel methodModel, List<? extends JsonType> bodyTypes, ClassModelCollector collector) {
        if (ObjectHelper.isEmpty(bodyTypes)) return null;
        Param param = new Param();
        param.setName("body");
        JsonType jsonType = bodyTypes.get(0);
        param.setType(jsonType2ClassModel(jsonType, collector, methodModel));
        return param;
    }

    private static List<Param> newParams(HttpMethodModel methodModel, MethodModel method, ClassModelCollector collector) {
        List<Param> result = new LinkedList<>();
        Param path = newParam(method, methodModel.getInput().getPath(), "Path", collector, false);
        if (path != null) {
            path.setParamType(ParamType.path.name());
        }
        Param param = newParam(method, methodModel.getInput().getParams(), "Param", collector, false);
        if (param != null) {
            param.setParamType(ParamType.query.name());
        }
        Param headers = newParam(method, methodModel.getInput().getHeaders(), "Header", collector, false);
        if (headers != null) {
            headers.setParamType(ParamType.header.name());
        }
        addIfNotNull(result, path);
        addIfNotNull(result, param);
        addIfNotNull(result, headers);
        if ("form".equals(methodModel.getInput().getReqType())) {
            Param bodyParam = newParam(method, methodModel.getInput().getBody(), "Form", collector, false);
            if (bodyParam != null) {
                bodyParam.setParamType(ParamType.body.name());
            }
            addIfNotNull(result, bodyParam);
        } else {
            Param bodyParam = newBodyParam(method, methodModel.getInput().getBody(), collector);
            if (bodyParam != null) {
                bodyParam.setParamType(ParamType.body.name());
            }
            addIfNotNull(result, bodyParam);
        }
        return result;
    }

}
