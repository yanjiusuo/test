package com.jd.workflow.console.rpc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.console.base.enums.EnvTypeEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.InterfaceManageDTO;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.doc.DocConfigDto;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceMethodGroup;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.env.IEnvConfigService;
import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.interfaceManage.*;
import com.jd.workflow.server.service.InterfaceManageRpcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MethodManageRpcServiceImpl implements InterfaceManageRpcService {

    @Resource
    private IInterfaceManageService interfaceManageService;
    @Resource
    private IInterfaceMethodGroupService interfaceMethodGroupService;
    @Resource
    private IMethodManageService methodManageService;
    @Resource
    private IEnvConfigService envConfigService;


    @Override
    public QueryResult<List<JsfBatchInterface>> addMethodBatch(Long appId, List<JsfBatchInterface> groups) {

        List<InterfaceManage> manages = interfaceManageService.getAppInterfaces(appId, null, InterfaceTypeEnum.EXTENSION_POINT.getCode());
        //获取增加的目录
        List<JsfBatchInterface> addFirstDics = groups;
        List<JsfBatchInterface> existFirstDics = null;
        if (CollectionUtils.isNotEmpty(manages)) {
            List<String> firstDicExistNames = manages.stream().map(InterfaceManage::getName).collect(Collectors.toList());
            addFirstDics = groups.stream().filter(item -> !firstDicExistNames.contains(item.getFirstDic().getName())).collect(Collectors.toList());
            existFirstDics = groups.stream().filter(item -> firstDicExistNames.contains(item.getFirstDic().getName())).collect(Collectors.toList());
        }
        List<JsfBatchInterface> fillIdRes=new ArrayList<>();
        try {
            if (CollectionUtils.isNotEmpty(addFirstDics)) {
                log.info("添加一级目录Adding---》{},{}", appId,addFirstDics);
                for (JsfBatchInterface addDic : addFirstDics) {
                    addDic.getFirstDic().setAppId(appId);
                    Long interfaceId = interfaceManageService.add(toInterfaceManageDto(addDic.getFirstDic(),null));
                    List<EnvModel> envModel=convertModel(addDic.getFirstDic().getEnvInfo());
                    envConfigService.batchSaveItem(envModel,appId);
                    addSecondDic(interfaceId, addDic.getSecondDic(), null);
                }
                fillIdRes.addAll(addFirstDics);
            }
            if (CollectionUtils.isNotEmpty(existFirstDics)) {
                log.info("存在一级目录,{},{}", appId, existFirstDics);
                Map<String, Long> name2InterfaceId = manages.stream().collect(Collectors.toMap(InterfaceManage::getName, InterfaceManage::getId, (s1, s2) -> s1));
                for (JsfBatchInterface firstDic : existFirstDics) {
                    Long interfaceId = name2InterfaceId.get(firstDic.getFirstDic().getName());
                    interfaceManageService.edit(toInterfaceManageDto(firstDic.getFirstDic(), interfaceId));
                    List<EnvModel> envModel = convertModel(firstDic.getFirstDic().getEnvInfo());
                    envConfigService.batchSaveItem(envModel, appId);
                    groupSecondDic(interfaceId, firstDic.getSecondDic());
                    fillIdRes.addAll(existFirstDics);
                }
            }
        } catch (Exception e) {
            log.error("推送方法失败", e);
            throw e;
        }
        //回填id信息
        this.fillId(fillIdRes);
        return QueryResult.buildSuccessResult(fillIdRes);
    }

    private void fillId(List<JsfBatchInterface> fillIdRes) {
        for (JsfBatchInterface fillIdRe : fillIdRes) {
            fillMethodIdByGroup(fillIdRe.getSecondDic());
            fillMethodId(fillIdRe.getSecondMethods());
        }
    }

    private void fillMethodId(List<JsfMethodManage> methods){
        if(CollectionUtils.isEmpty(methods)){
            return;
        }
        for (JsfMethodManage secondMethod : methods) {
            if (null == secondMethod.getId()) {
                List<Long> ids = methodManageService.getMethodByCode(secondMethod.getMethodCode(), secondMethod.getInterfaceId());
                if(CollectionUtils.isNotEmpty(ids)){
                    secondMethod.setId(ids.get(0));
                }else{
                    log.error("Method " + secondMethod.getMethodCode() +secondMethod.getInterfaceId()+"查询结果为空");
                }
            }
        }
    }

    private void fillMethodIdByGroup(List<JsfInterfaceMethodGroup> groups){
        if(CollectionUtils.isEmpty(groups)){
            return;
        }
        for (JsfInterfaceMethodGroup group : groups) {
            fillMethodIdByGroup(group.getChildDic());
            fillMethodId(group.getThirdMethods());
        }
    }

    private List<EnvModel> convertModel(List<JsfEnvModel> envInfo) {
        List<EnvModel> rest= envInfo.stream().map(item->{
            EnvModel model=new EnvModel();
            BeanUtils.copyProperties(item, model);
            model.setType(convertEnvType(item.getType()));
            return model;
        }).collect(Collectors.toList());
        return rest;
    }

    private EnvTypeEnum convertEnvType(com.jd.workflow.server.dto.interfaceManage.EnvTypeEnum env){
       if(com.jd.workflow.server.dto.interfaceManage.EnvTypeEnum.TEST.equals(env)){
           return EnvTypeEnum.TEST;
       }
        if(com.jd.workflow.server.dto.interfaceManage.EnvTypeEnum.PRE.equals(env)){
            return EnvTypeEnum.PRE;
        }
        if(com.jd.workflow.server.dto.interfaceManage.EnvTypeEnum.RELEASE.equals(env)){
            return EnvTypeEnum.RELEASE;
        }
        return EnvTypeEnum.TEST;
    }

    private void addSecondDic(Long interfaceId, List<JsfInterfaceMethodGroup> groups, Long parentGroupId) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        for (JsfInterfaceMethodGroup methodGroup : groups) {
            methodGroup.setInterfaceId(interfaceId);
            methodGroup.setParentId(parentGroupId);
            Long[] ids={interfaceId};
            List<InterfaceMethodGroup> dtos=interfaceMethodGroupService.searchGroup(1,methodGroup.getName(), Arrays.asList(ids));
            Long groupId = null;
            if (dtos.size() == 0) {
                groupId = interfaceMethodGroupService.addGroup(methodGroup.getName(), methodGroup.getEnName(), interfaceId, parentGroupId);
            } else {
                groupId = dtos.get(0).getId();
            }
            //当前目录子方法
            addMethod(methodGroup.getThirdMethods(), groupId, interfaceId);
            //当前目录子目录
            addSecondDic(interfaceId, methodGroup.getChildDic(), groupId);
        }
    }


    private void handleExistDic(Long interfaceId, List<JsfInterfaceMethodGroup> groups, Map<String, Long> name2GroupId) {
        if (CollectionUtils.isNotEmpty(groups)) {
            for (JsfInterfaceMethodGroup group : groups) {
                group.setInterfaceId(interfaceId);
                Long groupId = name2GroupId.get(group.getEnName());
                //当前目录子方法
                addMethod(group.getThirdMethods(), groupId, interfaceId);
                //当前目录子目录
                addSecondDic(interfaceId, group.getChildDic(), name2GroupId.get(group.getEnName()));
            }
        }
    }

    private void groupSecondDic(Long interfaceId, List<JsfInterfaceMethodGroup> secondDics) {
        if (CollectionUtils.isNotEmpty(secondDics)) {
            fillInterFaceId(interfaceId, secondDics);
            Map<Long, InterfaceMethodGroup> existGroup = interfaceMethodGroupService.findInterfaceGroups(interfaceId);
            Map<String, Long> name2Id = existGroup.values().stream().collect(Collectors.toMap(InterfaceMethodGroup::getEnName, InterfaceMethodGroup::getId, (s1, s2) -> s1));
            List<String> secondDicExistNames = existGroup.values().stream().map(InterfaceMethodGroup::getEnName).collect(Collectors.toList());
            List<JsfInterfaceMethodGroup> secondAddDics = secondDics.stream().filter(item -> !secondDicExistNames.contains(item.getEnName())).collect(Collectors.toList());
            List<JsfInterfaceMethodGroup> secondExistDics = secondDics.stream().filter(item -> secondDicExistNames.contains(item.getEnName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(secondAddDics)) {
                log.info("添加的二级目录,{}", JSONObject.toJSONString(secondAddDics));
                addSecondDic(interfaceId, secondAddDics, null);
            }
            if (CollectionUtils.isNotEmpty(secondExistDics)) {
                log.info("存在的二级目录,{}", JSONObject.toJSONString(secondExistDics));
                handleExistDic(interfaceId, secondExistDics, name2Id);
            }
        }
    }

    private void fillInterFaceId(Long interfaceId, List<JsfInterfaceMethodGroup> secondDics) {
        if(CollectionUtils.isEmpty(secondDics)){
            return;
        }
        for (JsfInterfaceMethodGroup secondDic : secondDics) {
            secondDic.setInterfaceId(interfaceId);
            fillInterFaceId(interfaceId,secondDic.getChildDic());
            fillMethodInterFaceId(interfaceId,secondDic.getThirdMethods());
        }
    }

    private void fillMethodInterFaceId(Long interfaceId, List<JsfMethodManage> methods) {
        if(CollectionUtils.isEmpty(methods)){
            return;
        }
        for (JsfMethodManage method : methods) {
            method.setInterfaceId(interfaceId);
        }
    }


    private void addMethod(List<JsfMethodManage> groups, Long groupId, Long interfaceId) {
        if(CollectionUtils.isEmpty(groups)){
            return;
        }
        for (JsfMethodManage method : groups) {
            method.setGroupId(groupId);
            method.setInterfaceId(interfaceId);
            // 查询是否已存在
            List<Long> rest = methodManageService.getMethodByCode(method.getMethodCode(), method.getInterfaceId());
            if (rest.size() > 0) {
                method.setId(rest.get(0));
                methodManageService.edit(toMethodManageDto(method));
                return;
            }
            Long id = methodManageService.add(toMethodManageDto(method));
            method.setId(id);
        }
    }

    private MethodManageDTO toMethodManageDto(JsfMethodManage method) {
        MethodManageDTO dto = new MethodManageDTO();
        MethodDocConfig docConfig = new MethodDocConfig();
        if (null != method) {
            BeanUtils.copyProperties(method, dto);
        }
        if (null != method.getDocConfig()) {
            BeanUtils.copyProperties(method.getDocConfig(), docConfig);
        }
        dto.setId(method.getId()+"");
        dto.setContent(JSONObject.toJSONString(method.getContent()));
        dto.setDocConfig(docConfig);
        return dto;
    }

    private InterfaceManageDTO toInterfaceManageDto(JsfInterfaceManage firstDic,Long id) {
        InterfaceManageDTO dto = new InterfaceManageDTO();

        if (null == firstDic) {
            log.info("一级目录为空{}", firstDic);
            return dto;
        }
        DocConfigDto docConfig = new DocConfigDto();
        if (null != firstDic.getDocConfig()) {
            BeanUtils.copyProperties(firstDic.getDocConfig(), docConfig);
        }
        BeanUtils.copyProperties(firstDic, dto);
        dto.setDocConfig(docConfig);
        dto.setEnv(JSONArray.toJSONString(firstDic.getEnvInfo()));
        dto.setTenantId(firstDic.getTenantId());
        dto.setId(id);
        return dto;
    }
}
