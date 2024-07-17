package com.jd.workflow.console.dto;


import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * jsfMethodDemon
 */
@Data
public class JsfMethodDemonModel {

    /**
     * 入参     List<? extends JsonType> input;
     */
    Map<String,Object> input;

    /**
     * 出参 JsonType output
     */
    Object output;
    String interfaceName;
    String methodName;
    String alias;
    /**
     * jsfRegistrySite
     */
    String site;
    String env;
    String[] exceptions;
    /**
     * List<? extends JsonType>
     */
    Map<String,Object> attachments;

    String protocol;


    public static JsfMethodDemonModel convertDemonByJsfMethodModel(JsfStepMetadata model){
        if(model==null)return null;
        JsfMethodDemonModel demonModel = new JsfMethodDemonModel();
        demonModel.setAlias(model.getAlias());
        demonModel.setInterfaceName(model.getInterfaceName());
        demonModel.setMethodName(model.getMethodName());
        demonModel.setEnv(model.getEnv());
        demonModel.setSite(model.getSite());
        demonModel.setExceptions(model.getExceptions());
        if(model.getInput()!=null){
            demonModel.setInput(model.getInput().stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
        }
        if(model.getAttachments()!=null){
            demonModel.setAttachments(model.getAttachments().stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
        }
        if(model.getOutput()!=null){
            demonModel.setOutput(model.getOutput().toDescJson());
        }
        return demonModel;
    }
}
