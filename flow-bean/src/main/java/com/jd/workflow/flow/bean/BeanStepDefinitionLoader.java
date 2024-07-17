package com.jd.workflow.flow.bean;

import com.jd.workflow.flow.core.bean.IBeanStepProcessor;
import com.jd.workflow.flow.core.bean.annotation.FlowConfigParam;
import com.jd.workflow.flow.core.bean.annotation.FlowMethod;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.parser.ClassParser;
import com.jd.workflow.soap.common.parser.TypeConverterRegistry;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BeanStepDefinitionLoader {
    static final Logger log = LoggerFactory.getLogger(BeanStepDefinitionLoader.class);
    static Map<String/* type */, IBeanStepProcessor> beanProcessors = new ConcurrentHashMap<>();

    private static  Map<String/* type */,BeanTemplateDefinition> beanDefinition = new ConcurrentHashMap<>();
    static ClassParser classParser = new ClassParser();
    public static  void init(){
        ServiceLoader<IBeanStepProcessor> serviceLoader = ServiceLoader.load(IBeanStepProcessor.class);
        for (IBeanStepProcessor processor : serviceLoader){
            register(processor);
            beanDefinition.put(processor.getName(),buildBeanDefinition(processor));
        }
    }
    static {
        try{
            init();
        }catch (Exception e){
            log.info("bean.err_init_bean_processor",e);
        }

    }
    public static BeanTemplateDefinition getBeanDefinition(String name){
        return beanDefinition.get(name);
    }
    public static  void register(IBeanStepProcessor processor){
        beanProcessors.put(processor.getName(),processor);
    }



    /**
     *
     * @param key beanType_methodName_methodCount
     * @return
     */
    public static MethodMetadata getBeanMethodByKey(String key){
        final String[] result = StringUtils.split(key, "_");
        final BeanTemplateDefinition beanDefinition = getBeanDefinition(result[0]);
        if(beanDefinition == null) return null;
        for (MethodMetadata method : beanDefinition.getMethods()) {
            if(method.getMethodName().equals(result[1])
             && method.getInput().size() == Variant.valueOf(result[2]).toInt()
            ){
                return method;
            }
        }
        return null;
    }

    public static  Map<String/* type */,BeanTemplateDefinition> getBeanDefinition(){
        return beanDefinition;
    }

    public static  IBeanStepProcessor getBeanProcessor(String name) {
        return beanProcessors.get(name);
    }
    public static BeanTemplateDefinition buildBeanDefinition(IBeanStepProcessor processor){
        BeanTemplateDefinition stepMetadata = new BeanTemplateDefinition();
        Class initClass = processor.getInitConfigClass();
        stepMetadata.setInitConfigClass(initClass.getName());
        stepMetadata.setType(processor.getName());

        Set<String/* methodName_paramCount */> alreadyExistMethod = new HashSet<>();

        for (Object o : processor.getExportMethods()) {

            Method exportMethod = (Method) o;

            MethodMetadata methodMetadata = ClassParser.buildMethodInfo(exportMethod);

         /*   BeanStepMetadata beanStepMetadata = new BeanStepMetadata();
            beanStepMetadata.setInput(jsfStepMetadata.getInput());
            beanStepMetadata.setType("bean");
            beanStepMetadata.setBeanType(processor.getName());
            beanStepMetadata.setMethodName(jsfStepMetadata.getMethodName());
            beanStepMetadata.setOutput(jsfStepMetadata.getOutput());*/
            buildMethodDesc(exportMethod,methodMetadata);
            stepMetadata.getMethods().add(methodMetadata);


            String methodKey = methodMetadata.getMethodName() +"_"+ methodMetadata.getInput().size();
            if(alreadyExistMethod.contains(methodKey)){
                throw new StdException("bean.err_found_duplicate_method").param("method",methodMetadata.getMethodName());
            }
            alreadyExistMethod.add(methodKey);


        }
        return stepMetadata;
    }
    private static void buildMethodDesc(Method method,MethodMetadata metadata){
        final FlowMethod flowMethod = method.getAnnotation(FlowMethod.class);
        if(flowMethod != null && !StringUtils.isBlank(flowMethod.desc())){
            metadata.setDesc(flowMethod.desc());
        }
        int i = 0;
        for (Parameter parameter : method.getParameters()) {
            FlowConfigParam configParam = parameter.getAnnotation(FlowConfigParam.class);
            if(configParam != null && !StringUtils.isBlank(configParam.desc())){
                metadata.getInput().get(i).setDesc(configParam.desc());
            }
            i++;
        }
    }

    private static JsonType parseType(Class clazz){

        Map<String, List<Field>> fieldMap = Arrays.stream(clazz.getDeclaredFields()).collect(Collectors.groupingBy(Field::getName));
        JsonType jsonType = TypeConverterRegistry.processJsonType(clazz);
       if(jsonType instanceof ComplexJsonType){
           for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
               Field field = fieldMap.get(child.getName()).get(0);
               FlowConfigParam param = field.getAnnotation(FlowConfigParam.class);
               if(param != null){
                   child.setDesc(param.label());
               }
           }
       }
       return jsonType;
    }
}
