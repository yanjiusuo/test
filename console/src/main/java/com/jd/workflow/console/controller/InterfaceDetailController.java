package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.FindMethodInterfaceParam;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.group.GroupAddDto;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.dto.manage.InterfaceOrMethod;
import com.jd.workflow.console.dto.manage.InterfaceOrMethodSearchDto;
import com.jd.workflow.console.dto.manage.JsfInterfaceAndMethod;
import com.jd.workflow.console.dto.manage.MethodRelatedDto;
import com.jd.workflow.console.dto.requirement.FlowInstanceVo;
import com.jd.workflow.console.dto.requirement.MethodRemoveDto;
import com.jd.workflow.console.dto.test.CaseEntity;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.MethodDocDto;
import com.jd.workflow.console.service.doc.importer.JapiDataSyncService;
import com.jd.workflow.console.service.group.ITreeModifyService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.group.impl.AppGroupManageServiceImpl;
import com.jd.workflow.console.service.group.impl.FlowStepGroupManageServiceImpl;
import com.jd.workflow.console.service.group.impl.RequirementGroupServiceImpl;
import com.jd.workflow.console.service.group.service.IGroupManageService;
import com.jd.workflow.console.service.manage.TopSupportInfoService;
import com.jd.workflow.console.service.requirement.*;
import com.jd.workflow.console.service.test.RequirementWorkflowService;
import com.jd.workflow.console.service.test.TestRequirementInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口详情页相关的接口
 * @menu 接口详情相关接口
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/interfaceDetail")
@UmpMonitor
public class InterfaceDetailController {
    @Autowired
    AppGroupManageServiceImpl appGroupManageService;
    @Autowired
    FlowStepGroupManageServiceImpl flowStepGroupManageService;
    @Autowired
    RequirementGroupServiceImpl requirementGroupService;
    @Autowired
    DocExportController docExportController;
    @Autowired
    TestRequirementInfoService testRequirementInfoService;

    @Autowired
    TopSupportInfoService topSupportInfoService;

    @Autowired
    RequirementInterfaceGroupService requirementInterfaceGroupService;

    @Autowired
    JapiDataSyncService japiDataSyncService;
    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
    RequirementWorkflowService requirementWorkflowService;

    private IGroupManageService service(GroupResolveDto dto){
        if(GroupTypeEnum.APP.getCode().equals(dto.getType())){
            return appGroupManageService;
        }else if(GroupTypeEnum.PRD.getCode().equals(dto.getType())){
            return requirementGroupService;
        }else {
            return flowStepGroupManageService;
        }
    }
    private IGroupManageService prdOrFlowStepNew(GroupResolveDto dto){
        if(GroupTypeEnum.PRD.getCode().equals(dto.getType())){
            return requirementGroupService;
        }else if(GroupTypeEnum.STEP.getCode().equals(dto.getType())){
            return flowStepGroupManageService;
        }else{
            return appGroupManageService;
//            throw new BizException("应用不支持追加接口");
        }
    }


    private ITreeModifyService prdOrFlowStep(GroupResolveDto dto) {
        if (GroupTypeEnum.PRD.getCode().equals(dto.getType())) {
            return requirementGroupService;
        } else if (GroupTypeEnum.STEP.getCode().equals(dto.getType())) {
            return flowStepGroupManageService;
        } else {
            throw new BizException("应用不支持追加接口");
        }
    }

    /**
     * @param dto           分组类型，每个字段都需要传
     * @param interfaceType 接口类型 1-jsf 2-http 3-jsf
     * @return
     * @title 获取接口列表
     * @description 根据接口描述获取接口信息，该节点是根节点,层级是接口
     */
    @RequestMapping("/getRootGroups")
    public CommonResult<List<InterfaceTopCountModel>> getRootGroups(GroupResolveDto dto, int interfaceType) {
        if (InterfaceTypeEnum.BEAN.equals(interfaceType) && !GroupTypeEnum.APP.getCode().equals(dto.getType())) {
            throw new BizException("只有应用才能查看bean接口,需求不可以");
        }
        final List<InterfaceCountModel> countModels = service(dto).getRootGroups(dto, interfaceType);
        List<InterfaceTopCountModel> topCountModels = new ArrayList<>();
        List<Long> topIds = new ArrayList<>();
        if (dto.getId() == null || dto.getId().equals(0L)) {
            topIds = topSupportInfoService.getTopIds(countModels.stream().map(item -> item.getId()).collect(Collectors.toList()), dto.getType());
            sortByTopIds(countModels, topIds);
        }
        final List<Long> finalTopIds = topIds;
        countModels.stream().map(countModel -> {
            InterfaceTopCountModel topCountModel = new InterfaceTopCountModel();
            BeanUtils.copyProperties(countModel, topCountModel);
            topCountModel.setTopped(finalTopIds.contains(countModel.getId()));
            topCountModels.add(topCountModel);
            return topCountModel;
        }).collect(Collectors.toList());
        for (InterfaceTopCountModel topCountModel : topCountModels) {
            topCountModel.initKey();
        }
        return CommonResult.buildSuccessResult(topCountModels);
    }

    private void sortByTopIds(List<InterfaceCountModel> result, List<Long> topIds) {
        result.sort((o1, o2) -> {
            if (topIds.contains(o1.getId()) && topIds.contains(o2.getId())) {
                return topIds.indexOf(o1.getId()) - topIds.indexOf(o2.getId());
            } else if (topIds.contains(o1.getId())) {
                return -1;
            } else if (topIds.contains(o2.getId())) {
                return 1;
            } else {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    /**
     * 添加接口分组
     *
     * @param dto
     * @param groupDto
     * @return 添加后的接口id
     */
    @RequestMapping("/addGroup")
    public CommonResult<Long> addGroup(GroupResolveDto dto, @RequestBody GroupAddDto groupDto) {
        return CommonResult.buildSuccessResult(service(dto).addGroup(dto, groupDto));
    }

    /**
     * 修改分组名称
     *
     * @param dto
     * @param groupId 分组id
     * @param name    分组中文名
     * @param enName  分组英文名
     * @return
     */
    @RequestMapping("/modifyGroupName")
    public CommonResult<Boolean> modifyGroupName(GroupResolveDto dto, Long groupId, String name, String enName) {
        Guard.notEmpty(name, "name不可为空");
        Guard.notEmpty(groupId, "groupId不可为空");
        return CommonResult.buildSuccessResult(service(dto).modifyGroupName(dto, groupId, name, enName));

    }

    /**
     * 移除分组或者方法
     *
     * @param dto
     * @param currentNodeId   当前节点id
     * @param currentNodeType 当前id类型：1-接口 2-文件夹 3-接口分组
     * @param rootId          根节点id, currentNodeType=1或者2的时候必传
     * @param parentId        父节点id， urrentNodeType=1或者2的时候必传，可为空
     * @return 是否移除成功：true-成功，false-失败
     */
    @RequestMapping("/removeNode")
    public CommonResult<Boolean> removeNode(GroupResolveDto dto, Long currentNodeId, Long currentNodeType, Long rootId, Long parentId) {
        Guard.notEmpty(currentNodeId, "currentNodeId不可为空");
        Guard.notEmpty(currentNodeType, "currentNodeType不可为空");
        if (TreeSortModel.TYPE_METHOD.equals(currentNodeType) || TreeSortModel.TYPE_GROUP.equals(currentNodeType)) {
            if (rootId == null) {
                throw new BizException("rootId不可为空");
            }
        }
        service(dto).removeNode(dto, currentNodeId, currentNodeType, rootId, parentId);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 批量删除方法
     *
     * @param dto
     * @param ids 要移除的方法id列表
     * @return
     */
    @RequestMapping("/removeMethodByIds")
    public CommonResult<Boolean> removeMethodByIds(GroupResolveDto dto, @RequestBody List<MethodRemoveDto> ids) {
        boolean result = service(dto).removeMethodByIds(dto, ids);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 删除java bean接口或者jsf分组列表
     *
     * @param dto
     * @param ids 要移除的记录id列表
     * @return
     */
    @RequestMapping("/removeInterfaceByIds")
    public CommonResult<Boolean> removeInterfaceByIds(GroupResolveDto dto, @RequestBody List<Long> ids) {
        boolean result = service(dto).removeInterfaceByIds(dto, ids);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 添加文档
     *
     * @param dto
     * @param interfaceId 接口id
     * @param parentId    父节点id
     * @param body        请求体
     * @return
     */
    @RequestMapping("/addDoc")
    public CommonResult<Long> addDoc(GroupResolveDto dto, Long interfaceId, Long parentId, @RequestBody MethodDocDto body) {
        Long id = prdOrFlowStepNew(dto).addDoc(dto, interfaceId, parentId, body);
        return CommonResult.buildSuccessResult(id);
    }

    /**
     * @param dto
     * @param interfaceId 接口id
     * @return
     * @title 加载接口的子节点
     */
    @RequestMapping("/findInterfaceChildTree")
    public CommonResult<MethodGroupTreeDTO> findInterfaceChildTree(GroupResolveDto dto, Long interfaceId) {
        Guard.notEmpty(interfaceId, "interfaceId不可为空");
        Guard.notEmpty(dto.getId(), "需求或应用id不可为空");
        final MethodGroupTreeDTO tree = service(dto).findMethodGroupTree(dto, interfaceId);
        tree.setInterfaceId(interfaceId);
        tree.getTreeModel().initKeys();
        return CommonResult.buildSuccessResult(tree);
    }

    /**
     * 分组树排序
     *
     * @param dto
     * @param groupTreeDto 要排序的树节点信息
     * @return
     */
    @RequestMapping("/modifyMethodGroupTree")
    public CommonResult<Boolean> modifyMethodGroupTree(GroupResolveDto dto, @RequestBody MethodGroupTreeDTO groupTreeDto) {
        return CommonResult.buildSuccessResult(service(dto).modifyMethodGroupTree(dto, groupTreeDto));
    }

    /**
     * 分组树检索
     *
     * @param dto
     * @return
     * @description 检索结果会有2级，第一级是接口层级，第二级是分组或者方法层级
     */
    @RequestMapping(value = "/searchTree", method = RequestMethod.POST)
    public CommonResult<List<TreeSortModel>> searchTree(@RequestBody InterfaceOrMethodSearchDto dto) {
        final List<TreeSortModel> treeSortModels = service(dto).searchTree(dto, dto.getInterfaceType(), dto.getSearch());
        MethodGroupTreeModel model = new MethodGroupTreeModel();
        model.setTreeItems(treeSortModels);
        model.initKeys();
        return CommonResult.buildSuccessResult(treeSortModels);
    }

    /**
     * 检索列表下的方法
     *
     * @param dto
     * @param interfaceId 接口id或者java bean分组id
     * @param groupId     分组id
     * @param search      搜索条件
     * @param current     当前页码
     * @param size        每页的大小
     * @return
     */
    @RequestMapping("/pageListGroupMethods")
    public CommonResult<IPage<MethodManage>> pageListGroupMethods(GroupResolveDto dto, Long interfaceId, Integer status, Long groupId, String search, Long current, Long size) {
        if (current == null) {
            current = 1L;
        }
        Guard.notEmpty(size, "size不可为空");
        IPage<MethodManage> groupMethods = service(dto).findGroupMethods(dto, interfaceId, status, groupId, search, current, size);
        for (MethodManage record : groupMethods.getRecords()) {
            record.initKey();
        }
        return CommonResult.buildSuccessResult(groupMethods);
    }

    @RequestMapping("/pageListInterface")
    public CommonResult<IPage<InterfaceOrMethod>> pageListInterface(InterfaceOrMethodSearchDto dto) {
        if (dto.getCurrent() == null) {
            dto.setCurrent(1L);
        }
        Guard.notEmpty(dto.getInterfaceType(), "interfaceType不可为空");
        Guard.notEmpty(dto.getSize(), "size不可为空");
        IPage<InterfaceOrMethod> result = service(dto).findGroupInterface(dto);
        for (InterfaceOrMethod record : result.getRecords()) {
            record.initKey();
        }
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 追加分组模型
     *
     * @param dto
     * @param models  要追加的数据，根节点必须为树节点
     * @param groupId 分组id，如果当前节点选中了分组的话，必填，否则不传。 groupId节点的type必须为2
     * @return
     */
    @RequestMapping("/appendGroupTreeModel")
    public CommonResult<Boolean> appendGroupTreeModel(GroupResolveDto dto, @RequestParam(required = false) Long groupId, @RequestBody List<InterfaceSortModel> models) {
        prdOrFlowStep(dto).appendGroupTreeModel(dto, groupId, models);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 查询jsf接口以及方法
     *
     * @param interfaceAndMethods
     * @return
     */
    @RequestMapping("/queryJsfInterfaceAndMethod")
    public CommonResult<List<InterfaceSortModel>> queryJsfInterfaceAndMethod(@RequestBody List<JsfInterfaceAndMethod> interfaceAndMethods) {
        List<InterfaceSortModel> interfaceSortModels = requirementInfoService.queryJsfInterfaces(interfaceAndMethods);
        return CommonResult.buildSuccessResult(interfaceSortModels);
    }


    /**
     * 导出sdk代码
     *
     * @param sdkType    sdk类型,目前固定为typescript
     * @param sortModels 要导出的代码
     * @param response
     * @param request
     */
    @RequestMapping("/exportSdk")
    public void exportSdk(String sdkType, @RequestBody(required = false) List<TreeSortModel> sortModels, HttpServletResponse response, HttpServletRequest request) {
        docExportController.exportSdk(sdkType, sortModels, response, request);
    }

    /**
     * @param docType    文档类型：md、html、pdf
     * @param dto        根节点分组id
     * @param sortModels 选中的接口列表
     * @param response
     * @param request
     */
    @RequestMapping("/exportDoc")
    public void exportDoc(String docType, GroupResolveDto dto, @RequestBody(required = false) List<InterfaceSortModel> sortModels, HttpServletResponse response, HttpServletRequest request) {
        if ("md".equals(docType)) {
            docExportController.exportMd(dto, sortModels, response, request);
        } else if ("html".equals(docType)) {
            docExportController.exportHtml(dto, sortModels, response, request);
        } else if ("pdf".equals(docType)) {
            docExportController.exportPdf(dto, sortModels, response, request);
        } else {
            throw new BizException("无效的文档类型");
        }
    }

    /**
     * 获取关联deeptest测试信息
     *
     * @param requirementId 需求id
     * @param env           环境
     */
    @RequestMapping("/getDeeptestInfo")
    public CommonResult<CaseEntity> getDeeptestInfo(Long requirementId, String env) {
        if ("master".equals(env)) {
            env = "China";
        }
        CaseEntity moduleEntity = testRequirementInfoService.getModuleEntity(requirementId, env);
        return CommonResult.buildSuccessResult(moduleEntity);
    }

    /**
     * 获取需求的菜单信息
     *
     * @param requirementId 需求id
     */
    @RequestMapping("/getRequirementMenu")
    public CommonResult<List<String>> getRequirementMenu(Long requirementId) {
        List<String> menus = testRequirementInfoService.getRequirementMenu(requirementId);
        return CommonResult.buildSuccessResult(menus);
    }

    /**
     * 获取jsf、http、java bean接口数量
     *
     * @param resolveDto
     * @return
     */
    @RequestMapping("/getInterfaceCount")
    public CommonResult<List<InterfaceTypeCount>> getInterfaceCount(GroupResolveDto resolveDto) {
        return CommonResult.buildSuccessResult(service(resolveDto).getInterfaceCount(resolveDto));
    }

    /**
     * 查询需求列表
     *
     * @param name    名称搜索条件
     * @param current 当前页数
     * @param type    类型：1-需求 2-接口空间
     * @param size    分页大小
     * @return
     */
    @RequestMapping("/queryRequirementList")
    public CommonResult<IPage<FlowInstanceVo>> queryRequirementList(String name,
                                                                    @RequestParam(required = false) Integer type,
                                                                    @RequestParam(required = false) Integer current, @RequestParam(required = false) Integer size) {
        if (current == null) current = 1;
        if (size == null) size = 20;
        return CommonResult.buildSuccessResult(requirementInfoService.queryLocalRequirementList(name, type, current, size));
    }

    /**
     * 查找方法所属的分组以及方法类型
     *
     * @param findMethodInterfaceParam
     * @return
     */
    @RequestMapping("/findMethodInterfaceId")
    public CommonResult<MethodRelatedDto> findMethodInterfaceId(@RequestBody FindMethodInterfaceParam findMethodInterfaceParam) {
        return CommonResult.buildSuccessResult(appGroupManageService.findMethodRelatedParent(findMethodInterfaceParam));
    }

    @RequestMapping("/getFlowDetailById")
    public CommonResult<FlowInstanceVo> getFlowDetailById(Long requirementId) {
        return CommonResult.buildSuccessResult(requirementInfoService.flowDetail(requirementId));
    }

    /**
     * 置顶接口
     *
     * @param type 1：接口 2：需求
     * @param id   接口分组id
     * @return
     */
    @RequestMapping("/top")
    public CommonResult<Boolean> top(Integer type, Long id) {
        topSupportInfoService.top(type, id);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 取消接口置顶
     *
     * @param type 1：接口 2：需求
     * @param id   接口分组id
     * @return
     */
    @RequestMapping("/cancelTop")
    public CommonResult<Boolean> canceltop(Integer type, Long id) {
        topSupportInfoService.cancelTop(type, id);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 获取需求的应用信息
     */
    @RequestMapping("/getRequirementAppInfos")
    public CommonResult<List<AppInfoDTO>> getRequirementApps(Long requirementId) {
        return CommonResult.buildSuccessResult(requirementInterfaceGroupService.getRequirementInterfaceId(requirementId));
    }

    /**
     * 迁移j-api到本地
     *
     * @param id          type=1 应用 type=2 需求  必填
     * @param type        type=1 应用 type=2 需求  必填
     * @param interfaceId 接口分组id,位于某个接口分组下的时候必填
     * @return
     */
    @RequestMapping("/updateJapiInterface")
    public CommonResult<Boolean> updateJapiInterface(Long id, Integer type, Long interfaceId) {
        japiDataSyncService.syncJapiInterface(id, type, interfaceId, true);
        return CommonResult.buildSuccessResult(true);
    }

    @RequestMapping("/syncRequirementTree")
    public CommonResult<Boolean> syncRequirementTree(Long projectId, Long interfaceId) {
        japiDataSyncService.syncRequirementTree(projectId, interfaceId);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 判断当前用户是否是需求或者应用成员
     *
     * @param dto
     * @return 是需求或者应用成员返回true，否则返回false
     */
    @GetMapping("/isRequirementOrAppMembers")
    public CommonResult<Boolean> isRequirementOrAppMembers(GroupResolveDto dto) {

        return CommonResult.buildSuccessResult(service(dto).isMember(dto));
    }

    /**
     * @param dto           分组类型，每个字段都需要传
     * @param interfaceType 接口类型 1-jsf 2-http 3-jsf
     * @return
     * @title 获取接口列表
     * @description 根据接口描述获取接口信息，该节点是根节点,层级是接口
     */
    @GetMapping("/getRootGroupsAllData")
    public CommonResult<List<InterfaceTopCountModel>> getRootGroupsAllData(GroupResolveDto dto, int interfaceType) {
        if (InterfaceTypeEnum.BEAN.equals(interfaceType) && !GroupTypeEnum.APP.getCode().equals(dto.getType())) {
            throw new BizException("只有应用才能查看bean接口,需求不可以");
        }
        final List<InterfaceCountModel> countModels = service(dto).getRootGroups(dto, interfaceType);
        List<InterfaceTopCountModel> topCountModels = new ArrayList<>();
        List<Long> topIds = new ArrayList<>();
        if (dto.getId() == null || dto.getId().equals(0L)) {
            topIds = topSupportInfoService.getTopIds(countModels.stream().map(item -> item.getId()).collect(Collectors.toList()), dto.getType());
            sortByTopIds(countModels, topIds);
        }
        final List<Long> finalTopIds = topIds;
        countModels.stream().map(countModel -> {
            InterfaceAllChildModel topCountModel = new InterfaceAllChildModel();
            BeanUtils.copyProperties(countModel, topCountModel);
            topCountModel.setTopped(finalTopIds.contains(countModel.getId()));
            topCountModels.add(topCountModel);
            return topCountModel;
        }).collect(Collectors.toList());
        for (InterfaceTopCountModel topCountModel : topCountModels) {
            topCountModel.initKey();
        }

        for (InterfaceTopCountModel topCountModel : topCountModels) {
            MethodGroupTreeDTO tree = service(dto).findMethodGroupTree(dto, topCountModel.getId());
            tree.setInterfaceId(topCountModel.getId());
            tree.getTreeModel().initKeys();
//            topCountModel.setMethodGroupTreeDTO(tree);
            topCountModel.setChildren(tree.getTreeModel().getTreeItems());
            topCountModel.setInterfaceId(topCountModel.getId());

        }


        return CommonResult.buildSuccessResult(topCountModels);
    }
}
