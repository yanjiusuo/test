package com.jd.workflow.soap.classinfo;

import com.jd.workflow.soap.classinfo.model.ClassInfo;
import com.jd.workflow.soap.classinfo.model.FieldInfo;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.*;

public class ClassGenerator {
    static  final Logger logger = LoggerFactory.getLogger(ClassGenerator.class);
    public static Map<String,String> generateClass(Collection<ClassInfo> classInfoList){
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        // 获取模板文件
        Template template = velocityEngine.getTemplate("template/EntityClass.vm");
        Map<String, String> ret = new HashMap<>();
        for (ClassInfo classInfo : classInfoList) {
            // 设置变量，velocityContext是一个类似map的结构
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("c",classInfo);

            // 输出渲染后的结果
            StringWriter stringWriter = new StringWriter();
            template.merge(velocityContext, stringWriter);
            String classData = stringWriter.toString();
            //logger.info("classGenerator.generate:classData={}",classData);
            ret.put(classInfo.fullClassName(), classData);
        }
        return ret;
    }
    public Map<String/*fullClassName*/,byte[]/*bytes*/> compile(Map<String,String> classes){
        Map<String,byte[]> ret = new HashMap<>();
        return ret;
    }

    public static void main(String[] args) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName("User");
        classInfo.setPackageName("com.jd");
        classInfo.setFields(new ArrayList<>());
        classInfo.getFields().add(new FieldInfo("id","java.util.String"));
        Map<String, String> map = generateClass(Collections.singleton(classInfo));
        System.out.println(map);
    }
}
