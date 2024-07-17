package com.jd.workflow.soap.common.util;

import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参考： TemplateRegisty.class
 */
public class TypeUtils {
    private static final Map<String, String> BASE_TO_FLOW_TYPES = new HashMap<>();
    private static final Map<Class, String> SIMPLE_TYPES = new HashMap<>();
    private static final Map<String, String> WRAP_TYPES = new HashMap<>();

    static {

        // object \ array \  long double string float integer boolean    --- char byte short

        BASE_TO_FLOW_TYPES.put("int", "integer");
        BASE_TO_FLOW_TYPES.put("long", "long");
        BASE_TO_FLOW_TYPES.put("double", "double");
        BASE_TO_FLOW_TYPES.put("float", "float");
        BASE_TO_FLOW_TYPES.put("boolean", "boolean");
        BASE_TO_FLOW_TYPES.put("char", "string");
        BASE_TO_FLOW_TYPES.put("byte", "integer");
        BASE_TO_FLOW_TYPES.put("short", "integer");

        WRAP_TYPES.put("int", "java.lang.Integer");
        WRAP_TYPES.put("long", "java.lang.Long");
        WRAP_TYPES.put("double", "java.lang.Double");
        WRAP_TYPES.put("float", "java.lang.Float");
        WRAP_TYPES.put("boolean", "java.lang.Boolean");
        WRAP_TYPES.put("char", "java.lang.Character");
        WRAP_TYPES.put("byte", "java.lang.Byte");
        WRAP_TYPES.put("short", "java.lang.Byte");


        BASE_TO_FLOW_TYPES.put("java.lang.Integer", "integer");
        BASE_TO_FLOW_TYPES.put("java.lang.Long", "long");
        BASE_TO_FLOW_TYPES.put("java.lang.Double", "float");
        BASE_TO_FLOW_TYPES.put("java.lang.Float", "float");
        BASE_TO_FLOW_TYPES.put("java.lang.Boolean", "boolean");
        BASE_TO_FLOW_TYPES.put("java.lang.Character", "string");
        BASE_TO_FLOW_TYPES.put("java.lang.Byte", "integer");
        BASE_TO_FLOW_TYPES.put("java.lang.Short", "integer");

        BASE_TO_FLOW_TYPES.put("java.lang.String", "string");

        SIMPLE_TYPES.put(BigDecimal.class, "string");
        SIMPLE_TYPES.put(String.class, "string");
        SIMPLE_TYPES.put(BigInteger.class, "string");
        SIMPLE_TYPES.put(Date.class, "string");
        SIMPLE_TYPES.put(java.util.Date.class, "string");
        SIMPLE_TYPES.put(Timestamp.class, "long");
        SIMPLE_TYPES.put(URL.class, "string");
        SIMPLE_TYPES.put(URI.class, "string");
        SIMPLE_TYPES.put(Charset.class, "string");
        SIMPLE_TYPES.put(ByteBuffer.class, "string");
        SIMPLE_TYPES.put(Class.class, "string");
        SIMPLE_TYPES.put(Calendar.class, "string");
        SIMPLE_TYPES.put(Locale.class, "string");
        SIMPLE_TYPES.put(TimeZone.class, "string");
        SIMPLE_TYPES.put(UUID.class, "string");

        SIMPLE_TYPES.put(Time.class, "string");
        SIMPLE_TYPES.put(LocalTime.class, "string");
        SIMPLE_TYPES.put(java.util.Date.class, "string");
        SIMPLE_TYPES.put(LocalDate.class, "string");
        SIMPLE_TYPES.put(LocalDateTime.class, "string");
        SIMPLE_TYPES.put(File.class, "string");

        //PRIMITIVE_TYPES.put("java.lang.String", java.lang.String.class);

        //PRIMITIVE_TYPES.put("void", Void.TYPE);
    }
    public static String getSimpleType(Class clazz){
       /* for (Map.Entry<Class, String> entry : SIMPLE_TYPES.entrySet()) {
            if(entry.getKey().getCanonicalName().equals(canonicalClassName)){
                return entry.getValue();
            }
        }*/
        return SIMPLE_TYPES.get(clazz);
    }
    public static String getSimpleTypeByShortTypeName(String shortName){
        for (Map.Entry<String, String> entry : BASE_TO_FLOW_TYPES.entrySet()) {
            String name = StringHelper.lastPart(entry.getKey(), '.');
            if(name.equals(shortName)) return entry.getValue();
        }
        for (Map.Entry<Class, String> entry : SIMPLE_TYPES.entrySet()) {
            String name = StringHelper.lastPart(entry.getKey().getName(), '.');
            if(name.equals(shortName)) return entry.getValue();
        }
        return null;
    }
    public static String getSimpleType(String name){
        for (Map.Entry<Class, String> entry : SIMPLE_TYPES.entrySet()) {
            if(entry.getKey().getName().equals(name)) return entry.getValue();
        }
        return null;
    }
    public static String getWrapClassName(String className){
        return WRAP_TYPES.get(className);
    }
    public static String getPrimitiveType(String canonicalClassName){
        return BASE_TO_FLOW_TYPES.get(canonicalClassName);
    }
    public static Class getClass(SimpleJsonType jsonType) throws ClassNotFoundException {
        String className = jsonType.getClassName();
        if(StringUtils.isEmpty(className)){
            SimpleParamType type = SimpleParamType.from(jsonType.getType());
            if(type == null) return null;
            className = type.getType().getName();
        }

        return getClass(className);
    }
    public static Class getClass(String typeStr) throws  ClassNotFoundException {

            Class clazz = null;
            if ("void".equals(typeStr)) {
                clazz = Void.TYPE;
            } else if ("boolean".equals(typeStr)) {
                clazz = Boolean.TYPE;
            } else if ("byte".equals(typeStr)) {
                clazz = Byte.TYPE;
            } else if ("char".equals(typeStr)) {
                clazz = Character.TYPE;
            } else if ("double".equals(typeStr)) {
                clazz = Double.TYPE;
            } else if ("float".equals(typeStr)) {
                clazz = Float.TYPE;
            } else if ("int".equals(typeStr)) {
                clazz = Integer.TYPE;
            } else if ("long".equals(typeStr)) {
                clazz = Long.TYPE;
            } else if ("short".equals(typeStr)) {
                clazz = Short.TYPE;
            } else {
                String jvmName = canonicalNameToJvmName(typeStr);
                clazz = Class.forName(jvmName);
            }



            return clazz;

    }
    private static String canonicalNameToJvmName(String typeStr) {
        boolean isarray = typeStr.endsWith("[]");
        if (isarray) {
            String t;
            for(t = ""; isarray; isarray = typeStr.endsWith("[]")) {
                typeStr = typeStr.substring(0, typeStr.length() - 2);
                t = t + "[";
            }

            if ("boolean".equals(typeStr)) {
                typeStr = t + "Z";
            } else if ("byte".equals(typeStr)) {
                typeStr = t + "B";
            } else if ("char".equals(typeStr)) {
                typeStr = t + "C";
            } else if ("double".equals(typeStr)) {
                typeStr = t + "D";
            } else if ("float".equals(typeStr)) {
                typeStr = t + "F";
            } else if ("int".equals(typeStr)) {
                typeStr = t + "I";
            } else if ("long".equals(typeStr)) {
                typeStr = t + "J";
            } else if ("short".equals(typeStr)) {
                typeStr = t + "S";
            } else {
                typeStr = t + "L" + typeStr + ";";
            }
        }

        return typeStr;
    }

    public static boolean isPrimitiveType(String canonicalClassName){
        return getPrimitiveType(canonicalClassName) != null;
    }

    public static boolean isCollection(Class clazz){
        return Collection.class.isAssignableFrom(clazz);
    }

    public static boolean isMap(Class clazz){
        return  Map.class.isAssignableFrom(clazz);
    }
}
