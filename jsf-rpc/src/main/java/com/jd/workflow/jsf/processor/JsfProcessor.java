package com.jd.workflow.jsf.processor;

import com.jd.jsf.gd.GenericService;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.error.NoAliveProviderException;
import com.jd.jsf.gd.registry.RegistryFactory;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.enums.JsfRegistryEnvEnum;
import com.jd.workflow.jsf.input.JsfInput;
import com.jd.workflow.jsf.input.JsfOutput;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class JsfProcessor implements StepProcessor<JsfStepMetadata> {
/*    static {
        StepProcessorRegistry.register("jsf",JsfProcessor.class);
    }*/
    static ParametersUtils utils = new ParametersUtils();
    JsfStepMetadata metadata;
    GenericService genericService;
    ConsumerConfig<GenericService> consumerConfig;
    Map<String,String> parameters = null;
    @Override
    public void init(JsfStepMetadata metadata) {
        this.metadata = metadata;

        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex(JsfRegistryEnvEnum.online.getAddress());


        // 服务消费者连接注册中心，设置属性
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId(metadata.getInterfaceName());// 这里写真实的类名

        consumerConfig.setRegistry(jsfRegistry);


        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl(metadata.getUrl());
        Integer timeout = null;
        if(metadata.getTaskDef()!=null){
            timeout = metadata.getTaskDef().getTimeout();
        }
        if(timeout == null || timeout <= 0){
            timeout = 5*1000;
        }
        consumerConfig.setTimeout(timeout);
        consumerConfig.setAlias(metadata.getAlias());
        consumerConfig.setParameter(".warnning","false");

        //consumerConfig.setLazy(true);
        consumerConfig.setGeneric(true); // 需要指定是Generic调用true
        // consumerConfig.setAsync(true); // 如果异步
        if(metadata.getAttachments() != null){
            Map<String,String> params= new HashMap<>();
            for (JsonType attachment : metadata.getAttachments()) {
                params.put(attachment.getName(), Variant.valueOf(attachment.getValue()).toString());
            }
            parameters = params;
            consumerConfig.getParameters().putAll(params);
        }
        // 得到泛化调用实例，此操作很重，请缓存consumerConfig或者service对象！！！！（用map或者全局变量）
        this.genericService = consumerConfig.refer();
        this.consumerConfig = consumerConfig;
    }

    @Override
    public String getTypes() {
        return "jsf";
    }

    private String[] getParamTypes(){
        List<? extends JsonType> jsonTypes = metadata.getInput();
        String[] paramTypes = new String[jsonTypes.size()];
        for (int i = 0; i < jsonTypes.size(); i++) {
            paramTypes[i] = jsonTypes.get(i).getClassName();
        }
        return paramTypes;
    }
    @Override
    public void process(Step currentStep) {
        JsfInput input = new JsfInput();
        if(parameters != null){
            input.setHeaders(parameters);
        }
        JsfOutput output = new JsfOutput();
        currentStep.setInput(input);
        currentStep.setOutput(output);

        List<? extends JsonType> jsonTypes = metadata.getInput();




        String[] paramTypes = getParamTypes();
        List paramValue =   new ArrayList();
        if(metadata.getPreProcess() != null){
            Map<String, Object> vars = utils.getMvelExecVars(currentStep.getContext());
            vars.put("input",input);
            MvelUtils.eval(metadata.getId(),"preProcess", metadata.getPreProcess(),vars,input);
        }
        if(metadata.getScript() != null){
            Map<String, Object> vars = utils.getMvelExecVars(currentStep.getContext());
            vars.put("input",input);
            MvelUtils.eval(metadata.getId(),"script", metadata.getScript(),vars,input);


            if(!(input.getBody() instanceof List)
            || (((List)input.getBody()).size() != jsonTypes.size())
            ){
                throw new StepExecException(metadata.getId(),"jsf.err_jsf_input_must_be_array_and_args_count_must_match")
                        .param("count",jsonTypes.size());
            }
            List list = (List) input.getBody();
            int index = 0;
            for (JsonType jsonType : jsonTypes) {
                paramValue.add(JsfParamConverterRegistry.convertValue(jsonType,list.get(index++)));
            }
        }else{
            Map<String,Object> extArgs = new HashMap<>();
            extArgs.put("input",input.attrsMap());
            ParamMappingContext paramMappingContext = new ParamMappingContext(currentStep.getContext(),extArgs);
            Map<String, Object> inputValues = utils.getJsonInputValue(jsonTypes, paramMappingContext);
            for (JsonType jsonType : jsonTypes) {
                Object value = inputValues.get(jsonType.getName());
                Object convertValue = JsfParamConverterRegistry.convertValue(jsonType, value);
                paramValue.add(convertValue);

            }
            input.setBody(paramValue);
        }



        try{
            Object result = genericService.$invoke(metadata.getMethodName(), paramTypes, paramValue.toArray(new Object[0]));
            output.setBody(result);

        }catch (NoAliveProviderException e){
            throw new StepExecException(metadata.getId(),"jsf.err_no_alive_provider",e).param("alias",metadata.getAlias())
                    .param("env",metadata.getEnv()).param("interface",metadata.getInterfaceName());
        }
    }



    @Override
    public void stop() {
        synchronized (this){
            if(consumerConfig != null){
                consumerConfig.unrefer();
            }
            consumerConfig = null;
        }

    }


}
