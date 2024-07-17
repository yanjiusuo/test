package com.jd.workflow.console.service.param.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.RunStatusEnum;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordDTO;
import com.jd.workflow.console.dto.requirement.ParamBuilderRecordParam;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.entity.param.ParamBuilder;
import com.jd.workflow.console.entity.param.ParamBuilderRecord;
import com.jd.workflow.console.dao.mapper.param.ParamBuilderRecordMapper;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.debug.FlowDebugLogService;
import com.jd.workflow.console.service.param.IParamBuilderRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.service.param.IParamBuilderService;
import com.jd.workflow.soap.common.lang.Guard;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 入参构建器记录 服务实现类
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
@Service
public class ParamBuilderRecordServiceImpl extends ServiceImpl<ParamBuilderRecordMapper, ParamBuilderRecord> implements IParamBuilderRecordService {

    @Autowired
    private FlowDebugLogService flowDebugLogService;

    @Autowired
    private IParamBuilderService paramBuilderService;

    @Autowired
    private IMethodManageService methodManageService;

    @Autowired
    private IInterfaceManageService interfaceManageService;

    @Override
    public Page listPage(ParamBuilderRecordParam param) {
        Page<ParamBuilderRecord> page = this.lambdaQuery()
                .eq(ParamBuilderRecord::getYn, DataYnEnum.VALID.getCode())
                .eq(ParamBuilderRecord::getParamBuilderId, param.getParamBuilderId())
                .eq(param.getType() == 1, ParamBuilderRecord::getCreator, UpLoginContextHelper.getUserPin())
                .orderByDesc(ParamBuilderRecord::getCreated)
                .page(new Page(param.getCurrent(), param.getSize()));
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            page.getRecords().forEach(record -> record.setRunStatusDesc(RunStatusEnum.getDescByType(record.getRunStatus())));
            List<Long> paramBuilderIds = page.getRecords().stream().map(ParamBuilderRecord::getParamBuilderId).distinct().collect(Collectors.toList());
            List<ParamBuilder> paramBuilders = paramBuilderService.listByIds(paramBuilderIds);
            Map<Long, String> paramBuilderIdNameMap = paramBuilders.stream().collect(Collectors.toMap(ParamBuilder::getId, ParamBuilder::getSceneName, (i1, i2) -> i1));
            page.getRecords().forEach(record -> record.setSceneName(paramBuilderIdNameMap.get(record.getParamBuilderId())));
            ParamBuilderRecord firstRecord = page.getRecords().get(0);
            if (RunStatusEnum.RUNNING.getType().equals(firstRecord.getRunStatus())) {
                firstRecord.setRedFlag(Boolean.TRUE);
            }
            List<Long> logIds = page.getRecords().stream().filter(record -> record.getDebugLogId() != null)
                    .map(ParamBuilderRecord::getDebugLogId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(logIds)) {
                List<FlowDebugLog> debugLogs = flowDebugLogService.listByIds(logIds);
                Map<Long, FlowDebugLog> debugMap = debugLogs.stream().collect(Collectors.toMap(FlowDebugLog::getId, Function.identity(), (i1, i2) -> i1));
                page.getRecords().forEach(record -> {
                    FlowDebugLog debugLog = debugMap.get(record.getDebugLogId());
                    if (Objects.nonNull(debugLog)) {
                        String content = debugLog.getLogContent();
                        JSONObject jsonObject = JSON.parseObject(content);
                        if (Objects.nonNull(jsonObject)) {
                            JSONObject outputObject = jsonObject.getJSONObject("output");
                            if (Objects.nonNull(outputObject)) {
                                JSONObject assertObject = outputObject.getJSONObject("assertionStatistics");
                                if (Objects.nonNull(assertObject)) {
                                    record.setAssertTotalNum(assertObject.getInteger("totalCount"));
                                    record.setAssertSuccessNum(assertObject.getInteger("successCount"));
                                    record.setAssertFailNum(assertObject.getInteger("failCount"));
                                }
                            }
                        }
                    }
                });
            }
        }
        return page;
    }

    @Override
    public ParamBuilderRecord getOneById(Long id) {
        ParamBuilderRecord record = this.getById(id);
        if (Objects.nonNull(record)) {
            // 获取执行记录参数
            FlowDebugLog flowDebugLog = flowDebugLogService.getById(record.getDebugLogId());
            if (Objects.nonNull(flowDebugLog)) {
                record.setResultJson(flowDebugLog.getLogContent());
            }
        }
        return record;
    }

    @Override
    public List<ParamBuilderRecordDTO> getByIds(List<Long> ids) {
        Guard.notEmpty(CollectionUtils.isNotEmpty(ids), "入参不能为空");
        List<ParamBuilderRecordDTO> list = new ArrayList<>();
        List<ParamBuilderRecord> paramBuilderRecords = this.listByIds(ids);
        List<Long> paramBuilderIds = paramBuilderRecords.stream().map(ParamBuilderRecord::getParamBuilderId).distinct().collect(Collectors.toList());
        Map<Long, ParamBuilder> pbMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(paramBuilderIds)) {
            List<ParamBuilder> paramBuilderList = paramBuilderService.list(new LambdaQueryWrapper<ParamBuilder>().in(ParamBuilder::getId, paramBuilderIds));
            pbMap = paramBuilderList.stream().collect(Collectors.toMap(ParamBuilder::getId, Function.identity(), (i1, i2) -> i1));
        }

        Map<Long, MethodManage> methodMap = new HashMap<>(16);
        List<Long> methodIds = paramBuilderRecords.stream().map(ParamBuilderRecord::getMethodManageId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(methodIds)) {
            List<MethodManage> methodList = methodManageService.list(new LambdaQueryWrapper<MethodManage>().in(MethodManage::getId, methodIds));
            methodMap = methodList.stream().collect(Collectors.toMap(MethodManage::getId, Function.identity(), (i1, i2) -> i1));
        }

        Map<Long, ParamBuilder> finalPbMap = pbMap;
        Map<Long, MethodManage> finalMethodMap = methodMap;
        paramBuilderRecords.forEach(paramBuilderRecord -> {
            ParamBuilderRecordDTO dto = new ParamBuilderRecordDTO();
            dto.setId(paramBuilderRecord.getId());
            ParamBuilder builder = finalPbMap.get(paramBuilderRecord.getParamBuilderId());
            if (Objects.nonNull(builder)) {
                dto.setSceneName(builder.getSceneName());
            }
            // 解析出接口名称和方法名称
            String paramJson = builder.getParamJson();
            JSONObject jsonObject = JSON.parseObject(paramJson);
            if (Objects.nonNull(jsonObject)) {
                MethodManage methodManage = finalMethodMap.get(paramBuilderRecord.getMethodManageId());
                if (Objects.nonNull(methodManage)) {
                    if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType())) {
                        dto.setMethodType(InterfaceTypeEnum.HTTP.getCode());
                        dto.setUrl(jsonObject.getString("url"));
                    } else if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
                        dto.setMethodType(InterfaceTypeEnum.JSF.getCode());
                        dto.setInterfaceName(jsonObject.getString("interfaceName"));
                        dto.setMethodName(jsonObject.getString("methodName"));
                    }
                }
            }
            dto.setRunStatus(paramBuilderRecord.getRunStatus());
            dto.setRunMsg(paramBuilderRecord.getRunMsg());
            dto.setRunStatusDesc(RunStatusEnum.getDescByType(paramBuilderRecord.getRunStatus()));
            list.add(dto);
        });
        return list;
    }
}
