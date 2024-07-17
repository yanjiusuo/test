package com.jd.workflow.console.service.group;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.group.FlowStepInterfaceGroupMapper;
import com.jd.workflow.console.dto.doc.InterfaceDocConfig;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.entity.requirement.FlowStepInterfaceGroup;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.service.group.impl.FlowStepGroupManageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlowStepInterfaceGroupService extends ServiceImpl<FlowStepInterfaceGroupMapper, FlowStepInterfaceGroup> {
    @Autowired
    FlowStepGroupManageServiceImpl flowStepGroupManageService;
    public List<FlowStepInterfaceGroup> getEntities(Long stepId,Integer interfaceType,boolean excludeBigTextField){
        LambdaQueryWrapper<FlowStepInterfaceGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(interfaceType!=null,FlowStepInterfaceGroup::getInterfaceType,interfaceType);
        if(excludeBigTextField){
            excludeBigTextFiled(lqw);
        }
        lqw.eq(FlowStepInterfaceGroup::getFlowStepId,stepId);
        return list(lqw);
    }
    public void excludeBigTextFiled(LambdaQueryWrapper<FlowStepInterfaceGroup> lqw){
        lqw.select(FlowStepInterfaceGroup.class,x->{
            String[] bigTextFields = new String[]{"sort_group_tree"};
            return Arrays.asList(bigTextFields).indexOf(x.getColumn()) == -1;
        });

    }
    public FlowStepInterfaceGroup findEntity(Long id, Long interfaceId){
        LambdaQueryWrapper<FlowStepInterfaceGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowStepInterfaceGroup::getInterfaceId,interfaceId);
        lqw.eq(FlowStepInterfaceGroup::getFlowStepId,id);
        return getOne(lqw);
    }
    public List<FlowStepInterfaceGroup> findEntities(Long id, List<Long> interfaceIds){
        LambdaQueryWrapper<FlowStepInterfaceGroup> lqw = new LambdaQueryWrapper<>();
        lqw.in(FlowStepInterfaceGroup::getInterfaceId,interfaceIds);
        lqw.eq(FlowStepInterfaceGroup::getFlowStepId,id);
        return list(lqw);
    }
    public void saveDocConfig(InterfaceSortModel interfaceSortModel,Long requirementId,Long stepId){
        GroupResolveDto dto = new GroupResolveDto();
        dto.setId(stepId);
        dto.setRequirementId(requirementId);
        dto.setType(GroupTypeEnum.STEP.getCode());
        flowStepGroupManageService.appendGroupTreeModel(dto,null, Collections.singletonList(interfaceSortModel));
    }
    public void removeDocConfig(InterfaceSortModel interfaceSortModel,Long requirementId,Long stepId){
        FlowStepInterfaceGroup entity = findEntity(stepId, interfaceSortModel.getId());
        flowStepGroupManageService.removeRelatedInterface(entity);
    }
    public void setSelectedRequirementInterfaceIds(Long requirementId,Long stepId,List<Long> interfaceIds){
        List<FlowStepInterfaceGroup> entities = getEntities(stepId,null,false);
        Set<Long> ids = entities.stream().map(item -> item.getInterfaceId()).collect(Collectors.toSet());
        List<Long> newInterfaceIds = new ArrayList<>();
        List<Long> removeInterfaceIds = new ArrayList<>();
        for (Long interfaceId : interfaceIds) {
            if(!ids.contains(interfaceId)) {
                newInterfaceIds.add(interfaceId);
            }
        }
        for (Long id : ids) {
            if(!interfaceIds.contains(id)){
                removeInterfaceIds.add(id);
            }
        }
        for (Long removeInterfaceId : removeInterfaceIds) {
            InterfaceSortModel sortModel = new InterfaceSortModel();
            sortModel.setId(removeInterfaceId);
            removeDocConfig(sortModel,requirementId,stepId);
        }
        for (Long newInterfaceId : newInterfaceIds) {
            InterfaceSortModel docConfig = new InterfaceSortModel();
            docConfig.setId(newInterfaceId);
            saveDocConfig(docConfig,requirementId,stepId);
        }
    }
}
