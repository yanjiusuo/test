package com.jd.workflow.console.service.share.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.share.InterfaceShareGroupMapper;
import com.jd.workflow.console.dao.mapper.share.InterfaceShareUsersMapper;
import com.jd.workflow.console.dto.InterfaceShareTreeDTO;
import com.jd.workflow.console.dto.InterfaceShareTreeModel;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.share.*;
import com.jd.workflow.console.entity.InterfaceMethodGroup;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.share.InterfaceShareGroup;
import com.jd.workflow.console.entity.share.InterfaceShareUser;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.share.IInterfaceShareGroupService;
import com.jd.workflow.console.service.share.IInterfaceShareUsersService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 14:32
 * @Description:
 */
@Service
public class InterfaceShareGroupServiceImpl extends ServiceImpl<InterfaceShareGroupMapper, InterfaceShareGroup> implements IInterfaceShareGroupService {

    @Resource
    IInterfaceShareUsersService interfaceShareUsersService;

    @Autowired
    IMethodManageService methodManageService;
    @Resource
    InterfaceShareGroupMapper interfaceShareGroupMapper;

    @Resource
    InterfaceShareUsersMapper interfaceShareUsersMapper;

    @Transactional
    @Override
    public Long addInterfaceShare(InterfaceShareDTO interfaceShareDTO) {
        //判断分享名称是否存在
        if (groupIsExt(interfaceShareDTO)) {
            throw new BizException("分享名称已存在!");
        }
        Long groupId = addInterfaceShareGroup(interfaceShareDTO);
        batchAddInterfaceShareUsers(interfaceShareDTO, groupId);
        return groupId;
    }



    @Override
    public Boolean validShareName(String shareName) {
        InterfaceShareDTO dto = new InterfaceShareDTO();
        dto.setShareGroupName(shareName);
        return groupIsExt(dto);
    }

    @Override
    public Boolean addShareUser(Long shareGroupId) {
        InterfaceShareGroup interfaceShareGroup = interfaceShareGroupMapper.selectById(shareGroupId);
        if (Objects.isNull(interfaceShareGroup)) {
            throw new BizException("分享分组不存在！");
        }
        if (Objects.equals(interfaceShareGroup.getCreator(), UserSessionLocal.getUser().getUserId())) {
            // 被分享人是分享人，不存储
            return false;
        }
        InterfaceShareUser shareUserByGroupIdAndUserCoe = interfaceShareUsersService.getShareUserByGroupIdAndUserCoe(shareGroupId, UserSessionLocal.getUser().getUserId());
        if (Objects.nonNull(shareUserByGroupIdAndUserCoe)) {
            //已分享过不在存储
            return false;
        }
        // 当前用户存入分享分组成员列表中
        return addShareUser(shareGroupId, UserSessionLocal.getUser().getUserId());
    }

    @Override
    public Boolean removeInterfaceShare(RemoveShareGroupDTO dto) {

        int i = 0;
        if (Objects.equals(dto.getType(), 0)) {
            LambdaQueryWrapper<InterfaceShareGroup> lqw = new LambdaQueryWrapper();
            lqw.eq(InterfaceShareGroup::getId, dto.getShareGroupId())
                    .eq(InterfaceShareGroup::getYn, DataYnEnum.VALID.getCode())
                    .eq(InterfaceShareGroup::getCreator, UserSessionLocal.getUser().getUserId());
            InterfaceShareGroup exist = interfaceShareGroupMapper.selectOne(lqw);
            if (Objects.isNull(exist)) {
                throw new BizException("分享分组不存在！");
            }
            // 分享人删除分享信息
            i = interfaceShareGroupMapper.deleteById(dto.getShareGroupId());

        } else {
            // 被分享人取消收藏
            InterfaceShareUser shareUserByGroupIdAndUserCoe = interfaceShareUsersService.getShareUserByGroupIdAndUserCoe(dto.getShareGroupId(), UserSessionLocal.getUser().getUserId());
            if (Objects.isNull(shareUserByGroupIdAndUserCoe)) {
                throw new BizException("暂无权限取消分享！");
            }
            i = interfaceShareUsersMapper.deleteById(shareUserByGroupIdAndUserCoe.getId());
        }
        if (i < 1) {
            return false;
        }
        return true;
    }

    @Override
    public InterfaceShareGroup getEntity(Long id) {
        Guard.notEmpty(id,"分享id不可为空");
        InterfaceShareGroup group = getById(id);
        Guard.notEmpty(group,"无效的分享id");
        return group;
    }

    /**
     * 分享名称是否存在
     *
     * @param interfaceShareDTO
     * @return
     */
    private boolean groupIsExt(InterfaceShareDTO interfaceShareDTO) {
        LambdaQueryWrapper<InterfaceShareGroup> lqw = new LambdaQueryWrapper();
        lqw.eq(InterfaceShareGroup::getShareGroupName, interfaceShareDTO.getShareGroupName())
                .eq(InterfaceShareGroup::getYn, DataYnEnum.VALID.getCode())
                .eq(InterfaceShareGroup::getCreator, UserSessionLocal.getUser().getUserId());
        InterfaceShareGroup exist = interfaceShareGroupMapper.selectOne(lqw);
        return Objects.nonNull(exist);
    }

    @Override
    public QueryShareGroupResultDTO queryInterfaceShareGroup(QueryShareGroupReqDTO query) {
        QueryShareGroupResultDTO resultDTO = new QueryShareGroupResultDTO();
        query.initPageParam(10000);
        List<InterfaceShareGroup> interfaceShareGroups = interfaceShareGroupMapper.queryShareGroupList(query);
        Long count = interfaceShareGroupMapper.queryShareGroupCount(query);
        resultDTO.setTotalCnt(count);
        resultDTO.setList(interfaceShareGroups);
        resultDTO.setCurrentPage(query.getCurrentPage());
        resultDTO.setPageSize(query.getPageSize());
        return resultDTO;
    }

    @Override
    public InterfaceShareTreeDTO findInterfaceShareTree(Long shareGroupId) {
        InterfaceShareGroup interfaceShareGroup = interfaceShareGroupMapper.selectById(shareGroupId);
        if (Objects.isNull(interfaceShareGroup)) {
            throw new BizException("分享分组不存在！");
        }
        InterfaceShareTreeDTO dto = new InterfaceShareTreeDTO();
        dto.setShareGroupId(shareGroupId);
        dto.setGroupLastVersion(interfaceShareGroup.getLastVersion());
        List<MethodSortModel> methodModels = interfaceShareGroup.getSortInterfaceShareTree().allMethods();
        List<Long> methodIds = methodModels.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        List<MethodManage> methods = methodManageService.listMethods(methodIds);
        Map<Long, List<MethodManage>> id2Methods = methods.stream().collect(Collectors.groupingBy(MethodManage::getId));

        for (MethodSortModel methodModel : methodModels) {
            List<MethodManage> methodManages = id2Methods.get(methodModel.getId());
            if(methodManages==null){
                interfaceShareGroup.getSortInterfaceShareTree().removeMethod(methodModel.getId());
            }else{
                methodModel.setName(methodManages.get(0).getName());
                methodModel.setEnName(methodManages.get(0).getMethodCode());
                methodModel.setInterfaceType(methodManages.get(0).getType());
                String path = methodManages.get(0).getPath();
                if(StringUtils.isEmpty(path)){
                    path = methodModel.getName();
                }
                methodModel.setPath(path);
            }

        }

        dto.setTreeModel(interfaceShareGroup.getSortInterfaceShareTree());
        return dto;
    }
    private void validateSameTree(InterfaceShareTreeDTO prev,InterfaceShareTreeDTO next){
        List<MethodSortModel> method1 = prev.getTreeModel().allMethods();
        List<MethodSortModel> method2 = next.getTreeModel().allMethods();
       if(method1.size() != method2.size()){
           throw new BizException("排序失败，排序前后的方法数量不一致！");
       }
        Map<Long, List<MethodSortModel>> id2Methods = method2.stream().collect(Collectors.groupingBy(MethodSortModel::getId));
        for (MethodSortModel method : method1) {
            List<MethodSortModel> exist = id2Methods.get(method.getId());
            if(exist == null){
                throw new BizException("排序失败，"+(method.getName()==null?method.getPath():method.getName())+"方法丢失");
            }
        }

    }
    public Boolean modifyInterfaceShareTree(InterfaceShareTreeDTO dto){
        return modifyInterfaceShareTree(dto,false);
    }
    @Override
    public Boolean modifyInterfaceShareTree(InterfaceShareTreeDTO dto,boolean validate) {
        InterfaceShareTreeDTO groupDto = findInterfaceShareTree(dto.getShareGroupId());

        if (!Objects.equals(dto.getGroupLastVersion(), groupDto.getGroupLastVersion())) {
            throw new BizException("多人同时操作，版本不一致，请重新刷新页面操作!");
        }
        if (dto.getTreeModel() == null) {
            dto.setTreeModel(new InterfaceShareTreeModel());
        }
        if(validate){
            validateSameTree(groupDto,dto);
        }

        //更新数据库
        InterfaceShareGroup update = new InterfaceShareGroup();
        update.setId(dto.getShareGroupId());
        update.setLastVersion(String.valueOf(System.currentTimeMillis()));
        update.setSortInterfaceShareTree(dto.getTreeModel());
        update.setModified(new Date());
        update.setModifier(UserSessionLocal.getUser().getUserId());
        LambdaQueryWrapper<InterfaceShareGroup> updateLqw = new LambdaQueryWrapper();
        updateLqw.eq(InterfaceShareGroup::getId, update.getId());
        updateLqw.eq(InterfaceShareGroup::getYn, DataYnEnum.VALID.getCode());
        updateLqw.eq(InterfaceShareGroup::getLastVersion, dto.getGroupLastVersion());
        int result = interfaceShareGroupMapper.update(update, updateLqw);
        if (result < 1) {
            throw new BizException("更新重复操作频繁,请重新刷新页面操作!");
        }
        return true;
    }

    @Override
    public Long addGroup(String name, Long shareGroupId, Long parentId) {
        Long newGroupId = System.currentTimeMillis();
        InterfaceShareTreeDTO dto = findInterfaceShareTree(shareGroupId);
        dto.removeGroup(newGroupId);
        dto.insertGroup(parentId, newGroupId, name);
        modifyInterfaceShareTree(dto);
        return newGroupId;
    }
    private void mergeData(InterfaceShareTreeModel shareTreeModel,List<TreeSortModel> models){
        shareTreeModel.mergeTreeItems(models);
    }
    @Override
    public Long appendInterfaceShare(AppendShareDTO target) {
        InterfaceShareTreeDTO dto = findInterfaceShareTree(target.getId());
        //dto.getTreeModel().getTreeItems().addAll(target.getTreeModel().getTreeItems());
        final List<TreeSortModel> treeItems = target.getTreeModel().getTreeItems();
        List<TreeSortModel> newTree = new ArrayList<>();
        for (TreeSortModel treeItem : treeItems) {
            if(treeItem instanceof InterfaceSortModel){
                GroupSortModel group = new GroupSortModel();
                BeanUtils.copyProperties(treeItem,group);
                group.setChildren(((InterfaceSortModel) treeItem).getChildren());
                newTree.add(group);
            }else{
                newTree.add(treeItem);
            }
        }
        dto.getTreeModel().removeExist(newTree);
        dto.getTreeModel().removeEmptyGroup(newTree);
        if(newTree.isEmpty()){
            return target.getId();
        }

        mergeData(dto.getTreeModel(), newTree);
        modifyInterfaceShareTree(dto);
        return target.getId();
    }

    @Override
    public boolean modifyGroupName(Long shareGroupId, Long id, String name, Long parentId) {
        InterfaceShareTreeDTO dto = findInterfaceShareTree(shareGroupId);
        dto.updateGroupName(id, name, parentId);
        return modifyInterfaceShareTree(dto);
    }

    @Override
    public boolean removeGroup(Long shareGroupId, Long id, Long parentId) {
        InterfaceShareTreeDTO dto = findInterfaceShareTree(shareGroupId);
        dto.removeGroup(id, parentId);
        return modifyInterfaceShareTree(dto);
    }

    @Override
    public boolean removeShareMethod(Long shareGroupId, Long id, Long parentId) {
        InterfaceShareTreeDTO dto = findInterfaceShareTree(shareGroupId);
        dto.removeMethod(id, parentId);
        modifyInterfaceShareTree(dto);
        return true;
    }

    /**
     * 添加分享人员信息
     *
     * @param groupId
     * @param userCode
     */
    private boolean addShareUser(Long groupId, String userCode) {
        InterfaceShareUser shareUser = new InterfaceShareUser();
        shareUser.setGroupId(groupId);
        shareUser.setSharedUserCode(userCode);
        Date opTime = new Date();
        shareUser.setCreated(opTime);
        shareUser.setModified(opTime);
        shareUser.setCreator(UserSessionLocal.getUser().getUserId());
        shareUser.setModifier(UserSessionLocal.getUser().getUserId());
        shareUser.setYn(DataYnEnum.VALID.getCode());
        boolean flag = interfaceShareUsersService.saveOrUpdate(shareUser);
        if (BooleanUtils.isFalse(flag)) {
            log.error("IInterfaceShareServiceImpl.addShareUser error!!!");
        }
        return true;
    }

    /**
     * 批量添加分享人员信息
     *
     * @param interfaceShareDTO
     */
    private void batchAddInterfaceShareUsers(InterfaceShareDTO interfaceShareDTO, Long groupId) {
        String shareErp = interfaceShareDTO.getShareErp();
        if (StringUtils.isBlank(shareErp)) {
            return;
        }
        List<String> sharedUserCodeList = Arrays.asList(shareErp.split(","));
        if(sharedUserCodeList.contains(UserSessionLocal.getUser().getUserId())){
            throw new BizException("不允许分享给自己");
        }
        List<InterfaceShareUser> shareUsersList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sharedUserCodeList)) {
            // 获取已分享人员信息
            List<String> sharedUser = interfaceShareUsersService.getSharedUserCodeList(groupId);
            if(CollectionUtils.isNotEmpty(sharedUser)){
                // 祛除已分享人员信息
                boolean b = sharedUserCodeList.removeAll(sharedUser);
            }
            sharedUserCodeList.forEach(e -> {
                // 不允许分享给自己
                if (!Objects.equals(e, UserSessionLocal.getUser().getUserId())) {
                    InterfaceShareUser shareUsers = new InterfaceShareUser();
                    shareUsers.setGroupId(groupId);
                    shareUsers.setSharedUserCode(e);
                    Date opTime = new Date();
                    shareUsers.setCreated(opTime);
                    shareUsers.setModified(opTime);
                    shareUsers.setCreator(UserSessionLocal.getUser().getUserId());
                    shareUsers.setModifier(UserSessionLocal.getUser().getUserId());
                    shareUsers.setYn(DataYnEnum.VALID.getCode());
                    shareUsersList.add(shareUsers);
                }
            });
        }

        interfaceShareUsersService.saveBatch(shareUsersList);
    }

    /**
     * 添加分享分组信息
     *
     * @param interfaceShareDTO
     */
    private Long addInterfaceShareGroup(InterfaceShareDTO interfaceShareDTO) {
        InterfaceShareGroup group = new InterfaceShareGroup();
        group.setAcrossApp(interfaceShareDTO.getAcrossApp());
        group.setShareGroupName(interfaceShareDTO.getShareGroupName());
        InterfaceShareTreeModel treeModel = interfaceShareDTO.getInterfaceShareTreeDTO().getTreeModel();
        group.setSortInterfaceShareTree(treeModel);
        group.setLastVersion(String.valueOf(System.currentTimeMillis()));
        Date opTime = new Date();
        group.setCreated(opTime);
        group.setModified(opTime);
        group.setCreator(UserSessionLocal.getUser().getUserId());
        group.setModifier(UserSessionLocal.getUser().getUserId());
        group.setYn(DataYnEnum.VALID.getCode());
         interfaceShareGroupMapper.insert(group);
         return group.getId();
    }
}
