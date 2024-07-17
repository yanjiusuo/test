package com.jd.workflow.console.service.listener.impl;

import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.model.ModelRefRelation;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.RefJsonTypeService;
import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.service.model.IModelRefRelationService;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Service
public class MethodRefModelChangeListener implements InterfaceChangeListener {

    @Autowired
    private MethodManageServiceImpl methodManageService;

    @Autowired
    private IModelRefRelationService modelRefRelationService;
    @Autowired
    RefJsonTypeService refJsonTypeService;





    @Override
    public void onMethodAdd(InterfaceManage interfaceManage, List<MethodManage> methods) {
        for (MethodManage method : methods) {
            List<String> refModels = new ArrayList<>();
            if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                refModels = refJsonTypeService.collectHttpRefs((HttpMethodModel) method.getContentObject());

            }else if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                refModels = refJsonTypeService.collectJsfRefs((JsfStepMetadata) method.getContentObject());
            }
            if(!refModels.isEmpty()){
                modelRefRelationService.merge(null,refModels,interfaceManage.getAppId(),method.getId(), ModelRefRelation.TYPE_INTERFACE);
            }
        }

    }


    @Override
    public void onMethodAfterUpdate(InterfaceManage interfaceManage, List<MethodManage> afterMethods,List<MethodManage> beforeMethods){
        for (int i = 0; i < afterMethods.size(); i++) {
            MethodManage after = afterMethods.get(i);
            MethodManage before = beforeMethods.get(i);
            List<String> beforeModels = new ArrayList<>();
            List<String> afterModels = new ArrayList<>();
            if(InterfaceTypeEnum.HTTP.getCode().equals(before.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(before.getType())){
                beforeModels = refJsonTypeService.collectHttpRefs((HttpMethodModel) before.getContentObject());
                afterModels = refJsonTypeService.collectHttpRefs((HttpMethodModel) after.getContentObject());

            }else if(InterfaceTypeEnum.JSF.getCode().equals(before.getType())){
                beforeModels = refJsonTypeService.collectJsfRefs((JsfStepMetadata) before.getContentObject());
                afterModels = refJsonTypeService.collectJsfRefs((JsfStepMetadata) after.getContentObject());
            }
            if(!beforeModels.isEmpty() || !afterModels.isEmpty()){
                modelRefRelationService.merge(beforeModels,afterModels,interfaceManage.getAppId(),before.getId(), ModelRefRelation.TYPE_INTERFACE);
            }
        }

    }


    @Override
    public void onMethodRemove(InterfaceManage interfaceManage, List<MethodManage> methods) {
        for (MethodManage method : methods) {
            List<String> refModels = new ArrayList<>();
            if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                refModels = refJsonTypeService.collectHttpRefs((HttpMethodModel) method.getContentObject());

            }else if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                refModels = refJsonTypeService.collectJsfRefs((JsfStepMetadata) method.getContentObject());
            }
            if(!refModels.isEmpty()){
                modelRefRelationService.removeMethodRef(interfaceManage.getAppId(),refModels,method.getId());
            }
        }

    }
}
