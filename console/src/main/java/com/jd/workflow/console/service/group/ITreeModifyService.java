package com.jd.workflow.console.service.group;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.group.GroupAddDto;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.dto.manage.InterfaceAppSearchDto;
import com.jd.workflow.console.dto.manage.InterfaceOrMethod;
import com.jd.workflow.console.dto.manage.InterfaceOrMethodSearchDto;
import com.jd.workflow.console.dto.requirement.MethodRemoveDto;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.doc.MethodDocDto;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.group.impl.AppGroupManageServiceImpl;
import com.jd.workflow.console.service.group.service.IGroupManageService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public   abstract class ITreeModifyService<T extends ITreeEntitySupport>  implements IGroupManageService {
    @Autowired
    IInterfaceMethodGroupService methodGroupService;
    @Autowired
    AppGroupManageServiceImpl appGroupManageService;

    @Autowired
    IMethodManageService methodManageService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
     RelationMethodTagService relationMethodTagService;


    public abstract List<Long> getInterfaceIds(List<T> entities);
    public abstract List<T> getEntities(GroupResolveDto dto, Integer type,boolean excludeBigTextField);
    public abstract T findEntity(Long id, Long interfaceId);
    public abstract List<T> findEntities(Long id, List<Long> interfaceId);
    public abstract void updateEntityById(T entity);
    public abstract void saveEntity(T entity);
    public abstract boolean removeEntity(Long id);

    @Override
    public Long addDoc(GroupResolveDto dto, Long interfaceId, Long groupId, MethodDocDto methodDocDto){
        Guard.notEmpty(interfaceId,"interfaceId不可为空");
        if(interfaceId.equals(0L)){
            throw new BizException("接口id不可为0");
        }
        T entity = findEntity(dto.getId(), interfaceId);
        methodDocDto.setInterfaceId(interfaceId);
        Long id = methodManageService.createDoc(methodDocDto);
        MethodSortModel sortModel = new MethodSortModel();
        sortModel.setId(id);
        sortModel.setInterfaceType(InterfaceTypeEnum.DOC.getCode());
        sortModel.setName(methodDocDto.getName());
        if(groupId != null){
            GroupSortModel group = entity.getSortGroupTree().findGroup(groupId);
            group.getChildren().add(sortModel);
        }else{
            entity.getSortGroupTree().getTreeItems().add(sortModel);
        }
        modifyTree(entity,entity.toGroupDto());
        return id;
    }

    public void validateDto(GroupResolveDto dto){
        Guard.notEmpty(dto.getId(),"id不可为空");
        Guard.notEmpty(dto.getType(),"类型不可为空");
        if(GroupTypeEnum.STEP.getCode().equals(dto.getType())){
            if(dto.getRequirementId() == null){
                throw new BizException("requirementId为空");
            }
        }
    }

    /**
     * 批量删除方法节点，实际的方法不会被删除
     * @param dto
     * @param ids
     */
    @Override
    public boolean removeMethodByIds(GroupResolveDto dto,List<MethodRemoveDto> ids){
        validateDto(dto);
        if(ids.isEmpty()) return false;

        final Map<Long, List<MethodRemoveDto>> id2MethodRemoveDtos = ids.stream().collect(Collectors.groupingBy(MethodRemoveDto::getInterfaceId));
        for (Map.Entry<Long, List<MethodRemoveDto>> entry : id2MethodRemoveDtos.entrySet()) {
            T entity = findEntity(dto.getId(), entry.getKey());
            for (MethodRemoveDto item : entry.getValue()) {
                entity.getSortGroupTree().removeMethod(item.getId(),null);
            }
            modifyTree(entity,entity.toGroupDto());
        }
        return true;
    }
    public boolean removeInterfaceByIds(GroupResolveDto dto, List<Long> ids){
        if(ids.isEmpty()) return false;
        final List<T> entities = findEntities(dto.getId(), ids);
        for (T entity : entities) {
            removeEntity(entity.getId());
        }
        return true;
    }

    public abstract List<InterfaceMethodGroup> getInterfaceRelatedGroup(Long entityId);
    public abstract T newEntity(InterfaceSortModel sortModel,GroupResolveDto dto);
    private MethodGroupTreeModel mergeGroups(T entity,InterfaceSortModel model,int type,MethodGroupTreeModel treeModel,GroupSortModel toGroup){
        MethodGroupTreeModel mergedTreeModel = treeModel;
        if(toGroup != null){
            mergedTreeModel = new MethodGroupTreeModel();
            mergedTreeModel.getTreeItems().add(toGroup);
        }
        List<InterfaceMethodGroup> groups = new ArrayList<>();
        List<GroupSortModel> newGroups = mergedTreeModel.mergeGroupByRelatedId(model.getChildren());
        if(toGroup != null){
            if(mergedTreeModel.getTreeItems().size() > 1){ // 大于1说明合并的分组和当前分组不是同一个分组
                List<TreeSortModel> added = mergedTreeModel.getTreeItems().stream().filter(item -> !item.getId().equals(toGroup.getId())).collect(Collectors.toList());
                toGroup.getChildren().addAll(added);
            }
        }
        Map<GroupSortModel,InterfaceMethodGroup> sort2Interface = new HashMap<>();
        for (GroupSortModel group : newGroups) {
            InterfaceMethodGroup methodGroup = newMethodGroup( entity.getId(),model, type, group);
            groups.add(methodGroup);
            sort2Interface.put(group,methodGroup);
        }
        if(!groups.isEmpty()){
            methodGroupService.saveBatch(groups);
        }
        for (Map.Entry<GroupSortModel, InterfaceMethodGroup> entry : sort2Interface.entrySet()) {
            entry.getKey().setId(entry.getValue().getId());
        }
        return treeModel;
    }
    public boolean removeRelatedInterface(T entity){
        List<GroupSortModel> groups = entity.getSortGroupTree().allGroups();
        List<Long> groupIds = groups.stream().map(item -> item.getId()).collect(Collectors.toList());
        methodGroupService.removeByIds(groupIds);
        return removeEntity(entity.getId());
    }

    @Override
    public boolean  removeNode(GroupResolveDto dto,Long currentNodeId,Long currentNodeType,Long rootId,Long parentId){
       Long interfaceId = rootId;
        if(TreeSortModel.TYPE_INTERFACE.equals(currentNodeType+"")){
            interfaceId = currentNodeId;
        }
        validateDto(dto);
        T entity = findEntity(dto.getId(), interfaceId);
        if(TreeSortModel.TYPE_METHOD.equals(currentNodeType+"")){
            //entity.getSortGroupTree().removeMethod(groupId,parentId);
            return removeMethodModel(dto,interfaceId,currentNodeId,parentId);
        }else if(TreeSortModel.TYPE_INTERFACE.equals(currentNodeType+"" )){
            return removeRelatedInterface(entity);
        }else{
            //entity.getSortGroupTree().removeGroup(groupId,parentId);
            return removeGroupModel(dto,interfaceId,currentNodeId,parentId);
        }
        //updateById(entity);
    }

    @NotNull
    private InterfaceMethodGroup newMethodGroup(Long id,InterfaceSortModel model, int type, GroupSortModel group) {
        InterfaceMethodGroup methodGroup = new InterfaceMethodGroup();
        methodGroup.setType(type);
        methodGroup.setYn(1);
        methodGroup.setInterfaceId(id);
        methodGroup.setName(group.getName());
        methodGroup.setEnName(group.getEnName());
        methodGroup.setRelatedGroupId(group.getId());
        return methodGroup;
    }
    public void afterAppendGroupTreeModel(GroupResolveDto dto,Long groupId,List<InterfaceSortModel> models){}
    public boolean appendGroupTreeModel(GroupResolveDto dto,Long groupId,List<InterfaceSortModel> models){
        if(groupId != null){
            if(models.size() != 1){
                throw new BizException("groupId不为空时，只能有一个接口分组");
            }
        }
        List<InterfaceSortModel> clonedModels = models.stream().map(item->(InterfaceSortModel)item.clone()).collect(Collectors.toList());
        boolean result = false;
        for (InterfaceSortModel model : models) {
            T entity = findEntity(dto.getId(), model.getId());
            if(entity == null){
                /*if(groupId == null){
                    throw new BizException("groupId为空时，选中的接口分组必须为该group对应的分组");
                }*/
                T newEntity = newEntity(model, dto);
                saveEntity(newEntity);
                MethodGroupTreeModel treeModel = new MethodGroupTreeModel();
                mergeGroups(newEntity,model,dto.getType(),treeModel,null);

                newEntity.setGroupLastVersion(DateUtil.getCurrentDateMillTime());
                newEntity.setSortGroupTree(treeModel);
                updateEntityById(newEntity);
                result = true;
            }else{
                MethodGroupTreeModel sortTree = entity.getSortGroupTree();
                sortTree.removeExist(model.getChildren());
                if(!model.allChildMethods().isEmpty()){
                    result = true;
                }
                List<InterfaceMethodGroup> interfaceRelatedGroup = getInterfaceRelatedGroup(entity.getId());
                for (InterfaceMethodGroup methodGroup : interfaceRelatedGroup) {
                    GroupSortModel groupModel = sortTree.findGroup(methodGroup.getId());
                    if(groupModel != null){
                        groupModel.setRelatedId(methodGroup.getRelatedGroupId());
                    }
                }
                GroupSortModel toGroup = null;
                if(groupId !=null){

                    toGroup = sortTree.findGroup(groupId);

                }
                mergeGroups(entity,model, dto.getType(), sortTree,toGroup);
                updateEntityById(entity);
            }
        }
        afterAppendGroupTreeModel(dto,groupId, clonedModels);
        return result;
    }

    public boolean removeGroupModel(GroupResolveDto dto,Long interfaceId,Long groupId,Long parentId){
        T entity = findEntity(dto.getId(), interfaceId);
        boolean result = entity.getSortGroupTree().removeGroup(groupId,parentId);
        modifyTree(entity,entity.toGroupDto());
        methodGroupService.removeById(groupId);
        return result;
    }
    public boolean removeMethodModel(GroupResolveDto dto,Long interfaceId,Long groupId,Long parentId){
        T entity = findEntity(dto.getId(), interfaceId);
        boolean result = entity.getSortGroupTree().removeMethod(groupId,parentId);
        modifyTree(entity,entity.toGroupDto());
        return result;
    }

    public void modifyTree(T tree,MethodGroupTreeDTO dto){

        if(dto.getTreeModel().containsInterface()) {
            throw  new BizException("修改失败，不可包含接口分组节点");
        }
        Guard.notEmpty(dto.getGroupLastVersion(),"分组版本不可为空");
        if(tree.getGroupLastVersion()!=null && !tree.getGroupLastVersion().equals(dto.getGroupLastVersion())){
            throw new BizException("多人同时操作，版本不一致，请重新刷新页面操作!");
        }
        tree.setGroupLastVersion(DateUtil.getCurrentDateMillTime());
        tree.setSortGroupTree(dto.getTreeModel());
        updateEntityById(tree);
    }
    @Override
    public List<InterfaceCountModel> getRootGroups(GroupResolveDto dto, Integer type) {
        validateDto(dto);
        List<T> entities = getEntities(dto, type,false);
        List<Long> interfaceIds = getInterfaceIds(entities);
        List<AppInterfaceCount> list = new ArrayList<>();

        {
            for (T entity : entities) {
                AppInterfaceCount count = new AppInterfaceCount();
                count.setInterfaceId(entity.getInterfaceId());
                count.setCount(entity.getSortGroupTree().allMethods().size());
                count.setType(entity.getInterfaceType());
                list.add(count);
            }

        }

        List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(interfaceIds);
        return GroupHelper.toInterfaceSortModels(interfaceManages,type,list);
    }

    @Override
    public Long addGroup(GroupResolveDto dto, GroupAddDto groupDto) {
        validateDto(dto);
        Guard.notEmpty(groupDto.getInterfaceId(),"interfaceId不可为空");
        T tree = findEntity(dto.getId(), groupDto.getInterfaceId());

        InterfaceMethodGroup group = new InterfaceMethodGroup();
        group.setEnName(groupDto.getEnName());
        group.setName(groupDto.getName());
        group.setInterfaceId(tree.getId());
        group.setType(dto.getType());

        methodGroupService.save(group);


        MethodGroupTreeDTO methodDto = tree.toGroupDto();

        methodDto.insertGroup(groupDto.getParentId(),group.getId());
        modifyTree(tree,methodDto);

        return group.getId();
    }

    @Override
    public Boolean modifyGroupName(GroupResolveDto dto, Long id, String name, String enName) {
        validateDto(dto);
        InterfaceMethodGroup methodGroup = methodGroupService.getById(id);
        methodGroup.setName(name);
        methodGroup.setEnName(enName);
        methodGroupService.updateById(methodGroup);
        return true;
    }


    public Boolean removeGroup(GroupResolveDto dto, Long id) {
        InterfaceMethodGroup methodGroup = methodGroupService.getById(id);
        T tree = findEntity(dto.getId(), methodGroup.getInterfaceId());
        MethodGroupTreeDTO groupDto = tree.toGroupDto();
        groupDto.removeGroup(id);
        modifyTree(tree,groupDto);
        return true;
    }

    @Override
    public MethodGroupTreeDTO findMethodGroupTree(GroupResolveDto dto, Long interfaceId) {
        validateDto(dto);
        T entity = findEntity(dto.getId(), interfaceId);
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        MethodGroupTreeDTO groupTree = entity.toGroupDto();
        List<GroupSortModel> sortGroups = groupTree.getTreeModel().allGroups();
        List<Long> groupIds = sortGroups.stream().map(item -> item.getId()).collect(Collectors.toList());
        if(!groupIds.isEmpty()){
            List<InterfaceMethodGroup> groups = methodGroupService.listByIds(groupIds);
            Map<Long, List<InterfaceMethodGroup>> id2Groups = groups.stream().collect(Collectors.groupingBy(InterfaceMethodGroup::getId));
            for (GroupSortModel sortGroup : sortGroups) {
                List<InterfaceMethodGroup> found = id2Groups.get(sortGroup.getId());
                if(found == null){
                    groupTree.getTreeModel().removeGroup(sortGroup.getId(),null);
                }else{
                    sortGroup.setEnName(found.get(0).getEnName());
                    sortGroup.setName(found.get(0).getName());
                    sortGroup.setAppId(interfaceManage.getAppId());
                    sortGroup.setRelatedId(found.get(0).getRelatedGroupId());
                }

            }
        }
        List<MethodSortModel> allMethods = groupTree.getTreeModel().allMethods();
        List<Long> methodIds = allMethods.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        if(!allMethods.isEmpty()){
            List<MethodManage> methods = methodManageService.listMethods(methodIds);
            Map<Long, List<MethodManage>> id2Methods = methods.stream().collect(Collectors.groupingBy(MethodManage::getId));
            for (MethodSortModel allMethod : allMethods) {
                List<MethodManage> found = id2Methods.get(allMethod.getId());
                if(found == null){
                    groupTree.getTreeModel().removeMethod(allMethod.getId(),null);
                }else{
                    allMethod.setName(found.get(0).getName());
                    allMethod.setPath(found.get(0).getPath());
                    allMethod.setEnName(found.get(0).getMethodCode());
                    allMethod.setAppId(interfaceManage.getAppId());
                    allMethod.setInterfaceType(found.get(0).getType());

                }
            }
        }
        groupTree.setInterfaceId(interfaceId);
        groupTree.getTreeModel().removeDuplicated();
        return groupTree;
    }

    @Override
    public Boolean modifyMethodGroupTree(GroupResolveDto dto, MethodGroupTreeDTO groupTreeDto) {
        validateDto(dto);
        T entity = findEntity(dto.getId(), groupTreeDto.getInterfaceId());
        if(entity == null){
            throw new BizException("该需求不存在");
        }
        groupTreeDto.getTreeModel().initKeys();
        modifyTree(entity,groupTreeDto);
        return true;
    }
    private List<Long> getAllMethodIds(List<T> requirementInterfaces){
        return requirementInterfaces.stream().map(item->{
            if(item.getSortGroupTree() == null){
                return new ArrayList<MethodSortModel>();
            }
            return item.getSortGroupTree().allMethods();
        }).flatMap(item->{
                    return item.stream();
                }).map(treeSortModel -> {
                    return treeSortModel.getId();
                }).distinct().collect(Collectors.toList());
    }
    private int getExistItemsCount(List<Long> methodIds,List<Long> existMethodIds){
        return methodIds.stream().filter(item->existMethodIds.contains(item)).collect(Collectors.toList()).size();
    }
    private List<AppInterfaceCount> getAppInterfaceCount(GroupResolveDto dto){
        List<T> entities = getEntities(dto, null, false);
        List<AppInterfaceCount> list = new ArrayList<>();
        List<Long> existMethodIds = new ArrayList<>();
        for (T entity : entities) {
            existMethodIds.addAll(entity.getSortGroupTree().allMethods().stream().map(item -> item.getId()).collect(Collectors.toList()));
        }
        existMethodIds = methodManageService.getExistIds(existMethodIds);
        for (T entity : entities) {
            AppInterfaceCount count = new AppInterfaceCount();
            count.setInterfaceId(entity.getInterfaceId());
            int existItemsCount = getExistItemsCount(entity.getSortGroupTree().allMethods().stream().map(item -> item.getId()).collect(Collectors.toList()), existMethodIds);
            count.setCount(existItemsCount);
            count.setType(entity.getInterfaceType());
            list.add(count);
        }
        return list;
    }
    public List<InterfaceTypeCount> getInterfaceCount(GroupResolveDto dto){
        List<InterfaceTypeCount> result = new ArrayList<>();
        List<AppInterfaceCount> list = getAppInterfaceCount(dto);
        Map<Integer, List<AppInterfaceCount>> type2App = list.stream().collect(Collectors.groupingBy(AppInterfaceCount::getType));
        for (Map.Entry<Integer, List<AppInterfaceCount>> entry : type2App.entrySet()) {
            InterfaceTypeCount item = new InterfaceTypeCount();
            item.setType(entry.getKey());
            int count = 0;
            for (AppInterfaceCount appInterface : entry.getValue()) {
                count+=appInterface.getCount();
            }
            item.setCount(count);
            result.add(item);
        }
        return result;
    }
    @Override
    public List<TreeSortModel> searchTree(GroupResolveDto dto, int interfaceType, String search) {
        validateDto(dto);
        List<T> entities = getEntities(dto,interfaceType,false);
        List<Long> interfaceIds = getInterfaceIds(entities);
        List<InterfaceManage> interfaces = interfaceManageService.listInterfaceByIds(interfaceIds);
        List<Long> allMethodIds = getAllMethodIds(entities);
        //根据tagName 过滤方法
        filterMethodIdterByTagName(allMethodIds,interfaces,dto.getTagName());
        List<InterfaceMethodGroup> groups = methodGroupService.searchGroup(dto.getType(), search, entities.stream().map(item->item.getId()).collect(Collectors.toList()));

        Map<Long,Long> groupId2InterfaceId = new HashMap<>();
        for (T entity : entities) {
            if(entity.getSortGroupTree() == null) continue;
            for (GroupSortModel allGroup : entity.getSortGroupTree().allGroups()) {
                groupId2InterfaceId.put(allGroup.getId(),entity.getInterfaceId());
            }
        }
        for (InterfaceMethodGroup group : groups) {
            final Long interfaceId = groupId2InterfaceId.get(group.getId());
            if(interfaceId == null) continue;
            group.setInterfaceId(interfaceId);
        }

        Page<MethodManage> methods = methodManageService.listMethodsByIds(search,null, allMethodIds, 1, 50);
        List<TreeSortModel> treeSortModels = GroupHelper.buildGroupSearchTree(interfaces, groups, methods.getRecords(),search,dto.getTagName());

        return treeSortModels;
    }

    /**
     * 根据标签过滤 方法信息
     * @param allMethodIds
     * @param interfaces
     * @param tagName
     */
    private void filterMethodIdterByTagName(List<Long> allMethodIds, List<InterfaceManage> interfaces, List<String> tagName) {
        List<Long> appIds=interfaces.stream().map(InterfaceManage::getAppId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(appIds) && CollectionUtils.isNotEmpty(tagName)) {
            List<Long> methodIds = relationMethodTagService.queryMethodIdByAppIdsAndTagName(appIds,tagName);
            if (CollectionUtils.isEmpty(methodIds)) {
                allMethodIds.clear();
            } else {
                Iterator<Long> it=allMethodIds.iterator();
                while (it.hasNext()) {
                    if (!methodIds.contains(it.next())) {
                        it.remove();
                    }
                }
            }
        }
    }

    public List<MethodSortModel> searchMethod(GroupResolveDto dto,int interfaceType,String search){
        List<T> entities = getEntities(dto,interfaceType,false);
        List<MethodSortModel> allMethods = getAllMethods(entities);
        if(StringUtils.isNotBlank(search)){
            allMethods = allMethods.stream().filter(item->{
                return item.getName() != null && item.getName().contains(search) ||
                item.getPath() !=null && item.getPath().contains(search);
            }).collect(Collectors.toList());
        }
        return allMethods.subList(0,Math.min(allMethods.size(),20));
    }

    private List<MethodSortModel> getAllMethods(List<T> requirementInterfaces){
        return requirementInterfaces.stream().map(item->{
            if(item.getSortGroupTree() == null){
                return new ArrayList<MethodSortModel>();
            }
            return item.getSortGroupTree().allMethods();
        }).flatMap(item->{
            return item.stream();
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<MethodManage> findGroupMethods(GroupResolveDto dto, Long interfaceId,Integer status, Long groupId, String search, Long pageNo, Long pageSize) {
        validateDto(dto);
        T groupSortTree = findEntity(dto.getId(), interfaceId);
        if(groupSortTree ==null){
            throw new BizException("interfaceId无效");
        }
        InterfaceManage manage = interfaceManageService.getById(interfaceId);
        List<MethodSortModel> children;
        if(groupId == null){
            children = groupSortTree.getSortGroupTree().allMethods();
        }else{
            final GroupSortModel group = groupSortTree.getSortGroupTree().findGroup(groupId);
            if(group == null){
                throw new BizException("groupId无效");
            }
            children = group.allChildMethods();
        }

        Page<MethodManage> page = methodManageService.listMethodsByIds(search,status, children.stream().map(item -> item.getId()).collect(Collectors.toList()), pageNo, pageSize);
        appGroupManageService.fixTagNames(page.getRecords(),manage.getAppId());
        for (MethodManage record : page.getRecords()) {
            record.setAppId(manage.getAppId());
            GroupSortModel parent = groupSortTree.getSortGroupTree().findMethodParent(record.getId());
            if(parent != null){
                record.setGroupName(parent.getName());
            }
        }
        return page;
    }
    public IPage<InterfaceOrMethod> findGroupInterface(InterfaceOrMethodSearchDto dto){
        List<T> entities = null;
        if(InterfaceTypeEnum.HTTP.getCode().equals(dto.getInterfaceType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(dto.getInterfaceType())) {
            entities = getEntities(dto, dto.getInterfaceType(), false);
            if(entities.isEmpty()){
                return new Page<>(dto.getCurrent(),dto.getSize());
            }
            Map<Long,MethodGroupTreeModel> id2Group = new HashMap<>();
            List<Long> methodIds = entities.stream().map(item -> {
                id2Group.put(item.getInterfaceId(),item.getSortGroupTree());
                return item.getSortGroupTree().allMethods();
            }).flatMap(item -> {
                return item.stream();
            }).filter(item->item != null).map(item -> item.getId()).collect(Collectors.toList());
            List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(entities.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList()),null);
            final Map<Long, List<InterfaceManage>> id2Interfaces = interfaceManages.stream().collect(Collectors.groupingBy(InterfaceManage::getId));
            Page<MethodManage> page = methodManageService.listMethodsByIds(dto.getSearch(),dto.getStatus(), methodIds, dto.getCurrent(), dto.getSize());

            Map<Long,List<MethodManage>> appId2Methods = new HashMap<>();
            for (MethodManage record : page.getRecords()) {
                List<InterfaceManage> interfaces = id2Interfaces.get(record.getInterfaceId());
                if(interfaces !=null){
                    appId2Methods.computeIfAbsent(interfaces.get(0).getAppId(),k->new ArrayList<>()).add(record);
                }
            }
            for (Map.Entry<Long, List<MethodManage>> entry : appId2Methods.entrySet()) {
                appGroupManageService.fixTagNames(entry.getValue(),entry.getKey());
            }

            return page.convert(item -> {
                InterfaceOrMethod interfaceOrMethod = new InterfaceOrMethod();
                BeanUtils.copyProperties(item, interfaceOrMethod);
                final List<InterfaceManage> interfaces = id2Interfaces.get(item.getInterfaceId());
                if(interfaces !=null){
                    interfaceOrMethod.setAppId(interfaces.get(0).getAppId());
                }
                final MethodGroupTreeModel methodGroupTreeModel = id2Group.get(item.getInterfaceId());
                if(methodGroupTreeModel == null) return interfaceOrMethod;
                GroupSortModel methodParent = methodGroupTreeModel.findMethodParent(item.getId());
                if(methodParent != null){
                    interfaceOrMethod.setGroupName(methodParent.getName());
                }
                return interfaceOrMethod;
            });
        }else{
            entities = getEntities(dto, dto.getInterfaceType(), true);
            if(entities == null || entities.size() == 0){
                return new Page<>(dto.getCurrent(),dto.getSize());
            }
            List<Long> interfaceIds = entities.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());

            InterfaceAppSearchDto searchDto = new InterfaceAppSearchDto();
            searchDto.setInterfaceIds(interfaceIds);
            searchDto.setSize(dto.getSize());
            searchDto.setCurrent(dto.getCurrent());
            //searchDto.setAppId(dto.getId());
            searchDto.setName(dto.getSearch());
            searchDto.setType(dto.getInterfaceType());

            Page<InterfaceManage> page = interfaceManageService.listInterface(searchDto);
            interfaceManageService.initInterfaceAppAndAdminInfo(page.getRecords());
            return page.convert(item->{
                item.init();
                InterfaceOrMethod interfaceOrMethod = new InterfaceOrMethod();
                BeanUtils.copyProperties(item,interfaceOrMethod);
                interfaceOrMethod.setAppId(item.getAppId());
                return interfaceOrMethod;
            });
        }



    }


    public boolean isMember(GroupResolveDto dto){
        List<MemberRelation> members = requirementInfoService.getMembers(dto.getId());
        List<String> users = members.stream().map(item -> item.getUserCode()).collect(Collectors.toList());
        if(users.contains(UserSessionLocal.getUser().getUserId())){
            return true;
        }
        List<T> entities = getEntities(dto,null,true);
        List<Long> interfaceIds = entities.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(interfaceIds);
        if(interfaceManages.isEmpty()){
            return false;
        }

        for (InterfaceManage interfaceManage : interfaceManages) {
            boolean isMember = appInfoService.isMember(interfaceManage.getAppId());
            if(isMember) return true;
        }
        return false;
    }

}
