package com.jd.workflow.jsf.cast;

import com.jd.workflow.jsf.cast.impl.*;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * 类似jsf注册中心
 * @see com.jd.org.msgpack.template.TemplateRegistry
 */
@Slf4j
public class JsfParamConverterRegistry {
    static Map<Class, JsfParamConverter> converterMap = new HashMap<>();
    static AnyParamConverter anyParamConverter = new AnyParamConverter();
    public static JsfParamConverter getConverter(Class targetClass) {
        if(targetClass == null) return null;
        JsfParamConverter converter = converterMap.get(targetClass);
        if (converter != null) {
            return converter;
        }
        return getSuperclassOrInterfaceTypes(targetClass);
    }

    static void registerConverter(Class clazz, JsfParamConverter converter) {
        converterMap.put(clazz,converter);
    }

    static {
        PrimitiveParamConverter primitiveParamConverter = new PrimitiveParamConverter();
        BigNumberConverter bigNumberConverter = new BigNumberConverter();
        registerConverter(boolean.class, primitiveParamConverter);
        registerConverter(Boolean.class, primitiveParamConverter);
        registerConverter(byte.class, primitiveParamConverter);
        registerConverter(Byte.class, primitiveParamConverter);
        registerConverter(short.class, primitiveParamConverter);
        registerConverter(Short.class, primitiveParamConverter);
        registerConverter(int.class, primitiveParamConverter);
        registerConverter(Integer.class, primitiveParamConverter);
        registerConverter(long.class, primitiveParamConverter);
        registerConverter(Long.class, primitiveParamConverter);
        registerConverter(float.class, primitiveParamConverter);
        registerConverter(Float.class, primitiveParamConverter);
        registerConverter(double.class, primitiveParamConverter);
        registerConverter(Double.class, primitiveParamConverter);
        registerConverter(BigInteger.class, bigNumberConverter);
        registerConverter(char.class, primitiveParamConverter);
        registerConverter(Character.class, primitiveParamConverter);
       /* registerConverter(boolean[].class, BooleanArrayTemplate.getInstance());
        registerConverter(short[].class, ShortArrayTemplate.getInstance());
        registerConverter(int[].class, IntegerArrayTemplate.getInstance());
        registerConverter(long[].class, LongArrayTemplate.getInstance());
        registerConverter(float[].class, FloatArrayTemplate.getInstance());
        registerConverter(double[].class, DoubleArrayTemplate.getInstance());*/
        registerConverter(String.class, primitiveParamConverter);
       // registerConverter(byte[].class, ByteArrayTemplate.getInstance());
        registerConverter(ByteBuffer.class, new ByteBufferConverter());

        registerConverter(BigDecimal.class, bigNumberConverter);
        registerConverter(java.util.Date.class, new UtilDateConverter());

        registerConverter(HashMap.class, new MapParamConveter());
        registerConverter(Map.class, new MapParamConveter());
        registerConverter(HashSet.class, new CollectionParamConverter());
        registerConverter(Collection.class, new CollectionParamConverter());
        registerConverter(Object.class, new AnyParamConverter());
        registerConverter(ArrayList.class, new CollectionParamConverter());
      //  registerConverter(char[].class, (Template) CharArrayTemplate.getInstance());
       // registerConverter(Character[].class, (Template) CharacterArrayTemplate.getInstance());
        registerConverter(Throwable.class, new ExceptionConverter());
        registerConverter(StackTraceElement.class, new StackTraceConverter());
        //registerConverter(StackTraceElement[].class, (Template) JSFStackTraceElementArrayTemplate.getInstance());
        //registerConverter(Invocation.class, (Template) InvocationTemplate.getInstance());
        registerConverter(java.sql.Date.class, new SqlDateConverter());
        registerConverter(Time.class, new TimeConverter());
        registerConverter(Timestamp.class, new TimestampConverter());
        registerConverter(File.class, new FileParamConverter());
        registerConverter(URL.class, new URLParamConverter());
        registerConverter(URI.class, new URIParamConverter());
        registerConverter(Charset.class, new StringParamConverter());
        registerConverter(Class.class,new StringParamConverter() );
        registerConverter(Calendar.class, new CalendarConverter());
        registerConverter(Locale.class, new LocaleConverter());
        registerConverter(TimeZone.class, new TimeZoneConverter());
        registerConverter(UUID.class, new UuidParamConverter());
    }

    private static JsfParamConverter getSuperclassOrInterfaceTypes(Class targetClass) {
        Class<?> superClass = targetClass.getSuperclass();
        JsfParamConverter tmpl = null;
        if (superClass != null) {
            for (; superClass != Object.class; superClass = superClass.getSuperclass()) {
                tmpl = lookupInterfaceTypes((Class) superClass);
                if (tmpl != null) {
                    return tmpl;
                }

            }
        }


        return tmpl;
    }

    private static JsfParamConverter lookupInterfaceTypes(Class targetClass) {
        Class<?>[] infTypes = targetClass.getInterfaces();
        JsfParamConverter tmpl = null;
        for (Class<?> infType : infTypes) {
            tmpl = getConverter(infType);
            if (tmpl != null) {
                if (!targetClass.isInterface() && tmpl.getClass().equals(AnyParamConverter.class)) {
                    tmpl = null;
                } else {

                    return tmpl;
                }
            } else {

            }

        }

        return tmpl;
    }
    private static boolean isByteArr(ArrayJsonType arrayJsonType){
        return arrayJsonType.getChildren().size() == 1 && "java.lang.Byte".equals( arrayJsonType.getChildren().get(0).getClassName());
    }
    private static boolean isCharArr(ArrayJsonType arrayJsonType){
        return arrayJsonType.getChildren().size() == 1 && "java.lang.Character".equals( arrayJsonType.getChildren().get(0).getClassName());
    }
    public static JsfParamConverter getConverter(JsonType jsonType) {
        String className = jsonType.getClassName();
        if (jsonType instanceof ArrayJsonType) {
            if(isByteArr((ArrayJsonType) jsonType)){// 字节数组的值为base64编码的数据
                return new ByteArrayParamConverter();
            }else if(isCharArr((ArrayJsonType) jsonType)){
                return new CharArrayParamConverter();
            }
            return new CollectionParamConverter();
        } else if (jsonType instanceof ObjectJsonType) {
            if (className != null && className.startsWith("java.")) {
                Class clazz = getClass(className);
                return getConverter(clazz);
            } else {
                return new JavaEntityParamConveter();
            }

        }else{

            // 简单类型
            try {
                return getConverter(TypeUtils.getClass((SimpleJsonType) jsonType));
            } catch (ClassNotFoundException e) {
                throw StdException.adapt(e);
            }
        }

    }
    public static Class getSimpleTypeClass(JsonType jsonType){
        if(StringUtils.isEmpty(jsonType.getClassName())){
            SimpleParamType type = SimpleParamType.from(jsonType.getType());
            if(type == null) return null;
            return type.getType();
        }
        // 简单类型
        return getClass(jsonType.getClassName());
    }

    public static Object convertValue(JsonType jsonType, Object value) {
        if(value == null || (value instanceof String && StringUtils.isEmpty((String)value))) return null;
        JsfParamConverter converter = JsfParamConverterRegistry.getConverter(jsonType);
        if(converter == null){
            converter = anyParamConverter;
            //System.out.println(1);
        }
        return converter.write(jsonType, value);
    }

    static Class getClass(String className) {
        if(StringUtils.isEmpty(className)) return null;
        try {
            return TypeUtils.getClass(className);
        } catch (ClassNotFoundException e) {
            throw StdException.adapt(e);
        }
    }

   public static Object buildDemoValue(JsonType jsonType) {
        if (jsonType instanceof SimpleJsonType) {
            final JsfParamConverter converter = JsfParamConverterRegistry.getConverter(jsonType);
            if(converter == null){
                return null;
            }
            //if (converter instanceof PrimitiveParamConverter) {
                try {
                    return converter.getDemoValue(TypeUtils.getClass((SimpleJsonType) jsonType));
                } catch (Exception e) {
                   log.error("jsf.err_build_demo_value:name={}",jsonType.getName(),e);
                }
           // }
        }
        return null;
    }

    public static void main(String[] args) {
        String data = "{\"name\":\"file\",\"type\":\"object\",\"className\":\"org.springframework.web.multipart.MultipartFile\",\"children\":[{\"name\":\"size\",\"type\":\"long\",\"className\":null},{\"name\":\"resource\",\"type\":\"object\",\"className\":\"org.springframework.core.io.Resource\",\"children\":[{\"name\":\"readable\",\"type\":\"boolean\",\"className\":null},{\"name\":\"file\",\"type\":\"object\",\"className\":\"java.io.File\",\"children\":[{\"name\":\"parent\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"parentFile\",\"type\":\"object\",\"className\":\"java.io.File\",\"children\":[]},{\"name\":\"hidden\",\"type\":\"boolean\",\"className\":null},{\"name\":\"freeSpace\",\"type\":\"long\",\"className\":null},{\"name\":\"totalSpace\",\"type\":\"long\",\"className\":null},{\"name\":\"usableSpace\",\"type\":\"long\",\"className\":null},{\"name\":\"canonicalFile\",\"type\":\"object\",\"className\":\"java.io.File\",\"children\":[]},{\"name\":\"directory\",\"type\":\"boolean\",\"className\":null},{\"name\":\"absoluteFile\",\"type\":\"object\",\"className\":\"java.io.File\",\"children\":[]},{\"name\":\"file\",\"type\":\"boolean\",\"className\":null},{\"name\":\"absolute\",\"type\":\"boolean\",\"className\":null},{\"name\":\"name\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"canonicalPath\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"absolutePath\",\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"filename\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"description\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"inputStream\",\"type\":\"object\",\"className\":\"java.io.InputStream\",\"children\":[]},{\"name\":\"uRI\",\"type\":\"object\",\"className\":\"java.net.URI\",\"children\":[{\"name\":\"rawFragment\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"userInfo\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"opaque\",\"type\":\"boolean\",\"className\":null},{\"name\":\"scheme\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"query\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"schemeSpecificPart\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"rawUserInfo\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"path\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"rawPath\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"fragment\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"rawSchemeSpecificPart\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"port\",\"type\":\"integer\",\"className\":null},{\"name\":\"absolute\",\"type\":\"boolean\",\"className\":null},{\"name\":\"rawAuthority\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"authority\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"host\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"rawQuery\",\"type\":\"string\",\"className\":\"java.lang.String\"}]},{\"name\":\"open\",\"type\":\"boolean\",\"className\":null},{\"name\":\"uRL\",\"type\":\"object\",\"className\":\"java.net.URL\",\"children\":[{\"name\":\"defaultPort\",\"type\":\"integer\",\"className\":null},{\"name\":\"path\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"userInfo\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"protocol\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"ref\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"file\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"port\",\"type\":\"integer\",\"className\":null},{\"name\":\"query\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"authority\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"host\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"content\",\"type\":\"object\",\"className\":\"java.lang.Object\",\"children\":[]}]}]},{\"name\":\"bytes\",\"type\":\"array\",\"className\":null,\"children\":[{\"name\":null,\"type\":\"integer\",\"className\":null}]},{\"name\":\"name\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"inputStream\",\"type\":\"object\",\"className\":\"java.io.InputStream\",\"children\":[]},{\"name\":\"contentType\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"originalFilename\",\"type\":\"string\",\"className\":\"java.lang.String\"},{\"name\":\"empty\",\"type\":\"boolean\",\"className\":null}]}";

        JsonType output = JsonUtils.parse(data, JsonType.class);
        Object value = output.toExprValue(new ValueBuilderAcceptor() {
            @Override
            public Object afterSetValue(Object value, JsonType jsonType) {
                final Object demoValue = buildDemoValue(jsonType);
                if (demoValue != null) return demoValue;
                return value;
            }
        });
        Object result = JsfParamConverterRegistry.convertValue(JsonUtils.parse(data, JsonType.class), value);
        System.out.println(int.class.getName());
    }


}
