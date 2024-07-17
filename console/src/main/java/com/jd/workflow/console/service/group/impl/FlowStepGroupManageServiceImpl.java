package com.jd.workflow.console.service.group.impl;

import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceMethodGroup;
import com.jd.workflow.console.entity.requirement.FlowStepInterfaceGroup;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.group.FlowStepInterfaceGroupService;
import com.jd.workflow.console.service.group.ITreeModifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlowStepGroupManageServiceImpl extends ITreeModifyService<FlowStepInterfaceGroup> {
    @Autowired
    FlowStepInterfaceGroupService flowStepInterfaceGroupService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IInterfaceMethodGroupService interfaceMethodGroupService;
    @Autowired
    RequirementGroupServiceImpl requirementGroupService;
    @Override
    public List<Long> getInterfaceIds(List<FlowStepInterfaceGroup> entities) {
        return entities.stream().map(item->item.getInterfaceId()).collect(Collectors.toList());
    }

    @Override
    public List<FlowStepInterfaceGroup> getEntities(GroupResolveDto dto, Integer type,boolean excludeBigTextField) {
        return flowStepInterfaceGroupService.getEntities(dto.getId(),type, excludeBigTextField);
    }

    @Override
    public FlowStepInterfaceGroup findEntity(Long id, Long interfaceId) {
        return flowStepInterfaceGroupService.findEntity(id,interfaceId);
    }

    @Override
    public List<FlowStepInterfaceGroup> findEntities(Long id, List<Long> interfaceIds) {
        return flowStepInterfaceGroupService.findEntities(id,interfaceIds);
    }

    public List<Long> getSelectedRequirementInterfaceList(Long requirementId,Long stepId){
        List<FlowStepInterfaceGroup> entities = flowStepInterfaceGroupService.getEntities(stepId, null, true);
        List<Long> interfaceIds = entities.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        return interfaceIds;
    }



    @Override
    public void updateEntityById(FlowStepInterfaceGroup entity) {
        flowStepInterfaceGroupService.updateById(entity);
    }

    @Override
    public void saveEntity(FlowStepInterfaceGroup entity) {
        flowStepInterfaceGroupService.save(entity);
    }

    @Override
    public boolean removeEntity(Long id) {
        return flowStepInterfaceGroupService.removeById(id);
    }

    @Override
    public List<InterfaceMethodGroup> getInterfaceRelatedGroup(Long entityId) {
        return interfaceMethodGroupService.list(entityId, GroupTypeEnum.STEP.getCode());
    }

    @Override
    public void afterAppendGroupTreeModel(GroupResolveDto dto,Long groupId, List<InterfaceSortModel> models) {
        Long requirementId = dto.getRequirementId();
        GroupResolveDto requirementDto = new GroupResolveDto();
        requirementDto.setType(GroupTypeEnum.PRD.getCode());
        requirementDto.setId(requirementId);
        requirementGroupService.appendGroupTreeModel(requirementDto,groupId,models);
    }

    @Override
    public FlowStepInterfaceGroup newEntity(InterfaceSortModel sortModel, GroupResolveDto dto) {
        FlowStepInterfaceGroup group = new FlowStepInterfaceGroup();
        group.setFlowStepId(dto.getId()+"");
        group.setRequirementId(dto.getRequirementId()+"");
        InterfaceManage interfaceManage = interfaceManageService.getById(sortModel.getId());
        group.setInterfaceType(interfaceManage.getType());
        group.setInterfaceId(sortModel.getId());
        return group;
    }


}
