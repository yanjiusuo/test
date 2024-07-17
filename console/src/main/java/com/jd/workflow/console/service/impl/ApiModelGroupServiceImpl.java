package com.jd.workflow.console.service.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.jd.jsf.gd.util.StringUtils;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.ApiModelGroupMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.model.RequireModelPageQuery;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.ApiModelGroup;
import com.jd.workflow.console.entity.model.ApiModelTree;
import com.jd.workflow.console.entity.requirement.RequirementApiModelTreeSnapshot;
import com.jd.workflow.console.entity.requirement.RequirementAppModelSnapshot;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.model.IApiModelGroupService;
import com.jd.workflow.console.service.model.IApiModelService;
import com.jd.workflow.console.service.model.IApiModelTreeService;
import com.jd.workflow.console.service.requirement.RequirementApiModelTreeSnapshotService;
import com.jd.workflow.console.service.requirement.RequirementAppModelSnapshotService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/20
 */
@Slf4j
@Service
public class ApiModelGroupServiceImpl extends ServiceImpl<ApiModelGroupMapper, ApiModelGroup> implements IApiModelGroupService {


    @Autowired
    private IApiModelTreeService apiModelGroupList;

    @Autowired
    private IApiModelService apiModelService;

    @Autowired
    private RequirementInterfaceGroupService requirementInterfaceGroupService;

    @Autowired
    private RequirementApiModelTreeSnapshotService requirementApiModelTreeSnapshotService;

    @Autowired
    private RequirementAppModelSnapshotService requirementAppModelSnapshotService;
    @Autowired
    private RequirementInfoService requirementInfoService;

    @Autowired
    private IAppInfoService appInfoService;

    /**
     * 添加分组
     *
     * @param appId
     * @param name
     * @param enName
     * @param parentId
     * @return
     */
    @Transactional
    @Override
    public Long addGroup(Long appId, String name, String enName, Long parentId) {

        //check
        if (StringUtils.isEmpty(name)) {
            throw new BizException("名字不能为空");
        }
        if (StringUtils.isEmpty(enName)) {
            throw new BizException("英文名称不能为空");
        }
        if (Objects.isNull(appId) || appId == 0) {
            throw new BizException("应用id不能为空");
        }


        ApiModelGroup apiModelGroup = new ApiModelGroup();
        apiModelGroup.setAppId(appId);
        apiModelGroup.setEnName(enName);
        apiModelGroup.setName(name);
        apiModelGroup.setYn(1);
        apiModelGroup.setCreator(UserSessionLocal.getUser().getUserId());
        apiModelGroup.setModifier(UserSessionLocal.getUser().getUserId());
        boolean result = save(apiModelGroup);
        Long id = 0L;

        id = apiModelGroup.getId();
        log.error("apiModelGroup :{} result{}", JSON.toJSONString(apiModelGroup), result);

        MethodGroupTreeDTO methodGroupTreeDTO = findMethodGroupTree(appId);
        log.info("addGroup methodGroupTreeDTO:{},parentId:{}, id:{}", JSON.toJSONString(methodGroupTreeDTO), parentId, id);
        methodGroupTreeDTO.removeGroup(id);

        methodGroupTreeDTO.insertGroup(parentId, id);
        log.info("addGroup methodGroupTreeDTO after insertGroup :{}", JSON.toJSONString(methodGroupTreeDTO));
        modifyMethodGroupTree(methodGroupTreeDTO);
        return id;
    }

    @Override
    public boolean modifyGroupName(ModelGroupDTO modelGroupDTO) {
        //check

        //保存
        ApiModelGroup apiModelGroup = new ApiModelGroup();

        apiModelGroup.setEnName(modelGroupDTO.getEnName());
        apiModelGroup.setName(modelGroupDTO.getName());
        apiModelGroup.setId(modelGroupDTO.getGroupId());
        apiModelGroup.setModifier(UserSessionLocal.getUser().getUserId());
        boolean result = updateById(apiModelGroup);
        return result;
    }

    @Transactional
    @Override
    public boolean removeGroup(Long appId, Long groupId) {
        //check 合法性  存在子节点，不能删
        MethodGroupTreeDTO methodGroupTreeDTO = findMethodGroupTree(appId);
        boolean canRemove = checkRemoveGroupById(methodGroupTreeDTO.getTreeModel().getTreeItems(), groupId);
        if (!canRemove) {
            throw new BizException("存在子节点，不可删除");
        }
        //保存
        ApiModelGroup apiModelGroup = new ApiModelGroup();
        apiModelGroup.setId(groupId);
        apiModelGroup.setYn(DataYnEnum.INVALID.getCode());
        apiModelGroup.setModifier(UserSessionLocal.getUser().getUserId());
        boolean result = updateById(apiModelGroup);
        methodGroupTreeDTO.removeGroup(groupId);
        modifyMethodGroupTree(methodGroupTreeDTO);
        log.info("removeGroup methodGroupTreeDTO after groupId:{}, insertGroup :{}", groupId, JSON.toJSONString(methodGroupTreeDTO));

        return result;
    }

    @Override
    public MethodGroupTreeDTO findMethodGroupTree(Long appId) {
        return findMethodGroupTree(appId, null,false);
    }

    @Override
    public MethodGroupTreeDTO findMethodGroupTree(Long appId, String modelName,Boolean compactPackage) {
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
        dto.setInterfaceId(appId);

        dto.setTreeModel(new MethodGroupTreeModel());


        LambdaQueryWrapper<ApiModelTree> lqw = new LambdaQueryWrapper();
        lqw.eq(BaseEntity::getYn, 1).eq(ApiModelTree::getAppId, appId);

        List<ApiModelTree> apiModelTreeList = apiModelGroupList.list(lqw);
        if (CollectionUtils.isEmpty(apiModelTreeList)) {
            return dto;
        }
        dto.getTreeModel().setTreeItems(apiModelTreeList.get(0).getTreeModel().getTreeItems());
        dto.setInterfaceId(appId);
        dto.getTreeModel().setAppId(appId);
        //渲染所有文件夹名称
        List<GroupSortModel> groupSortModelList = dto.getTreeModel().allGroups();
        if (CollectionUtils.isNotEmpty(groupSortModelList)) {
            List<Long> groupIds = groupSortModelList.stream().map(TreeSortModel::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ApiModelGroup> lqwGroup = new LambdaQueryWrapper();
            lqwGroup.in(ApiModelGroup::getId, groupIds);
            lqwGroup.eq(BaseEntity::getYn, 1).eq(ApiModelGroup::getAppId, appId);

            List<ApiModelGroup> apiModelGroupList = list(lqwGroup);
            Map<Long, ApiModelGroup> apiModelGroupMap = apiModelGroupList.stream().collect(Collectors.toMap(ApiModelGroup::getId, apiModelGroup -> apiModelGroup));
            for (GroupSortModel groupSortModel : groupSortModelList) {
                if (apiModelGroupMap.containsKey(groupSortModel.getId())) {
                    ApiModelGroup apiModelGroup = apiModelGroupMap.get(groupSortModel.getId());
                    groupSortModel.setName(apiModelGroup.getName());
                    groupSortModel.setEnName(apiModelGroup.getEnName());
                }
            }
        }
        //渲染所有应用名称
        List<MethodSortModel> methodSortModelList = dto.getTreeModel().allMethods();
        if (CollectionUtils.isNotEmpty(methodSortModelList)) {
            List<Long> modelIds = methodSortModelList.stream().map(TreeSortModel::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ApiModel> lqwModel = new LambdaQueryWrapper();
            lqwModel.in(ApiModel::getId, modelIds).eq(BaseEntity::getYn, 1).eq(ApiModel::getAppId, appId);
            lqwModel.select(ApiModel::getId, ApiModel::getName, ApiModel::getDesc, ApiModel::getAppId);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(modelName)) {
                lqwModel.like(ApiModel::getName, modelName);
            }
            List<ApiModel> apiModelList = apiModelService.list(lqwModel);
            if (CollectionUtils.isNotEmpty(apiModelList)) {
                Map<Long, ApiModel> apiModelMap = apiModelList.stream().collect(Collectors.toMap(ApiModel::getId, apiModel -> apiModel));
                for (MethodSortModel methodSortModel : methodSortModelList) {
                    if (apiModelMap.containsKey(methodSortModel.getId())) {
                        ApiModel apiModel = apiModelMap.get(methodSortModel.getId());
                        methodSortModel.setName(apiModel.getName());
                    } else {
                        //没有查到model的节点，就移除
                        GroupSortModel parent = dto.getTreeModel().findMethodParent(methodSortModel.getId());
                        if (Objects.isNull(parent)) {
                            dto.getTreeModel().removeMethod(methodSortModel.getId(), null);
                        } else {
                            dto.getTreeModel().removeMethod(methodSortModel.getId(), parent.getId());
                        }
                    }
                }

            }
        }
        dto.getTreeModel().removeDuplicated();
        //模糊搜索的时候，去掉没有子节点的目录
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(modelName)) {
            dto.getTreeModel().removeEmptyGroup(dto.getTreeModel().getTreeItems());
        }

        if (null!=compactPackage&&compactPackage) {
            List<TreeSortModel> rest = new ArrayList<TreeSortModel>();
            dto.getTreeModel().compactGroup(dto.getTreeModel().getTreeItems());
        }


        return dto;
    }

    @Override
    public Boolean modifyMethodGroupTree(MethodGroupTreeDTO dto) {
        //校验
        LambdaQueryWrapper<ApiModelTree> lqw = new LambdaQueryWrapper();
        lqw.eq(BaseEntity::getYn, 1).eq(ApiModelTree::getAppId, dto.getInterfaceId());
        List<ApiModelTree> apiModelTreeList = apiModelGroupList.list(lqw);
        ApiModelTree apiModelTreeOld = null;
        if (CollectionUtils.isNotEmpty(apiModelTreeList)) {
            apiModelTreeOld = apiModelTreeList.get(0);
        }


        ApiModelTree update = new ApiModelTree();
        update.setAppId(dto.getInterfaceId());
        update.setGroupLastVersion(DateUtil.getCurrentDateMillTime());
        update.setTreeModel(dto.getTreeModel());
        update.setModified(new Date());
        update.setModifier(UserSessionLocal.getUser().getUserId());
        update.setYn(1);
        if (Objects.isNull(apiModelTreeOld)) {
            return apiModelGroupList.save(update);
        } else {
            update.setId(apiModelTreeOld.getId());
            return apiModelGroupList.updateById(update);
        }


    }

    @Override
    public List<ApiModelGroup> getGroupsByAppId(Long appId) {
        LambdaQueryWrapper<ApiModelGroup> lqw = new LambdaQueryWrapper();
        lqw.eq(BaseEntity::getYn, 1).eq(ApiModelGroup::getAppId, appId);
        List<ApiModelGroup> apiModelGroupList = list(lqw);
        return apiModelGroupList;
    }

    @Override
    public MethodGroupTreeDTO findRequireModelChildTree(Long requirementId, String modelName) {
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
        dto.setTreeModel(new MethodGroupTreeModel());
        dto.getTreeModel().setTreeItems(Lists.newArrayList());
        RequirementInfo requirementInfo = requirementInfoService.getById(requirementId);
        if (Objects.isNull(requirementInfo)) {
            return dto;
        }
        List<AppInfoDTO> appInfoDTOList = requirementInterfaceGroupService.getRequirementInterfaceId(requirementId);
        if (CollectionUtils.isEmpty(appInfoDTOList)) {
            log.info("findRequireModelChildTree requirementId:{} 查不到appInfo", requirementId);
            return dto;
        }
        if (requirementInfo.getStatus() == 2) {
            LambdaQueryWrapper<RequirementApiModelTreeSnapshot> lqw = new LambdaQueryWrapper();
            lqw.eq(BaseEntity::getYn, 1).eq(RequirementApiModelTreeSnapshot::getRequirementId, requirementId);
            List<RequirementApiModelTreeSnapshot> requirementApiModelTreeSnapshotList = requirementApiModelTreeSnapshotService.list(lqw);
            for (RequirementApiModelTreeSnapshot requirementApiModelTreeSnapshot : requirementApiModelTreeSnapshotList) {

                AppInfo appInfo = appInfoService.getById(requirementApiModelTreeSnapshot.getAppId());
                GroupSortModel groupSortModel = new GroupSortModel();

                groupSortModel.setEnName(appInfo.getAppCode());
                groupSortModel.setName(appInfo.getAppName());
                groupSortModel.setId(null);
                groupSortModel.setAppId(appInfo.getId());
                groupSortModel.setKey(appInfo.getAppName() + appInfo.getId());
                groupSortModel.setType("4");
                groupSortModel.setChildren(requirementApiModelTreeSnapshot.getTreeModel().getTreeItems());
                dto.getTreeModel().getTreeItems().add(groupSortModel);
            }

        } else {
            for (AppInfoDTO appInfoDTO : appInfoDTOList) {

                MethodGroupTreeDTO methodGroupTreeDTO = findMethodGroupTree(appInfoDTO.getId(), modelName,false);
                GroupSortModel groupSortModel = new GroupSortModel();
                groupSortModel.setEnName(appInfoDTO.getAppCode());
                groupSortModel.setName(appInfoDTO.getAppName());
                groupSortModel.setId(null);
                groupSortModel.setAppId(appInfoDTO.getId());
                groupSortModel.setType("4");
                groupSortModel.setKey(appInfoDTO.getAppName() + appInfoDTO.getId());
                groupSortModel.setChildren(methodGroupTreeDTO.getTreeModel().getTreeItems());


                dto.getTreeModel().getTreeItems().add(groupSortModel);
            }

        }
        return dto;
    }

    @Transactional
    @Override
    public boolean saveRequireModelSnapshot(Long requirementId) {
        List<AppInfoDTO> appInfoDTOList = requirementInterfaceGroupService.getRequirementInterfaceId(requirementId);
        if (CollectionUtils.isEmpty(appInfoDTOList)) {
            log.info("saveRequireModelSnapshot requirementId:{} 查不到appInfo", requirementId);
            return false;
        }
        for (AppInfoDTO appInfoDTO : appInfoDTOList) {

            LambdaQueryWrapper<ApiModelTree> lqw = new LambdaQueryWrapper();
            lqw.eq(BaseEntity::getYn, 1).eq(ApiModelTree::getAppId, appInfoDTO.getId());

            List<ApiModelTree> apiModelTreeList = apiModelGroupList.list(lqw);
            if (CollectionUtils.isEmpty(apiModelTreeList)) {
                continue;
            }

            MethodGroupTreeDTO methodGroupTreeDTO = findMethodGroupTree(appInfoDTO.getId());
            RequirementApiModelTreeSnapshot requirementApiModelTreeSnapshot = new RequirementApiModelTreeSnapshot();
            BeanUtils.copyProperties(apiModelTreeList.get(0), requirementApiModelTreeSnapshot);

            requirementApiModelTreeSnapshot.setTreeModel(methodGroupTreeDTO.getTreeModel());
            requirementApiModelTreeSnapshot.setRequirementId(requirementId.toString());
            //保存目录结构
            requirementApiModelTreeSnapshotService.saveOrUpdate(requirementApiModelTreeSnapshot);

            List<Long> modelIdList = methodGroupTreeDTO.getTreeModel().allMethods().stream().map(TreeSortModel::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(modelIdList)) {
                continue;
            }
            List<RequirementAppModelSnapshot> requirementAppModelSnapshotList = Lists.newArrayList();
            for (Long modelId : modelIdList) {
                ApiModelDTO apiModelDTO = apiModelService.getModelById(modelId, null);
                RequirementAppModelSnapshot requirementAppModelSnapshot = new RequirementAppModelSnapshot();
                BeanUtils.copyProperties(apiModelDTO, requirementAppModelSnapshot);
                requirementAppModelSnapshot.setRequirementId(requirementId.toString());
                requirementAppModelSnapshot.setId(null);
                requirementAppModelSnapshot.setModelId(apiModelDTO.getId());
                requirementAppModelSnapshot.setYn(1);
                requirementAppModelSnapshotList.add(requirementAppModelSnapshot);
            }
            if (CollectionUtils.isNotEmpty(requirementAppModelSnapshotList)) {
                //保存模型数据
                requirementAppModelSnapshotService.saveOrUpdateBatch(requirementAppModelSnapshotList);
            }


        }


        return true;
    }

    @Override
    public Page<QueryModelsResponseDTO> findRequireModelPage(RequireModelPageQuery requireModelPageQuery) {

        Page<QueryModelsResponseDTO> page = new Page<>();
        page.setRecords(null);
        page.setTotal(0);
        RequirementInfo requirementInfo = requirementInfoService.getById(requireModelPageQuery.getRequirementId());
        if (Objects.isNull(requirementInfo)) {
            return page;
        }

        List<Long> appIdList = Lists.newArrayList();
        List<Long> modelIdList = null;
        if (requireModelPageQuery.getAppId() != null && requireModelPageQuery.getAppId() > 0) {
            appIdList.add(requireModelPageQuery.getAppId());
        }


        if (requireModelPageQuery.getGroupId() != null && requireModelPageQuery.getGroupId() > 0) {
            MethodGroupTreeDTO methodGroupTreeDTO = findRequireModelChildTree(requireModelPageQuery.getRequirementId(), null);
            GroupSortModel groupSortModel = methodGroupTreeDTO.getTreeModel().findGroup(requireModelPageQuery.getGroupId());
            modelIdList = groupSortModel.allChildMethods().stream().map(TreeSortModel::getId).collect(Collectors.toList());
        }


        //
        if (requirementInfo.getStatus() == 2) {
            Page<RequirementAppModelSnapshot> pvEntity = new Page<>(requireModelPageQuery.getCurrent(), requireModelPageQuery.getSize());
            LambdaQueryWrapper<RequirementAppModelSnapshot> lqw = new LambdaQueryWrapper();
            lqw.eq(RequirementAppModelSnapshot::getRequirementId, requireModelPageQuery.getRequirementId());
//                    .eq(BaseEntity::getYn, 1);
            if (CollectionUtils.isNotEmpty(appIdList)) {
                lqw.in(RequirementAppModelSnapshot::getAppId, appIdList);
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(requireModelPageQuery.getModelName())) {
                lqw.eq(RequirementAppModelSnapshot::getName, requireModelPageQuery.getModelName());
            }
            if (CollectionUtils.isNotEmpty(modelIdList)) {
                lqw.in(RequirementAppModelSnapshot::getModelId, modelIdList);
            }
            log.info("findRequireModelPage lqw:{}", lqw.getTargetSql());
            Page<RequirementAppModelSnapshot> requirementAppModelSnapshotPage = requirementAppModelSnapshotService.page(pvEntity, lqw);
            if (CollectionUtils.isNotEmpty(requirementAppModelSnapshotPage.getRecords())) {
                List<QueryModelsResponseDTO> queryModelsResponseDTOList = Lists.newArrayList();
                for (RequirementAppModelSnapshot record : requirementAppModelSnapshotPage.getRecords()) {
                    QueryModelsResponseDTO queryModelsResponseDTO = new QueryModelsResponseDTO();
                    BeanUtils.copyProperties(record, queryModelsResponseDTO);
                    queryModelsResponseDTO.setId(record.getModelId());
                    queryModelsResponseDTOList.add(queryModelsResponseDTO);
                }
                page.setRecords(queryModelsResponseDTOList);
                page.setTotal(requirementAppModelSnapshotPage.getTotal());
            }
        } else {
            if (CollectionUtils.isEmpty(appIdList)) {
                List<AppInfoDTO> appInfoDTOList = requirementInterfaceGroupService.getRequirementInterfaceId(requireModelPageQuery.getRequirementId());
                if (CollectionUtils.isNotEmpty(appInfoDTOList)) {
                    appIdList = appInfoDTOList.stream().map(AppInfoDTO::getId).collect(Collectors.toList());
                }
            }
            Page<ApiModel> pvEntity = new Page<>(requireModelPageQuery.getCurrent(), requireModelPageQuery.getSize());
            LambdaQueryWrapper<ApiModel> lqw = new LambdaQueryWrapper();
            lqw.eq(BaseEntity::getYn, 1);
            if (CollectionUtils.isNotEmpty(appIdList)) {
                lqw.in(ApiModel::getAppId, appIdList);
            }
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(requireModelPageQuery.getModelName())) {
                lqw.eq(ApiModel::getName, requireModelPageQuery.getModelName());
            }
            if (CollectionUtils.isNotEmpty(modelIdList)) {
                lqw.in(ApiModel::getId, modelIdList);
            }
            Page<ApiModel> apiModelPage = apiModelService.page(pvEntity, lqw);
            if (CollectionUtils.isNotEmpty(apiModelPage.getRecords())) {
                List<QueryModelsResponseDTO> queryModelsResponseDTOList = Lists.newArrayList();
                for (ApiModel record : apiModelPage.getRecords()) {
                    QueryModelsResponseDTO queryModelsResponseDTO = new QueryModelsResponseDTO();
                    BeanUtils.copyProperties(record, queryModelsResponseDTO);
                    queryModelsResponseDTOList.add(queryModelsResponseDTO);
                }
                page.setRecords(queryModelsResponseDTOList);
                page.setTotal(apiModelPage.getTotal());
            }

        }


        return page;
    }

    private boolean checkRemoveGroupById(List<TreeSortModel> treeModels, Long groupId) {
        if (CollectionUtils.isEmpty(treeModels)) {
            return true;
        }
        for (TreeSortModel treeModel : treeModels) {
            if (!(treeModel instanceof GroupSortModel)) {
                continue;
            }
            GroupSortModel group = (GroupSortModel) treeModel;
            if (group.getId().equals(groupId)) {

                //子分组信息
                if (CollectionUtils.isNotEmpty(group.childGroups())) {
                    return false;
                }
                return true;
            } else {
                return checkRemoveGroupById(group.getChildren(), groupId);
            }
        }
        return true;
    }
}
