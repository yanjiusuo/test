package com.jd.workflow.console.service.param.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.RunStatusEnum;
import com.jd.workflow.console.controller.utils.DtBeanUtils;
import com.jd.workflow.console.dao.mapper.param.ParamBuilderMapper;
import com.jd.workflow.console.dto.flow.param.HttpOutputExt;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.console.dto.jsf.JsfDebugData;
import com.jd.workflow.console.dto.jsf.NewJsfDebugDto;
import com.jd.workflow.console.dto.jsf.SsoDto;
import com.jd.workflow.console.dto.requirement.AssertionStatisticsDTO;
import com.jd.workflow.console.dto.requirement.ParamBuilderAddParam;
import com.jd.workflow.console.dto.requirement.ParamBuilderDTO;
import com.jd.workflow.console.dto.requirement.ParamBuilderParam;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.param.ParamBuilder;
import com.jd.workflow.console.entity.param.ParamBuilderRecord;
import com.jd.workflow.console.service.DebugService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.debug.HttpDebugDataDto;
import com.jd.workflow.console.service.param.IParamBuilderRecordService;
import com.jd.workflow.console.service.param.IParamBuilderService;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 入参构建器 服务实现类
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
@Service
@Slf4j
public class ParamBuilderServiceImpl extends ServiceImpl<ParamBuilderMapper, ParamBuilder> implements IParamBuilderService {

    /**
     *
     */
    @Resource
    IParamBuilderRecordService recordService;

    /**
     *
     */
    @Resource
    DebugService debugService;

    @Autowired
    private IMethodManageService methodManageService;

    static ParametersUtils utils = new ParametersUtils();
    
    @Override
    public void run(Long id, String cookie) {
        ParamBuilder pb = getById(id);
        Guard.notNull(pb, "用例不存在");
        ParamBuilderRecord paramBuilderRecord = new ParamBuilderRecord();
        paramBuilderRecord.setParamBuilderId(id);
        paramBuilderRecord.setCreator(UpLoginContextHelper.getUserPin());
        paramBuilderRecord.setMethodManageId(pb.getMethodManageId());
        paramBuilderRecord.setRunStatus(RunStatusEnum.WAIT.getType());
        recordService.save(paramBuilderRecord);
        // 更新cookie
        updateCookie(pb, cookie);
    }

    @Override
    public void batchRun(String ids, String cookie) {
        String[] idArray = ids.split(",");
        Guard.notEmpty(idArray, "入参不能为空");
        for (String id : idArray) {
            run(Long.valueOf(id), cookie);
        }
    }

    @Override
    public Page listPage(ParamBuilderParam param) {
        Page<ParamBuilder> page = this.lambdaQuery().eq(ParamBuilder::getYn, DataYnEnum.VALID.getCode())
                .eq(Objects.nonNull(param.getMethodManageId()), ParamBuilder::getMethodManageId, param.getMethodManageId())
                .like(Objects.nonNull(param.getSceneName()), ParamBuilder::getSceneName, param.getSceneName())
                .orderByDesc(ParamBuilder::getCreated)
                .page(new Page(param.getCurrent(), param.getSize()));
        if(CollectionUtil.isEmpty(page.getRecords())){
            return page;
        }
        Page<ParamBuilderDTO> dtoPage = calExt(page);
        return dtoPage;
    }

    /**
     * 计算额外字段
     * @param page
     * @return
     */
    private Page<ParamBuilderDTO> calExt(Page<ParamBuilder> page) {
        List<Long> paramBuilderIdList = page.getRecords().stream().map(ParamBuilder::getId).collect(Collectors.toList());
        List<ParamBuilderRecord> list = recordService.lambdaQuery().select(ParamBuilderRecord::getId, ParamBuilderRecord::getParamBuilderId)
                .eq(ParamBuilderRecord::getYn, DataYnEnum.VALID.getCode()).in(ParamBuilderRecord::getParamBuilderId, paramBuilderIdList).list();
        Map<Long, List<ParamBuilderRecord>> map = list.stream().collect(Collectors.groupingBy(ParamBuilderRecord::getParamBuilderId));
        List<ParamBuilderDTO> pbdList = Lists.newArrayList();
        for (ParamBuilder record : page.getRecords()) {
            ParamBuilderDTO paramBuilderDTO = new ParamBuilderDTO();
            BeanUtils.copyProperties(record, paramBuilderDTO);
            paramBuilderDTO.setTotal(Objects.isNull(map.get(record.getId()))?0:map.get(record.getId()).size());
            pbdList.add(paramBuilderDTO);
        }
        Page<ParamBuilderDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(page, dtoPage);
        dtoPage.setRecords(pbdList);
        return dtoPage;
    }

    @Override
    public boolean saveCase(ParamBuilderAddParam param, String cookie) {
        Guard.notEmpty(param.getParamJson(), "入参不能为空");
        // 名称必填
        Guard.notEmpty(param.getSceneName(), "请输入用例名称");
        // 字符校验
        Guard.assertTrue(param.getSceneName().length() < 50, "请输入1~50位字符");
        // 查询方法下是否存在相同名称的用例
        LambdaQueryWrapper<ParamBuilder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ParamBuilder::getMethodManageId, param.getMethodManageId())
                .eq(ParamBuilder::getYn, DataYnEnum.VALID.getCode())
                .eq(ParamBuilder::getSceneName, param.getSceneName());
        int sameNameCount = this.count(lqw);
        Guard.assertTrue(sameNameCount == 0, "用例名称已存在");
        // 保存
        Map paramJsonMap = JsonUtils.parse(param.getParamJson(), Map.class);
        paramJsonMap.put("Cookie", cookie);
        param.setParamJson(JsonUtils.toJSONString(paramJsonMap));
        ParamBuilder paramBuilder = DtBeanUtils.getCreatBean(param, ParamBuilder.class);
        return this.save(paramBuilder);
    }

    @Override
    public boolean logicDelete(Long id) {
        ParamBuilder paramBuilder = new ParamBuilder();
        paramBuilder.setId(id);
        paramBuilder.setYn(DataYnEnum.INVALID.getCode());
        return this.updateById(paramBuilder);
    }

    @Override
    public long copy(Long id) {
        ParamBuilder paramBuilder = this.getById(id);
        Guard.assertTrue(Objects.nonNull(paramBuilder), "复制失败，用例不存在");
        ParamBuilder newParamBuilder = new ParamBuilder();
        // 复制名称
        String newName = paramBuilder.getSceneName() + "-副本";
        Optional<ParamBuilder> existingRecord = this.findBySceneName(newName, paramBuilder.getMethodManageId());
        int counter = 1;
        while (existingRecord.isPresent()) {
            newName = paramBuilder.getSceneName() + "-副本" + counter++;
            existingRecord = this.findBySceneName(newName, paramBuilder.getMethodManageId());
        }
        newParamBuilder.setSceneName(newName);
        newParamBuilder.setParamJson(paramBuilder.getParamJson());
        newParamBuilder.setMethodManageId(paramBuilder.getMethodManageId());
        newParamBuilder.setCreator(UpLoginContextHelper.getUserPin());
        newParamBuilder.setModifier(UpLoginContextHelper.getUserPin());
        boolean flag = this.save(newParamBuilder);
        return flag ? newParamBuilder.getId() : 0;
    }

    private Optional<ParamBuilder> findBySceneName(String name, Long methodManageId) {
        return this.lambdaQuery().eq(ParamBuilder::getSceneName, name)
                .eq(ParamBuilder::getMethodManageId, methodManageId)
                .eq(ParamBuilder::getYn, DataYnEnum.VALID.getCode())
                .oneOpt();
    }

    @Override
    public List<ParamBuilder> listByMethodIds(String methodIds) {
        Guard.notEmpty(methodIds, "参数有误");
        String[] methodIdArr = methodIds.split(",");
        Guard.assertTrue(methodIdArr.length > 0, "参数有误");
        return this.lambdaQuery().in(ParamBuilder::getMethodManageId, methodIdArr)
                .eq(ParamBuilder::getYn, DataYnEnum.VALID.getCode())
                .list();
    }

    @Override
    public ParamBuilderRecord run(Long id, String ip, String alias, HttpDebugDto dto) {
        // 获取用例
        ParamBuilder paramBuilder = getById(id);
        Guard.notEmpty(paramBuilder, "用例不存在");
        // 获取方法信息
        MethodManage methodManage = methodManageService.getById(paramBuilder.getMethodManageId());
        Guard.notEmpty(methodManage, "用例对应的方法不存在");
        if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType())) {
            return runForHttp(id, dto, paramBuilder);
        } else if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
            return runForJsf(id, ip, alias, paramBuilder);
        }
        return null;
    }

    @Override
    public boolean update(ParamBuilderAddParam param, String cookie) {
        Map jsonMap = JsonUtils.parse(param.getParamJson(), Map.class);
        jsonMap.put("Cookie", cookie);
        param.setParamJson(JsonUtils.toJSONString(jsonMap));
        ParamBuilder paramBuilder = DtBeanUtils.getUpdateBean(param, ParamBuilder.class);
        return this.updateById(paramBuilder);
    }

    private ParamBuilderRecord runForJsf(Long id, String ip, String alias, ParamBuilder paramBuilder) {
        ParamBuilderRecord paramBuilderRecord = new ParamBuilderRecord();
        paramBuilderRecord.setParamBuilderId(id);
        paramBuilderRecord.setCreator(StringUtils.isEmpty(UpLoginContextHelper.getUserPin()) ? "System" : UpLoginContextHelper.getUserPin());
        paramBuilderRecord.setMethodManageId(paramBuilder.getMethodManageId());
        NewJsfDebugDto dto = new NewJsfDebugDto();
        try {
            dto = JsonUtils.parse(paramBuilder.getParamJson(), NewJsfDebugDto.class);
            dto.setAlias(alias);
            dto.setIp(ip);
            JsfOutputExt result = debugService.debugJsfNew(dto);
            paramBuilderRecord.setRunStatus(result.isSuccess() ? RunStatusEnum.SUCCESS.getType() : RunStatusEnum.FAIL.getType());
            paramBuilderRecord.setDebugLogId(result.getLogId());
            // 获取断言信息
            AssertionStatisticsDTO assertionStatisticsDTO = result.getAssertionStatistics();
            if (Objects.nonNull(assertionStatisticsDTO)) {
                if (!assertionStatisticsDTO.isRes()) {
                    log.info("runForJsf 执行断言失败，入参dto：{},出参：{}", JsonUtils.toJSONString(dto), JsonUtils.toJSONString(result));
                    paramBuilderRecord.setRunStatus(RunStatusEnum.FAIL.getType());
                }
                if (assertionStatisticsDTO.getTotalCount() > 0) {
                    paramBuilderRecord.setRunMsg(assertionStatisticsDTO.getTotalCount() + "条断言，" + assertionStatisticsDTO.getSuccessCount() + "条成功，" + assertionStatisticsDTO.getFailCount() + "条失败");
                }
            }
        } catch (Exception e) {
            // 截取异常信息的前200个字符
            String exceptionSummary = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 200)) : "无详细异常信息";
            JsfDebugData jsfDebugData = new JsfDebugData();
            jsfDebugData.setInput(dto);
            paramBuilderRecord.setRunMsg(exceptionSummary);
            paramBuilderRecord.setResultJson(JsonUtils.toJSONString(jsfDebugData));
            paramBuilderRecord.setRunStatus(RunStatusEnum.FAIL.getType());
            log.error("调用runForJsf方法异常:", e);
        }

        boolean flag = recordService.save(paramBuilderRecord);
        if (flag) {
            ParamBuilderRecord record = new ParamBuilderRecord();
            record.setId(paramBuilderRecord.getId());
            record.setRunStatus(paramBuilderRecord.getRunStatus());
            record.setRunStatusDesc(RunStatusEnum.getDescByType(paramBuilderRecord.getRunStatus()));
            return record;
        }
        return null;
    }

    private ParamBuilderRecord runForHttp(Long id, HttpDebugDto dto, ParamBuilder paramBuilder) {
        ParamBuilderRecord paramBuilderRecord = new ParamBuilderRecord();
        paramBuilderRecord.setParamBuilderId(id);
        paramBuilderRecord.setCreator(StringUtils.isEmpty(UpLoginContextHelper.getUserPin()) ? "System" : UpLoginContextHelper.getUserPin());
        paramBuilderRecord.setMethodManageId(paramBuilder.getMethodManageId());
        HttpDebugDto dtoNew = new HttpDebugDto();
        try {
            // 将入参转换为快捷调用所需的HttpDebugDto
            dtoNew = JsonUtils.parse(paramBuilder.getParamJson(), HttpDebugDto.class);
            // dto中的数据需替换dtoNew中部分数据
            // 站点环境
            dtoNew.setSite(dto.getSite());
            dtoNew.setEnvName(dto.getEnvName());
            // 前置url
            dtoNew.setTargetAddress(dto.getTargetAddress());
            // headers、params、path
            dtoNew.getInput().setHeaders(mergeLists(dtoNew.getInput().getHeaders(), dto.getInput().getHeaders()));
            dtoNew.getInput().setParams(mergeLists(dtoNew.getInput().getParams(), dto.getInput().getParams()));
            dtoNew.getInput().setPath(mergeLists(dtoNew.getInput().getPath(), dto.getInput().getPath()));
            // 登陆认证
            if (isSsoValid(dto.getSso())) {
                dtoNew.setSso(dto.getSso());
            }

            // cookie
            String onlineCookie;
            Map paramJsonMap = JsonUtils.parse(paramBuilder.getParamJson(), Map.class);
            if ("China".equals(dto.getSite()) && Objects.nonNull(paramJsonMap.get("Cookie"))) {
                onlineCookie = paramJsonMap.get("Cookie").toString();
            } else {
                onlineCookie = null;
            }
            HttpOutputExt result = debugService.debugHttp(dtoNew, onlineCookie);
            paramBuilderRecord.setRunStatus(result.isSuccess() ? RunStatusEnum.SUCCESS.getType() : RunStatusEnum.FAIL.getType());
            paramBuilderRecord.setDebugLogId(result.getLogId());
            // 获取断言信息
            AssertionStatisticsDTO assertionStatisticsDTO = result.getAssertionStatistics();
            if (Objects.nonNull(assertionStatisticsDTO)) {
                if (!assertionStatisticsDTO.isRes()) {
                    log.info("runForHttp 执行断言失败，入参dto：{},出参：{}", JsonUtils.toJSONString(dto), JsonUtils.toJSONString(result));
                    paramBuilderRecord.setRunStatus(RunStatusEnum.FAIL.getType());
                }
                if (assertionStatisticsDTO.getTotalCount() > 0) {
                    paramBuilderRecord.setRunMsg(assertionStatisticsDTO.getTotalCount() + "条断言，" + assertionStatisticsDTO.getSuccessCount() + "条成功，" + assertionStatisticsDTO.getFailCount() + "条失败");
                }
            }
        } catch (Exception e) {
            // 截取异常信息的前200个字符
            String exceptionSummary = e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 200)) : "无详细异常信息";
            HttpDebugDataDto debugDataDto = new HttpDebugDataDto();
            debugDataDto.setSite(dtoNew.getSite());
            debugDataDto.setInput(buildHttpInput(dtoNew));
            paramBuilderRecord.setRunMsg(exceptionSummary);
            paramBuilderRecord.setResultJson(JsonUtils.toJSONString(debugDataDto));
            paramBuilderRecord.setRunStatus(RunStatusEnum.FAIL.getType());
            log.error("调用runForHttp方法异常:", e);
        }
        boolean flag = recordService.save(paramBuilderRecord);
        if (flag) {
            ParamBuilderRecord record = new ParamBuilderRecord();
            record.setId(paramBuilderRecord.getId());
            record.setRunStatus(paramBuilderRecord.getRunStatus());
            record.setRunStatusDesc(RunStatusEnum.getDescByType(paramBuilderRecord.getRunStatus()));
            return record;
        }
        return null;
    }

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

    /**
     * 合并两个List
     * @param oldList
     * @param newList
     * @param <T>
     * @return
     */
    private <T extends JsonType> List<T> mergeLists(List<T> oldList, List<T> newList) {
        if (CollectionUtil.isEmpty(newList)) {
            return oldList;
        }
        List<T> result = new ArrayList<>(newList);
        if (oldList != null) {
            for (T oldItem : oldList) {
                if (newList.stream().noneMatch(newItem -> oldItem.getName().equals(newItem.getName()))) {
                    result.add(oldItem);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param paramBuilder
     * @param cookie
     */
    private void updateCookie(ParamBuilder paramBuilder, String cookie) {
        Map paramJsonMap = JsonUtils.parse(paramBuilder.getParamJson(), Map.class);
        paramJsonMap.put("Cookie", cookie);
        LambdaUpdateWrapper<ParamBuilder> luw = new LambdaUpdateWrapper<>();
        luw.set(ParamBuilder::getParamJson, JsonUtils.toJSONString(paramJsonMap))
                .eq(ParamBuilder::getId, paramBuilder.getId());
        this.update(luw);
    }

    /**
     * 校验sso
     * @param sso
     * @return
     */
    private boolean isSsoValid(SsoDto sso) {
        return sso != null && StringUtils.isNotBlank(sso.getUser()) && StringUtils.isNotBlank(sso.getPwd());
    }

}
