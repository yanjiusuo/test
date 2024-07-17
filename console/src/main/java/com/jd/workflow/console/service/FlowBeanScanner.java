package com.jd.workflow.console.service;

import com.jd.businessworks.annotation.JtfBeanBind;
import com.jd.businessworks.annotation.JtfBeanInfo;
import com.jd.businessworks.annotation.JtfMethod;
import com.jd.businessworks.annotation.JtfServiceID;
import com.jd.businessworks.domain.FlowBeanInfo;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.parser.ClassParser;
import com.jd.workflow.soap.common.util.StringHelper;
import org.slf4j.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * 用来
 */
public class FlowBeanScanner {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FlowBeanScanner.class);


    //需要读取的注解方法
    private final static Class<? extends Annotation> filterAnnotation = JtfBeanInfo.class;
    //需要读取注解的类
    private final static Class<? extends Annotation> filterClazz = JtfBeanBind.class;
    //需要读取注解的扩展点的方法
    private final static Class<? extends Annotation> JtfServiceIDClazz = JtfServiceID.class;
    private final static Class<? extends Annotation> filterMethodAnnotation = JtfMethod.class;


    public static boolean isBeanMethod(Method declaredMethod){
        if(!declaredMethod.isAnnotationPresent(filterAnnotation) && !declaredMethod.isAnnotationPresent(JtfServiceIDClazz)){
             return false;
        }
        return true;
    }
    public static FlowBeanInfo beanMethodInfo(Class myClass) {

        //注解信息
        JtfBeanBind jndiBindInfo = (JtfBeanBind) myClass.getAnnotation(JtfBeanBind.class);
        FlowBeanInfo beanInfo = new FlowBeanInfo();
        JtfBeanInfo jtfBeanInfo = (JtfBeanInfo) myClass.getAnnotation(JtfBeanInfo.class);
        if(jtfBeanInfo != null && !StringHelper.isEmpty(jtfBeanInfo.value())){
            beanInfo.setDesc(jtfBeanInfo.value());
        }
        beanInfo.setFullClassName(myClass.getName());
        //类信息
        beanInfo.setBeanName(jndiBindInfo.value());
        beanInfo.setServiceType(FlowBeanInfo.BEAN_TYPE_SERVICE);
        beanInfo.setMethods(new ArrayList<>());

        //设置方法名字,参数类型
        Method[] declaredMethods = myClass.getDeclaredMethods();
        for(Method declaredMethod : declaredMethods){
            //过滤没有注解的方法
            if(!declaredMethod.isAnnotationPresent(filterAnnotation) && !declaredMethod.isAnnotationPresent(JtfServiceIDClazz)
             && !declaredMethod.isAnnotationPresent(filterMethodAnnotation)
            ){
                continue;
            }
            MethodMetadata methodMetadata = ClassParser.buildMethodInfo(declaredMethod);
            JtfMethod method = declaredMethod.getAnnotation(JtfMethod.class);
            if(method != null && !StringHelper.isEmpty(method.desc())){
                methodMetadata.setDesc(method.desc());
            }
            JtfBeanInfo methodBeanInfo = declaredMethod.getAnnotation(JtfBeanInfo.class);
            if(methodBeanInfo != null && !StringHelper.isEmpty(methodBeanInfo.value())){
                methodMetadata.setDesc(methodBeanInfo.value());
            }
            beanInfo.getMethods().add(methodMetadata);

        }
        return beanInfo;
    }

    public static boolean filterClazz(Class<?> myClazz) {
        return myClazz.isAnnotationPresent(filterClazz);
    }



}
