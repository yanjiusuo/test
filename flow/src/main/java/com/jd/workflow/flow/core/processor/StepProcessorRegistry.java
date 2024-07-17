package com.jd.workflow.flow.core.processor;

import com.jd.workflow.flow.core.context.FlowContextAware;
import com.jd.workflow.flow.core.context.FlowExecContext;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.processor.impl.*;
import com.jd.workflow.flow.utils.TypeConverterUtils;
import com.jd.workflow.soap.common.exception.StdException;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class StepProcessorRegistry {
    static Map<String,Class<? extends StepProcessor>> stepProcessors = new HashMap<>();
    static Map<String, StepProcessor> instances = new HashMap<>();
    static{
        loadStepProcessor();
      /*  register("exception", ExceptionProcessor.class);
        register("transform", OutputCollectProcessor.class);
        register("reqValidate", InputValidateProcessor.class);
        register("collect", OutputCollectProcessor.class);
        register("fallback", FallbackStepProcessor.class);

        register("ws2http", Ws2HttpStepProcessor.class);
        register("http2ws", Http2WsStepProcessor.class);
        register("http", HttpStepProcessor.class);*/
    }
    private static void loadStepProcessor(){
        ServiceLoader<StepProcessor> serviceLoader = ServiceLoader.load(StepProcessor.class);
        for (StepProcessor processor : serviceLoader){
            register(processor);
        }
    }
    public static void register(StepProcessor processor){

        try {

            String[] types = StringUtils.split(processor.getTypes(), ',');
            for (String type : types) {
                stepProcessors.put(type, processor.getClass());
                instances.put(type, processor);
            }

        } catch (Exception e) {
            throw StdException.adapt(e);
        }
    }
    public static void register(String type,Class<? extends StepProcessor> processor){
        stepProcessors.put(type, processor);
        try {
            instances.put(type, (StepProcessor) processor.newInstance());
        } catch (Exception e) {
             throw StdException.adapt(e);
        }
    }
    public static Class<StepMetadata> getMetadataType(String type){
        StepProcessor newStepProcessor = instances.get(type);
        if(newStepProcessor == null){
            throw new StepParseException("step.err_miss_processor_for_type").param("type",type);
        }
        return newStepProcessor.getMetadataType();
    }

    public static StepMetadata parseMetadata(Map<String,Object> args){
        Class<StepMetadata> clazz = StepProcessorRegistry.getMetadataType((String) args.get("type"));

        StepMetadata stepMetadata = TypeConverterUtils.cast(args, clazz,(String) args.get("id"));
        stepMetadata.init();
        return stepMetadata;
    }
    public static List<StepMetadata> parseListMetadata(List<Map<String,Object>> args){
        if(args == null) return Collections.emptyList();
        List<StepMetadata> list = new ArrayList<>();
        for (Map<String, Object> arg : args) {
            list.add(parseMetadata(arg));
        }
        return list;
    }
    public static StepProcessor instance(String type, FlowExecContext execContext){
        return  instance(type,execContext,true);
    }
    public static StepProcessor instance(String type, FlowExecContext execContext, boolean useFallback){
        Class clazz = StepProcessorRegistry.get(type);
        if(clazz == null){
            throw new StepParseException("workflow.err_invalid_step_type").param("type",type);
        }
        try {
            Class<StepMetadata> metadataType = getMetadataType(type);
            if(useFallback && FallbackStepMetadata.class.isAssignableFrom(metadataType)) {
                clazz = FallbackStepProcessor.class;
            }
            StepProcessor processor =    (StepProcessor) clazz.newInstance();
            if(processor instanceof FlowContextAware){
                ((FlowContextAware)processor).setFlowContext(execContext);
            }
            return processor;
        } catch (Exception e) {
            throw new StepParseException("workflow.err_instance_processor_type").param("type",type);
        }
    }
    public static Class<? extends StepProcessor> get(String type){
        return stepProcessors.get(type);
    }
}
