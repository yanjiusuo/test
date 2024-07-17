package com.jd.workflow.console.service.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dao.mapper.errorcode.REnumMethodPropMapper;
import com.jd.workflow.console.dto.MethodPropDTO;
import com.jd.workflow.console.dto.errorcode.BindPropParam;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.errorcode.REnumMethodProp;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/4
 */
@Slf4j
@Service
public class REnumMethodPropServiceImpl extends ServiceImpl<REnumMethodPropMapper, REnumMethodProp> implements IREnumMethodPropService {

    @Autowired
    private IEnumsService enumsService;

    @Override
    public Boolean bindEnum(BindPropParam bindPropParam) {

        Long enumId = 0L;
        //错误码
        if (bindPropParam.getEnumType() == 0) {
            enumId = enumsService.getErrorCodeEnumId(bindPropParam.getAppId());
            if (enumId == 0) {
                return false;
            }
        } else {
            enumId = bindPropParam.getId();
        }
        if (Objects.isNull(enumId) || enumId == 0L) {
            throw new BizException("enumId 不能为空");
        }
        if (CollectionUtils.isEmpty(bindPropParam.getBindProps())) {
            return true;
        }

        List<MethodPropDTO> propList = Lists.newArrayList();
        for (MethodPropDTO bindProp : bindPropParam.getBindProps()) {
            LambdaQueryWrapper<REnumMethodProp> rEnumMethodPropLambdaQueryWrapper = new LambdaQueryWrapper<>();
            rEnumMethodPropLambdaQueryWrapper
                    .eq(REnumMethodProp::getAppId, bindPropParam.getAppId())
                    .eq(BaseEntity::getYn, 1)
                    .eq(REnumMethodProp::getEnumId, enumId)
                    .eq(REnumMethodProp::getPropName, bindProp.getPropName());
            List<REnumMethodProp> enumMethodPropList = list(rEnumMethodPropLambdaQueryWrapper);
            if (CollectionUtils.isEmpty(enumMethodPropList)) {
                propList.add(bindProp);
            }
        }


        List<REnumMethodProp> enumMethodPropList = Lists.newArrayList();

        for (MethodPropDTO bindProp : propList) {
            REnumMethodProp rEnumMethodProp = new REnumMethodProp();

            rEnumMethodProp.setAppId(bindPropParam.getAppId());
            rEnumMethodProp.setEnumId(enumId);
            rEnumMethodProp.setPropName(bindProp.getPropName());
            rEnumMethodProp.setPropType(bindProp.getPropType());
            rEnumMethodProp.setPropDesc(bindProp.getPropDesc());
            rEnumMethodProp.setYn(1);
            enumMethodPropList.add(rEnumMethodProp);
        }

        if (CollectionUtils.isEmpty(enumMethodPropList)) {
            return true;
        }
        return saveBatch(enumMethodPropList);
    }

    @Override
    public List<REnumMethodProp> bindEnumList(BindPropParam bindPropParam) {
        Long enumId = 0L;
        //错误码
        if (Objects.nonNull(bindPropParam.getEnumType()) && bindPropParam.getEnumType() == 0) {
            enumId = enumsService.getErrorCodeEnumId(bindPropParam.getAppId());
            if (enumId == 0) {
                return Lists.newArrayList();
            }
        } else {
            enumId = bindPropParam.getId();
        }

        LambdaQueryWrapper<REnumMethodProp> rEnumMethodPropLambdaQueryWrapper = new LambdaQueryWrapper<>();


        rEnumMethodPropLambdaQueryWrapper
                .eq(REnumMethodProp::getAppId, bindPropParam.getAppId())
                .eq(BaseEntity::getYn, 1);
        if (Objects.nonNull(enumId) && enumId > 0) {
            rEnumMethodPropLambdaQueryWrapper.eq(REnumMethodProp::getEnumId, enumId);
        }
        if (StringUtils.isNotEmpty(bindPropParam.getProp())) {
            rEnumMethodPropLambdaQueryWrapper.eq(REnumMethodProp::getPropName, bindPropParam.getProp());
        }

        List<REnumMethodProp> enumMethodPropList = list(rEnumMethodPropLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(enumMethodPropList)) {
            return Lists.newArrayList();
        }
        return enumMethodPropList;

    }

    @Override
    public Boolean deleteBindEnum(BindPropParam bindPropParam) {
        Long enumId = 0L;
        //错误码
        if (bindPropParam.getEnumType() == 0) {
            enumId = enumsService.getErrorCodeEnumId(bindPropParam.getAppId());
            if (enumId == 0) {
                return false;
            }
        } else {
            enumId = bindPropParam.getId();
        }
        if (Objects.isNull(enumId)) {
            return false;
        }
        LambdaQueryWrapper<REnumMethodProp> rEnumMethodPropLambdaQueryWrapper = new LambdaQueryWrapper<>();
        rEnumMethodPropLambdaQueryWrapper
                .eq(REnumMethodProp::getAppId, bindPropParam.getAppId())
                .eq(BaseEntity::getYn, 1)
                .eq(REnumMethodProp::getEnumId, enumId)
                .eq(REnumMethodProp::getPropName, bindPropParam.getProp());
        List<REnumMethodProp> enumMethodPropList = list(rEnumMethodPropLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(enumMethodPropList)) {
            return true;
        }
        for (REnumMethodProp rEnumMethodProp : enumMethodPropList) {
            rEnumMethodProp.setYn(0);
        }
        return saveOrUpdateBatch(enumMethodPropList);

    }
}
