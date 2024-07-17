package com.jd.workflow.console.service.method;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.method.MethodModifyDeltaInfoMapper;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.entity.IMethodInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.method.DeltaInfo;
import com.jd.workflow.console.entity.method.HttpDeltaInfo;
import com.jd.workflow.console.entity.method.JsfDeltaInfo;
import com.jd.workflow.console.entity.method.MethodModifyDeltaInfo;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.ApiModelDelta;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.model.IApiModelDeltaService;
import com.jd.workflow.console.utils.DeltaHelper;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class MethodModifyDeltaInfoService extends ServiceImpl<MethodModifyDeltaInfoMapper, MethodModifyDeltaInfo> {
   @Autowired
    IMethodManageService methodManageService;
   @Autowired
   IApiModelDeltaService apiModelDeltaService;
    public MethodModifyDeltaInfo getMethodDelta(Long methodId){
        LambdaQueryWrapper<MethodModifyDeltaInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodModifyDeltaInfo::getMethodId,methodId);
        return getOne(lqw);
    }
    public void initApiModelDelta(ApiModel apiModel){
        ApiModelDelta delta = apiModelDeltaService.getByModelId(apiModel.getId());
        if(delta != null){
            JsonType result = DeltaHelper.mergeDelta(apiModel.getContent(), delta.getContent());
            apiModel.setContent(result);
        }
    }
    public void saveApiModelDelta(ApiModel before,ApiModel after,boolean onlyDeltaRef){
        JsonType jsonType = DeltaHelper.deltaJsonType(before.getContent(), after.getContent(), onlyDeltaRef);
        if(jsonType != null){
            ApiModelDelta delta = new ApiModelDelta();
            delta.setName(before.getName());
            delta.setContent(jsonType);
            delta.setApiModelId(before.getId());
            delta.setYn(1);
            delta.setDesc(jsonType.getDesc());
            ApiModelDelta exist = apiModelDeltaService.getByModelId(before.getId());
            if(exist == null){
                apiModelDeltaService.save(delta);
            }else{
                delta.setId(exist.getId());
                apiModelDeltaService.updateById(delta);
            }

        }
    }
    public List<MethodModifyDeltaInfo> getMethodDeltas(List<Long> methodIds){
        LambdaQueryWrapper<MethodModifyDeltaInfo> lqw = new LambdaQueryWrapper<>();
        lqw.in(MethodModifyDeltaInfo::getMethodId,methodIds);
        return list(lqw);
    }
    public void updateInterfaceIdAndNameInfo(){
        List<MethodModifyDeltaInfo> deltaInfos = list();
        for (MethodModifyDeltaInfo delta : deltaInfos) {
            MethodManage method = methodManageService.getById(delta.getMethodId());

            if(method != null){
                delta.setInterfaceId(method.getInterfaceId());
                if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                    JsfDeltaInfo deltaInfo = JsonUtils.parse(delta.getDeltaContent(),JsfDeltaInfo.class);
                    String name = (String) deltaInfo.getDeltaAttrs().get("name");
                    String methodCode = (String) deltaInfo.getDeltaAttrs().get("methodCode");
                    delta.setName(name);
                    delta.setMethodCode(methodCode);
                    updateById(delta);

                }else if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                    HttpDeltaInfo deltaInfo = JsonUtils.parse(delta.getDeltaContent(),HttpDeltaInfo.class);
                    String name = (String) deltaInfo.getDeltaAttrs().get("name");
                    String methodCode = (String) deltaInfo.getDeltaAttrs().get("methodCode");
                    delta.setName(name);
                    delta.setMethodCode(methodCode);
                    updateById(delta);
                }

            }
        }
    }
    public void removeDelta(Long methodId){
        MethodModifyDeltaInfo methodDelta = getMethodDelta(methodId);
        if(methodDelta != null){
            removeById(methodDelta);
        }
    }
    public void initMethodDeltaInfo(List<MethodManage> methods){
        List<Long> methodIds = methods.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<MethodModifyDeltaInfo> deltas = getMethodDeltas(methodIds);
        Map<Long, List<MethodModifyDeltaInfo>> id2Delta = deltas.stream().collect(Collectors.groupingBy(MethodModifyDeltaInfo::getMethodId));
        for (MethodManage method : methods) {
            List<MethodModifyDeltaInfo> deltaInfos = id2Delta.get(method.getId());
            if(deltaInfos != null){
                initDeltaInfo(method,deltaInfos.get(0));
            }
        }
    }
    MethodManageDTO toMethodDto(MethodManage method){
        MethodManageDTO dto = new MethodManageDTO();
        BeanUtils.copyProperties(method,dto);
        if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
            HttpMethodModel model = JsonUtils.parse(method.getContent(), HttpMethodModel.class);
            dto.setContentObject(model);
        }else if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
            JsfStepMetadata model = JsonUtils.parse(method.getContent(),JsfStepMetadata.class);
            dto.setContentObject(model);
        }
        dto.setId(""+method.getKeyId());
        return dto;
    }
    public boolean saveDelta(MethodManage beforeMethod, MethodManage afterMethod,boolean onlyDeltaRef){
        DeltaInfo deltaInfo = null;
        if(InterfaceTypeEnum.HTTP.getCode().equals(beforeMethod.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(beforeMethod.getType())){

            deltaInfo = DeltaHelper.deltaHttpMethod(toMethodDto(beforeMethod), toMethodDto(afterMethod),onlyDeltaRef);

        }else if(InterfaceTypeEnum.JSF.getCode().equals(beforeMethod.getType())){
            deltaInfo = DeltaHelper.deltaJsfMethod(toMethodDto(beforeMethod), toMethodDto(afterMethod),onlyDeltaRef);
        }
        if(deltaInfo == null || deltaInfo.isEmpty()) return false;
        MethodModifyDeltaInfo entity = getMethodDelta(beforeMethod.getId());
        String name =(String) deltaInfo.getDeltaAttrs().get("name");
        String methodCode =(String) deltaInfo.getDeltaAttrs().get("methodCode");
        if(entity == null){
            entity = new MethodModifyDeltaInfo();
            entity.setName(name);
            entity.setMethodCode(methodCode);
            entity.setDeltaContent(JsonUtils.toJSONString(deltaInfo));
            entity.setInterfaceId(beforeMethod.getInterfaceId());
            entity.setMethodId(Long.valueOf(beforeMethod.getId()));
            save(entity);
        }else{
            entity.setName(name);
            entity.setMethodCode(methodCode);
            entity.setDeltaContent(JsonUtils.toJSONString(deltaInfo));
            updateById(entity);
        }

        return true;
    }
    public boolean initDeltaInfo(IMethodInfo dto){
        MethodModifyDeltaInfo delta = getMethodDelta(dto.getKeyId());
        if(delta == null) return false;
        initDeltaInfo(dto,delta);
        return true;
    }
    public boolean initDeltaInfo(IMethodInfo dto,MethodModifyDeltaInfo delta){

        if(InterfaceTypeEnum.JSF.getCode().equals(dto.getType())){
            JsfDeltaInfo deltaInfo = JsonUtils.parse(delta.getDeltaContent(),JsfDeltaInfo.class);
            DeltaHelper.mergeJsfMethod(dto,deltaInfo);
        }else if(InterfaceTypeEnum.HTTP.getCode().equals(dto.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(dto.getType())){
            HttpDeltaInfo deltaInfo = JsonUtils.parse(delta.getDeltaContent(),HttpDeltaInfo.class);
            DeltaHelper.mergeHttpMethod(dto,deltaInfo);
        }
        return true;
    }
}
