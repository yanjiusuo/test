package com.jd.workflow.soap.common.parser;


import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 Class：不仅包括我们平常所指的类、枚举、数组、注解，还包括基本类型int、float等等。
 TypeVariable：比如List 中的T等。
 WildcardType：也叫做泛型表达式类型，例如List<? extends Number> 这种。
 ParameterizedType：就是我们平常所用到的泛型List、Map（注意和TypeVariable的区别，参数化类型表示的是List这样的一个整体而不是T）。
 GenericArrayType：泛型数组类型，并不是我们工作中所使用的数组String[]、Byte[]，这些都是Class，而是带有泛型的数组，即T[]。

 原文链接：https://blog.csdn.net/tianzhonghaoqing/article/details/119705014
 */
public class ClassParser {
    private static final Logger logger = LoggerFactory.getLogger(ClassParser.class);
   static DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public ClassParser() {
    }

    public static MethodMetadata buildMethodInfo(Method method) {

        MethodMetadata metadata = new MethodMetadata();
        metadata.setInterfaceName(method.getDeclaringClass().getName());
        metadata.setMethodName(method.getName());



        if (method == null) {
            throw new StdException("jsf.err_parse_clazz_metadata_miss_methodName")
                    .param("class",method.getClass().getName())
                    .param("methodName",method.getName());
        } else {
            Class[] exceptions = method.getExceptionTypes();
            int i;
            if (exceptions.length > 0) {
                metadata.setExceptions(new String[exceptions.length]);
                for(i = 0; i < exceptions.length; ++i) {
                    Class exception = exceptions[i];
                    metadata.getExceptions()[i] = exception.getCanonicalName();

                }
            }
            Type returnType = method.getGenericReturnType();
            JsonType outputType = null;
            if("void".equals(returnType.getTypeName())){

            }else{
                BuilderJsonType returnParentType = new BuilderJsonType();
                returnParentType.setName("root");
                TypeConverterRegistry.processJsonType(returnType,new HashSet<>(),returnParentType,null);
                outputType = returnParentType.toJsonType();
            }


            metadata.setOutput(outputType);
            Type[] paramsType = method.getGenericParameterTypes();
            i = 1;

            List<JsonType> inputParams = new ArrayList<>();
            metadata.setInput(inputParams);
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
            for(int paramIndex = 0; paramIndex < paramsType.length; ++paramIndex) {
                BuilderJsonType paramType = new BuilderJsonType();
                if(parameterNames == null || parameterNames.length <= paramIndex){
                    paramType.setName("arg"+paramIndex);
                }else{
                    paramType.setName(parameterNames[paramIndex]);
                }

                TypeConverterRegistry.processJsonType(paramsType[paramIndex],new HashSet<>(),paramType,null);
                inputParams.add(paramType.toJsonType());
                ++i;
            }
            return metadata;
        }
    }
    public static MethodMetadata buildMethodInfo(Class clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        Method method = null;
        MethodMetadata metadata = new MethodMetadata();
        metadata.setInterfaceName(clazz.getName());
        metadata.setMethodName(methodName);

        for(int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            if (m.getName().equals(methodName)) {
                method = m;
            }
        }

        if (method == null) {
            throw new StdException("jsf.err_parse_clazz_metadata_miss_methodName")
                    .param("class",clazz.getName())
                    .param("methodName",methodName);
        }
        return buildMethodInfo(method);
    }
}
