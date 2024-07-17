package com.jd.workflow.console.service.parser.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jd.fastjson.JSON;
import com.jd.fastjson.JSONObject;
import com.jd.workflow.console.service.impl.ApiModelServiceImpl;
import com.jd.workflow.console.service.model.IApiModelService;
import com.jd.workflow.console.service.parser.ParserService;
import com.jd.workflow.console.service.parser.ParserUtils;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/24
 */
@Service
public class ParserServiceImpl implements ParserService {

    @Autowired
    private IApiModelService apiModelService;

    /**
     * 解析 jsonObject 里的所有属性
     *
     * @param childJsonTypes   兼容class类型解析 要把子属性放到这里（子属性JSONType集合）。
     * @param parserJsonObject       解析的jsonObject 对象。
     * @param dealKey          兼容 return类型 解析 jsonObject指定属性数据。
     * @param parentJsonObject 加绒 retrun 带泛型 ，找类对象时查找不到。
     * @return
     */
    public BuilderJsonType parseJsonType(List<String> parentClassList, List<BuilderJsonType> childJsonTypes, JSONObject parserJsonObject, String dealKey, JSONObject parentJsonObject) {
        BuilderJsonType jsonType = null;
        for (Map.Entry<String, Object> stringObjectEntry : parserJsonObject.entrySet()) {
            String key = stringObjectEntry.getKey();
            // 正常处理小写字母开通的  ， return 需要处理指定属性
            if ((!StringUtils.isEmpty(dealKey) && Objects.equals(key, dealKey)) || (StringUtils.isEmpty(dealKey) && !ParserUtils.isFirstLetterUpperCase(key))) {
                String value = String.valueOf(stringObjectEntry.getValue());
                // javaSdk里的类
                if (ParserUtils.isJdkClass(value)) {
                    BuilderJsonType propertyJsonType = new BuilderJsonType();
                    propertyJsonType.setName(key);
                    apiModelService.initParserTypeInfoAndChild(null, value, propertyJsonType);
                    dealGenericType(parentClassList, parserJsonObject, propertyJsonType, parentJsonObject);
                    if (Objects.nonNull(childJsonTypes)) {
                        childJsonTypes.add(propertyJsonType);
                    }
                    jsonType = propertyJsonType;
                } else {
                    BuilderJsonType propertyJsonType = new BuilderJsonType();
                    propertyJsonType.setName(key);
                    apiModelService.initParserTypeInfoAndChild(null, value, propertyJsonType);
                    //获取类名
                    String className = ParserUtils.getClassName(value);
                    //获取类名属性的JSONObject 描述
                    JSONObject childClassJsonInfo = parserJsonObject.getJSONObject(className);
                    //解决 返回值类型 泛型不准 需要取父层级找寻类属性的JSONObject
                    if (Objects.isNull(childClassJsonInfo) && Objects.nonNull(parentJsonObject)) {
                        childClassJsonInfo = parentJsonObject.getJSONObject(className);
                    }
                    if (Objects.nonNull(childClassJsonInfo) && !parentClassList.contains(propertyJsonType.getClassName())) {
                        //防止类循环依赖解析（死循环）
                        List<String> newParentClassList = new ArrayList<>();
                        newParentClassList.addAll(parentClassList);
                        newParentClassList.add(propertyJsonType.getClassName());
                        //当是类泛型时，推测Object属性类型
                        ParserUtils.inferPropertyType(parserJsonObject, value);
                        List<BuilderJsonType> childJsonTypeList = new ArrayList<>();
                        parseJsonType(newParentClassList, childJsonTypeList, childClassJsonInfo, null, parentJsonObject);
                        for (BuilderJsonType type : childJsonTypeList) {
                            dealGenericType(newParentClassList, parserJsonObject, type, parentJsonObject);
                            propertyJsonType.addChild(type);
                        }
                    }
                    dealGenericType(parentClassList, parserJsonObject, propertyJsonType, parentJsonObject);
                    if (Objects.nonNull(childJsonTypes)) {
                        childJsonTypes.add(propertyJsonType);
                    }
                    jsonType = propertyJsonType;
                }
            }
        }
        return jsonType;
    }

    /**
     * 处理带泛型的数据 添加对象属性数据
     *
     * @param jsonObject      解析的JSONObj对象
     * @param propertyJsonType JSONObj对象里属性对应的JsonType Result<T> 里的Result
     */
    private void dealGenericType(List<String> parentClassList, JSONObject jsonObject, BuilderJsonType propertyJsonType, JSONObject parentJsonObject) {
        List<JsonType> genericTypes = propertyJsonType.getGenericTypes();
        //是范型类型
        if (CollectionUtils.isNotEmpty(genericTypes)) {
            //解析下范型类的类对象 把子属性 放到 child里
            dealGenericClassType(parentClassList, jsonObject, propertyJsonType, parentJsonObject);
            for (JsonType genericType : genericTypes) {
                dealGenericClassType(parentClassList, jsonObject, (BuilderJsonType) genericType, parentJsonObject);
            }
        }
    }

    /**
     * 处理泛型相关类
     * 解析 builderJsonType 类对象属性 把子属性 放到 child里
     *
     * @param jsonObject
     * @param builderJsonType
     */
    private void dealGenericClassType(List<String> parentClassList, JSONObject jsonObject, BuilderJsonType builderJsonType, JSONObject parentJsonObject) {
        if (!ParserUtils.isJdkClass(builderJsonType.getClassName())) {
            String className = ParserUtils.getClassName(builderJsonType.getClassName());
            JSONObject parserChildClassJsonInfo = jsonObject.getJSONObject(className);
            if (Objects.nonNull(parserChildClassJsonInfo) && !parentClassList.contains(builderJsonType.getClassName())) {
                List<BuilderJsonType> childJsonTypeList = new ArrayList<>();
                //防止死循环
                List<String> newParentClassList = new ArrayList<>();
                newParentClassList.addAll(parentClassList);
                newParentClassList.add(builderJsonType.getClassName());
                parseJsonType(newParentClassList, childJsonTypeList, parserChildClassJsonInfo, null, parentJsonObject);
                builderJsonType.setChildren(childJsonTypeList);
            }
        }
    }
}
