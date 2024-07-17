package com.jd.workflow.console.service.local.impl;

import IceInternal.Ex;
import cn.hutool.core.collection.CollectionUtil;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.controller.utils.DtBeanUtils;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.local.LocalTestRecord;
import com.jd.workflow.console.entity.local.RRequirementCase;
import com.jd.workflow.console.dao.mapper.local.RRequirementCaseMapper;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.group.impl.RequirementGroupServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.local.IRRequirementCaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 需求空间与用例关系表 服务实现类
 * </p>
 *
 * @author sunchao81
 * @since 2024-07-02
 */
@Service
public class RRequirementCaseServiceImpl extends ServiceImpl<RRequirementCaseMapper, RRequirementCase> implements IRRequirementCaseService {


    /**
     *
     */
    @Resource
    IMethodManageService methodManageService;

    /**
     *
     */
    @Resource
    IInterfaceManageService interfaceManageService;

    /**
     *
     */
    @Resource
    RequirementGroupServiceImpl requirementGroupService;

    /**
     * 异步
     * 1、绑定case到需求上
     * 2、绑定方法到需求上
     */
    @Async
    public void bindRequirementCase(Long requirementId, LocalTestRecord record){
        try {
            boolean exists = this.lambdaQuery().eq(RRequirementCase::getYn, DataYnEnum.VALID.getCode())
                    .eq(RRequirementCase::getRequirementInfoId, requirementId)
                    .eq(RRequirementCase::getCaseId, record.getCaseId())
                    .eq(RRequirementCase::getCaseSource, record.getCaseSource()).exists();
            if(!exists){
                RRequirementCase creatBean = DtBeanUtils.getCreatBean(record, RRequirementCase.class);
                creatBean.setRequirementInfoId(requirementId);
                creatBean.setCreator(record.getCreator());
                this.save(creatBean);
            }

            MethodManage methodManage = getMethod(record);
            if(Objects.nonNull(methodManage)){
                GroupResolveDto groupResolveDto = new GroupResolveDto().setId(requirementId).setType(2);
                requirementGroupService.addMethod(groupResolveDto,methodManage);
            }
        }catch (Exception e){
            log.error("需求绑定接口错误"+requirementId+",流水ID"+record.getId());
            e.printStackTrace();
        }
    }

    /**
     *
     * @param record
     * @return
     */
    private MethodManage getMethod(LocalTestRecord record) {
        List<MethodManage> list = methodManageService.lambdaQuery().eq(MethodManage::getYn, DataYnEnum.VALID.getCode())
                .eq(MethodManage::getType, 1).eq(MethodManage::getPath, record.getMethodName()).list();
        if(CollectionUtil.isNotEmpty(list)){
            return list.get(0);
        }
        List<InterfaceManage> interfaceList = interfaceManageService.lambdaQuery().eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode())
                .eq(InterfaceManage::getType, 3).eq(InterfaceManage::getServiceCode, record.getInterfaceName()).list();
        if(CollectionUtil.isNotEmpty(interfaceList)){
            List<Long> interfaceIdList = interfaceList.stream().map(InterfaceManage::getId).collect(Collectors.toList());
            List<MethodManage> methodList = methodManageService.lambdaQuery().eq(MethodManage::getYn, DataYnEnum.VALID.getCode())
                    .in(MethodManage::getInterfaceId, interfaceIdList).eq(MethodManage::getType, 3).eq(MethodManage::getMethodCode, record.getMethodName()).list();
            if(CollectionUtil.isNotEmpty(methodList)){
                return methodList.get(0);
            }
        }
        return null;
    }
}
