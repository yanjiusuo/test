package com.jd.workflow.export;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;

import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GroupExportModel {
    GroupSortModel group;


    List<MethodSortModel> methodModels;


    List<MethodManageDTO> methods = new ArrayList<>();

    private void initHttpParam(MethodManageDTO method){
        HttpMethodModel methodModel = (HttpMethodModel) method.getContentObject();
        if("get".equalsIgnoreCase(method.getHttpMethod())){
            methodModel.getInput().setBody(null);
        }
        clearEmptyTable(methodModel.getInput().getBody());
        clearEmptyTable(methodModel.getOutput().getBody());
    }
    private void clearEmptyTable(List<JsonType> jsonTypes){
        if(jsonTypes == null || jsonTypes.isEmpty()) return;
        boolean isEmpty = false;
        for (JsonType jsonType : jsonTypes) {
            if(jsonType instanceof ObjectJsonType && StringUtils.isEmpty(jsonType.getDesc()) && ObjectHelper.isEmpty(((ObjectJsonType) jsonType).getChildren())){
                isEmpty = true;
            }
        }
        if(isEmpty){
            jsonTypes.clear();
        }
    }
    public void init(Map<String, List<MethodManageDTO>> id2Methods  ){
        if(methodModels == null){
            return;
        }
        for (MethodSortModel methodModel : methodModels) {
            MethodManageDTO method = id2Methods.get(methodModel.getId()+"").get(0);

            if(method.getContentObject() == null){
                if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                    HttpMethodModel httpMethodModel = JsonUtils.parse(method.getContent(), HttpMethodModel.class);
                    method.setContentObject(httpMethodModel);

                }else if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                    JsfStepMetadata contentJSFObject = JsonUtils.parse(method.getContent(), JsfStepMetadata.class);
                    method.setContentObject(contentJSFObject);
                }
            }
            if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                initHttpParam(method);
            }

            if(method.getDocConfig() != null){
                method.getDocConfig().setInputExample(JsonUtils.tryPretty(method.getDocConfig().getInputExample()));
                method.getDocConfig().setOutputExample(JsonUtils.tryPretty(method.getDocConfig().getOutputExample()));
            }
            methods.add(method);
        }
    }

}
