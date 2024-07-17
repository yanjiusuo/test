package com.jd.workflow.console.service.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.errorcode.EnumsMapper;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.WebServiceMethod;
import com.jd.workflow.console.dto.errorcode.BindPropParam;
import com.jd.workflow.console.dto.errorcode.DeleteEnumPropDTO;
import com.jd.workflow.console.dto.errorcode.EnumDTO;
import com.jd.workflow.console.dto.errorcode.EnumPropDTO;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.errorcode.Enums;
import com.jd.workflow.console.entity.errorcode.REnumMethodProp;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonEnumDTO;
import com.jd.workflow.soap.common.xml.schema.JsonEnumPropDTO;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */
@Slf4j
@Service
public class EnumsServiceImpl extends ServiceImpl<EnumsMapper, Enums> implements IEnumsService {

    @Autowired
    private IREnumMethodPropService irEnumMethodPropService;

    @Autowired
    private IInterfaceManageService iInterfaceManageService;

    @Autowired
    private IEnumPropService enumPropService;

    @Override
    public Long getErrorCodeEnumId(Long appId) {
        LambdaQueryWrapper<Enums> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Enums::getAppId, appId).eq(BaseEntity::getYn, 1).eq(Enums::getEnumType, 0);
        List<Enums> enumsList = list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(enumsList)) {
            return 0L;
        }
        return enumsList.get(0).getId();

    }

    @Override
    public List<EnumDTO> queryAllEnums(Long appId) {
        List<EnumDTO> result = Lists.newArrayList();
        LambdaQueryWrapper<Enums> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Enums::getAppId, appId).eq(BaseEntity::getYn, 1);
        List<Enums> enumsList = list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(enumsList)) {
            return result;
        }
        for (Enums enums : enumsList) {
            EnumDTO enumDTO = new EnumDTO();
            BeanUtils.copyProperties(enums, enumDTO);
            result.add(enumDTO);
        }
        return result;
    }

    @Override
    public List<EnumDTO> queryEnumsByType(Long appId, Integer enumType) {
        List<EnumDTO> result = Lists.newArrayList();
        LambdaQueryWrapper<Enums> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Enums::getAppId, appId).eq(BaseEntity::getYn, 1).eq(Enums::getEnumType, enumType);
        List<Enums> enumsList = list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(enumsList)) {
            return result;
        }
        for (Enums enums : enumsList) {
            EnumDTO enumDTO = new EnumDTO();
            BeanUtils.copyProperties(enums, enumDTO);
            result.add(enumDTO);
        }
        return result;
    }

    @Transactional
    @Override
    public Boolean deleteEnum(EnumDTO enumDTO) {
        Enums enums = new Enums();
        enums.setId(enumDTO.getId());
        enums.setYn(0);


        LambdaUpdateChainWrapper<REnumMethodProp> rEnumMethodPropLambdaQueryWrapper = irEnumMethodPropService.lambdaUpdate();
        rEnumMethodPropLambdaQueryWrapper.eq(REnumMethodProp::getEnumId, enumDTO.getId()).eq(BaseEntity::getYn, 1);
        rEnumMethodPropLambdaQueryWrapper.set(BaseEntity::getYn, 0);
        rEnumMethodPropLambdaQueryWrapper.update();
        return updateById(enums);

    }

    @Override
    public void initHttpEnums(HttpMethodModel httpMethodModel, Long interfaceId) {


        InterfaceManage interfaceManage = iInterfaceManageService.getById(interfaceId);
        if (Objects.isNull(interfaceManage.getAppId())) {
            return;
        }
        BindPropParam bindPropParam = new BindPropParam();
        bindPropParam.setEnumType(1);
        bindPropParam.setAppId(interfaceManage.getAppId());
        List<REnumMethodProp> rEnumMethodPropList = irEnumMethodPropService.bindEnumList(bindPropParam);
        Map<String, Long> propMap = Maps.newHashMap();
        for (REnumMethodProp rEnumMethodProp : rEnumMethodPropList) {
            if (!propMap.containsKey(rEnumMethodProp.getPropName())) {
                propMap.put(rEnumMethodProp.getPropName(), rEnumMethodProp.getEnumId());
            }
        }

        log.info("initHttpEnums interfaceId:{},propMap:{}", interfaceId, JSON.toJSONString(propMap));
        if (Objects.nonNull(httpMethodModel.getInput())) {
            if (CollectionUtils.isNotEmpty(httpMethodModel.getInput().getParams())) {
                for (JsonType param : httpMethodModel.getInput().getParams()) {
                    initEnumChild(param, propMap, interfaceManage.getAppId());
                }
            }
            if (CollectionUtils.isNotEmpty(httpMethodModel.getInput().getBody())) {
                for (JsonType jsonType : httpMethodModel.getInput().getBody()) {
                    initEnumChild(jsonType, propMap, interfaceManage.getAppId());
                }
            }

        }
        if (Objects.nonNull(httpMethodModel.getOutput())) {
            if (CollectionUtils.isNotEmpty(httpMethodModel.getOutput().getBody())) {
                for (JsonType jsonType : httpMethodModel.getOutput().getBody()) {
                    initEnumChild(jsonType, propMap, interfaceManage.getAppId());
                }
            }
        }


    }

    private void initEnumChild(JsonType param, Map<String, Long> propMap, Long appId) {
        if (Objects.nonNull(param.getEnumId()) || propMap.containsKey(param.getName())) {
            JsonEnumDTO jsonEnumDTO = new JsonEnumDTO();
            jsonEnumDTO.setType(null);
            Long enumId = 0L;
            //绑定在接口上的枚举
            if (Objects.nonNull(param.getEnumId())) {
                enumId = param.getEnumId();

            } else {
                //通用绑定
                enumId = propMap.get(param.getName());

            }
            if (enumId > 0) {
                Enums enums = this.getById(enumId);
                if (enums.getYn() > 0) {
                    if (StringUtils.isEmpty(enums.getEnumName())) {
                        jsonEnumDTO.setName(enums.getEnumCode());
                    } else {
                        jsonEnumDTO.setName(enums.getEnumName());
                    }
                    jsonEnumDTO.setType(enums.getEnumType());
                }

                List<EnumPropDTO> enumPropDTOList = enumPropService.queryEnumProp(appId, enumId);
                if (CollectionUtils.isNotEmpty(enumPropDTOList)) {
                    List<JsonEnumPropDTO> props = Lists.newArrayList();
                    jsonEnumDTO.setProps(props);
                    for (EnumPropDTO enumPropDTO : enumPropDTOList) {
                        JsonEnumPropDTO jsonEnumPropDTO = new JsonEnumPropDTO();
                        jsonEnumPropDTO.setEnumId(enumId);

                        jsonEnumPropDTO.setId(enumPropDTO.getId());
                        jsonEnumPropDTO.setPropCode(enumPropDTO.getPropCode());
                        jsonEnumPropDTO.setPropName(enumPropDTO.getPropName());
                        jsonEnumPropDTO.setPropDesc(enumPropDTO.getPropDesc());
                        jsonEnumPropDTO.setPropSolution(enumPropDTO.getPropSolution());

                        props.add(jsonEnumPropDTO);
                    }
                }
//            //枚举如果已经删除，或者枚举属性为空，那么就不显示绑定枚举了。
//            if (Objects.nonNull(jsonEnumDTO.getType()) && CollectionUtils.isNotEmpty(jsonEnumDTO.getProps())) {
//                param.setChildEnum(jsonEnumDTO);
//                param.setEnumId(enumId);
//            } else {
//
//                param.setEnumId(null);
//            }
                //为空也展示

                param.setChildEnum(jsonEnumDTO);

                param.setEnumId(enumId);
                if (StringUtils.isNotEmpty(enums.getEnumName())) {
                    addEnumDesc(param, enums.getEnumName());

                } else {
                    addEnumDesc(param, enums.getEnumCode());
                }
            }
        }
        if (param instanceof ComplexJsonType) {
            if (CollectionUtils.isNotEmpty(((ComplexJsonType) param).getChildren())) {
                for (JsonType child : ((ComplexJsonType) param).getChildren()) {
                    initEnumChild(child, propMap, appId);
                }
            }
        }
    }

    private void addEnumDesc(JsonType param, String enumName) {
        if (StringUtils.isEmpty(param.getDesc())) {
            param.setDesc(param.getDesc() + "@" + enumName);
        } else {
            if (!param.getDesc().contains("@" + enumName)) {
                param.setDesc(param.getDesc() + "@" + enumName);
            }
        }
    }

    @Override
    public void initJsfEnums(JsfStepMetadata contentJSFObject, Long interfaceId) {
        InterfaceManage interfaceManage = iInterfaceManageService.getById(interfaceId);
        if (Objects.isNull(interfaceManage.getAppId())) {
            return;
        }
        BindPropParam bindPropParam = new BindPropParam();
        bindPropParam.setEnumType(1);
        bindPropParam.setAppId(interfaceManage.getAppId());
        List<REnumMethodProp> rEnumMethodPropList = irEnumMethodPropService.bindEnumList(bindPropParam);
        Map<String, Long> propMap = Maps.newHashMap();
        for (REnumMethodProp rEnumMethodProp : rEnumMethodPropList) {
            if (!propMap.containsKey(rEnumMethodProp.getPropName())) {
                propMap.put(rEnumMethodProp.getPropName(), rEnumMethodProp.getEnumId());
            }
        }
        log.info("initJsfEnums interfaceId:{},propMap:{}", interfaceId, JSON.toJSONString(propMap));
        if (Objects.nonNull(contentJSFObject.getInput())) {
            for (JsonType jsonType : contentJSFObject.getInput()) {
                initEnumChild(jsonType, propMap, interfaceManage.getAppId());
            }
        }
        if (Objects.nonNull(contentJSFObject.getOutput())) {
            initEnumChild(contentJSFObject.getOutput(), propMap, interfaceManage.getAppId());
        }


    }

    @Override
    public Boolean saveEnum(EnumDTO enumDTO) {
        //新增判断是否重名

        LambdaQueryWrapper<Enums> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Enums::getAppId, enumDTO.getAppId()).eq(BaseEntity::getYn, 1).eq(Enums::getEnumName, enumDTO.getEnumName());
        List<Enums> enumsList = list(lambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(enumsList)) {
            if (Objects.isNull(enumDTO.getId())) {
                throw new BizException("已存在同名的枚举");
            } else if (enumsList.size() > 1) {
                throw new BizException("已存在同名的枚举");
            } else if (!enumsList.get(0).getId().equals(enumDTO.getId())) {
                throw new BizException("已存在同名的枚举");
            }
        }
        Enums enums = new Enums();
        BeanUtils.copyProperties(enumDTO, enums);
        enums.setYn(1);
        boolean result = this.saveOrUpdate(enums);
        enumDTO.setId(enums.getId());
        return result;


    }

    @Override
    public void initContentEnums(MethodManageDTO methodManageDTO) {
        String content = methodManageDTO.getContent();
        InterfaceTypeEnum type = InterfaceTypeEnum.getByCode(methodManageDTO.getType());
        switch (type) {
            case HTTP:
                HttpMethodModel httpMethodModel = (HttpMethodModel) methodManageDTO.getContentObject();
                if (methodManageDTO instanceof MethodManageDTO) {
                    initHttpEnums(httpMethodModel, ((MethodManageDTO) methodManageDTO).getInterfaceId());
                }
                methodManageDTO.setContentObject(httpMethodModel);
                //methodManageDTO.setContent(null);
                break;
            case EXTENSION_POINT:
                HttpMethodModel httpMethodModel2 = (HttpMethodModel) methodManageDTO.getContentObject();
                if (methodManageDTO instanceof MethodManageDTO) {
                    initHttpEnums(httpMethodModel2, ((MethodManageDTO) methodManageDTO).getInterfaceId());
                }
                methodManageDTO.setContentObject(httpMethodModel2);
                break;
            case WEB_SERVICE:
                break;
            case JSF:
                JsfStepMetadata contentJSFObject = (JsfStepMetadata) methodManageDTO.getContentObject();
                if (methodManageDTO instanceof MethodManageDTO) {
                    initJsfEnums(contentJSFObject, ((MethodManageDTO) methodManageDTO).getInterfaceId());
                }
                methodManageDTO.setContentObject(contentJSFObject);

                break;
            case ORCHESTRATION:

                break;
            default:

                methodManageDTO.setContentObject(JsonUtils.parse(content, Map.class));

                break;
        }
    }


}
