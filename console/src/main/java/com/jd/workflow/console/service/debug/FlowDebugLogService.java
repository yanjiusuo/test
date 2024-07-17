package com.jd.workflow.console.service.debug;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.LockTypeEnum;
import com.jd.workflow.console.dao.mapper.MethodManageMapper;
import com.jd.workflow.console.dao.mapper.debug.FlowDebugLogMapper;
import com.jd.workflow.console.dto.DebugFlowReqDTO;
import com.jd.workflow.console.dto.flow.param.FlowDebugLogDto;
import com.jd.workflow.console.dto.jsf.JsfDebugData;
import com.jd.workflow.console.dto.test.deeptest.CaseGroupCreateDto;
import com.jd.workflow.console.dto.test.deeptest.CaseInfo;
import com.jd.workflow.console.dto.test.deeptest.SuiteDetail;
import com.jd.workflow.console.dto.test.deeptest.TestResult;
import com.jd.workflow.console.dto.test.deeptest.step.DataGroup;
import com.jd.workflow.console.dto.test.deeptest.step.StepCaseDetail;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.entity.debug.StepDebugLog;
import com.jd.workflow.console.entity.debug.dto.PluginCallLog;
import com.jd.workflow.console.entity.test.TestCaseGroup;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.lock.JobLockService;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.console.service.test.IDeeptestRemoteCaller;
import com.jd.workflow.console.service.test.TestCaseGroupService;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class FlowDebugLogService extends ServiceImpl<FlowDebugLogMapper, FlowDebugLog> {

    @Resource
    private MethodManageMapper methodManageMapper;
    @Autowired
    IMethodManageService methodManageService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Resource
    private StepDebugLogService stepDebugLogService;

    @Resource
    private JobLockService jobLockService;
    @Autowired
    UserPrivilegeHelper userPrivilegeHelper;


    @Autowired
    EasyMockRemoteService testEasyMockRemoteService;
    @Autowired
    EasyMockRemoteService onlineEasyMockRemoteService;

    @Autowired
    TestCaseGroupService testCaseGroupService;

    @Autowired
    private IAppInfoService appInfoService;


    public EasyMockRemoteService remoteService(String env) {
        Guard.notEmpty(env, "环境不可为空");
        if ("test".equals(env)) {
            return testEasyMockRemoteService;
        } else {
            return onlineEasyMockRemoteService;
        }
    }

    public Page<FlowDebugLog> pageList(String methodId, long current, long size) {
        final LambdaQueryWrapper<FlowDebugLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowDebugLog::getMethodId, methodId);
        return page(new Page<>(current, size), lqw);
    }

    public FlowDebugLog getNewestDebugLog(Long methodId) {
        LambdaQueryWrapper<FlowDebugLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowDebugLog::getMethodId, methodId);
        //lqw.eq(FlowDebugLog::getSuccess,1);
        lqw.orderByDesc(FlowDebugLog::getId);
        lqw.last("LIMIT 1");
        List<FlowDebugLog> list = list(lqw);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public FlowDebugLog getUserNewestDebugLog(Long methodId, Integer tag, String creator) {
        LambdaQueryWrapper<FlowDebugLog> lqw = new LambdaQueryWrapper<>();
        if (tag == null) {
            tag = 0;
        }
        lqw.eq(FlowDebugLog::getMethodId, methodId);
        lqw.eq(FlowDebugLog::getMethodTag, tag);
        lqw.eq(StringUtils.isNotBlank(creator), FlowDebugLog::getCreator, creator);
        //lqw.eq(FlowDebugLog::getSuccess,1);
//      lqw.orderByDesc(FlowDebugLog::getTopFlag);
        lqw.orderByDesc(FlowDebugLog::getModified);
        lqw.orderByDesc(FlowDebugLog::getSuccess);
        lqw.orderByDesc(FlowDebugLog::getId);
        lqw.eq(FlowDebugLog::getYn, 1);
        lqw.last("LIMIT 1");
        List<FlowDebugLog> list = list(lqw);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public FlowDebugLog getUserNewestDebugLog(Long methodId, Integer tag) {
        return getUserNewestDebugLog(methodId, tag, UserSessionLocal.getUser().getUserId());
    }

    public void syncToMock(List<Long> ids) {
        if (ids.isEmpty()) return;
        List<FlowDebugLog> logs = listByIds(ids);
        String methodId = logs.get(0).getMethodId();
        MethodManage method = methodManageService.getById(methodId);
        InterfaceManage interfaceManage = interfaceManageService.getById(method.getInterfaceId());

        syncMockData("test", interfaceManage, method, logs);
        syncMockData("online", interfaceManage, method, logs);

    }

    private void syncMockData(String env, InterfaceManage interfaceManage, MethodManage method, List<FlowDebugLog> logs) {
        try {
            EasyMockRemoteService.SyncMockDataResult result = remoteService(env).syncMockData(interfaceManage, method);
            List<HttpDebugDataDto> httpDebugDataDtos = new ArrayList<>();
            List<JsfDebugData> jsfDebugDtos = new ArrayList<>();
            for (FlowDebugLog flowDebugLog : logs) {
                if (InterfaceTypeEnum.HTTP.getCode().equals(method.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())) {
                    HttpDebugDataDto data = JsonUtils.parse(flowDebugLog.getLogContent(), HttpDebugDataDto.class);
                    if (Objects.nonNull(data)) {
                        data.setDesc(flowDebugLog.getDesc());
                    }
                    httpDebugDataDtos.add(data);

                } else if (InterfaceTypeEnum.JSF.getCode().equals(method.getType())) {
                    JsfDebugData jsfDto = JsonUtils.parse(flowDebugLog.getLogContent(), JsfDebugData.class);
                    if (Objects.nonNull(jsfDto)) {
                        jsfDto.setDesc(flowDebugLog.getDesc());
                    }
                    jsfDebugDtos.add(jsfDto);
                }
            }
            for (HttpDebugDataDto httpDebugDataDto : httpDebugDataDtos) {
                remoteService(env).addHttpDebugData(method, httpDebugDataDto, result.getMethodId() + "");
            }
            if (!jsfDebugDtos.isEmpty()) {
                remoteService(env).addJsfDebugTemplate(interfaceManage, method, jsfDebugDtos, Variant.valueOf(result.getMethodId()).toInt());
            }
        } catch (Exception e) {
            log.error("同步mock数据失败:env={},methodid={}", env, method.getId(), e);
        }
    }

    public Page<FlowDebugLog> queryLogs(DebugFlowReqDTO req) {
        boolean hasAuth = userPrivilegeHelper.hasPrivilegeByMethodId(req.getMethodId(), UserSessionLocal.getUser().getUserId());
        boolean isMember = false;
        MethodManage methodManage = methodManageService.getById(req.getMethodId());
        if (Objects.nonNull(methodManage)) {
            InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());
            if (Objects.nonNull(interfaceManage)) {
                isMember = appInfoService.isMember(interfaceManage.getAppId());
            }
        }

        final LambdaQueryWrapper<FlowDebugLog> lqw = new LambdaQueryWrapper<>();
        if (req.getMethodId() != null) {
            lqw.eq(FlowDebugLog::getMethodId, req.getMethodId());
        }
        if (!hasAuth && !isMember) {
            lqw.eq(FlowDebugLog::getCreator, UserSessionLocal.getUser().getUserId());
        }
        if (hasAuth && isMember && StringUtils.isNotEmpty(req.getErp())) {
            lqw.eq(FlowDebugLog::getCreator, req.getErp());
        }
        if (req.getSuccess() != null) {
            lqw.eq(FlowDebugLog::getSuccess, req.getSuccess());
        }
        if (StringUtils.isNotBlank(req.getDesc())) {
            lqw.like(FlowDebugLog::getDesc, req.getDesc());
        }
        lqw.eq(FlowDebugLog::getYn, DataYnEnum.VALID.getCode());
        //不传递 查非color
        lqw.ne(null == req.getTag() || req.getTag() == 0, FlowDebugLog::getMethodTag, 1);
        lqw.eq(null != req.getTag() && req.getTag() != 0, FlowDebugLog::getMethodTag, req.getTag());
        lqw.orderByDesc(FlowDebugLog::getTopFlag);
        lqw.orderByDesc(FlowDebugLog::getModified);
        lqw.orderByDesc(FlowDebugLog::getId);
        try {
            return page(initPage(req.getCurrentPage(), req.getPageSize(), 1000), lqw);
        } catch (Exception e) {
            throw new BizException("查询历史记录失败，请联系管理员", e);
//            log.error("方法{}历史记录，一次数据量超过32M",req.getMethodId());
//            lqw.select(FlowDebugLog::getId,FlowDebugLog::getCreator,FlowDebugLog::getModifier,FlowDebugLog::getCreated,FlowDebugLog::getModified
//                    ,FlowDebugLog::getSuccess,FlowDebugLog::getMethodId,FlowDebugLog::getSite,FlowDebugLog::getEnvName,FlowDebugLog::getMethodTag);
//            Page<FlowDebugLog> page = page(initPage(req.getCurrentPage(), 1, 1000), lqw);
//            for (FlowDebugLog record : page.getRecords()) {
//                cn.hutool.json.JSONObject jsonObject = new cn.hutool.json.JSONObject().put("input", JSONUtil.parseObj(getById(record.getId()).getLogContent()).get("input"));
//                record.setLogContent(JsonUtils.toJSONString(jsonObject));
//            }
//            return page;
        }
    }

    public Long saveLog(StepContext stepContext, Long methodId) {
        FlowDebugLog log = new FlowDebugLog();
        log.setSuccess(stepContext.isSuccess() ? 1 : 0);
        log.setLogContent(JsonUtils.toJSONString(stepContext.toLog()));
        log.setMethodId(methodId + "");
        log.setYn(DataYnEnum.VALID.getCode());
        save(log);
        return log.getId();
    }

    public Long updateLog(Long id, StepContext stepContext, Long methodId) {
        FlowDebugLog log = new FlowDebugLog();
        log.setSuccess(stepContext.isSuccess() ? 1 : 0);
        log.setLogContent(JsonUtils.toJSONString(stepContext.toLog()));
        log.setMethodId(methodId + "");
        log.setId(id);
        updateById(log);
        return id;
    }

    public boolean deleteLog(Long id) {
        FlowDebugLog log = new FlowDebugLog();
        log.setId(id);
        log.setYn(DataYnEnum.INVALID.getCode());
        return updateById(log);
    }

    public boolean updateLogDesc(Long id, String desc) {
        FlowDebugLog log = new FlowDebugLog();
        log.setId(id);
        log.setDesc(desc);
        return updateById(log);
    }

    public boolean updateTopFlag(Long id, Integer topFlag) {

        FlowDebugLog log = new FlowDebugLog();
        log.setId(id);
        log.setTopFlag(topFlag);

        return updateById(log);
    }

    public Long queryLogByDigest(Long methodId, String digest) {
        final LambdaQueryWrapper<FlowDebugLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowDebugLog::getMethodId, methodId + "");
        lqw.eq(FlowDebugLog::getDigest, digest);
        FlowDebugLog obj = this.getOne(lqw);
        if (obj != null) {
            return obj.getId();
        }
        return null;
    }

    @Scheduled(cron = "${cleanDebugLog.lock.cron:0 0 1 * * ?}")
    public void clearHistoryLogLock() {
        jobLockService.createLock(LockTypeEnum.CLEAN_DEBUG_LOG, DateUtil.getCurrentDate());
    }

    public boolean manualClearHistoryLog(String manualOp) {
        log.info("Manual to operation clear debug history log , pin={} , manualOp={} >>>>>>>>>>>>>>", UserSessionLocal.getUser().getUserId(), manualOp);
        executeClearHistoryLog();
        return true;
    }

    @Scheduled(cron = "${cleanDebugLog.run.cron:9 0 1 * * ?}")
    public void scheduleClearHistoryLog() {
        boolean toRunCleanLog = jobLockService.getLock(LockTypeEnum.CLEAN_DEBUG_LOG, DateUtil.getCurrentDate());
        if (toRunCleanLog) {
            executeClearHistoryLog();
        }
    }

    public void executeClearHistoryLog() {
        log.info("start to clear debug history log >>>>>>>>>>>>>>");
        //start methodId
        try {
            Long startId = 0l;
            LambdaQueryWrapper<MethodManage> methodQuery = Wrappers.<MethodManage>lambdaQuery().orderByAsc(MethodManage::getId);
            methodQuery.gt(MethodManage::getId, startId);
            methodQuery.eq(MethodManage::getType, InterfaceTypeEnum.ORCHESTRATION.getCode());
            methodQuery.select(MethodManage::getId);
            Page<MethodManage> methodPage = methodManageMapper.selectPage(new Page<>(1, 1000, false), methodQuery);
            while (methodPage != null && CollectionUtils.isNotEmpty(methodPage.getRecords())) {
                for (MethodManage m : methodPage.getRecords()) {
                    if (m.getId() > startId) {
                        startId = m.getId();
                    }
                    LambdaQueryWrapper<FlowDebugLog> query = Wrappers.<FlowDebugLog>lambdaQuery().orderByAsc(FlowDebugLog::getYn);
                    query.eq(FlowDebugLog::getMethodId, m.getId());
                    query.select(FlowDebugLog::getId, FlowDebugLog::getMethodId, FlowDebugLog::getYn, FlowDebugLog::getModified);
                    List<FlowDebugLog> list = this.list(query);
                    if (CollectionUtils.isNotEmpty(list) && list.size() > 1000) {
                        //优先删除无效的数据
                        for (Iterator<FlowDebugLog> iterator = list.iterator(); iterator.hasNext(); ) {
                            FlowDebugLog next = iterator.next();
                            if (DataYnEnum.INVALID.getCode().equals(next.getYn())) {
                                //删除编排数据
                                this.removeById(next.getId());
                                //删除编排单步流程数据
                                stepDebugLogService.remove(Wrappers.<StepDebugLog>lambdaQuery().eq(StepDebugLog::getFlowId, next.getId()));
                                iterator.remove();
                            } else {
                                break;
                            }
                        }
                        //删除历史的数据
                        if (list.size() > 1000) {
                            list.sort((o1, o2) -> o2.getModified().compareTo(o1.getModified()));
                            List<FlowDebugLog> flowDebugLogs = list.subList(1000, list.size());
                            flowDebugLogs.forEach(o -> {
                                //删除编排数据
                                this.removeById(o.getId());
                                //删除编排单步流程数据
                                stepDebugLogService.remove(Wrappers.<StepDebugLog>lambdaQuery().eq(StepDebugLog::getFlowId, o.getId()));
                            });
                        }
                    }
                }
                methodQuery = Wrappers.<MethodManage>lambdaQuery().orderByAsc(MethodManage::getId);
                methodQuery.gt(MethodManage::getId, startId);
                methodQuery.eq(MethodManage::getType, InterfaceTypeEnum.ORCHESTRATION.getCode());
                methodQuery.select(MethodManage::getId);
                methodPage = methodManageMapper.selectPage(new Page<>(1, 1000, false), methodQuery);
            }
        } catch (Exception e) {
            log.error("FlowDebugLogService clearHistoryLog occur exception >>>>>>>>>>>", e);
        }
        log.info("end to clear debug history log >>>>>>>>>>>>>>");
    }


    private Page initPage(Integer currentPage, Integer pageSize, Integer maxPageSize) {
        Integer realCurrentPage = 1;
        if (currentPage != null && currentPage > realCurrentPage) {
            realCurrentPage = currentPage;
        }
        Integer realPageSize = maxPageSize;
        if (pageSize != null && pageSize > 0 && pageSize < maxPageSize) {
            realPageSize = pageSize;
        }
        return new Page(realCurrentPage, realPageSize);
    }

    public Long saveOrUpdateLogByDigest(FlowDebugLog log) {
        LambdaQueryWrapper<FlowDebugLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowDebugLog::getDigest, log.getDigest());
        lqw.eq(FlowDebugLog::getYn, 1);
        lqw.eq(FlowDebugLog::getMethodId, log.getMethodId());
        List<FlowDebugLog> list = list(lqw);
        if (!list.isEmpty()) {
            FlowDebugLog flowDebugLog = list.get(0);
            flowDebugLog.setLogContent(log.getLogContent());
            updateById(flowDebugLog);
            return flowDebugLog.getId();
        } else {
            save(log);
            return log.getId();
        }
    }


    public void saveDebugLog(FlowDebugLogDto dto) {
        FlowDebugLog log = new FlowDebugLog();
        log.setMethodId(dto.getMethodId() + "");
        //log.setDigest(dto.getDigest());
        log.setLogContent(dto.getLogContent());
        log.setYn(1);
        log.setDigest(dto.getDigest());
        log.setSite(dto.getSite());
        log.setEnvName(dto.getEnvName());
        save(log);

    }

    public Integer initDeeptestCase(Long id) {
        FlowDebugLog flowDebugLog = getById(id);
        if (Objects.isNull(flowDebugLog)) {
            log.info("initDeeptestCase id:{} flowDebugLog  is null", id);
            return 0;
        }
        String caseName = String.format("冒烟用例生成自快捷调用%s", id);
        if (StringUtils.isNotEmpty(flowDebugLog.getDesc())) {
            caseName = flowDebugLog.getDesc();
        }
        if (StringUtils.isNotEmpty(flowDebugLog.getDesc())) {
            caseName = flowDebugLog.getDesc();
        }

        MethodManage methodManage = methodManageService.getById(flowDebugLog.getMethodId());

        if (StringUtils.isEmpty(flowDebugLog.getSite())) {
            log.info("initDeeptestCase id:{} flowDebugLog.getSite()  is null", id);
            return 0;
        }
        if ("China".equals(flowDebugLog.getSite())) {
            flowDebugLog.setSite("master");
        }

        SuiteDetail suiteDetail = getTestCase(caseName, flowDebugLog.getSite(), methodManage.getId());
        if (Objects.isNull(suiteDetail)) {
            log.info("initDeeptestCase id:{} getTestCase  is null", id);
            return 1;
        }

        IDeeptestRemoteCaller deeptestRemoteCaller = testCaseGroupService.remoteCaller(flowDebugLog.getSite());

        String erp = UserSessionLocal.getUser().getUserId();

        StepCaseDetail stepCaseDetail = testCaseGroupService.getStepCaseDetail();
        stepCaseDetail.setOwnerErp(erp);
        stepCaseDetail.setApiErp(erp);
        stepCaseDetail.setLineId(suiteDetail.getLineId());
        stepCaseDetail.setCaseId(suiteDetail.getId());
        stepCaseDetail.setName(methodManage.getName());

        if (methodManage.getType() == 3) {
            //jsf接口
            InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());


            JsfDebugData jsfDebugData = JSON.parseObject(flowDebugLog.getLogContent(), JsfDebugData.class);


            stepCaseDetail.setInterfaceType("JSF");

            stepCaseDetail.setMethodName(methodManage.getMethodCode());
            stepCaseDetail.setInterfaceName(interfaceManage.getServiceCode());

            stepCaseDetail.setAlias(jsfDebugData.getInput().getAlias());

            stepCaseDetail.setInputParam(JSON.toJSONString(jsfDebugData.getInput().getInputData()));
            JSONObject output = JSON.parseObject(JSON.toJSONString(jsfDebugData.getOutput()));

            stepCaseDetail.setExpectResult(output.getString("body"));
            //设置成部分匹配。
            stepCaseDetail.setMatchType(0);
            DataGroup dataGroup = stepCaseDetail.getDataGroups().get(0);
            dataGroup.setInputParam(JSON.toJSONString(jsfDebugData.getInput().getInputData()));
            dataGroup.setExpectResult(output.getString("body"));
            //设置成部分匹配。
            dataGroup.setMatchType(0);


        } else if (methodManage.getType() == 1) {
            //http接口

            HttpDebugDataDto httpDebugDataDto = JSON.parseObject(flowDebugLog.getLogContent(), HttpDebugDataDto.class);


            stepCaseDetail.setInterfaceType("HTTP");
            stepCaseDetail.setRequestType(methodManage.getHttpMethod());


            stepCaseDetail.setInterfaceName(httpDebugDataDto.getInput().getTargetAddress() + httpDebugDataDto.getInput().getUrl());
            stepCaseDetail.setExpectResult(JSON.toJSONString(httpDebugDataDto.getOutput().getBody()));
            //设置成部分匹配。
            stepCaseDetail.setMatchType(0);

            DataGroup dataGroup = stepCaseDetail.getDataGroups().get(0);
            dataGroup.setExpectResult(JSON.toJSONString(httpDebugDataDto.getOutput().getBody()));
            //设置成部分匹配。
            dataGroup.setMatchType(0);

            if (Objects.nonNull(httpDebugDataDto.getInput().getHeaders())) {
                JSONArray jsonArray = new JSONArray();

                for (String key : httpDebugDataDto.getInput().getHeaders().keySet()) {
                    JSONObject item = new JSONObject();
                    item.put("header", key);
                    item.put("value", httpDebugDataDto.getInput().getHeaders().get(key));
                    jsonArray.add(item);
                }

                stepCaseDetail.setHeaderParam(jsonArray.toJSONString());
            }
            if (Objects.nonNull(httpDebugDataDto.getInput().getParams())) {
                JSONArray jsonArray = new JSONArray();

                for (String key : httpDebugDataDto.getInput().getParams().keySet()) {
                    JSONObject item = new JSONObject();
                    item.put("key", key);
                    item.put("value", httpDebugDataDto.getInput().getParams().get(key));
                    jsonArray.add(item);
                }
                stepCaseDetail.setUrlParam(jsonArray.toJSONString());
                stepCaseDetail.setDataGroups(null);
                dataGroup.setUrlParam(jsonArray.toJSONString());
            }
            if (Objects.nonNull(httpDebugDataDto.getInput().getPath())) {

            }
            if (Objects.nonNull(httpDebugDataDto.getInput().getBody()) && !"{}".equals(JSON.toJSONString(httpDebugDataDto.getInput().getBody()))) {
                stepCaseDetail.setInputParam(JSON.toJSONString(httpDebugDataDto.getInput().getBody()));

                stepCaseDetail.setBodyType("json");
                stepCaseDetail.setDataGroups(null);
                dataGroup.setUrlParam(JSON.toJSONString(httpDebugDataDto.getInput().getBody()));
            }


        } else {
            log.info("initDeeptestCase id:{} methodManage.getType:{}", id, methodManage.getType());
            return 0;
        }
        log.info("deeptestRemoteCaller.addTestCaseStep :{}", JSON.toJSONString(stepCaseDetail));
        TestResult<StepCaseDetail> result = deeptestRemoteCaller.addTestCaseStep(stepCaseDetail);
        if (result.isSuccess()) {
            return result.getData().getId().intValue();
        } else {
            log.info("initDeeptestCase id:{} addTestCaseStep fail:{}", id, JSON.toJSONString(result));
        }

        return 0;
    }

    private SuiteDetail getTestCase(String caseName, String site, Long methodId) {
        SuiteDetail result = null;
        if ("China".equals(site)) {
            site = "master";
        }
        TestCaseGroup testCaseGroup = testCaseGroupService.getMethod(site, methodId);

        IDeeptestRemoteCaller deeptestRemoteCaller = testCaseGroupService.remoteCaller(site);
        TestResult<List<SuiteDetail>> testResult = deeptestRemoteCaller.getCaseListByType(testCaseGroup.getRelatedTestCaseGroupId(), 1);
        log.info("deeptestRemoteCaller.getCaseListByType input:{},response:{}",(testCaseGroup.getRelatedTestCaseGroupId()),JSON.toJSONString(testResult));
        if (testResult.isSuccess()) {
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(testResult.getData())) {
                for (SuiteDetail datum : testResult.getData()) {
                    if (datum.getName().equals(caseName)) {
                        return null;
                    }
                }
            }
        }

        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setSource("cjg");
        caseInfo.setLineId(testCaseGroup.getRelatedTestModuleId());
        caseInfo.setSuiteId(testCaseGroup.getRelatedTestCaseGroupId());
        caseInfo.setName(caseName);
        caseInfo.setEditWay(0);
        caseInfo.setCaseType("1");
        caseInfo.setOwner(UserSessionLocal.getUser().getUserId());
        caseInfo.setPriority(0);
        TestResult<CaseGroupCreateDto> testResult1 = deeptestRemoteCaller.addTestCase(caseInfo);
        log.info("deeptestRemoteCaller.addTestCase :{},response:{}",JSON.toJSONString(caseInfo),JSON.toJSONString(testResult1));
        if (testResult1.isSuccess()) {
            result = new SuiteDetail();
            result.setId(testResult1.getData().getId());
            result.setSuiteId(testCaseGroup.getRelatedTestCaseGroupId());
            result.setLineId(testCaseGroup.getRelatedTestModuleId());
        }


        return result;
    }

    public boolean savePluginCallLogs(List<PluginCallLog> logs) {
        List<FlowDebugLog> flowDebugLogs = new ArrayList<>();
        for (PluginCallLog pluginCallLog : logs) {
            FlowDebugLog flowDebugLog = new FlowDebugLog();
            try {
                BeanUtils.copyProperties(flowDebugLog, pluginCallLog);
            } catch (Exception e) {
                throw new BizException("保存失败：" + e.getMessage(), e);
            }
            flowDebugLog.setCreator(UserSessionLocal.getUser().getUserId());
            flowDebugLog.setModifier(UserSessionLocal.getUser().getUserId());
            flowDebugLogs.add(flowDebugLog);
        }
        return saveBatch(flowDebugLogs);
    }
}
