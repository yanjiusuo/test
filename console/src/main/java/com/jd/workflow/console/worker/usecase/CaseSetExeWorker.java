package com.jd.workflow.console.worker.usecase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.enums.CaseTypeEnum;
import com.jd.workflow.console.dto.constant.SystemConstants;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.console.dto.usecase.CaseType;
import com.jd.workflow.console.dto.usecase.HttpDebugDtoSimple;
import com.jd.workflow.console.entity.jacoco.CoverageReport;
import com.jd.workflow.console.entity.jacoco.JacocoResult;
import com.jd.workflow.console.entity.param.ParamBuilderRecord;
import com.jd.workflow.console.entity.usecase.CaseSet;
import com.jd.workflow.console.entity.usecase.CaseSetExeLog;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDetail;
import com.jd.workflow.console.entity.usecase.PageCaseSetExeLogParam;
import com.jd.workflow.console.entity.usecase.enums.CaseSetExeLogStatusEnum;
import com.jd.workflow.console.service.jacoco.JacocoService;
import com.jd.workflow.console.service.param.IParamBuilderService;
import com.jd.workflow.console.service.usecase.CaseSetExeLogDetailManager;
import com.jd.workflow.console.service.usecase.CaseSetExeLogService;
import com.jd.workflow.console.service.usecase.CaseSetService;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.console.utils.RestTemplateUtils;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
@Service
@Slf4j
public class CaseSetExeWorker {

    @Autowired
    private CaseSetExeLogService caseSetExeLogService;

    @Autowired
    private IParamBuilderService paramBuilderService;

    @Autowired
    private CaseSetExeLogDetailManager caseSetExeLogDetailManager;

    @Autowired
    private CaseSetService caseSetService;

    /**
     *
     */
    @Resource
    private RestTemplateUtils restTemplateUtils;

    @Autowired
    private JacocoService jacocoService;

    /**
     *
     */
    private RestTemplate restTemplate;

    /**
     * 执行待执行的用例集记录
     *
     * @return
     */
    @XxlJob("caseSetExe")
    public ReturnT<String> caseSetExe() {
        PageCaseSetExeLogParam queryParam = new PageCaseSetExeLogParam();
        queryParam.setPageSize(100L);
        queryParam.setStatus(1);
        long starNo = 0L, total = 0L;
        do {
            Page<CaseSetExeLog> page = caseSetExeLogService.listCaseSetExeLogs(queryParam);
            for (CaseSetExeLog caseSetExeLog : page.getRecords()) {
                doExeCaseSetLog(caseSetExeLog);
            }
            starNo = queryParam.getCurrent() * queryParam.getPageSize();
            total = page.getTotal();
            queryParam.setCurrent(queryParam.getCurrent() + 1);
        } while (starNo < total);
        return ReturnT.SUCCESS;
    }

    /**
     * 执行用例集里的所有用例
     *
     * @param caseSetExeLog
     */
    private void doExeCaseSetLog(CaseSetExeLog caseSetExeLog) {
        try {
            //修改执行用例到 执行中状态
            boolean result = caseSetExeLogService.updateStatus(caseSetExeLog.getId(), CaseSetExeLogStatusEnum.WAITING_EXE, CaseSetExeLogStatusEnum.RUNNING, null);
            CaseSet caseSet = caseSetService.obtainById(caseSetExeLog.getCaseSetId());
            if (Objects.isNull(caseSet)) {
                throw new StdException("未查到用例集信息caseSetId[" + caseSetExeLog.getCaseSetId() + "]");
            }
            //selectedData --》 java对象  http 一个方法 jsf--一个方法
            CaseType casejson = JSON.parseObject(caseSet.getSelectedData(), CaseType.class);
            List<Long> jsfCaseIdList = obtainInterfaceIdList(casejson.getJsf().getSelected());
            List<Long> httpCaseIdList = obtainInterfaceIdList(casejson.getHttp().getSelected());
            List<Long> CaseIdList= Stream.concat(jsfCaseIdList.stream(), httpCaseIdList.stream())
                    .collect(Collectors.toList());
            for (Long caseId : CaseIdList) {
                doExeCase(caseSetExeLog, caseId);
            }
            boolean exportJacocoResult = jacocoService.exportJacoco(caseSetExeLog.obtainIpData(), caseSetExeLog.getId());
            if (exportJacocoResult) {
                //覆盖率计算中
                result = caseSetExeLogService.updateStatus(caseSetExeLog.getId(), CaseSetExeLogStatusEnum.RUNNING, CaseSetExeLogStatusEnum.COVERAGE_EXE, null);
            } else {
                //失败修改成
                result = caseSetExeLogService.updateStatus(caseSetExeLog.getId(), null, CaseSetExeLogStatusEnum.FAIL, "执行代码覆盖率请求失败，请在本地控制台查看日志");
            }
        } catch (Exception e) {
            log.error("CaseSetExeWorker.doExeCaseSetLog Exception", e);
            caseSetExeLogService.updateStatus(caseSetExeLog.getId(), null, CaseSetExeLogStatusEnum.FAIL, "执行中用例"+e.getMessage());
        }
    }

    private void doExeCase(CaseSetExeLog caseSetExeLog, long caseId) {
        try {
//            HttpDebugDto httpDebugDto = jsonToHttpDebugDto(caseSetExeLog.getHTTPEnv());
            HttpDebugDto httpDebugDto = null;
            if(StringUtils.isNotBlank(caseSetExeLog.getHttpEnv())){
                httpDebugDto = JsonUtils.parse(caseSetExeLog.getHttpEnv(), HttpDebugDto.class);;
                httpDebugDto.setMethodId("0");
            }
            //TODO待删
            ParamBuilderRecord paramBuilderRecord = paramBuilderService.run(caseId, caseSetExeLog.getIp(), caseSetExeLog.getJsfAlias(),httpDebugDto);
            CaseSetExeLogDetail caseSetExeLogDetail = new CaseSetExeLogDetail();
            caseSetExeLogDetail.setCaseId(caseId);
            caseSetExeLogDetail.setCaseSetExeLogId(caseSetExeLog.getId());
            if (Objects.nonNull(paramBuilderRecord)) {
                caseSetExeLogDetail.setCaseExeResultId(paramBuilderRecord.getId());
            }
            if (Objects.nonNull(paramBuilderRecord) && paramBuilderRecord.getRunStatus() == 3) {
                //状态 1-成功 2-失败
                caseSetExeLogDetail.setStatus(1);
            } else {
                caseSetExeLogDetail.setStatus(2);
            }
            caseSetExeLogDetailManager.saveCaseSetExeLogDetail(caseSetExeLogDetail);
        } catch (Exception e) {
            log.error("CaseSetExeWorker.doExeCase Exception ", e);
        }
    }



    /**
     * 获取用例ID数据
     *
     * @param caseIdStrList
     * @return
     */
    private List<Long> obtainInterfaceIdList(List<String> caseIdStrList) {

        if (Objects.isNull(caseIdStrList)) {
            caseIdStrList = new ArrayList<>();
        }
        List<Long> caseIdList = caseIdStrList.stream().map(data -> {
            if (StringUtils.isNotBlank(data)) {
                String[] split = data.split("-");
                if (split.length == 3) {
                    return NumberUtils.toLong(split[2]);
                }
            }
            return null;
        }).filter(data -> {
            return Objects.nonNull(data);
        }).collect(Collectors.toList());
        return caseIdList;
    }

    /**
     * 获取用例集执行结果
     *
     * @return
     */
    @XxlJob("queryJacocoStage")
    public ReturnT<String> queryJacocoStage() {
        PageCaseSetExeLogParam queryParam = new PageCaseSetExeLogParam();
        queryParam.setPageSize(100L);
        queryParam.setStatus(3);
        long starNo = 0L, total = 0L;
        do {
            Page<CaseSetExeLog> page = caseSetExeLogService.listCaseSetExeLogs(queryParam);
            for (CaseSetExeLog caseSetExeLog : page.getRecords()) {
                doQueryJacocoStage(caseSetExeLog);
            }
            starNo = queryParam.getCurrent() * queryParam.getPageSize();
            total = page.getTotal();
            queryParam.setCurrent(queryParam.getCurrent() + 1);
        } while (starNo < total);
        return ReturnT.SUCCESS;
    }

    public void doQueryJacocoStage(CaseSetExeLog caseSetExeLog) {
        try {
            CommonResult<JacocoResult> jacocoResult = jacocoService.queryJacocoStage(caseSetExeLog.obtainIpData(), caseSetExeLog.getId());
            if (Objects.nonNull(jacocoResult) && jacocoResult.getCode() == 0 && Objects.nonNull(jacocoResult.getData())) {
                // 0-执行失败  10-执行中  20-执行成功
                if (Objects.equals(jacocoResult.getData().getStatus(), 20)) {
                    CoverageReport coverageReport = obtainCoverageReport(jacocoResult.getData().getJacocoResultFileUrl());
                    CaseSetExeLog updateCaseSetExeLog = new CaseSetExeLog();
                    updateCaseSetExeLog.setId(caseSetExeLog.getId());
                    if (Objects.nonNull(coverageReport) && StringUtils.isNotBlank(coverageReport.getTotalLines())) {
                        String NewCodeCoverage = coverageReport.getTotalLines().replace("%", "");
                        updateCaseSetExeLog.setNewCodeCoverage(NewCodeCoverage);
                        updateCaseSetExeLog.setBranchName(coverageReport.getCurrentBranch());
                        if(StringUtils.isNotBlank(coverageReport.getRemoteUrl())){
                            String codingAddress = coverageReport.getRemoteUrl().replaceAll("git@coding\\.jd\\.com:|https://coding\\.jd\\.com/", "");
                            updateCaseSetExeLog.setCodingAddress(codingAddress);
                        }
                    }
                    updateCaseSetExeLog.setExeEndTime(new Date());
                    updateCaseSetExeLog.setBucketName(jacocoResult.getData().getJacocoResultFileUrl());
                    caseSetExeLogService.update2Success(updateCaseSetExeLog);
                }else if (Objects.equals(jacocoResult.getData().getStatus(), 10)) {
                    //不做处理
                } else {
                    //失败修改成
                    caseSetExeLogService.updateStatus(caseSetExeLog.getId(), null, CaseSetExeLogStatusEnum.FAIL, "获取代码覆盖率执行结果失败，请在本地控制台查看日志");
                }
            } else {
                //失败修改成
                caseSetExeLogService.updateStatus(caseSetExeLog.getId(), null, CaseSetExeLogStatusEnum.FAIL, "获取代码覆盖率执行结果失败，请在本地控制台查看日志");
            }
        } catch (Exception e) {
            log.error("CaseSetExeWorker.doQueryJacocoStage Exception");
            //失败修改成
            caseSetExeLogService.updateStatus(caseSetExeLog.getId(), null, CaseSetExeLogStatusEnum.FAIL, "获取代码覆盖率执行结果" + e.getMessage());
        }
    }


    /**
     * 获取结果
     *
     * @param path
     * @return
     */
    public CoverageReport obtainCoverageReport(String path) {
        CoverageReport coverageReport = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String coverageReportStr = restTemplateUtils.getAccept("http://"+ SystemConstants.OSS_HOST_NAME + path + "/coverage-report.json",headers);
            coverageReport = JSON.parseObject(coverageReportStr, CoverageReport.class);
        } catch (Exception e) {
            log.error("CaseSetExeWorker.extracted Exception", e);
        }
        return coverageReport;
    }
}
