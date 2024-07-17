package com.jd.workflow.soap.common.util;


import com.jd.workflow.soap.common.exception.StdException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class BeanTool {
    static PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();


    public static void clearFieldValues(Object bean,String ...fields) {
        try{
            for (String field : fields) {
                if(propertyUtils.getPropertyDescriptor(bean,field) == null){
                    continue;
                }
                BeanUtilsBean.getInstance().getPropertyUtils().setProperty(bean,field,null);
            }
        }catch (Exception e){
            throw StdException.adapt("bean.set_field_error",e);
        }

    }
    public static boolean hasProp(Object bean,String field){
        if(bean == null) return false;
        try {
            return propertyUtils.describe(bean).containsKey(field);
        } catch (Exception e) {
            throw new StdException("bean.err_describe_bean",e);
        }
    }
    public static Object getProp(Object bean, String field){
        try {
            return propertyUtils.getProperty(bean,field);
        } catch (Exception e) {
            throw  StdException.adapt("bean.error_get_prop",e);
        }
    }
    public static void setProp(Object bean, String field,Object val){
        try {
             propertyUtils.setProperty(bean,field,val);
        } catch (Exception e) {
            throw  StdException.adapt("bean.error_get_prop:prop="+field+",val="+val,e);
        }
    }
    public static Object getField(Object bean, String fldName){
        try {
            Field field = FieldUtils.getDeclaredField(bean.getClass(), fldName,true);
            if(field == null){
                throw new StdException("bean.err_find_invalid_field:class"+bean.getClass().getName()+",fldName="+fldName);
            }
            return field.get(bean);
        } catch (Exception e) {
            throw  StdException.adapt("bean.error_get_field",e);
        }
    }
    public static Map getProps(Object bean, String... fields){
        Map<String,Object> map = new HashMap<>();
        for (String field : fields) {
            try {
                map.put(field,propertyUtils.getProperty(bean,field));
            } catch (Exception e) {
                throw  StdException.adapt("bean.error_get_prop",e);
            }
        }
        return map;
    }
    public static void copySpecialProps(Object source,Object target,String ...props){
        for (String prop : props) {
            try {
                propertyUtils.setProperty(target,prop,propertyUtils.getProperty(source,prop));
            } catch (Exception e) {
                throw StdException.adapt("bean.err_copy_props",e);
            }
        }
    }
    public static Map toMap(Object bean){
        return (Map) toJsonObj(bean);
    }
    private static Object toJsonObj(Object bean){
        if(bean == null) return null;
        if(
          bean.getClass().isPrimitive()
                || bean instanceof String
                || bean.getClass().isEnum()
                  || TypeUtils.getPrimitiveType(bean.getClass().getCanonicalName()) !=  null
        ){
            return bean;
        }else if(bean instanceof Map){
            Map map = (Map) bean;
            for (Object o : map.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                final Object castValue = toJsonObj(entry.getValue());
                if(castValue != entry.getValue()){
                    map.put(entry.getKey(),entry.getValue());
                }
                return map;
            }
        }else if(bean instanceof Collection){
            List list = new ArrayList(((Collection) bean).size());
            for (Object o : ((Collection) bean)) {
                Object castValue = toJsonObj(o);
                if(castValue != o){
                    list.add(castValue);
                }else{
                    list.add(o);
                }
            }
            return list;
        }else if(bean.getClass().isArray()){
            final int length = Array.getLength(bean);
            List list = new ArrayList(length);

            for (int i = 0; i < length; i++) {
                Object o = Array.get(bean,i);
                Object castValue = toJsonObj(o);
                if(castValue != o){
                    list.add(castValue);
                }else{
                    list.add(o);
                }
            }
            return list;
        }
        try{
            final Map<String, Object> properties = PropertyUtils.describe(bean);
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if(
                    !(PropertyUtils.isReadable(bean,entry.getKey())
                            && PropertyUtils.isWriteable(bean,entry.getKey())
                    )
                ){
                    continue;
                }
                final Object value = entry.getValue();
                final Object castValue = toJsonObj(value);
                if(castValue != value){
                    properties.put(entry.getKey(),castValue);
                }
            }
            return properties;
        }catch (Exception e) {
            throw StdException.adapt(e);
        }

    }

    public static Field[] getFields(Class<?> targetClass) {
        if (targetClass == null) {
            return new Field[0];
        } else {
            List<Field[]> succ = new ArrayList();

            Field[] result;
            for(Class c = targetClass; c != Object.class && c != null; c = c.getSuperclass()) {
                result = c.getDeclaredFields();
                succ.add(result);
            }

            List<Field> resultList = new ArrayList();
            Iterator var11 = succ.iterator();

            while(var11.hasNext()) {
                Field[] fields = (Field[])var11.next();
                Field[] var5 = fields;
                int var6 = fields.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    Field f = var5[var7];
                    int mod = f.getModifiers();
                    if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
                        f.setAccessible(true);
                        resultList.add(f);
                    }
                }
            }

            result = new Field[resultList.size()];

            for(int i = resultList.size() - 1; i >= 0; --i) {
                result[i] = (Field)resultList.get(i);
            }

            return result;
        }
    }
}
