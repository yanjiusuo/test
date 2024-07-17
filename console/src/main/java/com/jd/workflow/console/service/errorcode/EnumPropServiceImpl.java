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
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dao.mapper.errorcode.EnumPropMapper;
import com.jd.workflow.console.dto.doc.EnumClassDTO;
import com.jd.workflow.console.dto.errorcode.DeleteEnumPropDTO;
import com.jd.workflow.console.dto.errorcode.EnumPropDTO;
import com.jd.workflow.console.dto.errorcode.SaveEnumDTO;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.errorcode.EnumProp;
import com.jd.workflow.console.entity.errorcode.Enums;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
public class EnumPropServiceImpl extends ServiceImpl<EnumPropMapper, EnumProp> implements IEnumPropService {

    @Autowired
    private IEnumsService enumsService;

    @Override
    public List<EnumPropDTO> queryErrorCodeProp(Long appId) {
        List<EnumPropDTO> result = Lists.newArrayList();

        Long enumsId = enumsService.getErrorCodeEnumId(appId);
        if (enumsId == 0) {
            log.info("queryErrorCodeProp appId:{} ,getErrorCodeEnumId is 0", appId);
            return result;
        }
        LambdaQueryWrapper<EnumProp> enumPropLambdaQueryWrapper = new LambdaQueryWrapper<>();
        enumPropLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(EnumProp::getEnumId, enumsId);
        List<EnumProp> enumPropList = list(enumPropLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(enumPropList)) {
            return result;
        }
        for (EnumProp enumProp : enumPropList) {
            EnumPropDTO enumPropDTO = new EnumPropDTO();
            BeanUtils.copyProperties(enumProp, enumPropDTO);
            result.add(enumPropDTO);
        }


        return result;
    }

    @Transactional
    @Override
    public Boolean saveEnumProps(SaveEnumDTO saveEnumDTO) {

        Long enumsId = 0L;
        if (Objects.nonNull(saveEnumDTO.getEnumId()) && saveEnumDTO.getEnumId() > 0) {
            enumsId = saveEnumDTO.getEnumId();
        } else {
            enumsId = enumsService.getErrorCodeEnumId(saveEnumDTO.getAppId());
        }
        if (enumsId == 0) {
            //不存在就新建
            enumsId = createError(saveEnumDTO.getAppId());

        }
        if (CollectionUtils.isEmpty(saveEnumDTO.getEnumPropDTOList())) {
            log.info("saveEnumProps saveEnumDTO.getEnumPropDTOList() isEmpty");
            return true;
        }

        //获取删除的枚举
        List<Long> idList = saveEnumDTO.getEnumPropDTOList().stream().map(EnumPropDTO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<EnumProp> enumPropLambdaQueryWrapper = new LambdaQueryWrapper<>();
        enumPropLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(EnumProp::getEnumId, enumsId).notIn(EnumProp::getId, idList);
        enumPropLambdaQueryWrapper.select(EnumProp::getId);
        List<EnumProp> deleteEnumPropList = list(enumPropLambdaQueryWrapper);
        List<EnumProp> enumPropList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(deleteEnumPropList)) {
            for (EnumProp enumProp : deleteEnumPropList) {
                enumProp.setYn(0);
                enumPropList.add(enumProp);
            }

        }


        for (EnumPropDTO enumPropDTO : saveEnumDTO.getEnumPropDTOList()) {
            EnumProp enumProp = new EnumProp();
            BeanUtils.copyProperties(enumPropDTO, enumProp);
            enumProp.setEnumId(enumsId);
            enumProp.setYn(1);
            saveOrUpdate(enumProp);
            enumPropDTO.setId(enumProp.getId());
            enumPropList.add(enumProp);
        }
        return true;


    }

    @Override
    public List<EnumPropDTO> queryEnumProp(Long appId, Long enumId) {
        List<EnumPropDTO> result = Lists.newArrayList();

        Enums enums = enumsService.getById(enumId);
        if (Objects.isNull(enums)) {
            throw new BizException("枚举不存在");
        }

        if (!enums.getAppId().equals(appId)) {
            throw new BizException("枚举不属于此应用");
        }

        LambdaQueryWrapper<EnumProp> enumPropLambdaQueryWrapper = new LambdaQueryWrapper<>();
        enumPropLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(EnumProp::getEnumId, enumId);
        List<EnumProp> enumPropList = list(enumPropLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(enumPropList)) {
            return result;
        }
        for (EnumProp enumProp : enumPropList) {
            EnumPropDTO enumPropDTO = new EnumPropDTO();
            BeanUtils.copyProperties(enumProp, enumPropDTO);
            result.add(enumPropDTO);
        }


        return result;
    }

    @Override
    public Boolean saveEnums(List<EnumClassDTO> enums, Long appId) {
        List<EnumProp> updateList = Lists.newArrayList();

        //枚举
        Map<String, List<EnumClassDTO>> enumsGroup = enums.stream().filter(enumClassDTO -> enumClassDTO.getType() == 1).collect(Collectors.groupingBy(EnumClassDTO::getEnumClassName, Collectors.toList()));
        if (Objects.nonNull(enumsGroup)) {
            for (String enumName : enumsGroup.keySet()) {
                LambdaQueryWrapper<Enums> enumPropLambdaQueryWrapper = new LambdaQueryWrapper<>();
                enumPropLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(Enums::getEnumName, enumName).eq(Enums::getAppId, appId);
                enumPropLambdaQueryWrapper.eq(Enums::getPackagePath, enumsGroup.get(enumName).get(0).getPackagePath());
                List<Enums> enumsList = enumsService.list(enumPropLambdaQueryWrapper);
                Long enumsId = null;
                if (CollectionUtils.isEmpty(enumsList)) {
                    //不存在就创建
                    Enums enumsCreate = new Enums();
                    enumsCreate.setYn(1);
                    enumsCreate.setEnumType(1);
                    enumsCreate.setAppId(appId);
                    enumsCreate.setEnumName(enumName);
                    if (StringUtils.isNotEmpty(enumsGroup.get(enumName).get(0).getEnumClassDesc())) {
                        enumsCreate.setEnumDesc(enumsGroup.get(enumName).get(0).getEnumClassDesc());
                    }
                    enumsCreate.setPackagePath(enumsGroup.get(enumName).get(0).getPackagePath());
                    enumsCreate.setEnumCode(enumName);
                    enumsService.save(enumsCreate);
                    enumsId = enumsCreate.getId();
                } else {
                    enumsId = enumsList.get(0).getId();
                    Enums enumsUpdate = enumsList.get(0);
                    enumsUpdate.setEnumName(enumName);
                    if (StringUtils.isNotEmpty(enumsGroup.get(enumName).get(0).getEnumClassDesc())) {
                        enumsUpdate.setEnumDesc(enumsGroup.get(enumName).get(0).getEnumClassDesc());
                    }
                    enumsUpdate.setPackagePath(enumsGroup.get(enumName).get(0).getPackagePath());
                    enumsUpdate.setEnumCode(enumName);
                    enumsService.updateById(enumsUpdate);

                }
                getUpdateEnumPropList(enumsGroup.get(enumName), enumsId, updateList);

            }
        }

        //处理错误码
        List<EnumClassDTO> errorCodes = enums.stream().filter(enumClassDTO -> enumClassDTO.getType() == 0).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorCodes)) {
            Long enumsId = enumsService.getErrorCodeEnumId(appId);

            if (enumsId == 0) {
                //不存在就新建
                enumsId = createError(appId);
            }
            //重名就更新
            getUpdateEnumPropList(errorCodes, enumsId, updateList);

        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            return saveOrUpdateBatch(updateList);
        }

        return true;
    }

    @Override
    public Boolean deleteEnumProp(EnumPropDTO enumPropDTO) {

        Enums enums = enumsService.getById(enumPropDTO.getEnumId());
        if (!enums.getAppId().equals(enumPropDTO.getAppId())) {
            throw new BizException("枚举不属于这个应用，无法删除");
        }

        EnumProp enumProp = new EnumProp();
        enumProp.setId(enumPropDTO.getId());
        enumProp.setYn(0);
        return updateById(enumProp);


    }

    @Override
    public Boolean deleteEnumProps(DeleteEnumPropDTO deleteEnumPropDTO) {


        LambdaUpdateChainWrapper<EnumProp> enumPropLambdaQueryWrapper = this.lambdaUpdate();
        enumPropLambdaQueryWrapper
                .set(BaseEntity::getYn, 0)
                .eq(BaseEntity::getYn, 1)
                .in(EnumProp::getId, deleteEnumPropDTO.getEnumPropIds());
//                .eq(EnumProp::getEnumId, deleteEnumPropDTO.getAppId());
        return enumPropLambdaQueryWrapper.update();

    }


    private void getUpdateEnumPropList(List<EnumClassDTO> errorCodes, Long enumsId, List<EnumProp> updateList) {
        List<String> errorNameList = errorCodes.stream().map(EnumClassDTO::getName).collect(Collectors.toList());
        LambdaQueryWrapper<EnumProp> enumPropLambdaQueryWrapper = new LambdaQueryWrapper<>();
        enumPropLambdaQueryWrapper.eq(BaseEntity::getYn, 1).eq(EnumProp::getEnumId, enumsId);
        if (CollectionUtils.isNotEmpty(errorNameList)) {
            enumPropLambdaQueryWrapper.in(EnumProp::getPropName, errorNameList);
        }
        List<EnumProp> enumPropList = this.list(enumPropLambdaQueryWrapper);
        Map<String, EnumProp> enumPropMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(enumPropList)) {
            enumPropMap = enumPropList.stream().collect(Collectors.toMap(EnumProp::getPropName, enumProp -> enumProp));
        }
        for (EnumClassDTO errorCode : errorCodes) {
            if (enumPropMap.containsKey(errorCode.getName())) {
                EnumProp enumProp = enumPropMap.get(errorCode.getName());
                enumProp.setPropDesc(errorCode.getDesc());
                enumProp.setPropCode(errorCode.getCode());

                enumProp.setYn(1);
                updateList.add(enumProp);
            } else {
                EnumProp enumProp = new EnumProp();
                enumProp.setPropDesc(errorCode.getDesc());
                enumProp.setPropCode(errorCode.getCode());
                enumProp.setPropName(errorCode.getName());
                enumProp.setEnumId(enumsId);
                enumProp.setYn(1);
                updateList.add(enumProp);
            }
        }
    }

    private Long createError(Long appId) {

        Enums error = new Enums();
        error.setAppId(appId);
        error.setEnumCode("errorCode");
        error.setEnumName("错误码");
        error.setEnumType(0);
        error.setYn(1);
        enumsService.save(error);
        return error.getId();
    }


}
