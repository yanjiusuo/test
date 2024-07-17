package com.jd.workflow.console.worker.param;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Maps;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.common.util.StringUtils;
import com.jd.neptune.painter.annotations.UMP;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.RunStatusEnum;
import com.jd.workflow.console.dto.flow.param.HttpOutputExt;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.console.dto.jsf.JsfDebugData;
import com.jd.workflow.console.dto.jsf.NewJsfDebugDto;
import com.jd.workflow.console.dto.requirement.AssertionStatisticsDTO;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.param.ParamBuilder;
import com.jd.workflow.console.entity.param.ParamBuilderRecord;
import com.jd.workflow.console.entity.param.ParamBuilderScript;
import com.jd.workflow.console.script.*;
import com.jd.workflow.console.service.DebugService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.debug.HttpDebugDataDto;
import com.jd.workflow.console.service.impl.OpenApiService;
import com.jd.workflow.console.service.param.IParamBuilderRecordService;
import com.jd.workflow.console.service.param.IParamBuilderScriptService;
import com.jd.workflow.console.service.param.IParamBuilderService;
import com.jd.workflow.console.utils.TemplateUtils;
import com.jd.workflow.console.utils.TestSsoCookieHelper;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author sunchao81
 * @date 2021/8/11 16:56
 * @description
 */
@Service
@Slf4j
public class ParamWorker {

    /**
     *
     */
    @Resource
    IParamBuilderRecordService recordService;

    /**
     *
     */
    @Resource
    IParamBuilderService paramBuilderService;

    /**
     *
     */
    @Resource
    IParamBuilderScriptService scriptService;

    @Resource
    OpenApiService openApiService;

    @Autowired
    DebugService debugService;



    /**
     * 每次处理个数
     */
    private static final Integer PER_NUMBER = 5;

    @Autowired
    private IMethodManageService methodManageService;

    static ParametersUtils utils = new ParametersUtils();

    /**
     * 测试
     */
    @XxlJob("testJob")
    public ReturnT<String> testJob() {
        log.info("testJob start");
        return  ReturnT.SUCCESS;
    }

    /**
     * 执行入参记录
     */
    @XxlJob("runParamRecord")
    @UMP(onException = "runParamRecordFallBack")
    public ReturnT<String> runParamRecord() {
        log.info("runParamRecord start");
        List<ParamBuilderRecord> recordList = recordService.lambdaQuery().select(ParamBuilderRecord::getId, ParamBuilderRecord::getParamBuilderId)
                .eq(ParamBuilderRecord::getYn, DataYnEnum.VALID.getCode())
                .eq(ParamBuilderRecord::getRunStatus, 1).last("limit " + PER_NUMBER).list();

        List<Long> paramBuilderIdList = recordList.stream().map(ParamBuilderRecord::getParamBuilderId).collect(Collectors.toList());
        List<ParamBuilder> paramBuilders = paramBuilderService.listByIds(paramBuilderIdList);
        Map<Long, ParamBuilder> pbMap = paramBuilders.stream().collect(Collectors.toMap(ParamBuilder::getId, Function.identity(), (i1, i2) -> i1));
        for (ParamBuilderRecord paramBuilderRecord : recordList) {
            Long paramBuilderId = paramBuilderRecord.getParamBuilderId();
            ParamBuilder paramBuilder = pbMap.get(paramBuilderId);
            List<String> keyList = TemplateUtils.getKeyList(paramBuilder.getParamJson());
            if(CollectionUtils.isEmpty(keyList)){
                ParamBuilderRecord record = recordService.getById(paramBuilderRecord.getId());
                record.setRunStatus(3);
                recordService.updateById(record);
            }else {
                Map<String, String> contextMap = parseKey(keyList);
                log.info("runParamRecord {} contextMap:{}", paramBuilderRecord.getId(),JsonUtils.toJSONString(contextMap));
                String text = TemplateUtils.renderTpl(paramBuilder.getParamJson(), contextMap);
                log.info("runParamRecord {} txt:{}", text);
                ParamBuilderRecord record = recordService.getById(paramBuilderRecord.getId());

                List<String> checkList = TemplateUtils.getKeyList(text);
                if(CollectionUtils.isEmpty(checkList)){
                    record.setRunStatus(3);
                    record.setResultJson(text);
                    recordService.updateById(record);
                }
            }
        }
        return  ReturnT.SUCCESS;
    }

    /**
     * 执行脚本，替换Key
     * @param keyList
     * @return
     */
    private Map<String, String> parseKeyAsync(List<String> keyList) {
        try {
            Map<String, String> contextMap = Maps.newHashMap();
            List<Future> futureList = Lists.newArrayList();
            for (String key : keyList) {
                String scriptStr = key.replaceAll("\\$<\\{", "").replaceAll("\\}>", "");
                String scriptId = scriptStr.split("\\.")[0];
                ParamBuilderScript script = scriptService.getById(scriptId);
                ScriptExecutor executor = ScriptExecutorFactory.createExecutor(ScriptType.SHELL);
                Future<ExecutionResult> future = executor.executeAsync(key,script.getScriptContent(), null);
                futureList.add(future);
            }

            for (Future<ExecutionResult> future : futureList) {
                ExecutionResult result = future.get();
                if (result.getException() != null) {
                    result.getException().printStackTrace();
                } else {
                    contextMap.put(result.getKey(),result.getOutput());
                }
            }
            return contextMap;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }


    /**
     * 执行脚本，替换Key
     * @param keyList
     * @return
     */
    public Map<String, String> parseKey(List<String> keyList) {
        try {
            Map<String, String> contextMap = Maps.newHashMap();
            for (String key : keyList) {
                String scriptStr = key.replaceAll("\\$<\\{", "").replaceAll("\\}>", "");
                String scriptId = scriptStr.split("\\.")[0];
                ParamBuilderScript script = scriptService.getById(scriptId);
                if(1==script.getType()){
                    String rest = openApiService.testDataExecute(JSON.parseObject(script.getScriptContent()));
                    contextMap.put(scriptStr, rest);
                }
                if(2==script.getType()){
                    contextMap.put(scriptStr, script.getScriptContent());
                }

            }
            return contextMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }




    public boolean runParamRecordFallBack(Throwable e) {
        e.printStackTrace();
        return Boolean.FALSE;
    }

    @XxlJob("runCase")
    public ReturnT<String> runCase() {
        log.info("runCase start");
        List<ParamBuilderRecord> recordList = recordService.lambdaQuery()
                .select(ParamBuilderRecord::getId, ParamBuilderRecord::getParamBuilderId, ParamBuilderRecord::getMethodManageId)
                .eq(ParamBuilderRecord::getYn, DataYnEnum.VALID.getCode())
                .eq(ParamBuilderRecord::getRunStatus, RunStatusEnum.WAIT.getType()).last("limit " + PER_NUMBER).list();
        if (CollectionUtils.isNotEmpty(recordList)) {
            List<Long> paramBuilderIdList = recordList.stream().map(ParamBuilderRecord::getParamBuilderId).collect(Collectors.toList());

            List<ParamBuilder> paramBuilders = paramBuilderService.listByIds(paramBuilderIdList);
            Map<Long, ParamBuilder> pbMap = paramBuilders.stream().collect(Collectors.toMap(ParamBuilder::getId, Function.identity(), (i1, i2) -> i1));
            for (ParamBuilderRecord paramBuilderRecord : recordList) {
                log.info("runCase 记录:{}", paramBuilderRecord.getId());
                // 获取方法信息
                MethodManage methodManage = methodManageService.getById(paramBuilderRecord.getMethodManageId());
                if (methodManage == null) {
                    log.error("runCase 方法不存在:{}", paramBuilderRecord.getMethodManageId());
                    continue;
                }
                if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType())) {
                    // 处理http方法
                    runCaseForHttp(paramBuilderRecord, pbMap);
                } else if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
                    // 处理jsf方法
                    runCaseForJsf(paramBuilderRecord, pbMap);
                }
            }
        }
        log.info("runCase end");
        return  ReturnT.SUCCESS;
    }

    /**
     * 处理http方法
     * @param paramBuilderRecord
     * @param pbMap
     */
    private void runCaseForHttp(ParamBuilderRecord paramBuilderRecord, Map<Long, ParamBuilder> pbMap) {
        log.info("runCaseForHttp 记录:{}", paramBuilderRecord.getId());
        HttpDebugDto dto = new HttpDebugDto();
        try {
            // 更新状态为执行中
            updateRecordInfo(paramBuilderRecord, RunStatusEnum.RUNNING.getType(), null, null, null);

            ParamBuilder paramBuilder = pbMap.get(paramBuilderRecord.getParamBuilderId());
            String paramJson = paramBuilder.getParamJson();
            // 将入参转换为快捷调用所需的HttpDebugDto
            dto = JsonUtils.parse(paramJson, HttpDebugDto.class);
            String onlineCookie;
            Map paramJsonMap = JsonUtils.parse(paramJson, Map.class);
            if (Objects.nonNull(dto) && "China".equals(dto.getSite()) && Objects.nonNull(paramJsonMap.get("Cookie"))) {
                onlineCookie = paramJsonMap.get("Cookie").toString();
            } else {
                onlineCookie = null;
            }
            HttpOutputExt result = debugService.debugHttp(dto, onlineCookie);
            Integer runStatus = result.isSuccess() ? RunStatusEnum.SUCCESS.getType() : RunStatusEnum.FAIL.getType();
            String runMsg = "";
            // 获取断言信息
            AssertionStatisticsDTO assertionStatisticsDTO = result.getAssertionStatistics();
            if (Objects.nonNull(assertionStatisticsDTO)) {
                // 断言失败影响结果
                if (!assertionStatisticsDTO.isRes()) {
                    log.info("runCase runCaseForHttp 执行断言失败，入参dto：{},出参：{}", JsonUtils.toJSONString(dto), JsonUtils.toJSONString(result));
                    runStatus = RunStatusEnum.FAIL.getType();
                }
                if (assertionStatisticsDTO.getTotalCount() > 0) {
                    runMsg = assertionStatisticsDTO.getTotalCount() + "条断言，" + assertionStatisticsDTO.getSuccessCount() + "条成功，" + assertionStatisticsDTO.getFailCount() + "条失败";
                }
            }
            // 更新执行记录
            updateRecordInfo(paramBuilderRecord, runStatus, runMsg, result.getLogId(), null);
        } catch (Exception e) {
            // 截取异常信息的前200个字符
            String exceptionSummary = getExceptionSummary(e);
            HttpDebugDataDto debugDataDto = new HttpDebugDataDto();
            debugDataDto.setSite(dto.getSite());
            debugDataDto.setInput(buildHttpInput(dto));
            updateRecordInfo(paramBuilderRecord, RunStatusEnum.FAIL.getType(), exceptionSummary, null, JsonUtils.toJSONString(debugDataDto));
            log.error("runCase runCaseForHttp 失败：paramBuilderRecord:{}", JSON.toJSONString(paramBuilderRecord));
            e.printStackTrace();
        }
    }

    /**
     * 处理jsf方法
     * @param paramBuilderRecord
     * @param pbMap
     */
    private void runCaseForJsf(ParamBuilderRecord paramBuilderRecord, Map<Long, ParamBuilder> pbMap) {
        log.info("runCaseForJsf 记录:{}", paramBuilderRecord.getId());
        NewJsfDebugDto dto = new NewJsfDebugDto();
        try {
            // 更新状态为执行中
            updateRecordInfo(paramBuilderRecord, RunStatusEnum.RUNNING.getType(), null, null, null);

            ParamBuilder paramBuilder = pbMap.get(paramBuilderRecord.getParamBuilderId());
            String paramJson = paramBuilder.getParamJson();
            // 将入参转换为快捷调用所需的NewJsfDebugDto
            dto = JsonUtils.parse(paramJson, NewJsfDebugDto.class);
            JsfOutputExt result = debugService.debugJsfNew(dto);
            Integer runStatus = result.isSuccess() ? RunStatusEnum.SUCCESS.getType() : RunStatusEnum.FAIL.getType();
            String runMsg = "";
            // 获取断言信息
            AssertionStatisticsDTO assertionStatisticsDTO = result.getAssertionStatistics();
            if (Objects.nonNull(assertionStatisticsDTO)) {
                // 断言失败影响结果
                if (!assertionStatisticsDTO.isRes()) {
                    log.info("runCase runCaseForJsf 执行断言失败，入参dto：{},出参：{}", JsonUtils.toJSONString(dto), JsonUtils.toJSONString(result));
                    runStatus = RunStatusEnum.FAIL.getType();
                }
                if (assertionStatisticsDTO.getTotalCount() > 0) {
                    runMsg = assertionStatisticsDTO.getTotalCount() + "条断言，" + assertionStatisticsDTO.getSuccessCount() + "条成功，" + assertionStatisticsDTO.getFailCount() + "条失败";
                }
            }
            // 更新执行记录
            updateRecordInfo(paramBuilderRecord, runStatus, runMsg, result.getLogId(), null);
        } catch (Exception e) {
            // 截取异常信息的前200个字符
            String exceptionSummary = getExceptionSummary(e);
            JsfDebugData jsfDebugData = new JsfDebugData();
            jsfDebugData.setInput(dto);
            updateRecordInfo(paramBuilderRecord, RunStatusEnum.FAIL.getType(), exceptionSummary, null, JsonUtils.toJSONString(jsfDebugData));
            log.error("runCase runCaseForJsf 失败：paramBuilderRecord:{}", JSON.toJSONString(paramBuilderRecord));
            e.printStackTrace();
        }
    }

    /**
     * 更新执行记录
     * @param record
     * @param runStatus
     * @param runMsg
     * @param logId
     * @param resultJson
     */
    private void updateRecordInfo(ParamBuilderRecord record, Integer runStatus, String runMsg, Long logId, String resultJson) {
        LambdaUpdateWrapper<ParamBuilderRecord> luw = new LambdaUpdateWrapper<>();
        luw.set(ParamBuilderRecord::getRunStatus, runStatus)
                .set(StringUtils.isNotEmpty(runMsg), ParamBuilderRecord::getRunMsg, runMsg)
                .set(logId != null, ParamBuilderRecord::getDebugLogId, logId)
                .set(StringUtils.isNotEmpty(resultJson), ParamBuilderRecord::getResultJson, resultJson)
                .eq(ParamBuilderRecord::getId, record.getId());
        recordService.update(luw);
    }

    /**
     * 截取异常信息
     * @param e
     * @return
     */
    private String getExceptionSummary(Exception e) {
        String exceptionSummary = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 200)) : "无详细异常信息";
        return exceptionSummary;
    }

    /**
     * 构建http请求入参
     * @param dto
     * @return
     */
    private HttpDebugDataDto.Input buildHttpInput(HttpDebugDto dto) {
        HttpDebugDataDto.Input inputData = new HttpDebugDataDto.Input();
        try {
            if (dto.getInput().getBodyData() != null) {
                inputData.setBody(dto.getInput().getBodyData());
            } else {
                inputData.setBody(utils.buildInput(dto.getInput().getBody()));
            }
            inputData.setHeaders(utils.buildInput(dto.getInput().getHeaders()));
            inputData.setColorHeaders(utils.buildInput(dto.getInput().getColorHeaders()));
            inputData.setColorInPutParam(utils.buildInput(dto.getInput().getColorInputParam()));
            inputData.setParams(utils.buildInput(dto.getInput().getParams()));
            if (!ObjectHelper.isEmpty(dto.getInput().getPath())) {
                Map<String, Object> pathData = utils.buildInput(dto.getInput().getPath());
                inputData.setPath(pathData);
                inputData.setUrl(StringHelper.replacePlaceholder(dto.getInput().getUrl(), pathData));
            }
            if (inputData.getUrl() != null && !inputData.getUrl().startsWith("/")) {
                inputData.setUrl("/" + inputData.getUrl());
            }
            inputData.setTargetAddress(dto.getTargetAddress());
        } catch (Exception e) {
            log.error("buildHttpInput error");
            e.printStackTrace();
        }
        return inputData;
    }
}
