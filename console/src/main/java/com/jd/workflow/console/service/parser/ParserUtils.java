package com.jd.workflow.console.service.parser;

import com.jd.common.util.StringUtils;
import com.jd.fastjson.JSONObject;
import com.jd.workflow.soap.common.util.StringHelper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/24
 */
public class ParserUtils {

    /**
     * 判断首字母是否大写
     *
     * @param str
     * @return true 首字母大写 ，false 首字母非大写
     */
    public static boolean isFirstLetterUpperCase(String str) {
        return StringUtils.isNotBlank(str) && Character.isUpperCase(str.charAt(0));
    }

    private static List<String> NORMAL_TYPE_LIST = Arrays.asList("int", "long", "double", "float",
            "boolean", "char", "byte", "short",
            "java.lang.Integer", "java.lang.Long", "java.lang.Double", "java.lang.Float", "java.lang.Object",
            "java.lang.Boolean", "java.lang.Character", "java.lang.Byte", "java.lang.Short", "java.lang.String", "java.util.Map");


    public static String getClassName(String classFullName) {
        String className = classFullName;
        if (StringUtils.isNotBlank(classFullName) && classFullName.contains(".")) {
            //如果是泛型类型 去掉泛型部分
            int index = classFullName.indexOf('<');
            if (index != -1) {
                classFullName = classFullName.substring(0, index);
            }
            className = StringHelper.lastPart(classFullName, '.');
        }
        return className;
    }

    private static List<String> BASE_TYPE_LIST = Arrays.asList("int", "long", "double", "float",
            "boolean", "char", "byte", "short");

    public static boolean isJdkClass(String classFullName) {
        return StringUtils.isNotBlank(classFullName) && (classFullName.startsWith("java") || BASE_TYPE_LIST.contains(classFullName));
    }

    /**
     * 获取类型
     *
     * @param type
     * @return
     */
    public static String parseType(String type) {
        final String typeName = getClassName(type);
        return StringUtils.isNotBlank(typeName) ? typeName.toLowerCase() : "";
    }

    /**
     * 是否为java 常用类型
     *
     * @param className
     * @return true 是java 通用类型 ，False 是自定义类对象
     */
    public static boolean isNormalType(String className) {
        return StringUtils.isNotBlank(className) && NORMAL_TYPE_LIST.contains(className);
    }

    /**
     * 判断是否是Object 类型
     *
     * @param className
     * @return true Object类型 ，False 非Object类型
     */
    public static boolean isObjectType(String className) {
        return Objects.equals("java.lang.Object", className);
    }

    public static String getMethodName(String dataStr) {
        return doPattern(dataStr,".* (.*?)\\(");
    }

    public static String getGenericContext(String dataStr) {
        return doPattern(dataStr,".*?<(.*)>");
    }


    /**
     * 执行正则匹配
     * @param dataStr
     * @param patternStr
     * @return
     */
    public static String doPattern(String dataStr,String patternStr) {
        String retStr = "";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(dataStr);
        if (matcher.find()) {
            retStr = matcher.group(1);
        }
        return retStr;
    }

    public static void main(String[] args) {
        final String methodName = ParserUtils.getGenericContext("com.jd.cjg.flow.sdk.model.result.FlowResult<Result<java.lang.Long>>");
        int i = 0;
    }

    /**
     * 推断泛型类属性类型 ，如果只有一个Object类型 把泛型类型放过去
     * @param parentJsonObject
     * @param returnType
     */
    public static void inferPropertyType(JSONObject parentJsonObject, String returnType) {
        if(returnType.indexOf("<") != -1){
            //获取泛型里的类型
            String genericContext = ParserUtils.getGenericContext(returnType);
            //有多个泛型类型 无法推断
            if(genericContext.contains(",")){
                return;
            }
            //获取泛型所有类类型
            String retClassName = ParserUtils.getClassName(returnType);
            JSONObject returnClassJsonInfo = parentJsonObject.getJSONObject(retClassName);
            if (Objects.nonNull(returnClassJsonInfo)) {
                List<String> objectTypeKeys = new ArrayList<>();
                for (Map.Entry<String, Object> entry : returnClassJsonInfo.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        String  type = (String) entry.getValue();
                        if(Objects.equals("java.lang.Object",type)){
                            objectTypeKeys.add(entry.getKey());
                        }
                    }
                }
                if (objectTypeKeys.size() == 1 && StringUtils.isNotBlank(genericContext)) {
                    returnClassJsonInfo.put(objectTypeKeys.get(0), genericContext);
                }
            }
        }
    }

}
