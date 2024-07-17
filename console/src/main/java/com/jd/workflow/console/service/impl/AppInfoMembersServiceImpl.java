package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.AppInfoMembersMapper;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.AppInfoMembers;
import com.jd.workflow.console.service.IAppInfoMembersService;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author wufagang
 * @description
 * @date 2023年06月01日 10:50
 */
@Slf4j
@Service
public class AppInfoMembersServiceImpl extends ServiceImpl<AppInfoMembersMapper, AppInfoMembers> implements IAppInfoMembersService {


    @Override
    public void delMembers(String appCode, Integer appId) {
        if(StringUtils.isEmpty(appCode) && Objects.isNull(appId)){
            throw new BizException("参数的参数不正确");
        }
        if(StringUtils.isNotEmpty(appCode)){
            LambdaQueryWrapper<AppInfoMembers> queryWrapper = new LambdaQueryWrapper<AppInfoMembers>();
            queryWrapper.eq(AppInfoMembers::getAppCode,appCode);
            this.remove(queryWrapper);
        }else if(Objects.nonNull(appId)){
            LambdaQueryWrapper<AppInfoMembers> queryWrapper = new LambdaQueryWrapper<AppInfoMembers>();
            queryWrapper.eq(AppInfoMembers::getAppId,appId);
            this.remove(queryWrapper);
        }
    }
    public List<AppInfoMembers> findMembersByAppId(Long appId){
        LambdaQueryWrapper<AppInfoMembers> queryWrapper = new LambdaQueryWrapper<AppInfoMembers>();
        queryWrapper.eq(AppInfoMembers::getAppId,appId);
        queryWrapper.eq(AppInfoMembers::getYn,1);
        return this.list(queryWrapper);
    }

    @Override
    public void saveMembersByStr(AppInfo appInfo, String appCode) {
        String erpStr = appInfo.getMembers();
        if(StringUtils.isEmpty(erpStr)||StringUtils.isEmpty(appCode)){
            throw new BizException("人员信息和应用code不能为空！");
        }
        String[] erpArr = erpStr.split(",");
        List<AppInfoMembers> appInfoMembersList = new ArrayList<>();
        for (String erpOne : erpArr ) {
            if(StringUtils.isEmpty(erpOne)) continue;
            AppInfoMembers appInfoMembers = new AppInfoMembers();
            appInfoMembers.setAppCode(appCode);
            appInfoMembers.setYn(1);
            appInfoMembers.setAppId(appInfo.getId());
            String[] strs = erpOne.split("-");
            if(strs.length == 0){
                continue;
            }else if(strs.length == 1){
                if(StringUtils.isNumeric(strs[0])){
                    continue;
                }
                appInfoMembers.setRoleType(AppUserTypeEnum.MEMBER.getType()); //
                appInfoMembers.setErp(strs[0]);
            }else {
                appInfoMembers.setRoleType(Integer.valueOf(strs[0]));
                appInfoMembers.setErp(strs[1]);
            }

            appInfoMembersList.add(appInfoMembers);
        }
        mergeMembersByErpAndRoleType(findMembersByAppId(appInfo.getId()),appInfoMembersList);
    }
    private AppInfoMembers findByErpAndType(List<AppInfoMembers> members,String erp,Integer roleType){
        for (AppInfoMembers member : members) {
            if(member.getErp().equals(erp) && member.getRoleType().equals(roleType)){
                return member;
            }
        }
        return null;
    }
    public void mergeMembersByErpAndRoleType(List<AppInfoMembers> oldMembers,List<AppInfoMembers> newMembers){
        List<AppInfoMembers> addedMembers = new ArrayList<>();
        List<AppInfoMembers> deletedMembers = new ArrayList<>();
        for (AppInfoMembers oldMember : oldMembers) {
            AppInfoMembers newMember = findByErpAndType(newMembers,oldMember.getErp(),oldMember.getRoleType());
            if(Objects.isNull(newMember)){
                deletedMembers.add(oldMember);
            }
        }
        for (AppInfoMembers newMember : newMembers) {
            AppInfoMembers oldMember = findByErpAndType(oldMembers,newMember.getErp(),newMember.getRoleType());
            if(Objects.isNull(oldMember)){
                addedMembers.add(newMember);
            }
        }
        if(CollectionUtils.isNotEmpty(addedMembers)){
            saveBatch(addedMembers);
        }
        if(CollectionUtils.isNotEmpty(deletedMembers)){
            LambdaQueryWrapper<AppInfoMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(AppInfoMembers::getId,deletedMembers.stream().map(AppInfoMembers::getId).toArray());
            remove(lambdaQueryWrapper);
        }
    }

    @Override
    public List<AppInfoMembers> listAppCodeByErp(String erp) {
        if(StringUtils.isEmpty(erp)){
            throw new BizException("参数erp不能为空");
        }
        LambdaQueryWrapper<AppInfoMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppInfoMembers::getErp,erp);
        lambdaQueryWrapper.eq(AppInfoMembers::getYn, DataYnEnum.VALID.getCode());
        return list(lambdaQueryWrapper);
    }

    @Override
    public List<AppInfoMembers> listErpByAppCode(String appCode) {
        if(StringUtils.isEmpty(appCode)){
            throw new BizException("参数erp不能为空");
        }
        LambdaQueryWrapper<AppInfoMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppInfoMembers::getAppCode,appCode);
        lambdaQueryWrapper.eq(AppInfoMembers::getYn, DataYnEnum.VALID.getCode());
        return list(lambdaQueryWrapper);
    }

    @Override
    public AppInfoMembers getMemberByErp(String erp, Long appId) {
        if(StringUtils.isEmpty(erp) || Objects.isNull(appId)){
            throw new BizException("参数erp和appId不能为空");
        }
        LambdaQueryWrapper<AppInfoMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppInfoMembers::getErp,erp);
        lambdaQueryWrapper.eq(AppInfoMembers::getAppId,appId);
        lambdaQueryWrapper.eq(AppInfoMembers::getYn, DataYnEnum.VALID.getCode());
        final List<AppInfoMembers> list = list(lambdaQueryWrapper);
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<AppInfoMembers> getMemberByAppId( Long appId) {
        if(Objects.isNull(appId)){
            throw new BizException("参数erp和appId不能为空");
        }
        LambdaQueryWrapper<AppInfoMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppInfoMembers::getAppId, appId);
        lambdaQueryWrapper.eq(AppInfoMembers::getYn, DataYnEnum.VALID.getCode());
        return list(lambdaQueryWrapper);

    }

}
