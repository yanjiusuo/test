package com.jd.workflow.console.service.test;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/18
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.test.TestCaseGroupMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.jsf.JsfBaseInfoDto;
import com.jd.workflow.console.dto.test.DeepTestBaseDto;
import com.jd.workflow.console.dto.test.MethodQueryDto;
import com.jd.workflow.console.dto.test.deeptest.*;
import com.jd.workflow.console.dto.test.deeptest.step.CompareRuleInfo;
import com.jd.workflow.console.dto.test.deeptest.step.CompareScript;
import com.jd.workflow.console.dto.test.deeptest.step.DataGroup;
import com.jd.workflow.console.dto.test.deeptest.step.StepCaseDetail;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.test.TestCaseGroup;
import com.jd.workflow.console.entity.test.TestRequirementInfo;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import com.jd.workflow.console.utils.DeltaHelper;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.net.URL;
import java.time.Instant;
import java.util.*;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/18
 */
@Slf4j
@Service
public class TestCaseGroupService extends ServiceImpl<TestCaseGroupMapper, TestCaseGroup> {

    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @Autowired
    IDeeptestRemoteCaller testRemoteCaller;
    @Autowired
    IDeeptestRemoteCaller onlineRemoteCaller;

    @Autowired
    private IMethodManageService methodManageService;

    @Autowired
    private IAppInfoService appInfoService;

    @Autowired
    private IInterfaceManageService interfaceManageService;

    @Resource
    IMethodModifyLogService methodModifyLogService;

    public IDeeptestRemoteCaller remoteCaller(String env) {
        if ("test".equals(env)) {
            return testRemoteCaller;
        }
        return onlineRemoteCaller;
    }


    /**
     * 获取
     *
     * @param env
     * @return
     */
    public TestCaseGroup getModule(String env) {

        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TestCaseGroup::getEnv, env).eq(TestCaseGroup::getModuleId, 0);
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            //创建模块
            IDeeptestRemoteCaller deeptestRemoteCaller = remoteCaller(env);
            ModuleCreateDto dto = new ModuleCreateDto();
            dto.setName("在线联调");
            dto.setNote("自动创建");
            log.info("getModule createModule:{}", JSON.toJSONString(dto));
            TestResult<ModuleCreateDto> testResult = deeptestRemoteCaller.createModule(dto);
            log.info("getModule createModule:{}，testResult:{}", JSON.toJSONString(dto), JSON.toJSONString(testResult));
            if (!testResult.isSuccess()) {
                throw new BizException("创建deeptest失败:" + testResult.getMsg());
            }
            Long deeptestModuleId = testResult.getData().getId();
            TestCaseGroup testCaseGroup = new TestCaseGroup();
            testCaseGroup.setEnv(env);
            testCaseGroup.setModuleId(0L);
            testCaseGroup.setRelatedTestModuleId(deeptestModuleId);
            testCaseGroup.setRelatedType(0);
            testCaseGroup.setParentId(0L);
            testCaseGroup.setRelatedId(0L);
            testCaseGroup.setRelatedTestCaseGroupId(0L);
            save(testCaseGroup);
            return testCaseGroup;


        }
        return testCaseGroupList.get(0);
    }

    public TestCaseGroup getApp(TestCaseGroup module, Long appId) {
        AppInfoDTO appInfo = appInfoService.findApp(appId);
        if (Objects.isNull(appId)) {
            throw new BizException("应用不存在：" + appId);
        }
        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TestCaseGroup::getEnv, module.getEnv()).eq(TestCaseGroup::getModuleId, module.getId()).eq(TestCaseGroup::getRelatedType, 1).eq(TestCaseGroup::getRelatedId, appId);
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            //新建测试用例集
            IDeeptestRemoteCaller deeptestRemoteCaller = remoteCaller(module.getEnv());
            CaseGroupCreateDto dto = new CaseGroupCreateDto();
            dto.setLevel(1);
            dto.setOwner(UserSessionLocal.getUser().getUserId());
            dto.setBelongId(module.getRelatedTestModuleId());
            dto.setType(2);// 用例集
            dto.setName(appInfo.getAppName());

            TestResult<CaseGroupCreateDto> result = deeptestRemoteCaller.addCatalogTree(dto);
            log.info("deeptestRemoteCaller.addCatalogTree param:{},response:{}", JSON.toJSONString(dto), JSON.toJSONString(result));
            if (result.isSuccess()) {
                TestCaseGroup testCaseGroup = new TestCaseGroup();
                testCaseGroup.setEnv(module.getEnv());
                testCaseGroup.setModuleId(module.getId());
                testCaseGroup.setRelatedTestModuleId(module.getRelatedTestModuleId());
                testCaseGroup.setRelatedType(1);
                testCaseGroup.setParentId(module.getId());
                testCaseGroup.setRelatedId(appId);
                testCaseGroup.setRelatedTestCaseGroupId(result.getData().getId());
                save(testCaseGroup);
                if (CollectionUtils.isNotEmpty(appInfo.getMember())) {
                    List<MemberAddDto> memberAddDtoList = Lists.newArrayList();
                    for (String erp : appInfo.getMember()) {
                        MemberAddDto memberAddDto = new MemberAddDto();
                        memberAddDto.setMemberErp(erp);
                        memberAddDto.setScope(1); // 用户维度
                        memberAddDto.setModuleId(module.getRelatedTestModuleId());
                        memberAddDto.setRoleId(2);
                        memberAddDtoList.add(memberAddDto);
                    }

                    log.info("getApp addMembers:{}", JSON.toJSONString(memberAddDtoList));
                    TestResult<List<MemberAddDto>> testResult = deeptestRemoteCaller.addMembers(memberAddDtoList);
                    log.info("getApp addMembers:{},result:{}", JSON.toJSONString(memberAddDtoList), JSON.toJSONString(testResult));

                }
                return testCaseGroup;
            }
            throw new BizException("创建模块失败：" + appId);
        }

        return testCaseGroupList.get(0);
    }

    public TestCaseGroup getInterface(TestCaseGroup module, Long interfaceId) {
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        if (Objects.isNull(interfaceManage)) {
            throw new BizException("不存在指定分组");
        }

        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(TestCaseGroup::getEnv, module.getEnv())
                .eq(TestCaseGroup::getModuleId, module.getId())
                .eq(TestCaseGroup::getRelatedType, 2)
                .eq(TestCaseGroup::getRelatedId, interfaceId);
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            TestCaseGroup appGroup = getApp(module, interfaceManage.getAppId());
            IDeeptestRemoteCaller deeptestRemoteCaller = remoteCaller(module.getEnv());
            CaseGroupCreateDto dto = new CaseGroupCreateDto();
            dto.setLevel(1);
            dto.setOwner(UserSessionLocal.getUser().getUserId());
            dto.setBelongId(module.getRelatedTestModuleId());
            dto.setType(2);// 用例集
            dto.setName(interfaceManage.getName());
            dto.setParentId(appGroup.getRelatedTestCaseGroupId());


            TestResult<CaseGroupCreateDto> result = deeptestRemoteCaller.addCatalogTree(dto);
            log.info("deeptestRemoteCaller.addCatalogTree param:{},response:{}", JSON.toJSONString(dto), JSON.toJSONString(result));
            if (result.isSuccess()) {
                TestCaseGroup testCaseGroup1 = new TestCaseGroup();
                testCaseGroup1.setEnv(module.getEnv());
                testCaseGroup1.setModuleId(module.getId());
                testCaseGroup1.setRelatedTestModuleId(module.getRelatedTestModuleId());
                testCaseGroup1.setRelatedType(2);
                testCaseGroup1.setParentId(appGroup.getId());
                testCaseGroup1.setRelatedId(interfaceId);
                testCaseGroup1.setRelatedTestCaseGroupId(result.getData().getId());
                save(testCaseGroup1);
                return testCaseGroup1;
            }

            throw new BizException("创建用例集失败");
        }
        return testCaseGroupList.get(0);
    }

    public TestCaseGroup getMethod(String env, Long methodId) {
        MethodManage methodManage = methodManageService.getById(methodId);
        if (Objects.isNull(methodManage)) {
            throw new BizException("不存在指定方法");
        }
        TestCaseGroup moduleGroup = getModule(env);
        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(TestCaseGroup::getEnv, moduleGroup.getEnv())
                .eq(TestCaseGroup::getModuleId, moduleGroup.getId())
                .eq(TestCaseGroup::getRelatedType, 3)
                .eq(TestCaseGroup::getRelatedId, methodId);
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        IDeeptestRemoteCaller deeptestRemoteCaller = remoteCaller(moduleGroup.getEnv());
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            //新建测试用例集
            TestCaseGroup interfaceGroup = getInterface(moduleGroup, methodManage.getInterfaceId());

            CaseGroupCreateDto dto = new CaseGroupCreateDto();
            dto.setLevel(1);
            dto.setOwner(UserSessionLocal.getUser().getUserId());
            dto.setBelongId(moduleGroup.getRelatedTestModuleId());
            dto.setType(2);// 用例集
            dto.setName(methodManage.getName());
            dto.setParentId(interfaceGroup.getRelatedTestCaseGroupId());
            TestResult<CaseGroupCreateDto> result = deeptestRemoteCaller.addCatalogTree(dto);
            log.info("deeptestRemoteCaller.addCatalogTree param:{},response:{}", JSON.toJSONString(dto), JSON.toJSONString(result));
            if (result.isSuccess()) {
                TestCaseGroup testCaseGroup1 = new TestCaseGroup();
                testCaseGroup1.setEnv(moduleGroup.getEnv());
                testCaseGroup1.setModuleId(moduleGroup.getId());
                testCaseGroup1.setRelatedTestModuleId(moduleGroup.getRelatedTestModuleId());
                testCaseGroup1.setRelatedType(3);
                testCaseGroup1.setParentId(interfaceGroup.getId());
                testCaseGroup1.setRelatedId(methodId);
                testCaseGroup1.setRelatedTestCaseGroupId(result.getData().getId());
                save(testCaseGroup1);
                return testCaseGroup1;
            }
            throw new BizException("创建用例集失败");

        }

        MemberAddDto memberAddDto = new MemberAddDto();
        memberAddDto.setModuleId(moduleGroup.getRelatedTestModuleId());
        memberAddDto.setMemberErp(UserSessionLocal.getUser().getUserId());
        memberAddDto.setRoleId(2);
        memberAddDto.setScope(1);
        memberAddDto.setDepartName("");
        TestResult<MemberAddDto> result = deeptestRemoteCaller.addMember(memberAddDto);
        log.info("deeptestRemoteCaller.addMember param:{},response:{}", JSON.toJSONString(memberAddDto), JSON.toJSONString(result));


        return testCaseGroupList.get(0);
    }

    public TestCaseGroup queryMethodTestCaseGroup(String env, Long methodId) {
        MethodManage methodManage = methodManageService.getById(methodId);
        if (Objects.isNull(methodManage)) {
            throw new BizException("不存在指定方法");
        }
        TestCaseGroup moduleGroup = getModule(env);
        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(TestCaseGroup::getEnv, moduleGroup.getEnv())
                .eq(TestCaseGroup::getModuleId, moduleGroup.getId())
                .eq(TestCaseGroup::getRelatedType, 3)
                .eq(TestCaseGroup::getRelatedId, methodId);
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            return null;
        }
        return testCaseGroupList.get(0);
    }

    public boolean queryInterfacesChanged(DeepTestBaseDto dto) {

        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TestCaseGroup::getRelatedTestModuleId, dto.getModuleId()).eq(TestCaseGroup::getRelatedTestCaseGroupId, dto.getRootSuiteId());
        lqw.eq(TestCaseGroup::getEnv, dto.getStation());
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            log.info("queryInterfacesChanged testCaseGroupList isEmpty");
            return false;
        }
        TestCaseGroup testCaseGroup = testCaseGroupList.get(0);

        MethodManageDTO methodManage = methodManageService.getEntity(testCaseGroup.getRelatedId().toString());
        if (Objects.isNull(methodManage)) {
            log.info("queryInterfacesChanged methodManage isEmpty");
            return false;
        }

        IDeeptestRemoteCaller deeptestRemoteCaller = remoteCaller(dto.getStation());
        TestResult<List<SuiteDetail>> testResult = deeptestRemoteCaller.getCaseListByType(dto.getRootSuiteId(), 1);
        log.info("deeptestRemoteCaller.getCaseListByType param:{},1;response:{}", dto.getRootSuiteId(), JSON.toJSONString(testResult));
        if (!testResult.isSuccess()) {
            log.info("queryInterfacesChanged getCaseListByType fail");
            return false;
        }
        if (CollectionUtils.isEmpty(testResult.getData())) {
            log.info("queryInterfacesChanged getCaseListByType isEmpty");
            return false;
        }


        Date updateTime = null;
        for (SuiteDetail suiteDetail : testResult.getData()) {
            try {
                Instant instant = Instant.ofEpochMilli(Long.parseLong(suiteDetail.getUpdateTime()));
                Date current = Date.from(instant);
                if (updateTime == null) {
                    updateTime = current;
                } else {
                    if (current.getTime() < updateTime.getTime()) {
                        updateTime = current;
                    }
                }

            } catch (Exception ex) {

            }
        }

        LambdaQueryWrapper<MethodModifyLog> methodModifyLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        methodModifyLogLambdaQueryWrapper.eq(BaseEntity::getYn, 1).ne(BaseEntityNoDelLogic::getCreated, updateTime).eq(MethodModifyLog::getMethodId, testCaseGroup.getRelatedId()).orderByDesc(MethodModifyLog::getId).last("limit 2");
        List<MethodModifyLog> methodModifyLogList = methodModifyLogService.list(methodModifyLogLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(methodModifyLogList)) {
            return false;
        }

        if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(methodManage.getType())) {
            HttpMethodModel after = JsonUtils.parse(methodManage.getContent(), HttpMethodModel.class);
            HttpMethodModel before = JsonUtils.parse(methodModifyLogList.get(0).getMethodContentSnapshot().getContent(), HttpMethodModel.class);
            if (Objects.nonNull(before.getInput()) && Objects.nonNull(after.getInput())) {
                JsonType jsonType = null;
                jsonType = compareJsonType(before.getInput().getParams(), after.getInput().getParams());
                if (Objects.nonNull(jsonType)) {
                    log.info("queryInterfacesChanged {} change HttpMethodModel getInput().getParams:{}", testCaseGroup.getRelatedId(), JsonUtils.toJSONString(jsonType));
                    return true;
                }
                jsonType = compareJsonType(before.getInput().getBody(), after.getInput().getBody());
                if (Objects.nonNull(jsonType)) {
                    log.info("queryInterfacesChanged {} change HttpMethodModel getInput().getBody:{}", testCaseGroup.getRelatedId(), JsonUtils.toJSONString(jsonType));
                    return true;
                }
            }
            if (Objects.nonNull(before.getOutput()) && Objects.nonNull(after.getOutput())) {
                JsonType jsonType = null;
                jsonType = compareJsonType(before.getOutput().getBody(), after.getOutput().getBody());
                if (Objects.nonNull(jsonType)) {
                    log.info("queryInterfacesChanged {} change HttpMethodModel getOutput().getBody:{}", testCaseGroup.getRelatedId(), JsonUtils.toJSONString(jsonType));
                    return true;
                }
            }

        } else if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
            JsfStepMetadata after = JsonUtils.parse(methodManage.getContent(), JsfStepMetadata.class);
            JsfStepMetadata before = JsonUtils.parse(methodModifyLogList.get(0).getMethodContentSnapshot().getContent(), JsfStepMetadata.class);
            if (Objects.nonNull(before.getInput()) && Objects.nonNull(after.getInput())) {
                JsonType jsonType = null;
                jsonType = compareJsonType(before.getInput(), after.getInput());
                if (Objects.nonNull(jsonType)) {
                    log.info("queryInterfacesChanged {} change JsfStepMetadata getInput():{}", testCaseGroup.getRelatedId(), JsonUtils.toJSONString(jsonType));
                    return true;
                }

            }
            if (Objects.nonNull(before.getOutput()) && Objects.nonNull(after.getOutput())) {
                JsonType jsonType = null;
                jsonType = DeltaHelper.deltaJsonType(before.getOutput(), after.getOutput(), false);
                if (Objects.nonNull(jsonType)) {
                    log.info("queryInterfacesChanged {} change JsfStepMetadata getOutput().getBody:{}", testCaseGroup.getRelatedId(), JsonUtils.toJSONString(jsonType));
                    return true;
                }
            }
        }


        return true;
    }

    private JsonType compareJsonType(List<? extends JsonType> before, List<? extends JsonType> after) {

        ObjectJsonType beforeObj = new ObjectJsonType();
        if (CollectionUtils.isNotEmpty(before)) {
            for (JsonType param : before) {
                beforeObj.addChild(param);
            }
        }
        ObjectJsonType afterObj = new ObjectJsonType();
        if (CollectionUtils.isNotEmpty(after)) {
            for (JsonType param : after) {
                afterObj.addChild(param);
            }
        }
        JsonType jsonType = DeltaHelper.deltaJsonType(beforeObj, afterObj, false);
        return jsonType;


    }

    public List<MethodSortModel> queryHttpInterfaces(DeepTestBaseDto dto, String search) {
        List<MethodSortModel> methodSortModelList = Lists.newArrayList();
        if ("China".equals(dto.getStation())) {
            dto.setStation("master");
        }
        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(TestCaseGroup::getEnv, dto.getStation())
                .eq(TestCaseGroup::getRelatedTestCaseGroupId, dto.getRootSuiteId())
                .eq(TestCaseGroup::getRelatedType, 3)
                .eq(TestCaseGroup::getRelatedTestModuleId, dto.getModuleId());
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            log.info("testCaseGroupService.queryHttpInterfaces testCaseGroupList isEmpty");
            return methodSortModelList;
        }
        MethodManage methodManage = methodManageService.getById(testCaseGroupList.get(0).getRelatedId());
        if (Objects.isNull(methodManage)) {
            log.info("testCaseGroupService.queryHttpInterfaces methodManage isNull");
            return methodSortModelList;
        }
        MethodSortModel methodSortModel = new MethodSortModel();
        methodSortModel.initKey();
        methodSortModel.setName(methodManage.getName());
        methodSortModel.setPath(methodManage.getPath());
        methodSortModel.setType("1");
        methodSortModel.setInterfaceId(methodManage.getInterfaceId());
        methodSortModel.setId(methodManage.getId());

        methodSortModelList.add(methodSortModel);
        return methodSortModelList;

    }

    public List<JsfBaseInfoDto> queryJsfInterfaces(DeepTestBaseDto dto, String search) {
        List<JsfBaseInfoDto> methodSortModelList = Lists.newArrayList();
        if ("China".equals(dto.getStation())) {
            dto.setStation("master");
        }
        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(TestCaseGroup::getEnv, dto.getStation())
                .eq(TestCaseGroup::getRelatedTestCaseGroupId, dto.getRootSuiteId())
                .eq(TestCaseGroup::getRelatedType, 3)
                .eq(TestCaseGroup::getRelatedTestModuleId, dto.getModuleId());
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            log.info("testCaseGroupService.queryJsfInterfaces testCaseGroupList isEmpty");
            return methodSortModelList;
        }
        MethodManage methodManage = methodManageService.getById(testCaseGroupList.get(0).getRelatedId());
        if (Objects.isNull(methodManage)) {
            log.info("testCaseGroupService.queryJsfInterfaces methodManage isNull");
            return methodSortModelList;
        }

        InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());
        if (Objects.isNull(interfaceManage)) {
            log.info("testCaseGroupService.queryJsfInterfaces interfaceManage isNull");
            return methodSortModelList;
        }


        JsfBaseInfoDto jsfBaseInfoDto = new JsfBaseInfoDto();
        jsfBaseInfoDto.setName(interfaceManage.getServiceCode());
        jsfBaseInfoDto.setId(interfaceManage.getId());
        methodSortModelList.add(jsfBaseInfoDto);
        return methodSortModelList;

    }

    public List<JsfBaseInfoDto> queryJsfMethod(DeepTestBaseDto dto) {
        List<JsfBaseInfoDto> methodSortModelList = Lists.newArrayList();
        if ("China".equals(dto.getStation())) {
            dto.setStation("master");
        }
        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(TestCaseGroup::getEnv, dto.getStation())
                .eq(TestCaseGroup::getRelatedTestCaseGroupId, dto.getRootSuiteId())
                .eq(TestCaseGroup::getRelatedType, 3)
                .eq(TestCaseGroup::getRelatedTestModuleId, dto.getModuleId());
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            log.info("testCaseGroupService.queryJsfMethod testCaseGroupList isEmpty");
            return methodSortModelList;
        }
        MethodManage methodManage = methodManageService.getById(testCaseGroupList.get(0).getRelatedId());
        if (Objects.isNull(methodManage)) {
            log.info("testCaseGroupService.queryJsfMethod methodManage isNull");
            return methodSortModelList;
        }

        JsfBaseInfoDto jsfBaseInfoDto = new JsfBaseInfoDto();
        jsfBaseInfoDto.setName(methodManage.getMethodCode());
        jsfBaseInfoDto.setId(methodManage.getId());
        methodSortModelList.add(jsfBaseInfoDto);
        return methodSortModelList;
    }


    public StepCaseDetail queryInterfaceStepCaseDetail(DeepTestBaseDto dto) {
        if ("China".equals(dto.getStation())) {
            dto.setStation("master");
        }
        LambdaQueryWrapper<TestCaseGroup> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(TestCaseGroup::getEnv, dto.getStation())
                .eq(TestCaseGroup::getRelatedTestCaseGroupId, dto.getRootSuiteId())
                .eq(TestCaseGroup::getRelatedType, 3)
                .eq(TestCaseGroup::getRelatedTestModuleId, dto.getModuleId());
        List<TestCaseGroup> testCaseGroupList = list(lqw);
        if (CollectionUtils.isEmpty(testCaseGroupList)) {
            log.info("testCaseGroupService.queryInterfaceStepCaseDetail testCaseGroupList isEmpty");
            return null;
        }
        return getStepCaseDetail(testCaseGroupList.get(0).getRelatedId().toString());
    }


    private StepCaseDetail getStepCaseDetail(String methodId) {
        MethodManageDTO methodManage = methodManageService.getEntity(methodId);
        if (Objects.isNull(methodManage)) {
            log.info("testCaseGroupService.queryHttpInterfaces methodManage isNull");
            return null;
        }
        StepCaseDetail stepCaseDetail = null;
        if (methodManage.getType() == 3) {
            //jsf接口
            stepCaseDetail = getStepCaseDetail();
            InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());
            stepCaseDetail.setName(methodManage.getName());
            stepCaseDetail.setInterfaceType("JSF");
            stepCaseDetail.setMethodName(methodManage.getMethodCode());
            stepCaseDetail.setInterfaceName(interfaceManage.getServiceCode());

            stepCaseDetail.setInputParam(methodManage.getDocConfig().getInputExample());
            stepCaseDetail.setExpectResult(methodManage.getDocConfig().getOutputExample());
            DataGroup dataGroup = stepCaseDetail.getDataGroups().get(0);
            dataGroup.setInputParam(methodManage.getDocConfig().getInputExample());
            dataGroup.setExpectResult(methodManage.getDocConfig().getOutputExample());


        } else if (methodManage.getType() == 1) {
            //http接口
            stepCaseDetail = getStepCaseDetail();
            stepCaseDetail.setName(methodManage.getName());
            stepCaseDetail.setInterfaceType("HTTP");
            stepCaseDetail.setRequestType(methodManage.getHttpMethod());
            stepCaseDetail.setInterfaceName(methodManage.getPath());
            stepCaseDetail.setExpectResult(methodManage.getDocConfig().getOutputExample());
            String inputEx = methodManage.getDocConfig().getInputExample();
            DataGroup dataGroup = stepCaseDetail.getDataGroups().get(0);
            dataGroup.setExpectResult(methodManage.getDocConfig().getOutputExample());
            JSONObject inputObj = JSON.parseObject(inputEx);
            if (inputObj.containsKey("headers")) {

                JSONArray jsonArray = new JSONArray();
                JSONObject paramsObj = inputObj.getJSONObject("headers");
                for (String key : paramsObj.keySet()) {
                    JSONObject item = new JSONObject();
                    item.put("header", key);
                    item.put("value", paramsObj.get(key));
                    jsonArray.add(item);
                }

                stepCaseDetail.setHeaderParam(jsonArray.toJSONString());


            }
            if (inputObj.containsKey("params")) {
                JSONArray jsonArray = new JSONArray();
                JSONObject paramsObj = inputObj.getJSONObject("params");
                for (String key : paramsObj.keySet()) {
                    JSONObject item = new JSONObject();
                    item.put("key", key);
                    item.put("value", paramsObj.get(key));
                    jsonArray.add(item);
                }
                stepCaseDetail.setUrlParam(jsonArray.toJSONString());
                dataGroup.setUrlParam(jsonArray.toJSONString());


            }
            if (inputObj.containsKey("path")) {

            }
            if (inputObj.containsKey("body")) {
                stepCaseDetail.setInputParam(inputObj.getString("body"));

                stepCaseDetail.setBodyType("json");
                dataGroup.setUrlParam(inputObj.getString("body"));
            }


        }

        return stepCaseDetail;
    }

    public StepCaseDetail getStepCaseDetail() {
        StepCaseDetail stepCaseDetail = new StepCaseDetail();
//        stepCaseDetail.setOwnerErp("chenyufeng18");
//        stepCaseDetail.setApiErp("chenyufeng18");
//        stepCaseDetail.setInterfaceType("HTTP");
        stepCaseDetail.setType(1);
//        stepCaseDetail.setCaseId(597051L);
//        stepCaseDetail.setName("addHttpStep2");
//        stepCaseDetail.setLineId(8724L);
        stepCaseDetail.setInterfaceId(-1L);
//        stepCaseDetail.setInterfaceName("http://11.158.77.112/testRelated/queryMethodGroup");
//        stepCaseDetail.setRequestType("POST");
        stepCaseDetail.setJdTls(0);
//        stepCaseDetail.setExpectResult("{\n    \"code\":200,\n    \"data\":{},\n    \"msg\":\"ok\"\n}");
        stepCaseDetail.setMatchType(1);
        stepCaseDetail.setColorConfig(0);
        stepCaseDetail.setUserAccount(0);
        stepCaseDetail.setSleeptime(0L);
        stepCaseDetail.setParallelNum(0);

//        stepCaseDetail.setUrlParam("[{\"key\":\"param1\",\"value\":\"123\"}]");
//        stepCaseDetail.setInputParam("{\n    \"body\":{\"item\":1}\n}");
//        stepCaseDetail.setBodyType("json");
        CompareRuleInfo compareRuleInfo = new CompareRuleInfo();
        CompareScript compareScript = new CompareScript();
        compareScript.setScriptType("Groovy");
        compareScript.setCustomScript("");
        //compareRuleInfo.setCompareScript(compareScript);
        compareRuleInfo.setIgnoreOrder(0);
        compareRuleInfo.setIgnoreNull(0);
        compareRuleInfo.setIgnorePaths("");
        stepCaseDetail.setCompareRuleInfo(compareRuleInfo);

        List<DataGroup> dataGroupList = org.apache.commons.compress.utils.Lists.newArrayList();
        DataGroup dataGroup = new DataGroup();
        dataGroup.setSequence(1);
        dataGroup.setName("第1组");
//        dataGroup.setUrlParam("[{\"key\":\"param1\",\"value\":\"123\"}]");
//        dataGroup.setInputParam("{\n    \"body\":{\"item\":1}\n}");
        dataGroup.setMatchType(1);
//        dataGroup.setExpectResult("{\n    \"code\":200,\n    \"data\":{},\n    \"msg\":\"ok\"\n}");
        dataGroup.setCompareRuleInfo(compareRuleInfo);

        dataGroupList.add(dataGroup);
        stepCaseDetail.setDataGroups(dataGroupList);
        return stepCaseDetail;
    }


    public StepCaseDetail queryMethodTestStep(MethodQueryDto methodQueryDto) {

        Map<String, String> param = parseQueryParams(methodQueryDto.getUrl());
        if (param.containsKey("methodId")) {
            return getStepCaseDetail(param.get("methodId"));
        } else {
            throw new BizException("提供接口路径不正确");
        }


    }

    public static Map<String, String> parseQueryParams(String url) {
        Map<String, String> queryParams = new HashMap<>();

        try {
            URL parsedUrl = new URL(url);
            String query = parsedUrl.getQuery();

            if (query != null) {
                String[] params = query.split("&");

                for (String param : params) {
                    String[] keyValue = param.split("=");

                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1];

                        queryParams.put(key, value);
                    }
                }
            }

        } catch (Exception e) {
            log.error("parseQueryParams url:" + url, e);
        }

        return queryParams;
    }
}
