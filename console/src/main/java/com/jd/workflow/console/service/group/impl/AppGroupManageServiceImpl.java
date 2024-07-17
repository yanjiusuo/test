package com.jd.workflow.console.service.group.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.FindMethodInterfaceParam;
import com.jd.workflow.console.dto.InterfacePageQuery;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.group.GroupAddDto;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.manage.InterfaceAppSearchDto;
import com.jd.workflow.console.dto.manage.InterfaceOrMethod;
import com.jd.workflow.console.dto.manage.InterfaceOrMethodSearchDto;
import com.jd.workflow.console.dto.manage.MethodRelatedDto;
import com.jd.workflow.console.dto.requirement.MethodRemoveDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceMethodGroup;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.RelationMethodTag;
import com.jd.workflow.console.entity.doc.MethodDocDto;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.RelationMethodTagService;
import com.jd.workflow.console.service.group.GroupHelper;
import com.jd.workflow.console.service.group.service.IGroupManageService;
import com.jd.workflow.console.service.impl.InterfaceMethodGroupServiceImpl;
import com.jd.workflow.console.service.model.IApiModelService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppGroupManageServiceImpl implements IGroupManageService {
    @Autowired
    IAppInfoService appInfoService;

    @Autowired
    IInterfaceManageService interfaceManageService;

    @Autowired
    InterfaceMethodGroupServiceImpl interfaceMethodGroupService;

    @Autowired
    IMethodManageService methodManageService;

    @Autowired
    private IApiModelService apiModelService;

    @Autowired
    private RelationMethodTagService relationMethodTagService;

    @Override
    public List<InterfaceCountModel> getRootGroups(GroupResolveDto dto, Integer type) {
        List<AppInterfaceCount> counts = methodManageService.queryInterfaceMethodCount(dto.getId());
        Map<Long, List<AppInterfaceCount>> id2AppInterfaces = counts.stream().collect(Collectors.groupingBy(AppInterfaceCount::getInterfaceId));
        if (dto.getId() == null || dto.getId().equals(0L)) {
            return interfaceManageService.getNoAppInterfaces(type).stream().map(vs -> {
                InterfaceCountModel sortModel = new InterfaceCountModel();
                sortModel.setId(vs.getId());
                sortModel.setName(vs.getName());
                sortModel.setInterfaceType(vs.getType());
                sortModel.setEnName(vs.getServiceCode());
                List<AppInterfaceCount> appInterfaceCounts = id2AppInterfaces.get(vs.getId());
                if (appInterfaceCounts != null) {
                    sortModel.setCount(appInterfaceCounts.get(0).getCount());
                }
                sortModel.setChildren(null);
                return sortModel;
            }).collect(Collectors.toList());
        }
        InterfacePageQuery query = new InterfacePageQuery();
        query.setSize(100L);
        query.setAppId(dto.getId());
        query.setType(type + "");
        List<InterfaceManage> pageResult = interfaceManageService.getAppInterfaces(dto.getId(), null, type);

        return pageResult.stream().map(vs -> {
            InterfaceCountModel sortModel = new InterfaceCountModel();
            sortModel.setId(vs.getId());
            sortModel.setName(vs.getName());
            sortModel.setInterfaceType(type);
            sortModel.setAppId(vs.getAppId());
            sortModel.setEnName(vs.getServiceCode());
            List<AppInterfaceCount> appInterfaceCounts = id2AppInterfaces.get(vs.getId());
            if (appInterfaceCounts != null) {
                sortModel.setCount(appInterfaceCounts.get(0).getCount());
            }
            sortModel.setChildren(null);
            return sortModel;
        }).collect(Collectors.toList());
    }

    public Long addDoc(GroupResolveDto dto, Long interfaceId, Long groupId, MethodDocDto methodDocDto) {
        Guard.notEmpty(interfaceId, "interfaceId不可为空");
        if (interfaceId.equals(0L)) {
            throw new BizException("接口id不可为0");
        }
//        T entity = findEntity(dto.getId(), interfaceId);
        InterfaceManage entity = interfaceMethodGroupService.findGroupSortTree(interfaceId);
        methodDocDto.setInterfaceId(interfaceId);
        Long id = methodManageService.createDoc(methodDocDto);
        MethodSortModel sortModel = new MethodSortModel();
        sortModel.setId(id);
        sortModel.setInterfaceType(InterfaceTypeEnum.DOC.getCode());
        sortModel.setName(methodDocDto.getName());
        if (groupId != null) {
            GroupSortModel group = entity.getSortGroupTree().findGroup(groupId);
            group.getChildren().add(sortModel);
        } else {
            entity.getSortGroupTree().getTreeItems().add(sortModel);
        }
        interfaceManageService.updateById(entity);
        return id;
    }


    @Override
    public Long addGroup(GroupResolveDto dto, GroupAddDto groupAddDto) {
        return interfaceMethodGroupService.addGroup(groupAddDto.getName(), groupAddDto.getEnName(), groupAddDto.getInterfaceId(), groupAddDto.getParentId());
    }

    @Override
    public Boolean modifyGroupName(GroupResolveDto dto, Long id, String name, String enName) {
        return interfaceMethodGroupService.modifyGroupName(id, name, enName);
    }

    @Override
    public boolean removeNode(GroupResolveDto dto, Long currentNodeId, Long currentNodeType, Long rootId, Long parentId) {
        if (TreeSortModel.TYPE_METHOD.equals(currentNodeType + "")) {
            return methodManageService.removeMethodByIds(Collections.singletonList(currentNodeId));
        } else if (TreeSortModel.TYPE_INTERFACE.equals(currentNodeType + "")) {
            return interfaceManageService.remove(currentNodeId);
        } else {
            return interfaceMethodGroupService.removeGroup(currentNodeId);
        }
    }

    @Override
    public boolean removeMethodByIds(GroupResolveDto dto, List<MethodRemoveDto> ids) {
        if (ids.isEmpty()) return false;
        return methodManageService.removeByIds(ids.stream().map(item -> item.getId()).collect(Collectors.toList()));
    }

    @Override
    public boolean removeInterfaceByIds(GroupResolveDto dto, List<Long> ids) {
        if (ids.isEmpty()) return false;
        boolean result = false;
        for (Long id : ids) {
            result = interfaceManageService.remove(id);
        }
        return result;
    }


    @Override
    public MethodGroupTreeDTO findMethodGroupTree(GroupResolveDto dto, Long interfaceId) {
        return interfaceMethodGroupService.findMethodGroupTree(interfaceId);
    }

    @Override
    public Boolean modifyMethodGroupTree(GroupResolveDto dto, MethodGroupTreeDTO groupTreeDto) {
        //针对分组之间移动场景
        modifyMethodInterfaceId(dto, groupTreeDto);
        //分组内部移动节点位置
        return interfaceMethodGroupService.modifyMethodGroupTree(groupTreeDto);
    }

    /**
     * 移动方法到新分组
     *
     * @param dto
     * @param groupTreeDto
     * @return
     */
    private Boolean modifyMethodInterfaceId(GroupResolveDto dto, MethodGroupTreeDTO groupTreeDto) {
        if (Objects.nonNull(dto.getFromInterfaceId()) && Objects.nonNull(groupTreeDto.getInterfaceId()) && Objects.nonNull(dto.getMethodId())) {
            if (dto.getFromInterfaceId().equals(groupTreeDto.getInterfaceId())) {
                return true;
            }
            //原分组上，移除这个方法。
            InterfaceManage interfaceManage = interfaceMethodGroupService.findGroupSortTree(dto.getFromInterfaceId());
            if (Objects.nonNull(interfaceManage.getSortGroupTree())) {
                GroupSortModel groupSortModel = interfaceManage.getSortGroupTree().findMethodParent(dto.getMethodId());
                if (Objects.nonNull(groupSortModel)) {
                    interfaceManage.getSortGroupTree().removeMethod(dto.getMethodId(), groupSortModel.getId());
                    interfaceManageService.updateById(interfaceManage);
                }

            }


            //新分组上，增加这个方法。
            MethodManage methodManage = methodManageService.getById(dto.getMethodId());
            methodManage.setInterfaceId(groupTreeDto.getInterfaceId());
            methodManageService.updateById(methodManage);
            return true;

        }

        if (Objects.nonNull(dto.getFromInterfaceId()) && Objects.nonNull(groupTreeDto.getInterfaceId()) && Objects.nonNull(dto.getGroupId())) {
            if (dto.getFromInterfaceId().equals(groupTreeDto.getInterfaceId())) {
                return true;
            }
            //原分组上，移除这个文件夹和下面的方法
            List<Long> methodIdList = Lists.newArrayList();
            InterfaceManage interfaceManage = interfaceMethodGroupService.findGroupSortTree(dto.getFromInterfaceId());
            if (Objects.nonNull(interfaceManage.getSortGroupTree())) {
                GroupSortModel groupSortModel = interfaceManage.getSortGroupTree().findGroup(dto.getGroupId());
                if (Objects.nonNull(groupSortModel)) {
                    if (CollectionUtils.isNotEmpty(groupSortModel.allChildMethods())) {
                        methodIdList = groupSortModel.allChildMethods().stream().map(TreeSortModel::getId).collect(Collectors.toList());

                    }
                    interfaceManage.getSortGroupTree().removeGroup(dto.getGroupId(), null);

                    interfaceManageService.updateById(interfaceManage);
                }

                //把分组文件夹挪到新的分组下
                InterfaceMethodGroup interfaceMethodGroup = interfaceMethodGroupService.getById(dto.getGroupId());
                if (Objects.nonNull(interfaceMethodGroup)) {
                    interfaceMethodGroup.setInterfaceId(groupTreeDto.getInterfaceId());
                    interfaceMethodGroupService.updateById(interfaceMethodGroup);
                }
            }

            if (CollectionUtils.isNotEmpty(methodIdList)) {
                //所有接口父级改成新的分组id
                methodManageService.lambdaUpdate().in(MethodManage::getId, methodIdList).set(MethodManage::getInterfaceId, groupTreeDto.getInterfaceId()).update();
            }
            return true;

        }

        return false;
    }

    @Override
    public List<TreeSortModel> searchTree(GroupResolveDto dto, int interfaceType, String search) {
        List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterfaces(dto.getId(), null, interfaceType);
        List<Long> interfaceIds = appInterfaces.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<InterfaceMethodGroup> groups = interfaceMethodGroupService.searchGroup(dto.getType(), search, interfaceIds);
        List<Integer> types = new ArrayList<>();
        types.add(interfaceType);
        types.add(InterfaceTypeEnum.DOC.getCode());
        List<Long> methodIds = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(dto.getTagName())) {
            methodIds = relationMethodTagService.queryMethodIdByTagName(dto.getId(), dto.getTagName());
            if (CollectionUtils.isEmpty(methodIds)) {
                methodIds.add(0L);
            }
        }
        List<MethodManage> methods = methodManageService.searchMethod(types, search, interfaceIds, methodIds);
//        if(dto.getType()==1&&CollectionUtils.isNotEmpty(dto.getTagName())&&CollectionUtils.isNotEmpty(methods)){
//            List<Long> methodIds=relationMethodTagService.queryMethodIdByTagName(dto.getId(),dto.getTagName());
//            if(CollectionUtils.isEmpty(methodIds)){
//                methods.clear();
//            }else{
//                Iterator<MethodManage> it=methods.iterator();
//                while (it.hasNext()) {
//                    if (!methodIds.contains(it.next().getId())) {
//                        it.remove();
//                    }
//                }
//            }
//        }
        return GroupHelper.buildGroupSearchTree(appInterfaces, groups, methods, search, dto.getTagName());
    }

    @Override
    public IPage<MethodManage> findGroupMethods(GroupResolveDto dto, Long interfaceId, Integer status, Long groupId, String search, Long pageNo, Long pageSize) {
        IPage<MethodManage> records = null;
        InterfaceManage interfaceObj = interfaceManageService.getById(interfaceId);
        if (groupId == null) {
            records = methodManageService.getInterfaceMethods(interfaceId, pageNo, pageSize, search);
        } else {
            InterfaceManage interfaceManage = interfaceMethodGroupService.findGroupSortTree(interfaceId);
            List<Long> ids = Lists.newArrayList();
            if (Objects.nonNull(interfaceManage) && Objects.nonNull(interfaceManage.getSortGroupTree())) {
                GroupSortModel groupSortModel = interfaceManage.getSortGroupTree().findGroup(groupId);
                if (Objects.nonNull(groupSortModel)) {
                    List<MethodSortModel> allMethodList = groupSortModel.allChildMethods();
                    if (CollectionUtils.isNotEmpty(allMethodList)) {
                        ids = groupSortModel.allChildMethods().stream().filter(item -> item.getInterfaceType() == null || item.getInterfaceType() != 20).map(item -> item.getId()).collect(Collectors.toList());
                    }
                    records = methodManageService.listMethodsByIds(search, status, ids, pageNo, pageSize);

                }
            }
        }
        if (Objects.isNull(records) || CollectionUtils.isEmpty(records.getRecords())) {
            return records;
        }
        for (MethodManage record : records.getRecords()) {
            if (interfaceObj.getSortGroupTree() == null) {
                continue;
            }
            GroupSortModel parent = interfaceObj.getSortGroupTree().findMethodParent(record.getId());
            if (parent == null) continue;
            record.setGroupName(parent.getName());
            if(record.getName() == null){
                record.setName(record.getMethodCode());
            }
        }
        for (MethodManage record : records.getRecords()) {
            record.setAppId(interfaceObj.getAppId());
        }
        fixTagNames(records.getRecords(), interfaceObj.getAppId());
        return records;
    }

    public void fixTagNames(List<MethodManage> methods, Long appId) {
        List<RelationMethodTag> methodTags = relationMethodTagService.getMethodTags(methods.stream().map(item -> item.getId()).collect(Collectors.toList()), appId);
        Map<Long, List<RelationMethodTag>> methodId2Tags = methodTags.stream().collect(Collectors.groupingBy(item -> item.getMethodId()));
        for (MethodManage method : methods) {
            List<RelationMethodTag> tags = methodId2Tags.get(method.getId());
            if (tags != null) {
                method.setTags(tags.stream().map(item -> item.getTagName()).collect(Collectors.toSet()));
            }
        }
    }

    @Override
    public IPage<InterfaceOrMethod> findGroupInterface(InterfaceOrMethodSearchDto dto) {
        if (!InterfaceTypeEnum.HTTP.getCode().equals(dto.getInterfaceType()) && !InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(dto.getInterfaceType())) {
            IPage<InterfaceOrMethod> result = new Page<>(dto.getCurrent(), dto.getSize());
          /*  InterfacePageQuery query = new InterfacePageQuery();
            query.setTenantId(UserSessionLocal.getUser().getTenantId());
            query.setCurrent(dto.getCurrent());
            query.setSize(dto.getSize());
            query.setAppId(dto.getId());
            query.setType(dto.getInterfaceType() + "");
            query.setName(dto.getSearch());*/

            InterfaceAppSearchDto searchDto = new InterfaceAppSearchDto();
            searchDto.setType(dto.getInterfaceType());
            searchDto.setAppId(dto.getId());
            searchDto.setName(dto.getSearch());
            searchDto.setCurrent(dto.getCurrent());
            searchDto.setSize(dto.getSize());

            IPage<InterfaceManage> interfacePage = interfaceManageService.listInterface(searchDto);
            result.setRecords(interfacePage.getRecords().stream().map(interfaceManage -> {
                interfaceManage.init();
                InterfaceOrMethod interfaceOrMethod = new InterfaceOrMethod();
                BeanUtils.copyProperties(interfaceManage, interfaceOrMethod);
                interfaceOrMethod.setAppId(interfaceManage.getAppId());
                return interfaceOrMethod;
            }).collect(Collectors.toList()));
            result.setTotal(interfacePage.getTotal());
            return result;
        } else {
            List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterfaces(dto.getId(), null, dto.getInterfaceType());
            Map<Long, List<InterfaceManage>> id2AppInterfaces = appInterfaces.stream().collect(Collectors.groupingBy(item -> item.getId()));
            List<Long> interfaceIds = appInterfaces.stream().map(item -> item.getId()).collect(Collectors.toList());

            if (interfaceIds.isEmpty()) {
                return new Page<>(dto.getCurrent(), dto.getSize());
            }
            LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(MethodManage::getType, dto.getInterfaceType());
            if (StringUtils.isNotBlank(dto.getSearch())) {
                appInterfaces.stream().filter(item -> item.getName().equals(dto.getSearch())).findFirst().ifPresent(item -> {
                    lqw.eq(MethodManage::getInterfaceId, item.getId());
                });
                lqw.and(wrapper -> wrapper.like(MethodManage::getName, dto.getSearch()).or().like(MethodManage::getPath, dto.getSearch()));
            }

            lqw.in(MethodManage::getInterfaceId, interfaceIds);
            lqw.eq(dto.getStatus() != null, MethodManage::getStatus, dto.getStatus());
            lqw.eq(MethodManage::getYn, 1);
            IPage<MethodManage> page = methodManageService.page(new Page(dto.getCurrent(), dto.getSize()), lqw);
            fixTagNames(page.getRecords(), dto.getId());
            return page.convert(item -> {
                InterfaceOrMethod interfaceOrMethod = new InterfaceOrMethod();
                BeanUtils.copyProperties(item, interfaceOrMethod);
                final InterfaceManage interfaceManage = id2AppInterfaces.get(item.getInterfaceId()).get(0);
                interfaceOrMethod.setGroupName(interfaceManage.getName());
                interfaceOrMethod.setAppId(interfaceManage.getAppId());

                return interfaceOrMethod;
            });
        }

    }

    @Override
    public List<InterfaceTypeCount> getInterfaceCount(GroupResolveDto dto) {
        List<InterfaceTypeCount> interfaceTypeCountList = methodManageService.queryInterfaceTypeCount(dto.getId());
        try {
            int modelCount = apiModelService.queryModelCount(dto.getId());
            InterfaceTypeCount interfaceTypeCount = new InterfaceTypeCount();
            interfaceTypeCount.setCount(modelCount);
            interfaceTypeCount.setType(InterfaceTypeEnum.MODEL.getCode());
            interfaceTypeCountList.add(interfaceTypeCount);

        } catch (Exception ex) {
            log.error("apiModelService.queryModelCount error", ex);
        }
        return interfaceTypeCountList;
    }

    @Override
    public boolean isMember(GroupResolveDto dto) {

        return appInfoService.isMember(dto.getId());
    }


    public MethodRelatedDto findMethodRelatedParent(FindMethodInterfaceParam findMethodInterfaceParam) {
        Guard.notEmpty(findMethodInterfaceParam.getMethodId(), "methodId不为为空");
        MethodRelatedDto result = new MethodRelatedDto(findMethodInterfaceParam.getMethodId(), findMethodInterfaceParam.getType());
        if (1 == findMethodInterfaceParam.getType()) {
            MethodManage methodManage = methodManageService.getById(findMethodInterfaceParam.getMethodId());
            Guard.notEmpty(methodManage, "无效的id");
            result.setInterfaceId(methodManage.getInterfaceId());
            methodManage.initKey();
            result.setKey(methodManage.getKey());
            result.setInterfaceType(methodManage.getType());

        } else if (3 == findMethodInterfaceParam.getType()) {
            InterfaceManage interfaceManage = interfaceManageService.getById(findMethodInterfaceParam.getMethodId());
            Guard.notEmpty(interfaceManage, "无效的id");
            result.setInterfaceType(interfaceManage.getType());
            result.setKey(findMethodInterfaceParam.getType() + "_" + interfaceManage.getType() + "_" + findMethodInterfaceParam.getMethodId());
        } else if (2 == findMethodInterfaceParam.getType()) {
            InterfaceMethodGroup interfaceMethodGroup = interfaceMethodGroupService.getById(findMethodInterfaceParam.getMethodId());
            Guard.notEmpty(interfaceMethodGroup, "无效的id");
            result.setInterfaceId(interfaceMethodGroup.getInterfaceId());
            result.setKey(findMethodInterfaceParam.getType() + "_null_" + findMethodInterfaceParam.getMethodId());
        }
        return result;
    }


}
