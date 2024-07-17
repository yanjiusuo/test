package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.jd.common.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.OpTypeEnum;
import com.jd.workflow.console.dao.mapper.InterfaceManageMapper;
import com.jd.workflow.console.dao.mapper.InterfaceMethodGroupMapper;
import com.jd.workflow.console.dao.mapper.MethodManageMapper;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.dto.version.CompareVersionDTO;
import com.jd.workflow.console.dto.version.InterfaceInfoReq;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceMethodGroup;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.utils.DigestUtils;
import com.jd.workflow.console.utils.MethodDigest;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.DigestInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 项目名称：联调平台控制台
 * 类 名 称：InterfaceMethodGroupServiceImpl
 * 类 描 述：service层实现
 * 创建时间：2022-11-08 16:46
 * 创 建 人：wangxiaofei8
 */
@Service
@Slf4j
public class InterfaceMethodGroupServiceImpl extends ServiceImpl<InterfaceMethodGroupMapper, InterfaceMethodGroup> implements IInterfaceMethodGroupService {


    @Resource
    private InterfaceManageMapper interfaceManageMapper;

    @Resource
    private MethodManageMapper methodManageMapper;
    @Resource
    private IMethodManageService methodManageService;
    @Autowired
    ScheduledThreadPoolExecutor defaultScheduledExecutor;
    @Autowired
    IInterfaceManageService interfaceManageService;

    @Transactional
    @Override
    public Long addGroup(String name, String enName, Long interfaceId, Long parentId) {
        /*InterfaceMethodGroup lastObj = this.getOne(Wrappers.<InterfaceMethodGroup>lambdaQuery().eq(InterfaceMethodGroup::getInterfaceId, interfaceId)
                .eq(InterfaceMethodGroup::getName, name).eq(InterfaceMethodGroup::getYn, DataYnEnum.VALID.getCode()));
        if(lastObj!=null){
            BizException e =  new BizException("接口下分组名已经存在!");
            e.data(lastObj.getId());
            throw e;
        }*/

        InterfaceMethodGroup entity = new InterfaceMethodGroup();
        entity.setInterfaceId(interfaceId);
        entity.setName(name);
        entity.setEnName(enName);
        entity.setYn(DataYnEnum.VALID.getCode());
        entity.setType(GroupTypeEnum.APP.getCode());
        Date opTime = new Date();
        entity.setCreated(opTime);
        entity.setModified(opTime);
        entity.setCreator(UserSessionLocal.getUser().getUserId());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        boolean save = save(entity);
        Long newGroupId = entity.getId();
        MethodGroupTreeDTO dto = findMethodGroupTree(interfaceId);
        dto.removeGroup(newGroupId);
        dto.insertGroup(parentId, newGroupId);
        modifyMethodGroupTree(dto);
        if (!save) {
            log.error("IInterfaceMethodGroupService.addGroup execute save but return false , param={}>>>>>>>", JSON.toJSONString(entity));
        }
        return entity.getId();
    }

    @Override
    public Boolean modifyGroupName(Long id, String name, String enName) {
        InterfaceMethodGroup lastObj = this.getOne(Wrappers.<InterfaceMethodGroup>lambdaQuery()
                .eq(InterfaceMethodGroup::getId, id).eq(InterfaceMethodGroup::getYn, DataYnEnum.VALID.getCode()));
        if (lastObj == null) {
            throw new BizException("接口下的分组不存在!");
        }
       /* if(name.equals(lastObj.getName())){
            return true;
        }*/
        lastObj.setId(id);
        lastObj.setEnName(enName);
        lastObj.setName(name);
        lastObj.setModified(new Date());
        lastObj.setModifier(UserSessionLocal.getUser().getUserId());
        return updateById(lastObj);
    }

    @Override
    public Boolean removeGroup(Long id) {
        InterfaceMethodGroup lastObj = this.getOne(Wrappers.<InterfaceMethodGroup>lambdaQuery()
                .eq(InterfaceMethodGroup::getId, id).eq(InterfaceMethodGroup::getYn, DataYnEnum.VALID.getCode()));
        if (lastObj == null) {
            throw new BizException("接口下的分组不存在!");
        }
        Long interfaceId = lastObj.getInterfaceId();
        //校验接口是否存在、查询接口表json字段
        InterfaceManage interfaceManager = findGroupSortTree(interfaceId);
        if (interfaceManager == null) {
            throw new BizException("接口不存在!");
        }
        //查询接口下所有方法
        Map<Long, MethodManage> interfaceMethods = findInterfaceMethods(interfaceId, false);
        //校验分组下是否有方法或者其它分组
        if (interfaceManager.getSortGroupTree() != null && !checkRemoveGroupById(interfaceManager.getSortGroupTree().getTreeItems(), id, interfaceMethods)) {
            throw new BizException("分组下存在方法或其它分组,不允许删除!");
        }
        InterfaceMethodGroup entity = new InterfaceMethodGroup();
        entity.setId(id);
        entity.setYn(DataYnEnum.INVALID.getCode());
        entity.setModified(new Date());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        return updateById(entity);
    }

    @Override
    public Map<Long, InterfaceMethodGroup> findInterfaceGroups(Long interfaceId) {
        return findInterfaceGroups(interfaceId, false);
    }

    public MethodGroupTreeDTO findAppHttpTree(Long appId) {
        List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterfaces(appId, null, InterfaceTypeEnum.HTTP.getCode());
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
        MethodGroupTreeModel methodGroupTreeModel = new MethodGroupTreeModel();
        dto.setTreeModel(methodGroupTreeModel);
        List<Future> list = new ArrayList<>();
        for (InterfaceManage appInterface : appInterfaces) {
            InterfaceSortModel interfaceSortModel = new InterfaceSortModel();
            interfaceSortModel.setId(appInterface.getId());
            interfaceSortModel.setName(appInterface.getName());
            methodGroupTreeModel.getTreeItems().add(interfaceSortModel);
            Future<?> future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {

                    MethodGroupTreeDTO treeDto = findMethodGroupTree(appInterface.getId());
                    interfaceSortModel.setChildren(treeDto.getTreeModel().getTreeItems());

                }
            });
            list.add(future);
        }
        for (Future future : list) {
            try {
                future.get();
            } catch (Exception e) {
                throw new BizException("获取失败", e);
            }
        }
        return dto;
    }


    @Override
    public MethodGroupTreeDTO findMethodGroupTree(Long interfaceId) {
        InterfaceManage interfaceMangae = findGroupSortTree(interfaceId);
        if (interfaceMangae == null) {
            throw new BizException("接口不存在!");
        }
        //方法集合
        Map<Long, MethodManage> interfaceMethods = findInterfaceMethods(interfaceId, false);
        Map<Long, InterfaceMethodGroup> interfaceGroups = findInterfaceGroups(interfaceId, false);
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
        dto.setInterfaceId(interfaceId);
        dto.setGroupLastVersion(interfaceMangae.getGroupLastVersion());
        dto.setTreeModel(new MethodGroupTreeModel());
        //接口下无有效方法，则直接返回
        /*if(interfaceMethods.size()==0){
            return dto;
        }*/
        //如果之前无分组树的维护则直接返回默认列表
        if (interfaceMangae.getSortGroupTree() == null) {
            for (Map.Entry<Long, InterfaceMethodGroup> entry : interfaceGroups.entrySet()) {
                GroupSortModel groupSortModel = new GroupSortModel();
                groupSortModel.setId(entry.getValue().getId());
                groupSortModel.setName(entry.getValue().getName());
                groupSortModel.setEnName(entry.getValue().getEnName());
                dto.getTreeModel().getTreeItems().add(groupSortModel);
            }
            List<MethodSortModel> methods = interfaceMethods.values().stream().map(obj -> {
                MethodSortModel m = new MethodSortModel();
                m.setId(obj.getId());
                m.setName(obj.getName());
                m.setEnName(obj.getMethodCode());
                m.setPath(obj.getPath());
                m.setInterfaceType(obj.getType());
                // m.setOrder(count.getAndIncrement());
                if (StringUtils.isNotEmpty(obj.getFunctionId())) {
                    m.setIsColor(true);
                } else {
                    m.setIsColor(false);
                }

                return m;
            }).collect(Collectors.toList());
            dto.getTreeModel().getTreeItems().addAll(methods);
            return dto;
        }
        //去重方法id
        Set<Long> uniqueMethodIds = new HashSet<>();
        //处理分组信息
        if (CollectionUtils.isNotEmpty(interfaceMangae.getSortGroupTree().getTreeItems())) {
            buildMethodGroupTree(interfaceMangae.getSortGroupTree().getTreeItems(), interfaceGroups, interfaceMethods, uniqueMethodIds);
            dto.getTreeModel().setTreeItems(interfaceMangae.getSortGroupTree().getTreeItems());
        }
        //处理无分组信息的方法
     /*   if(CollectionUtils.isNotEmpty(interfaceMangae.getSortGroupTree().getMethodsOfNoGroup())){
            wrapperMethodName(interfaceMangae.getSortGroupTree().getMethodsOfNoGroup(),interfaceMethods,uniqueMethodIds);
            dto.getTreeModel().setMethodsOfNoGroup(interfaceMangae.getSortGroupTree().getMethodsOfNoGroup());
        }*/
        if (interfaceMethods.size() == uniqueMethodIds.size()) {
            dto.getTreeModel().setAppId(interfaceMangae.getAppId());
            dto.setInterfaceId(interfaceId);
            dto.getTreeModel().removeDuplicated();
            return dto;
        }
        //补齐新增的接口方法
        AtomicInteger order = null;
        /*if(CollectionUtils.isEmpty(dto.getTreeModel().getMethodsOfNoGroup())){
            order = new AtomicInteger(1);
        }else{
            order = new AtomicInteger(dto.getTreeModel().getMethodsOfNoGroup().get(dto.getTreeModel().getMethodsOfNoGroup().size()-1).getOrder()+1);
        }*/
        if (dto.getTreeModel().getTreeItems() == null) {
            dto.getTreeModel().setTreeItems(new ArrayList<>());
        }
        for (Map.Entry<Long, MethodManage> entry : interfaceMethods.entrySet()) {
            Long k = entry.getKey();
            MethodManage v = entry.getValue();
            if (!uniqueMethodIds.contains(k)) {
                MethodSortModel m = new MethodSortModel();
                m.setId(v.getId());
                m.setName(v.getName());
                m.setEnName(v.getMethodCode());
                m.setPath(v.getPath());
                m.setInterfaceType(v.getType());
                if (StringUtils.isNotEmpty(v.getFunctionId())) {
                    m.setIsColor(true);
                } else {
                    m.setIsColor(false);
                }
                //m.setOrder(order.getAndIncrement());
                dto.getTreeModel().getTreeItems().add(m);
            }
        }
        dto.getTreeModel().setAppId(interfaceMangae.getAppId());
        dto.setInterfaceId(interfaceId);
        dto.getTreeModel().removeDuplicated();
        return dto;
    }

    @Override
    public Boolean modifyMethodGroupTree(MethodGroupTreeDTO dto) {
        InterfaceManage interfaceMangae = findGroupSortTree(dto.getInterfaceId());
        if (interfaceMangae == null) {
            throw new BizException("接口不存在!");
        }
        //去掉，这个逻辑也不能保证锁住。
//        if (!dto.getGroupLastVersion().equals(interfaceMangae.getGroupLastVersion())) {
//            throw new BizException("多人同时操作，版本不一致，请重新刷新页面操作!");
//        }
        if (dto.getTreeModel() == null) {
            dto.setTreeModel(new MethodGroupTreeModel());
        }
        //方法集合
        Map<Long, MethodManage> interfaceMethods = findInterfaceMethods(dto.getInterfaceId(), false);
        List<MethodSortModel> methodsOfNoGroup = dto.getTreeModel().childMethods();
      /*  if(interfaceMethods.size()==0){
            if(CollectionUtils.isNotEmpty(dto.getTreeModel().getTreeItems())
                    ||
              CollectionUtils.isNotEmpty(methodsOfNoGroup)){
                throw new BizException("接口下已无有效的方法，请重新刷新页面操作!");
            }
        }else{*/
        if (CollectionUtils.isEmpty(dto.getTreeModel().childGroup()) && CollectionUtils.isEmpty(methodsOfNoGroup)) {
            throw new BizException("入参分组信息与方法参数不能为空，请重新操作!");
        }
        Set<Long> uniqueMethodIds = new HashSet<>();
        //校验分组以及分组下方法参数
        if (CollectionUtils.isNotEmpty(dto.getTreeModel().getTreeItems())) {
            Set<Long> uniqueGroupIds = new HashSet<>();
            Map<Long, InterfaceMethodGroup> interfaceGroups = findInterfaceGroups(dto.getInterfaceId());
            checkRequestGroupProperty(dto.getTreeModel().getTreeItems(), interfaceGroups, interfaceMethods, uniqueMethodIds, uniqueGroupIds);
        }
        //校验未分组的方法参数
        // checkRequestMethodProperty(methodsOfNoGroup,interfaceMethods,uniqueMethodIds);
        //校验有效方法数量
        if (uniqueMethodIds.size() != interfaceMethods.size()) {
            throw new BizException("入参方法数与有效方法数不一致，请重新刷新页面操作!");
        }
        List<TreeSortModel> duplicated = dto.getTreeModel().findDuplicated();
        if (CollectionUtils.isNotEmpty(duplicated)) {
            throw new BizException("分组下存在重复方法，请重新操作!");
        }
        // }
        //更新数据库
        InterfaceManage update = new InterfaceManage();
        update.setId(dto.getInterfaceId());
        update.setGroupLastVersion(DateUtil.getCurrentDateMillTime());
        update.setSortGroupTree(dto.getTreeModel());
        update.setModified(new Date());
        update.setModifier(UserSessionLocal.getUser().getUserId());
        LambdaQueryWrapper<InterfaceManage> updateLqw = new LambdaQueryWrapper();
        updateLqw.eq(InterfaceManage::getId, update.getId());
        updateLqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
//        updateLqw.eq(InterfaceManage::getGroupLastVersion, dto.getGroupLastVersion());
        int result = interfaceManageMapper.update(update, updateLqw);
        if (result < 1) {
            throw new BizException("更新重复操作频繁,请重新刷新页面操作!");
        }
        return true;
    }

    @Override
    public MethodGroupTreeDTO findMethodGroupTreeSnapshot(InterfaceManage interfaceMangae, MethodGroupTreeModel groupTreeSnapshot) {
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
        dto.setInterfaceId(interfaceMangae.getId());
        dto.setGroupLastVersion(interfaceMangae.getGroupLastVersion());
        dto.setTreeModel(new MethodGroupTreeModel());
        //分组快照为空则直接返回
        if (groupTreeSnapshot == null || CollectionUtils.isEmpty(groupTreeSnapshot.getTreeItems())) {
            return dto;
        }
        //方法集合
        Map<Long, MethodManage> interfaceMethods = findInterfaceMethods(interfaceMangae.getId(), true);
        Map<Long, InterfaceMethodGroup> interfaceGroups = findInterfaceGroups(interfaceMangae.getId(), true);
        //接口下无有效方法，则直接返回
        if (interfaceMethods.size() == 0) {
            return dto;
        }
        buildMethodGroupTree(groupTreeSnapshot.getTreeItems(), interfaceGroups, interfaceMethods, Sets.newHashSet());
        dto.getTreeModel().setTreeItems(groupTreeSnapshot.getTreeItems());
        return dto;
    }

    @Override
    public MethodGroupTreeModel createGroupTreeSnapshot(InterfaceManage interfaceMangae) {
        //方法集合
        Map<Long, MethodManage> interfaceMethods = findInterfaceMethods(interfaceMangae.getId(), false);
        //接口下无有效方法，则直接返回
        if (interfaceMethods.size() == 0) {
            return new MethodGroupTreeModel();
        }
        Map<Long, InterfaceMethodGroup> interfaceGroups = findInterfaceGroups(interfaceMangae.getId(), false);
        MethodGroupTreeModel tree = new MethodGroupTreeModel();
        //如果之前无分组树的维护则直接返回默认列表
        if (interfaceMangae.getSortGroupTree() == null || CollectionUtils.isNotEmpty(interfaceMangae.getSortGroupTree().getTreeItems())) {
            for (Map.Entry<Long, InterfaceMethodGroup> entry : interfaceGroups.entrySet()) {
                GroupSortModel groupSortModel = new GroupSortModel();
                groupSortModel.setId(entry.getValue().getId());
                tree.getTreeItems().add(groupSortModel);
            }
            List<MethodSortModel> methods = interfaceMethods.values().stream().map(obj -> {
                MethodSortModel m = new MethodSortModel();
                m.setId(obj.getId());
                return m;
            }).collect(Collectors.toList());
            tree.getTreeItems().addAll(methods);
            return tree;
        }
        //去重方法id
        Set<Long> uniqueMethodIds = new HashSet<>();
        buildMethodGroupTree(interfaceMangae.getSortGroupTree().getTreeItems(), interfaceGroups, interfaceMethods, uniqueMethodIds, false, null, null, null);
        tree.setTreeItems(interfaceMangae.getSortGroupTree().getTreeItems());
        if (interfaceMethods.size() == uniqueMethodIds.size()) {
            return tree;
        }
        for (Map.Entry<Long, MethodManage> entry : interfaceMethods.entrySet()) {
            Long k = entry.getKey();
            MethodManage v = entry.getValue();
            if (!uniqueMethodIds.contains(k)) {
                MethodSortModel m = new MethodSortModel();
                m.setId(v.getId());
                tree.getTreeItems().add(m);
            }
        }
        return tree;
    }

    @Override
    public void findGroupTreeVersionDiff(InterfaceInfoReq req, InterfaceManage interfaceObj, CompareVersionDTO dto) {
        Map<Long, InterfaceMethodGroup> interfaceGroups = findInterfaceGroups(interfaceObj.getId(), true);
        Map<Long, MethodManage> interfaceMethods = findInterfaceMethods4Compare(interfaceObj.getId());
        TreeCompareElement treeBaseElement = assembleTreeCompareElement(interfaceObj, dto.getBaseVersion(), interfaceGroups, interfaceMethods);
        TreeCompareElement treeCompareElement = null;
        if (!dto.getBaseVersion().getVersion().equals(dto.getCompareVersion().getVersion())) {
            treeCompareElement = assembleTreeCompareElement(interfaceObj, dto.getCompareVersion(), interfaceGroups, interfaceMethods);
        }
        MethodGroupTreeModel methodGroupTreeModel = compareTree(treeBaseElement, treeCompareElement, interfaceGroups, interfaceMethods);
        //计数
        Map<String, AtomicInteger> count = new HashMap<String, AtomicInteger>() {
            {
                for (OpTypeEnum value : OpTypeEnum.values()) {
                    put(value.getCode(), new AtomicInteger(0));
                }
            }
        };
        filterModifyMethodGroupTree(methodGroupTreeModel.getTreeItems(), count, BooleanUtils.isTrue(req.getOnlyModify()));
        dto.initModifyCnt(count.get(OpTypeEnum.ADD.getCode()).get(), count.get(OpTypeEnum.MODIFY.getCode()).get(), count.get(OpTypeEnum.DELETE.getCode()).get());
        dto.setTreeModel(methodGroupTreeModel);
    }

    @Override
    public List<InterfaceMethodGroup> searchGroup(int type, String search, List<Long> interfaceIds) {
        if (interfaceIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<InterfaceMethodGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceMethodGroup::getYn, 1);
        lqw.in(InterfaceMethodGroup::getInterfaceId, interfaceIds);
        lqw.eq(InterfaceMethodGroup::getType, type);
        lqw.and(wrapper -> {
            wrapper.or().like(InterfaceMethodGroup::getEnName, search).or().like(InterfaceMethodGroup::getName, search);
        });
        return list(lqw);
    }

    @Override
    public List<InterfaceMethodGroup> list(Long interfaceId, int type) {
        LambdaQueryWrapper<InterfaceMethodGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceMethodGroup::getInterfaceId, interfaceId);
        lqw.eq(InterfaceMethodGroup::getType, type);
        lqw.eq(InterfaceMethodGroup::getYn, 1);
        return list(lqw);
    }

    @Override
    public MethodGroupTreeDTO findAppJsfTree(Long appId) {
        List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterfaces(appId, null, InterfaceTypeEnum.JSF.getCode());
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
        MethodGroupTreeModel methodGroupTreeModel = new MethodGroupTreeModel();
        dto.setTreeModel(methodGroupTreeModel);
        List<Future> list = new ArrayList<>();
        for (InterfaceManage appInterface : appInterfaces) {
            InterfaceSortModel interfaceSortModel = new InterfaceSortModel();
            interfaceSortModel.setId(appInterface.getId());
            interfaceSortModel.setName(appInterface.getName());
            methodGroupTreeModel.getTreeItems().add(interfaceSortModel);
            Future<?> future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {

                    MethodGroupTreeDTO treeDto = findMethodGroupTree(appInterface.getId());
                    interfaceSortModel.setChildren(treeDto.getTreeModel().getTreeItems());

                }
            });
            list.add(future);
        }
        for (Future future : list) {
            try {
                future.get();
            } catch (Exception e) {
                throw new BizException("获取失败", e);
            }
        }
        return dto;
    }


    /**
     * 查询接口下所有方法信息
     *
     * @param interfaceId
     * @return
     */
    private Map<Long, MethodManage> findInterfaceMethods(Long interfaceId, boolean isSnapshot) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper();
        lqw.eq(MethodManage::getInterfaceId, interfaceId);
        List<Integer> interfaceTypes = new ArrayList<>();
        interfaceTypes.add(InterfaceTypeEnum.HTTP.getCode());
        interfaceTypes.add(InterfaceTypeEnum.JSF.getCode());
        interfaceTypes.add(InterfaceTypeEnum.BEAN.getCode());
        interfaceTypes.add(InterfaceTypeEnum.EXTENSION_POINT.getCode());
        interfaceTypes.add(InterfaceTypeEnum.DOC.getCode());
        lqw.in(MethodManage::getType, interfaceTypes);
        lqw.eq(!isSnapshot, MethodManage::getYn, DataYnEnum.VALID.getCode());
        lqw.select(MethodManage::getId, MethodManage::getName, MethodManage::getYn, MethodManage::getPath, MethodManage::getType, MethodManage::getMethodCode,MethodManage::getFunctionId);
        List<MethodManage> list = methodManageMapper.selectList(lqw);
        methodManageService.initMethodDeltaInfos(list);
        return CollectionUtils.isEmpty(list) ? Collections.emptyMap() : list.stream().collect(Collectors.toMap(MethodManage::getId, MethodManage -> MethodManage));
    }

    /**
     * 比较接口版本方法摘要查询
     *
     * @param interfaceId
     * @return
     */
    private Map<Long, MethodManage> findInterfaceMethods4Compare(Long interfaceId) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper();
        lqw.eq(MethodManage::getInterfaceId, interfaceId);
        lqw.select(MethodManage::getId, MethodManage::getName, MethodManage::getYn, MethodManage::getDigest);
        List<MethodManage> list = methodManageMapper.selectList(lqw);
        return CollectionUtils.isEmpty(list) ? Collections.emptyMap() : list.stream().collect(Collectors.toMap(MethodManage::getId, MethodManage -> MethodManage));
    }

    /**
     * 查询分组
     *
     * @param interfaceId
     * @param isSnapshot
     * @return
     */
    private Map<Long, InterfaceMethodGroup> findInterfaceGroups(Long interfaceId, boolean isSnapshot) {
        List<InterfaceMethodGroup> list = list(Wrappers.<InterfaceMethodGroup>lambdaQuery().eq(InterfaceMethodGroup::getInterfaceId, interfaceId)
                .eq(InterfaceMethodGroup::getType, GroupTypeEnum.APP.getCode())
                .eq(!isSnapshot, InterfaceMethodGroup::getYn, DataYnEnum.VALID.getCode()));
        return CollectionUtils.isEmpty(list) ? Collections.emptyMap() : list.stream().collect(Collectors.toMap(InterfaceMethodGroup::getId, InterfaceMethodGroup -> InterfaceMethodGroup));
    }

    /**
     * 查询接口的树信息
     *
     * @param interfaceId
     * @return
     */
    public InterfaceManage findGroupSortTree(Long interfaceId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper();
        lqw.eq(InterfaceManage::getId, interfaceId);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.select(InterfaceManage::getId, InterfaceManage::getName, InterfaceManage::getGroupLastVersion, InterfaceManage::getSortGroupTree);
        InterfaceManage interfaceManage = interfaceManageMapper.selectOne(lqw);
        return interfaceManage;
    }

    /**
     * 校验是否能删除分组
     *
     * @param
     * @param groupId
     * @return
     */
    private boolean checkRemoveGroupById(List<TreeSortModel> treeModels, Long groupId, Map<Long, MethodManage> interfaceMethods) {
        if (CollectionUtils.isEmpty(treeModels)) return true;
        for (TreeSortModel treeModel : treeModels) {
            if (!(treeModel instanceof GroupSortModel)) continue;
            GroupSortModel group = (GroupSortModel) treeModel;
            if (group.getId().equals(groupId)) {

                //子分组信息
                if (CollectionUtils.isNotEmpty(group.childGroups())) {
                    return false;
                }
                List<MethodSortModel> childMethods = group.childMethods();
                //有效的方法列表
                if (CollectionUtils.isNotEmpty(childMethods)
                        && childMethods.stream()
                        .filter(obj -> interfaceMethods.containsKey(obj.getId())).findAny().isPresent()) {
                    return false;
                }
                return true;
            } else {
                if (!checkRemoveGroupById(group.getChildren(), groupId, interfaceMethods)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 封装group信息
     *
     * @param groups
     * @param interfaceGroups
     * @param interfaceMethods
     */
    private void buildMethodGroupTree(List<? super TreeSortModel> groups, Map<Long, InterfaceMethodGroup> interfaceGroups, Map<Long, MethodManage> interfaceMethods, Set<Long> uniqueMethodIds) {
        buildMethodGroupTree(groups, interfaceGroups, interfaceMethods, uniqueMethodIds, true, null, null, null);
        //Collections.sort(groups,(a,b)->a.getOrder().compareTo(b.getOrder()));
    }


    private void buildMethodGroupTree(List<? super TreeSortModel> groups, Map<Long, InterfaceMethodGroup> interfaceGroups, Map<Long, MethodManage> interfaceMethods, Set<Long> uniqueMethodIds, boolean isFillName, Map<Long, String> methodDigest, Map<Long, Long> methodGrouId, Long groupId) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        Iterator<? super TreeSortModel> iter = groups.iterator();
        while (iter.hasNext()) {
            TreeSortModel obj = (TreeSortModel) iter.next();
            if (obj instanceof MethodSortModel) {
                if (interfaceMethods.containsKey(((MethodSortModel) obj).getId())) {
                    if (isFillName) {
                        MethodManage methodManage = interfaceMethods.get(((MethodSortModel) obj).getId());
                        obj.setName(methodManage.getName());
                        obj.setPath(methodManage.getPath());
                        obj.setEnName(methodManage.getMethodCode());
                        obj.setInterfaceType(methodManage.getType());
                        if (StringUtils.isNotEmpty(methodManage.getFunctionId())) {
                            obj.setIsColor(true);
                        } else {
                            obj.setIsColor(false);
                        }
                    }
                    uniqueMethodIds.add(((MethodSortModel) obj).getId());
                    if (methodDigest != null)
                        methodDigest.put(obj.getId(), interfaceMethods.get(obj.getId()).getDigest());
                    if (methodGrouId != null) methodGrouId.put(obj.getId(), groupId == null ? 0l : groupId);
                } else {// 分组被移除了
                    log.warn("buildMethodGroupTree method is not existed , methodSortModel={}>>>>", JSON.toJSONString(obj));
                    iter.remove();
                }
            } else if (obj instanceof GroupSortModel) {
                if (interfaceGroups.containsKey(obj.getId())) {
                    if (isFillName) {
                        InterfaceMethodGroup group = interfaceGroups.get(obj.getId());
                        obj.setName(group.getName());
                        obj.setEnName(group.getEnName());
                    }
                } else { // 分组被移除了
                    log.error("buildMethodGroupTree groupId is not match database , groupId={}", obj.getId());
                    iter.remove();
                    continue;
                }
                GroupSortModel groupSortModel = (GroupSortModel) obj;
                //处理子分组 递归处理
                if (CollectionUtils.isNotEmpty(groupSortModel.getChildren())) {
                    buildMethodGroupTree(groupSortModel.getChildren(), interfaceGroups, interfaceMethods, uniqueMethodIds, isFillName, methodDigest, methodGrouId, obj.getId());
                }
            }
        }

    }

    /**
     * 赋值方法名以及过滤删除的方法
     * @param methods
     * @param interfaceMethods
     * @param uniqueMethodIds
     */
 /*   private void wrapperMethodName(List<MethodSortModel> methods, Map<Long, MethodManage> interfaceMethods, Set<Long> uniqueMethodIds){
        for(Iterator<MethodSortModel> iterator = methods.iterator(); iterator.hasNext();){
            MethodSortModel m = iterator.next();
            if(interfaceMethods.containsKey(m.getMethodId())){
                m.setMethodName(interfaceMethods.get(m.getMethodId()).getName());
                uniqueMethodIds.add(m.getMethodId());
            }else{
                log.warn("buildMethodGroupTree method is not existed , methodSortModel={}>>>>",JSON.toJSONString(m));
                iterator.remove();
            }
        }
        Collections.sort(methods,(a,b)->a.getOrder().compareTo(b.getOrder()));
    }*/


    /**
     * 校验分组信息
     *
     * @param groups
     * @param interfaceGroups
     * @param interfaceMethods
     * @param uniqueMethodIds
     */
    private void checkRequestGroupProperty(List<TreeSortModel> groups, Map<Long, InterfaceMethodGroup> interfaceGroups, Map<Long, MethodManage> interfaceMethods, Set<Long> uniqueMethodIds, Set<Long> uniqueGroupIds) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        groups.stream().forEach(obj -> {

           /* if(obj.getOrder()==null||obj.getId()==null||!interfaceGroups.containsKey(obj.getId())){
                throw new BizException("入参分组信息非法，排序、分组ID必须非空且有效，请检查后重新操作！");
            }*/
//            obj.setName(null);
//            obj.setPath(null);
            if (obj instanceof GroupSortModel) {
                if (uniqueGroupIds.contains(obj.getId())) {
                    log.error("checkRequestGroupProperty groupId is duplicate, groupId={} , groupName={}>>>>", obj.getId(), interfaceGroups.get(obj.getId()).getName());
                    throw new BizException("入参中同一分组不能重复使用，请检查后重新操作！");
                }
                uniqueGroupIds.add(obj.getId());
                //处理子分组 递归处理
                checkRequestGroupProperty(((GroupSortModel) obj).getChildren(), interfaceGroups, interfaceMethods, uniqueMethodIds, uniqueGroupIds);
            } else if (obj instanceof MethodSortModel) {
                if (uniqueMethodIds.contains((obj).getId())) {
                    log.error("checkRequestMethodProperty methodId is duplicate, methodId={} , methodName={}>>>>", obj.getId(), interfaceMethods.get(obj.getId()).getName());
                    throw new BizException("入参中同一方法不能挂在不同的分组下，请检查后重新操作！");
                }
                ((MethodSortModel) obj).setName(null);
                uniqueMethodIds.add(obj.getId());
            }

        });

    }


    /**
     * 校验方法信息
     *
     * @param
     * @param
     * @param
     */
   /* private void checkRequestMethodProperty(List<MethodSortModel> methods, Map<Long, MethodManage> interfaceMethods, Set<Long> uniqueMethodIds){
        if(CollectionUtils.isEmpty(methods)){
            return;
        }
        methods.forEach(obj->{
            if(obj.getOrder()==null||obj.getMethodId()==null||!interfaceMethods.containsKey(obj.getMethodId())){
                throw new BizException("入参分组信息非法，排序、分组ID必须非空且有效，请检查后重新操作！");
            }
            if(uniqueMethodIds.contains(obj.getMethodId())){
                log.error("checkRequestMethodProperty methodId is duplicate, methodId={} , methodName={}>>>>",obj.getMethodId(),interfaceMethods.get(obj.getMethodId()).getName());
                throw new BizException("入参中同一方法不能挂在不同的分组下，请检查后重新操作！");
            }
            obj.setMethodName(null);
            uniqueMethodIds.add(obj.getMethodId());
        });
        Collections.sort(methods,(a,b)->a.getOrder().compareTo(b.getOrder()));
    }*/


    static class TreeCompareElement {
        public MethodGroupTreeModel sortGroupTree;
        public Map<Long, String> methodDigest;
        public Map<Long, Long> methodGrouId;
        public boolean isLatest = false;
    }

    /**
     * 封装比较tree
     *
     * @param interfaceObj
     * @param compareVersion
     * @param interfaceGroups
     * @param interfaceMethods
     * @return
     */
    private TreeCompareElement assembleTreeCompareElement(InterfaceManage interfaceObj, InterfaceVersion compareVersion, Map<Long, InterfaceMethodGroup> interfaceGroups, Map<Long, MethodManage> interfaceMethods) {
        TreeCompareElement result = new TreeCompareElement();
        Map<Long, String> methodDigest = new HashMap<>();
        Map<Long, Long> methodGrouId = new HashMap<>();
        if (compareVersion.getVersion().equals(interfaceObj.getLatestDocVersion())) {
            MethodGroupTreeModel sortGroupTree = interfaceObj.getSortGroupTree();
            if (sortGroupTree == null || CollectionUtils.isEmpty(sortGroupTree.getTreeItems())) {
                sortGroupTree = new MethodGroupTreeModel();
                for (Map.Entry<Long, InterfaceMethodGroup> entry : interfaceGroups.entrySet()) {
                    if (DataYnEnum.VALID.getCode().equals(entry.getValue().getYn())) {
                        GroupSortModel groupSortModel = new GroupSortModel();
                        groupSortModel.setId(entry.getValue().getId());
                        groupSortModel.setName(entry.getValue().getName());
                        groupSortModel.setEnName(entry.getValue().getEnName());
                        sortGroupTree.getTreeItems().add(groupSortModel);
                    }
                }
                List<MethodSortModel> methods = interfaceMethods.values().stream().filter(obj -> DataYnEnum.VALID.getCode().equals(obj.getYn())).map(obj -> {
                    MethodSortModel m = new MethodSortModel();
                    m.setId(obj.getId());
                    m.setName(obj.getName());
                    m.setEnName(obj.getMethodCode());
                    methodDigest.put(obj.getId(), obj.getDigest());
                    methodGrouId.put(obj.getId(), 0l);
                    return m;
                }).collect(Collectors.toList());
                sortGroupTree.getTreeItems().addAll(methods);
            } else {
                //去重方法id
                Set<Long> uniqueMethodIds = new HashSet<>();
                buildMethodGroupTree(sortGroupTree.getTreeItems(), interfaceGroups, interfaceMethods, uniqueMethodIds, true, methodDigest, methodGrouId, 0l);
                if (interfaceMethods.size() != uniqueMethodIds.size()) {
                    for (Map.Entry<Long, MethodManage> entry : interfaceMethods.entrySet()) {
                        Long k = entry.getKey();
                        MethodManage v = entry.getValue();
                        if (!uniqueMethodIds.contains(k) && DataYnEnum.VALID.getCode().equals(v.getYn())) {
                            MethodSortModel m = new MethodSortModel();
                            m.setId(v.getId());
                            m.setName(v.getName());
                            m.setEnName(v.getMethodCode());
                            methodDigest.put(v.getId(), v.getDigest());
                            methodGrouId.put(v.getId(), 0l);
                            sortGroupTree.getTreeItems().add(m);
                        }
                    }
                }
            }
            result.sortGroupTree = sortGroupTree;
            result.methodDigest = methodDigest;
            result.methodGrouId = methodGrouId;
            result.isLatest = true;
        } else {
            MethodGroupTreeModel sortGroupTree = compareVersion.getGroupTreeSnapshot();
            result.sortGroupTree = sortGroupTree;
            /*if(sortGroupTree==null||CollectionUtils.isEmpty(sortGroupTree.getTreeItems())){
                result.sortGroupTree = new MethodGroupTreeModel();
            }else{
                buildMethodGroupTree(sortGroupTree.getTreeItems(),interfaceGroups,interfaceMethods,Sets.newHashSet(),true,null,methodGrouId,0l);
                result.sortGroupTree = sortGroupTree;
            }*/
            MethodSnapshot methodSnapshot = compareVersion.getMethodSnapshot();
            if (methodSnapshot != null && CollectionUtils.isNotEmpty(methodSnapshot.getMethods())) {
                result.methodDigest = methodSnapshot.getMethods().stream().map(item -> {
                    if (item.getDigest() == null) {
                        item.setDigest("");
                    }
                    return item;
                }).collect(Collectors.toMap(MethodSnapshotItem::getMethodId, MethodSnapshotItem::getDigest));
            } else {
                result.methodDigest = new HashMap<>();
            }
            result.methodGrouId = methodGrouId;
            result.isLatest = false;
        }
        return result;
    }

    /**
     * 比对
     *
     * @param baseTree
     * @param compareTree
     * @param interfaceGroups
     * @param interfaceMethods
     * @return
     */
    private MethodGroupTreeModel compareTree(TreeCompareElement baseTree, TreeCompareElement compareTree, Map<Long, InterfaceMethodGroup> interfaceGroups, Map<Long, MethodManage> interfaceMethods) {
        //版本相同
        if (compareTree == null) {
            processOpTypeMethodGroupTree(baseTree.sortGroupTree.getTreeItems(), OpTypeEnum.CONSTANT);
            return baseTree.sortGroupTree;
        }
        //基础版本为空
        if (baseTree.sortGroupTree == null || CollectionUtils.isEmpty(baseTree.sortGroupTree.getTreeItems())) {
            processOpTypeMethodGroupTree(compareTree.sortGroupTree.getTreeItems(), OpTypeEnum.DELETE);
            return compareTree.sortGroupTree;
        }
        //groupType打标
        Map<Long, Set<String>> groupOpType = new HashMap<>();
        //处理方法更新、新增、不变
        processMethodDiff(baseTree.sortGroupTree.getTreeItems(), baseTree, compareTree, null, groupOpType);
        //处理删除的方法
        Map<Long, List<TreeSortModel>> deleteMethods = new HashMap<Long, List<TreeSortModel>>() {{
            put(0l, new ArrayList<>());
        }};
        for (Map.Entry<Long, String> entry : compareTree.methodDigest.entrySet()) {
            Long methodId = entry.getKey();
            TreeSortModel obj = new MethodSortModel();
            obj.setId(methodId);
            obj.setName(interfaceMethods.containsKey(methodId) ? interfaceMethods.get(methodId).getName() : (methodId + "（已删除）"));
            obj.setOpType(OpTypeEnum.DELETE.getCode());
            Long groupId = compareTree.methodGrouId.get(methodId);
            if (groupId != null && groupOpType.containsKey(groupId)) {
                //操作类型
                groupOpType.get(groupId).add(OpTypeEnum.DELETE.getCode());
                //删除的分组方法
                if (!deleteMethods.containsKey(groupId)) deleteMethods.put(groupId, new ArrayList<>());
                deleteMethods.get(groupId).add(obj);
            } else {
                deleteMethods.get(0l).add(obj);
            }
        }
        //赋值分组操作类型以及补充删除的方法
        processGroupDiff(baseTree.sortGroupTree.getTreeItems(), deleteMethods);
        //补充分组无对应的删除方法
        if (deleteMethods.get(0L).size() > 0) {
            if (baseTree.sortGroupTree.getTreeItems() == null) {
                baseTree.sortGroupTree.setTreeItems(new ArrayList<>());
            }
            baseTree.sortGroupTree.getTreeItems().addAll(deleteMethods.get(0L));
        }
        return baseTree.sortGroupTree;
    }

    /**
     * 处理方法
     *
     * @param baseGroups
     * @param baseTree
     * @param compareTree
     * @param group
     * @param groupOpType
     */
    private void processMethodDiff(List<? super TreeSortModel> baseGroups, TreeCompareElement baseTree, TreeCompareElement compareTree, GroupSortModel group, Map<Long, Set<String>> groupOpType) {
        if (group != null && !groupOpType.containsKey(group.getId())) {
            groupOpType.put(group.getId(), new HashSet<>());
        }
        if (CollectionUtils.isEmpty(baseGroups)) {
            return;
        }
        baseGroups.stream().forEach(item -> {
            TreeSortModel obj = (TreeSortModel) item;
            if (obj instanceof MethodSortModel) {
                if (!compareTree.methodDigest.containsKey(obj.getId())) {
                    obj.setOpType(OpTypeEnum.ADD.getCode());
                } else {
                    //比对是否有变更
                    if (isEqualStruct(compareTree.methodDigest.get(obj.getId()), baseTree.methodDigest.get(obj.getId()))) {
                        obj.setOpType(OpTypeEnum.CONSTANT.getCode());
                    } else {
                        obj.setOpType(OpTypeEnum.MODIFY.getCode());
                    }
                    compareTree.methodDigest.remove(obj.getId());
                }
                if (group != null) groupOpType.get(group.getId()).add(obj.getOpType());
            } else if (obj instanceof GroupSortModel) {
                GroupSortModel groupSortModel = (GroupSortModel) obj;
                //处理子分组 递归处理
                processMethodDiff(groupSortModel.getChildren(), baseTree, compareTree, groupSortModel, groupOpType);
            }
        });
    }

    /**
     * 处理相同操作类型的方法
     *
     * @param groups
     * @param opType
     */
    private void processOpTypeMethodGroupTree(List<? super TreeSortModel> groups, OpTypeEnum opType) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        groups.stream().forEach(item -> {
            TreeSortModel obj = (TreeSortModel) item;
            if (obj instanceof MethodSortModel) {
                obj.setOpType(opType.getCode());
            } else if (obj instanceof GroupSortModel) {
                obj.setOpType(opType.getCode());
                GroupSortModel groupSortModel = (GroupSortModel) obj;
                //处理子分组 递归处理
                if (CollectionUtils.isNotEmpty(groupSortModel.getChildren())) {
                    processOpTypeMethodGroupTree(groupSortModel.getChildren(), opType);
                }
            }
        });
    }

    /**
     * 处理分组
     *
     * @param baseGroups
     * @param deleteMethods
     */
    private void processGroupDiff(List<? super TreeSortModel> baseGroups, Map<Long, List<TreeSortModel>> deleteMethods) {
        if (CollectionUtils.isEmpty(baseGroups)) {
            return;
        }
        baseGroups.stream().forEach(item -> {
            TreeSortModel obj = (TreeSortModel) item;
            if (obj instanceof GroupSortModel) {
                processGroupOpType((GroupSortModel) obj, deleteMethods);
            }
        });
    }

    /**
     * 递归分组操作类型打标
     *
     * @param groupSortModel
     * @param deleteMethods
     * @return
     */
    private String processGroupOpType(GroupSortModel groupSortModel, Map<Long, List<TreeSortModel>> deleteMethods) {
        if (StringUtils.isNotBlank(groupSortModel.getOpType())) {
            return groupSortModel.getOpType();
        }
        //添加删除的方法
        if (deleteMethods.containsKey(groupSortModel.getId())) {
            if (groupSortModel.getChildren() == null) groupSortModel.setChildren(new ArrayList<>());
            groupSortModel.getChildren().addAll(deleteMethods.get(groupSortModel.getId()));
        }
        if (CollectionUtils.isEmpty(groupSortModel.getChildren())) {
            groupSortModel.setOpType(OpTypeEnum.CONSTANT.getCode());
            return OpTypeEnum.CONSTANT.getCode();
        } else {
            Set<String> ops = new HashSet<>();
            for (TreeSortModel item : groupSortModel.getChildren()) {
                if (item instanceof MethodSortModel) {
                    ops.add(item.getOpType());
                } else if (item instanceof GroupSortModel) {
                    ops.add(processGroupOpType((GroupSortModel) item, deleteMethods));
                }
            }
            ops.remove(OpTypeEnum.CONSTANT.getCode());
            String result = OpTypeEnum.MODIFY.getCode();
            if (ops.size() == 0) {
                result = OpTypeEnum.CONSTANT.getCode();
            } else if (ops.size() == 1) {
                result = ops.iterator().next();
            }
            ops.clear();
            ops = null;
            groupSortModel.setOpType(result);
            return result;
        }
    }

    /**
     * 过滤修改数量
     *
     * @param groups
     * @param modifyCount
     * @param isModify
     */
    private void filterModifyMethodGroupTree(List<? super TreeSortModel> groups, Map<String, AtomicInteger> modifyCount, boolean isModify) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        //Iterator<? super TreeSortModel> iterator = groups.iterator();
        for (Iterator<? super TreeSortModel> iterator = groups.iterator(); iterator.hasNext(); ) {
            TreeSortModel obj = (TreeSortModel) iterator.next();
            if (obj instanceof MethodSortModel) {
                modifyCount.get(obj.getOpType()).incrementAndGet();
                if (isModify && OpTypeEnum.CONSTANT.getCode().equals(obj.getOpType())) {
                    iterator.remove();
                }
            } else if (obj instanceof GroupSortModel) {
                if (isModify && OpTypeEnum.CONSTANT.getCode().equals(obj.getOpType())) {
                    iterator.remove();
                } else {
                    GroupSortModel groupSortModel = (GroupSortModel) obj;
                    //处理子分组 递归处理
                    if (CollectionUtils.isNotEmpty(groupSortModel.getChildren())) {
                        filterModifyMethodGroupTree(groupSortModel.getChildren(), modifyCount, isModify);
                    }
                }
            }
        }
    }

    /**
     * 比对结构是否相等
     *
     * @param digest
     * @param compareDigest
     * @return
     */
    private boolean isEqualStruct(String digest, String compareDigest) {
        if (StringUtils.equals(digest, compareDigest)) return true;
        MethodDigest obj = MethodDigest.parse(digest);
        if (obj == null) return false;
        MethodDigest compare = MethodDigest.parse(compareDigest);
        if (compare == null) return false;
        return StringUtils.equals(obj.getStructDigest(), compare.getStructDigest());
    }
}
