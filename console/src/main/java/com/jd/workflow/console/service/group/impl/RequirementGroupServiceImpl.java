package com.jd.workflow.console.service.group.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.InterfaceTypeCount;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceMethodGroup;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.requirement.RequirementAppModelSnapshot;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.env.IEnvConfigService;
import com.jd.workflow.console.service.group.GroupHelper;
import com.jd.workflow.console.service.group.ITreeModifyService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.model.IApiModelService;
import com.jd.workflow.console.service.requirement.RequirementAppModelSnapshotService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequirementGroupServiceImpl extends ITreeModifyService<RequirementInterfaceGroup> {
    @Autowired
    RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    IInterfaceMethodGroupService interfaceMethodGroupService;
    @Autowired
    IEnvConfigService envConfigService;

    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    private RequirementInfoService requirementInfoService;

    @Autowired
    private IApiModelService apiModelService;

    @Autowired
    private RequirementAppModelSnapshotService requirementAppModelSnapshotService;

    @Override
    public List<Long> getInterfaceIds(List<RequirementInterfaceGroup> entities) {
        return entities.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
    }

    @Override
    public List<RequirementInterfaceGroup> getEntities(GroupResolveDto dto, Integer type, boolean excludeBigTextField) {
        return requirementInterfaceGroupService.getInterfaceGroups(dto.getId(), type, excludeBigTextField);
    }


    @Override
    public RequirementInterfaceGroup findEntity(Long id, Long interfaceId) {
        return requirementInterfaceGroupService.findEntity(id, interfaceId);
    }

    @Override
    public List<RequirementInterfaceGroup> findEntities(Long id, List<Long> interfaceIds) {
        return requirementInterfaceGroupService.findEntities(id, interfaceIds);
    }

    @Override
    public void updateEntityById(RequirementInterfaceGroup entity) {
        requirementInterfaceGroupService.updateById(entity);
    }

    @Override
    public void saveEntity(RequirementInterfaceGroup entity) {
        requirementInterfaceGroupService.save(entity);
    }

    @Override
    public boolean removeEntity(Long id) {
        return requirementInterfaceGroupService.removeById(id);
    }

    @Override
    public List<InterfaceMethodGroup> getInterfaceRelatedGroup(Long entityId) {
        return interfaceMethodGroupService.list(entityId, GroupTypeEnum.PRD.getCode());
    }

    @Override
    public RequirementInterfaceGroup newEntity(InterfaceSortModel sortModel, GroupResolveDto dto) {
        RequirementInterfaceGroup group = new RequirementInterfaceGroup();
        InterfaceManage interfaceManage = interfaceManageService.getById(sortModel.getId());
        group.setInterfaceType(interfaceManage.getType());
        group.setRequirementId(dto.getId());
        group.setInterfaceId(sortModel.getId());
        return group;
    }

    @Override
    public void afterAppendGroupTreeModel(GroupResolveDto dto, Long groupId, List<InterfaceSortModel> models) {
        // 初始化需求环境信息
        envConfigService.initDemandConfig(dto.getId());
    }

    @Override
    public List<InterfaceTypeCount> getInterfaceCount(GroupResolveDto dto) {
        List<InterfaceTypeCount> interfaceTypeCountList = super.getInterfaceCount(dto);

        //拼上模型的数量
        InterfaceTypeCount interfaceTypeCount = new InterfaceTypeCount();
        interfaceTypeCount.setType(21);
        interfaceTypeCount.setCount(0);
        interfaceTypeCountList.add(interfaceTypeCount);
        RequirementInfo requirementInfo = requirementInfoService.getById(dto.getId());
        if (Objects.isNull(requirementInfo)) {
            return interfaceTypeCountList;

        }
        List<AppInfoDTO> appInfoDTOList = requirementInterfaceGroupService.getRequirementInterfaceId(dto.getId());
        if (CollectionUtils.isEmpty(appInfoDTOList)) {
            return interfaceTypeCountList;
        }
        if (requirementInfo.getStatus() == 2) {
            LambdaQueryWrapper<RequirementAppModelSnapshot> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(RequirementAppModelSnapshot::getRequirementId, dto.getId());
            Integer count = requirementAppModelSnapshotService.count(lambdaQueryWrapper);
            interfaceTypeCount.setCount(count);

        } else {
            LambdaQueryWrapper<ApiModel> lambdaQueryWrapper = new LambdaQueryWrapper<>();

            lambdaQueryWrapper.eq(BaseEntity::getYn, 1).in(ApiModel::getAppId, appInfoDTOList.stream().map(AppInfoDTO::getId).collect(Collectors.toList()));
            Integer count = apiModelService.count(lambdaQueryWrapper);
            interfaceTypeCount.setCount(count);
        }
        return interfaceTypeCountList;

    }


    /**
     *
     * @param dto
     * @param method
     */
    public void addMethod(GroupResolveDto dto, MethodManage method) {
        try{
            InterfaceSortModel interfaceSortModel = new InterfaceSortModel();
            TreeSortModel treeSortModel = GroupHelper.toSortModel(method);
            treeSortModel.setType(1+"");
            interfaceSortModel.getChildren().add(treeSortModel);
            interfaceSortModel.setId(method.getInterfaceId());
            List<InterfaceSortModel> list = Lists.newArrayList(interfaceSortModel);
            appendGroupTreeModel(dto, null, list);
        }catch(Exception e){
            log.error("#上报方法{}#绑定需求{}",method.getId(),dto.getId());
            e.printStackTrace();
        }
    }
}
