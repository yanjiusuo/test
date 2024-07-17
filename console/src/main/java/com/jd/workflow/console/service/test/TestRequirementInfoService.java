package com.jd.workflow.console.service.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.cjg.flow.sdk.model.dto.submit.WorkFlowInstanceVO;
import com.jd.common.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.test.TestRequirementInfoMapper;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.dto.jsf.JsfBaseInfoDto;
import com.jd.workflow.console.dto.manage.EnvConfigItemDto;
import com.jd.workflow.console.dto.manage.JsfOrHttpDemo;
import com.jd.workflow.console.dto.requirement.MenuItem;
import com.jd.workflow.console.dto.share.CaseBatchExecuteDto;
import com.jd.workflow.console.dto.share.CaseBatchExecuteResult;
import com.jd.workflow.console.dto.test.CaseEntity;
import com.jd.workflow.console.dto.test.DeepTestBaseDto;
import com.jd.workflow.console.dto.test.SmokeExecuteResult;
import com.jd.workflow.console.dto.test.deeptest.*;
import com.jd.workflow.console.dto.test.jagile.DemandDetail;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.env.EnvConfigItem;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.entity.test.TestCasesExecuteInfo;
import com.jd.workflow.console.entity.test.TestProjectInfo;
import com.jd.workflow.console.entity.test.TestRequirementInfo;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.env.IEnvConfigItemService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.group.impl.RequirementGroupServiceImpl;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.jd.workflow.console.dto.test.FlowNodeTypeCodeEnum.FUNCTIONAL_TESTING;

@Slf4j
@Service
public class TestRequirementInfoService extends ServiceImpl<TestRequirementInfoMapper, TestRequirementInfo> {
    public static List<String> interface_def_list = Arrays.asList("interface_definition"
            , "front_development", "backend_development", "test_case_design"
            , "functional_testing", "front_backend_debugging");
    public static List<String> test_list = Arrays.asList("interface_definition", "test_case_design"
            , "functional_testing", "functional_acceptance");

    public static List<String> env_list = Arrays.asList("test", "China", "master");

    @Autowired
    JagileRemoteCaller jagileRemoteCaller;
    @Autowired
    IDeeptestRemoteCaller testRemoteCaller;
    @Autowired
    IDeeptestRemoteCaller onlineRemoteCaller;
    @Autowired
    TestProjectInfoService testProjectInfoService;

    @Autowired
    IInterfaceManageService interfaceManageService;

    @Autowired
    RequirementGroupServiceImpl requirementGroupService;
    @Autowired
    RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    TestCasesExecuteInfoService testCasesExecuteInfoService;
    @Autowired
    IMethodManageService methodManageService;


    @Autowired
    RequirementInfoService requirementInfoService;
    @Autowired
    IEnvConfigItemService envConfigItemService;

    @Autowired
    RequirementWorkflowService workflowService;

    @Autowired
    private TestCaseGroupService testCaseGroupService;


    public IDeeptestRemoteCaller remoteCaller(String env) {
        if ("test".equals(env)) {
            return testRemoteCaller;
        }
        return onlineRemoteCaller;
    }

    public SmokeExecuteResult getExecuteResult(Long flowStepId) {
        TestCasesExecuteInfo latestExecuteInfo = testCasesExecuteInfoService.getLatestExecuteInfo(flowStepId);
        SmokeExecuteResult executeResult = new SmokeExecuteResult();
        executeResult.setTotalCount(0);

        if (latestExecuteInfo == null) {
            executeResult.setExecuted(false);
            return executeResult;
        }
        List<String> caseIds = StringHelper.split(latestExecuteInfo.getCaseIds(), ",");
        executeResult.setTotalCount(caseIds.size());
        executeResult.setExecuted(true);
        TestResult<List<CaseBatchExecuteResult>> result = remoteCaller(latestExecuteInfo.getEnv()).getCaseExecuteResult(Long.valueOf(latestExecuteInfo.getTestExecuteId()));
        if (!result.isSuccess()) {
            throw new BizException("获取执行结果失败：" + result.getMsg());
        }
        if (result.getData().isEmpty()) {
            executeResult.setExecutingCount(0);
            executeResult.setFailedCount(0);
            executeResult.setSucceedCount(caseIds.size());
            return executeResult;
        }
        Map<Long, List<CaseBatchExecuteResult>> caseId2Map = result.getData().stream().collect(Collectors.groupingBy(CaseBatchExecuteResult::getCaseId));
        int failed = 0;
        for (Map.Entry<Long, List<CaseBatchExecuteResult>> entry : caseId2Map.entrySet()) {
            for (CaseBatchExecuteResult datum : entry.getValue()) {
                if (StringUtils.isNotBlank(datum.getErrorReason())) {
                    failed++;
                    break;
                }
            }
        }

        executeResult.setFailedCount(failed);
        executeResult.setSucceedCount(caseId2Map.size() - failed);
        executeResult.setExecutingCount(executeResult.getTotalCount() - caseId2Map.size());
        return executeResult;

    }

    public TestRequirementInfo getRequirement(Long requirementId, String env) {
        LambdaQueryWrapper<TestRequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TestRequirementInfo::getRequirementId, requirementId);
        lqw.eq(TestRequirementInfo::getEnv, env);
        return getOne(lqw);
    }

    public void executeSmokeCase(String env, Long requirementId, Long stepId, List<Long> caseIds) {
        TestRequirementInfo requirement = getRequirement(requirementId, env);
        Long relatedTestModuleId = requirement.getRelatedTestModuleId();
        Long suiteId = requirement.getRelatedTestCaseGroupId();
        CaseBatchExecuteDto c = new CaseBatchExecuteDto();
        c.setSuiteId(Long.valueOf(suiteId));
        c.setCaseIds(StringHelper.join(caseIds, ","));
        c.setErp(UserSessionLocal.getUser().getUserId());
        c.setSendType("1");
        c.setExecuteOrder(1);
        c.setInterrupted(0);
        c.setOnlySendError("0");
        TestResult<Long> result = remoteCaller(env).executeCaseSuite(c);
        if (!result.isSuccess()) {
            throw new BizException("执行失败：" + result.getMsg());
        }
        testCasesExecuteInfoService.newExecuteInfo(requirementId, env, stepId, caseIds, result.getData());
    }

    public List<SuiteDetail> getSmokeCase(String env, Long requirementId) {
        Long testCaseUniteId = getDeeptestModuleIdByRequirementId(requirementId, env);
        if (testCaseUniteId == null) {
            return Collections.emptyList();
        }
        TestResult<List<SuiteDetail>> result = remoteCaller(env).getCaseListByType(testCaseUniteId, 1);
        if (!result.isSuccess()) {
            throw new BizException("获取测试用例失败:" + result.getMsg());
        }

        return result.getData().stream().filter(item -> {
            return item.getStepCount() != null && item.getStepCount() > 0;
        }).collect(Collectors.toList());
    }

    public int getSmokeCaseAllCount(Long requirementId) {
        int total = 0;
        total += getSmokeCaseCount(requirementId, "test");
        total += getSmokeCaseCount(requirementId, "Chine");
        total += getSmokeCaseCount(requirementId, "online");
        return total;
    }

    private int getSmokeCaseCount(Long requirementId, String env) {
        Long testCaseUniteId = getDeeptestModuleIdByRequirementId(requirementId, "test");
        if (testCaseUniteId == null) {
            return 0;
        }
        TestResult<List<SuiteDetail>> result = remoteCaller(env).getCaseListByType(testCaseUniteId, 1);
        if (!result.isSuccess()) {
            throw new BizException("获取测试用例失败:" + result.getMsg());
        }
        return result.getData().size();
    }

    public Long getDeeptestModuleIdByRequirementId(Long requirementId, String env) {
        LambdaQueryWrapper<TestRequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TestRequirementInfo::getEnv, env);
        lqw.eq(TestRequirementInfo::getRequirementId, requirementId);
        TestRequirementInfo requirementInfo = getOne(lqw);
        if (requirementInfo == null) return null;
        return Long.valueOf(requirementInfo.getRelatedTestCaseGroupId());
    }

    /**
     * 根据需求id获取行云需求id
     *
     * @param requirementId
     * @return
     */
    private String getJagileDemandByRequirementId(Long requirementId) {
        WorkFlowInstanceVO vo = workflowService.validateRequirementId(requirementId);


        return vo.getCode();
    }

    private String getRequirementName(Long requirement) {
        return workflowService.validateRequirementId(requirement).getName();
    }

    public List<String> getRequirementMenu(Long requirementId) {
        List<String> menus = new ArrayList();
        menus.add(MenuItem.flowGraph.name());
        WorkFlowInstanceVO vo = workflowService.validateRequirementId(requirementId);
        for (String flowStepCode : vo.getNodeTypeCodeSet()) {
            if (interface_def_list.contains(flowStepCode)) {
                menus.add(MenuItem.interfaceDef.name());
            } else if (FUNCTIONAL_TESTING.getCode().equals(flowStepCode)) {
                menus.add(MenuItem.test.name());
            }
        }
        return menus;
    }

    //@Transactional
    public CaseEntity getModuleEntity(Long requirementId, String env) {
        Guard.notEmpty(requirementId, "需求id不能为空");

        RequirementInfo requirementInfo = requirementInfoService.getById(requirementId);
        if (requirementInfo.getType() == 2) {
            throw new BizException("非工作流需求暂时不支持创建deeptest模块");
        }
        Guard.notEmpty(requirementInfo, "需求不存在");
        TestRequirementInfo requirement = getRequirement(requirementId, env);

        if (requirement == null) {
            TestProjectInfo projectInfo = createDeeptestModule(requirementInfo, env);
            requirement = createCaseGroup(requirementInfo, env, projectInfo);
        }
        syncMembersToModule(env, requirement.getRelatedTestModuleId(), requirementInfo.getRelatedId());
        syncSpaceMembersToModule(env, requirement.getRelatedTestModuleId(), requirementInfo.getId());
        return new CaseEntity(requirement.getRelatedTestModuleId(), requirement.getRelatedTestCaseGroupId());
    }

    public TestRequirementInfo createCaseGroup(RequirementInfo selfRequirementInfo, String env, TestProjectInfo projectInfo) {
        CaseGroupCreateDto dto = new CaseGroupCreateDto();
        dto.setLevel(1);
        dto.setOwner(UserSessionLocal.getUser().getUserId());
        dto.setBelongId(projectInfo.getRelatedTestModuleId());
        dto.setType(2);// 用例集
        dto.setName(selfRequirementInfo.getName());
        if (StringUtils.isEmpty(dto.getName())) {
            dto.setName(selfRequirementInfo.getSpaceName());
        }
        TestResult<CaseGroupCreateDto> result = remoteCaller(env).addCatalogTree(dto);
        if (result.isSuccess()) {
            TestRequirementInfo requirementInfo = new TestRequirementInfo();
            requirementInfo.setRelatedType(1);
            requirementInfo.setProjectId(projectInfo.getId());
            requirementInfo.setRequirementId(selfRequirementInfo.getId());
            requirementInfo.setEnv(env);
            requirementInfo.setRelatedTestModuleId(projectInfo.getRelatedTestModuleId());
            requirementInfo.setRelatedTestCaseGroupId(result.getData().getId());
            save(requirementInfo);
            return requirementInfo;
        } else {
            throw new BizException("创建deeptest用例集失败：" + result.getMsg());
        }
    }

    public void syncMembers(Long requirementId) {
        syncMembers("test", requirementId);
        syncMembers("China", requirementId);
    }

    private void syncMembers(String env, Long requirementId) {
        TestRequirementInfo testRequirementInfo = getRequirement(requirementId, env);
        if (testRequirementInfo != null) {
            syncMembersToModule(env, testRequirementInfo.getRelatedTestModuleId(), requirementId);
        }
    }

    public void syncMembers(String erp, Long deeptestModuleId, String env) {
        TestResult<List<ModuleUserInfo>> result = remoteCaller(env).getRoleManageList(deeptestModuleId);
        if (!result.isSuccess()) {
            throw new BizException("获取deeptest成员失败:" + result.getMsg());
            //log.error("同步deeptest成员失败:"+ JsonUtils.toJSONString(result));
        }
        List<String> users = result.getData().stream().filter(userInfo -> {
            return userInfo.getScope() == 1;// 过滤出用户维度的
        }).map(user -> {
            return user.getMemberInfo().getErp();
        }).collect(Collectors.toList());

        MemberAddDto dto = new MemberAddDto();
        dto.setMemberErp(erp);
        dto.setScope(1); // 用户维度
        dto.setModuleId(deeptestModuleId);
        dto.setRoleId(2);
        TestResult<List<MemberAddDto>> result1 = remoteCaller(env).addMembers(Collections.singletonList(dto));
        if (!result1.isSuccess()) {
            throw new BizException("同步deeptest成员失败：" + result.getMsg());
        }

    }

    public void syncMembersToModule(String env, Long deeptestModuleId, Long requirementId) {
        Set<String> erps = workflowService.getPersonsOfFlow(requirementId);
        List<MemberRelation> memberRelationList = requirementInfoService.getMembers(requirementId);
        Set<String> requireErps = memberRelationList.stream().map(MemberRelation::getUserCode).collect(Collectors.toSet());
        erps.addAll(requireErps);
        {
            TestResult<List<ModuleUserInfo>> result = remoteCaller(env).getRoleManageList(deeptestModuleId);
            if (!result.isSuccess()) {
                throw new BizException("获取deeptest成员失败:" + result.getMsg());
                //log.error("同步deeptest成员失败:"+ JsonUtils.toJSONString(result));
            }
            List<String> users = result.getData().stream().filter(userInfo -> {
                return userInfo.getScope() == 1;// 过滤出用户维度的
            }).map(user -> {
                return user.getMemberInfo().getErp();
            }).collect(Collectors.toList());
            for (String user : users) {
                erps.remove(user);
            }
        }
        if (erps.isEmpty()) return;
        List<MemberAddDto> members = new ArrayList<>();

        for (String erp : erps) {
            MemberAddDto dto = new MemberAddDto();
            dto.setMemberErp(erp);
            dto.setScope(1); // 用户维度
            dto.setModuleId(deeptestModuleId);
            dto.setRoleId(2);
            members.add(dto);
        }
        TestResult<List<MemberAddDto>> result = remoteCaller(env).addMembers(members);
        if (!result.isSuccess()) {
            throw new BizException("同步deeptest成员失败：" + result.getMsg());
        }
    }

    public void syncSpaceMembersToModule(String env, Long deeptestModuleId, Long requirementId) {

        List<MemberRelation> memberRelationList = requirementInfoService.getMembers(requirementId);
        Set<String> erps = memberRelationList.stream().map(MemberRelation::getUserCode).collect(Collectors.toSet());

        {
            TestResult<List<ModuleUserInfo>> result = remoteCaller(env).getRoleManageList(deeptestModuleId);
            if (!result.isSuccess()) {
                throw new BizException("获取deeptest成员失败:" + result.getMsg());
                //log.error("同步deeptest成员失败:"+ JsonUtils.toJSONString(result));
            }
            List<String> users = result.getData().stream().filter(userInfo -> {
                return userInfo.getScope() == 1;// 过滤出用户维度的
            }).map(user -> {
                return user.getMemberInfo().getErp();
            }).collect(Collectors.toList());
            for (String user : users) {
                erps.remove(user);
            }
        }
        if (erps.isEmpty()) return;
        List<MemberAddDto> members = new ArrayList<>();

        for (String erp : erps) {
            MemberAddDto dto = new MemberAddDto();
            dto.setMemberErp(erp);
            dto.setScope(1); // 用户维度
            dto.setModuleId(deeptestModuleId);
            dto.setRoleId(2);
            members.add(dto);
        }
        TestResult<List<MemberAddDto>> result = remoteCaller(env).addMembers(members);
        if (!result.isSuccess()) {
            throw new BizException("同步deeptest成员失败：" + result.getMsg());
        }
    }

    public TestProjectInfo createDeeptestModule(RequirementInfo requirementInfo, String env) {
        if (requirementInfo.getType() == 2) {
            throw new BizException("非工作流需求暂时不支持创建deeptest模块");
        }
//        if (StringUtils.isNotEmpty(requirementInfo.getRelatedRequirementCode())) {
//            CommonResult<DemandDetail> result = jagileRemoteCaller.getDemandByCode(requirementInfo.getRelatedRequirementCode());
//            if (result.getCode().equals(200)) {
//                DemandDetail detailData = result.getData();
        TestProjectInfo project = testProjectInfoService.getProject("1", env);
        if (project == null) {
            ModuleCreateDto dto = new ModuleCreateDto();
            dto.setName("JAPI接口空间");
            dto.setNote("自动创建");
            TestResult<ModuleCreateDto> moduleCreateResult = remoteCaller(env).createModule(dto);
            if (!moduleCreateResult.isSuccess()) {
                throw new BizException("创建deeptest失败:" + moduleCreateResult.getMsg());
            }
            Long deeptestModuleId = moduleCreateResult.getData().getId();
            project = new TestProjectInfo();
            project.setRelatedProjectId("1");
            project.setRelatedProjectCode("JAPI");
            project.setRelatedProjectName("JAPI接口空间");
            project.setEnv(env);
            project.setRelatedTestModuleId(deeptestModuleId);
            testProjectInfoService.save(project);


        }
        return project;
        //        } else {
        //            throw new BizException("获取需求失败：" + result.getMessage());
        //            }
//        } else {
//            ModuleCreateDto dto = new ModuleCreateDto();
//            dto.setName(requirementInfo.getSpaceName());
//            dto.setNote("自动创建");
//            TestResult<ModuleCreateDto> moduleCreateResult = remoteCaller(env).createModule(dto);
//            if (!moduleCreateResult.isSuccess()) {
//                throw new BizException("创建deeptest失败:" + moduleCreateResult.getMsg());
//            }
//            Long deeptestModuleId = moduleCreateResult.getData().getId();
//            TestProjectInfo project = new TestProjectInfo();
//            project.setRelatedProjectId("0");
//            project.setRelatedProjectCode(requirementInfo.getName());
//            project.setRelatedProjectName(requirementInfo.getName());
//            project.setEnv(env);
//            project.setRelatedTestModuleId(deeptestModuleId);
//            testProjectInfoService.save(project);
//            return project;
//        }
    }

    public List<JsfBaseInfoDto> getJsfInterface(DeepTestBaseDto dto, String search) {
        TestRequirementInfo testRequirementInfo = getTestRequirementInfo(dto);
        if (testRequirementInfo == null) {
            log.info("getJsfInterface testRequirementInfo is null");
            return testCaseGroupService.queryJsfInterfaces(dto, search);

        }
        Long requirementId = testRequirementInfo.getRequirementId();

        List<InterfaceManage> requirementInterfaces = requirementInterfaceGroupService.getRequirementInterfaces(requirementId, InterfaceTypeEnum.JSF.getCode(), search);
        return requirementInterfaces.stream().map(vs -> {
            JsfBaseInfoDto base = new JsfBaseInfoDto();
            base.setId(vs.getId());
            base.setName(vs.getServiceCode());
            return base;
        }).collect(Collectors.toList());
    }

    public List<JsfBaseInfoDto> getJsfMethods(DeepTestBaseDto dto, Long interfaceId) {
        TestRequirementInfo testRequirementInfo = getTestRequirementInfo(dto);
        if (testRequirementInfo == null) {
            log.info("getJsfMethods testRequirementInfo is null");
            return testCaseGroupService.queryJsfMethod(dto);
        }
        Long requirementId = testRequirementInfo.getRequirementId();
        RequirementInterfaceGroup entity = requirementInterfaceGroupService.findEntity(requirementId, interfaceId);
        if (entity == null || entity.getSortGroupTree() == null || entity.getSortGroupTree().allMethods() == null) {
            return Collections.emptyList();
        }
        return entity.getSortGroupTree().allMethods().stream().map(item -> {
            JsfBaseInfoDto baseInfoDto = new JsfBaseInfoDto();
            baseInfoDto.setId(item.getId());
            baseInfoDto.setName(item.getName());
            return baseInfoDto;
        }).collect(Collectors.toList());
    }

    public List<MethodSortModel> queryHttpInterfaces(DeepTestBaseDto dto, String search) {
        TestRequirementInfo testRequirementInfo = getTestRequirementInfo(dto);
        log.info("queryHttpInterfaces testRequirementInfo:{}", JSON.toJSONString(testRequirementInfo));
        if (testRequirementInfo == null) {
            log.info("testCaseGroupService.queryHttpInterfaces start");
            return testCaseGroupService.queryHttpInterfaces(dto, search);
//            return Collections.emptyList();
        }
        GroupResolveDto resolveDto = new GroupResolveDto();
        resolveDto.setType(GroupTypeEnum.PRD.getCode());
        resolveDto.setId(testRequirementInfo.getRequirementId());
        return requirementGroupService.searchMethod(resolveDto, InterfaceTypeEnum.HTTP.getCode(), search);
    }

    public TestRequirementInfo getTestRequirementInfo(DeepTestBaseDto dto) {
        Guard.notEmpty(dto.getStation(), "station不可为空");
        Guard.notEmpty(dto.getModuleId(), "moduleId不可为空");
        Guard.notEmpty(dto.getRootSuiteId(), "rootSuiteId不可为空");
        if (!env_list.contains(dto.getStation())) {
            throw new BizException("无效的env参数");
        }
        LambdaQueryWrapper<TestRequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TestRequirementInfo::getEnv, dto.getStation());
        lqw.eq(TestRequirementInfo::getRelatedTestModuleId, dto.getModuleId());
        lqw.eq(TestRequirementInfo::getRelatedTestCaseGroupId, dto.getRootSuiteId());

        return getOne(lqw);
    }

    /**
     * 获取接口示例值
     *
     * @param methodId 方法id
     */
    public JsfOrHttpDemo queryInterfaceDemoValue(DeepTestBaseDto dto, String envConfigId, Long methodId) {
        MethodManageDTO method = methodManageService.getEntityById(methodId);
        String input = method.getDocConfig().getInputExample();
        String output = method.getDocConfig().getOutputExample();
        JsfOrHttpDemo jsfOrHttpDemo = new JsfOrHttpDemo();
        jsfOrHttpDemo.setInput(input);
        jsfOrHttpDemo.setOutput(output);
        jsfOrHttpDemo.setHttpMethod(method.getHttpMethod());
        TestRequirementInfo testRequirementInfo = getTestRequirementInfo(dto);
        InterfaceManage interfaceManage = interfaceManageService.getById(method.getInterfaceId());
        if (InterfaceTypeEnum.HTTP.getCode().equals(method.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())) {
            if (StringUtils.isNotBlank(envConfigId)) {
                // // TODO: 2023/6/13  需要调整一下
                List<EnvConfigItem> configItem = envConfigItemService.getEnvConfigItemListByName(interfaceManage.getAppId(), Objects.isNull(testRequirementInfo) ? null : testRequirementInfo.getRequirementId(), Objects.isNull(testRequirementInfo) ? dto.getStation() : testRequirementInfo.getEnv(), envConfigId);
                if (!ObjectHelper.isEmpty(configItem)) {
                    jsfOrHttpDemo.setEnvConfigItemId(configItem.get(0).getId());
                    final List<EnvConfigItemDto> dtos = configItem.stream().map(item -> {
                        EnvConfigItemDto itemDto = new EnvConfigItemDto();
                        itemDto.setId(item.getId());
                        itemDto.setUrl(item.getUrl());
                        return itemDto;
                    }).collect(Collectors.toList());
                    jsfOrHttpDemo.setEnvConfigItems(dtos);
                }
            }
          /*  jsfOrHttpDemo.setEnvConfigItemId(1L);
            List<EnvConfigItemDto> envConfigItems = new ArrayList<>();
            jsfOrHttpDemo.setEnvConfigItems(envConfigItems);
            {
                EnvConfigItemDto item = new EnvConfigItemDto();
                item.setId(1L);
                item.setUrl("http://jap-mock-data.jd.local");
                envConfigItems.add(item);
            }
            {
                EnvConfigItemDto item = new EnvConfigItemDto();
                item.setId(1L);
                item.setUrl("http://data-flow.jd.com");
                envConfigItems.add(item);
            }*/

        }


        return jsfOrHttpDemo;
    }

    public List<EnvConfigItemDto> queryInterfaceEnv(DeepTestBaseDto deepTestBaseDto, Long envConfigId) {
        TestRequirementInfo testRequirementInfo = getTestRequirementInfo(deepTestBaseDto);
        if (testRequirementInfo == null) {
            return Collections.emptyList();
        }
        List<EnvConfigItem> envList = envConfigItemService.getEnvConfigItemList(envConfigId);
        return envList.stream().map(item -> {
            EnvConfigItemDto dto = new EnvConfigItemDto();
            dto.setId(item.getId());
            dto.setUrl(item.getUrl());
            return dto;
        }).collect(Collectors.toList());

    }
}
