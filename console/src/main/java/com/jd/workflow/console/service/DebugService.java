package com.jd.workflow.console.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jd.common.util.StringUtils;
import com.jd.jsf.open.api.domain.Server;
import com.jd.neptune.painter.annotations.UMP;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.DataContextProvider;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.flow.param.HttpOutputExt;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.flow.param.QueryParamQuoteReqDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamQuoteResultDTO;
import com.jd.workflow.console.dto.jsf.*;
import com.jd.workflow.console.dto.requirement.AssertionResultDTO;
import com.jd.workflow.console.dto.requirement.AssertionStatisticsDTO;
import com.jd.workflow.console.entity.FlowParamQuote;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.service.debug.FlowDebugLogService;
import com.jd.workflow.console.service.debug.HttpDebugDataDto;
import com.jd.workflow.console.service.debug.JsfJarRecordService;
import com.jd.workflow.console.service.debug.StepDebugLogService;
import com.jd.workflow.console.service.debug.impl.DefaultJsfCallService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.service.param.IParamBuilderOptService;
import com.jd.workflow.console.service.plugin.JsfOpenService;
import com.jd.workflow.console.service.remote.MockDataBuildService;
import com.jd.workflow.console.utils.ColorSignMaker;
import com.jd.workflow.console.utils.JfsUtils;
import com.jd.workflow.console.utils.RpcUtils;
import com.jd.workflow.console.utils.TestSsoCookieHelper;
import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.definition.WorkflowInputDefinition;
import com.jd.workflow.flow.core.definition.WorkflowParam;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.metadata.impl.WebServiceStepMetadata;
import com.jd.workflow.flow.core.output.BaseOutput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.impl.Http2WsStepProcessor;
import com.jd.workflow.flow.core.processor.subflow.CamelSubflowProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.flow.utils.PseudoBuildUtils;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.jsf.enums.JsfRegistryEnvEnum;
import com.jd.workflow.jsf.input.JsfOutput;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.jsf.processor.JsfProcessor;
import com.jd.workflow.soap.common.Md5Utils;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprNodeUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DebugService {
    static ParametersUtils utils = new ParametersUtils();
    @Resource
    IFlowService flowService;

    @Autowired
    FlowDebugLogService flowDebugLogService;

    @Autowired
    MockDataBuildService mockDataBuildService;

    @Autowired
    StepDebugLogService stepDebugLogService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IFlowParamQuoteService flowParamQuoteService;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    JsfOpenService jsfOpenService;

    @Autowired
    DefaultJsfCallService defaultJsfCallService;


    @Autowired
    private JfsUtils jfsUtils;
    @Autowired
    JsfJarRecordService jsfJarRecordService;

    @Autowired
    private IMeasureDataService measureDataService;

    @Resource
    IParamBuilderOptService paramBuilderOptService;

    @Value("${sso.env}")
    private String ssoEnv;


    /**
     * 1==本地ip 2==非本地ip
     */
    private static ConcurrentHashMap<String,Integer> localJsfIp=new ConcurrentHashMap<String,Integer>();

    public Object getReqBodyDemoValue(Long methodId) {
        return methodManageService.getMethodReqBodyDemoValue(methodId);
    }

    public Object getMethodDemoOrHistoryValue(Long id,Integer tag) {
        Guard.notEmpty(id, "无效的方法id");
        MethodManage method = methodManageService.getById(id);
        Guard.notEmpty(method, "无效的方法id");
        methodManageService.initContentObject(method);
        InterfaceManage interfaceManage = interfaceManageService.getById(method.getInterfaceId());
        if(interfaceManage != null && interfaceManage.getAppId() != null){
            methodManageService.initMethodRefAndDelta(Collections.singletonList(method),interfaceManage.getAppId());
        }

        if (InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())) {
            HttpDebugDataDto data = mockDataBuildService.buildEmptyHttplue(method);
            HttpMethodModel httpMethodModel = (HttpMethodModel) method.getContentObject();
            fillHttpLogValue(data,method,tag, httpMethodModel.getInput().getReqType());
            return data;
        } else if (InterfaceTypeEnum.JSF.getCode().equals(method.getType())) {
            JsfStepMetadata stepMetadata = (JsfStepMetadata) method.getContentObject();
            FlowDebugLog flowDebugLog = flowDebugLogService.getUserNewestDebugLog(id,tag);
            if(flowDebugLog == null){ // 调试记录为空的话，为了方便取上一次调试记录
                flowDebugLog = flowDebugLogService.getUserNewestDebugLog(id,tag,null);
            }
            if(flowDebugLog != null){
                JsfDebugData jsfLog = JsonUtils.parse(flowDebugLog.getLogContent(), JsfDebugData.class);
                if(jsfLog.getInput()!=null && jsfLog.getInput().getMavenLocation() == null){
                    initJarCallerDefaultValue(jsfLog,stepMetadata,interfaceManage);
                }
                return jsfLog;
            }
            JsfDebugData data = mockDataBuildService.buildEmptyJsfValue(stepMetadata);
            initJarCallerDefaultValue(data,stepMetadata,interfaceManage);
            return data;
        }
        return null;
    }
    private void initJarCallerDefaultValue(JsfDebugData data,JsfStepMetadata stepMetadata,InterfaceManage interfaceManage){
        data.getInput().setMavenLocation(MavenJarLocation.from(interfaceManage.getPath()));
        List inputData = stepMetadata.getInput().stream().map(jsonType -> mockDataBuildService.buildJarJsfCallEmptyValue(jsonType)).collect(Collectors.toList());
        data.getInput().setJarInputData(JsonUtils.toJSONString(inputData));
        data.getInput().setInterfaceName(interfaceManage.getServiceCode());
    }

    //跟文档内容比较，key以文档为标准
    /*private void fillJsfLogValue(JsfDebugData needFillData,Long id,Integer tag){

        //MethodManage method = methodManageService.getById(id);
        if (flowDebugLog != null) {
            JsfDebugData jsfLog = JsonUtils.parse(flowDebugLog.getLogContent(), JsfDebugData.class);
            if (null != jsfLog   ) {
                 needFillData.setInput(jsfLog.getInput());

                *//*if (!CollectionUtils.isEmpty(needFillData.getInput().getInput()) && CollectionUtils.isEmpty(jsfLog.getInput().getInput())) {
                    List<? extends JsonType> logBody = jsfLog.getInput().getInput();
                    List<? extends JsonType> jsfParam = needFillData.getInput().getInput();
                    Map<String, JsonType> jsfMap = jsfParam.stream().collect(Collectors.toMap(JsonType::getName, JsonType -> JsonType, (i1, i2) -> i1));
                    List<String> docBodyKeys = jsfParam.stream().map(JsonType::getName).collect(Collectors.toList());
                    for (JsonType logjsf : logBody) {
                        if (docBodyKeys.contains(logjsf.getName())) {
                            jsfMap.get(logjsf.getName()).setValue(logjsf.getValue());
                        }
                    }
                    needFillData.getInput().setInput((List<? extends JsonType>) jsfMap.values());
                }*//*
            }
        }
    }*/

    //跟文档内容比较，key以文档为标准
    private void fillHttpLogValue(HttpDebugDataDto httpData, MethodManage method, Integer tag, String reqType){
        FlowDebugLog flowDebugLog = flowDebugLogService.getUserNewestDebugLog(method.getId(),tag);

        if (flowDebugLog != null) {
            if (InterfaceTypeEnum.HTTP.getCode().equals(method.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())) {
                HttpDebugDataDto logData = JsonUtils.parse(flowDebugLog.getLogContent(), HttpDebugDataDto.class);
                if (null != logData) {
                    httpData.getInput().setHeaders(logData.getInput().getHeaders());
                    httpData.getInput().setTargetAddress(logData.getInput().getTargetAddress());
                    httpData.getInput().setPath(logData.getInput().getPath());
                    httpData.getInput().setBody(logData.getInput().getBody());
                    //color 参数初始化
                    if(null!=tag&&tag==1&&ReqType.form.name().equals(reqType)){
                        colorFormParam(httpData,logData);
                    }else{
                        if (MapUtils.isNotEmpty(httpData.getInput().getParams()) && MapUtils.isNotEmpty(logData.getInput().getParams())) {
                            Set<String> docKeys = httpData.getInput().getParams().keySet();
                            for (Map.Entry<String, Object> logRecord : logData.getInput().getParams().entrySet()) {
                                if (docKeys.contains(logRecord.getKey())) {
                                    httpData.getInput().getParams().put(logRecord.getKey(), logRecord.getValue());
                                }
                            }
                        }

                        /*if (!ObjectUtils.isEmpty(logData.getInput().getBody()) && !ObjectUtils.isEmpty(httpData.getInput().getBody())) {
                            Map<String,Object> logBody = JsonUtils.objectToMap(logData.getInput().getBody());
                            Map<String,Object> httpBody = JsonUtils.objectToMap(httpData.getInput().getBody());
                            Set<String> docBodyKeys = httpBody.keySet();
                            for (Map.Entry<String, Object> logBodyE : logBody.entrySet()) {
                                if (docBodyKeys.contains(logBodyE.getKey())) {
                                    httpBody.put(logBodyE.getKey(), logBodyE.getValue());
                                }
                            }
                            httpData.getInput().setBody(httpBody);
                        }*/
                    }
                }
            }
        }

    }

    private void colorFormParam(HttpDebugDataDto httpData, HttpDebugDataDto logData) {
        if (null != httpData.getInput().getBody()) {
            Map<String, Object> httpBody = JsonUtils.objectToMap(httpData.getInput().getBody());
            httpBody.put("body", JsonUtils.toJSONString(logData.getInput().getBody()));
            Map<String, Object> params = logData.getInput().getParams();
            if (MapUtils.isNotEmpty(params)) {
                //key值以接口文档为准
                List<String> docBodyKeys = httpBody.keySet().stream().filter(i->!i.equals("body")).collect(Collectors.toList());
                for (Map.Entry<String, Object> logBodyE : params.entrySet()) {
                    if (docBodyKeys.contains(logBodyE.getKey())) {
                        httpData.getInput().getParams().put(logBodyE.getKey(), logBodyE.getValue());
                        httpBody.put(logBodyE.getKey(), logBodyE.getValue());
                    }
                }
            }
            httpData.getInput().setBody(httpBody);
        }
    }

    public void initValidParam(HttpDebugDto dto){
        String sign ="";
        if (!CollectionUtils.isEmpty(dto.getInput().getParams())) {
            Map<String, String> maps = dto.getInput().getParams().stream().filter(item -> item.getType().equals("array") && item.getType().equals("object"))
                    .collect(Collectors.toMap(i -> i.getName(), i -> i.getValue().toString(), (i1, i2) -> i1));
            sign = ColorSignMaker.generateSign(maps, "secret");
        }
        List<JsonType> jsonParam=dto.getInput().getColorInputParam();
        Boolean containSign = false;
        for (JsonType param : jsonParam) {
            if (param.getName().equals("sign")) {
                param.setValue(sign);
                containSign = true;
            }
            if (param.getName().equals("functionId")) {
                Guard.notEmpty(param.getValue(), "functionId不能为空");
            }
            if (param.getName().equals("appid")) {
                Guard.notEmpty(param.getValue(), "appid不能为空");
            }
        }
        if (!containSign) {
            JsonType signParam = new SimpleJsonType();
            signParam.setValue(sign);
            signParam.setName("sign");
            jsonParam.add(signParam);
        }
    }


    /**
     * 异常回调
     * @param e
     * @return
     */
    public HttpOutputExt debugHttpFallBack(Throwable e,HttpDebugDto dto, String onlineCookie) {
        log.error("测试ump异常回调");
        e.printStackTrace();
        return new HttpOutputExt();
    }

    /**
     *
     * @param dto
     * @param onlineCookie
     * @return
     */
    @UMP(onException = "debugHttpFallBack")
    public HttpOutputExt debugHttp(HttpDebugDto dto, String onlineCookie) {
        String hostIp = "";
        String host = "";
        HttpInput input = buildHttpInput(dto, onlineCookie);
        String address = dto.getTargetAddress();
        HttpOutputExt result = new HttpOutputExt();
        // 2、前置操作&入参渲染
        DataContextProvider.getContext().setParamDepParse(dto.getParamDep());
        paramBuilderOptService.preOpt(dto.getPreOpt());
        if (Objects.nonNull(dto.getInput().getBodyData())) {
            if(!"{}".equalsIgnoreCase(JsonUtils.toJSONString(dto.getInput().getBodyData()))){
                result.setParam(dto.getInput().getBodyData());
                String render = paramBuilderOptService.renderParam(JsonUtils.toJSONString(dto.getInput().getBodyData()));
                dto.getInput().setBodyData(render);
                result.setRender(render);
            }
        }
        if (!CollectionUtils.isEmpty(dto.getInput().getParams())) {
            result.setParam(dto.getInput().getParams());
            List<ParamDepDto> paramDepDtos = paramBuilderOptService.hitParamDep(0L, ParamOptTypeEnum.other,ParamOptPositionEnum.other);
            Map<String, ParamDepDto> queryMap = paramDepDtos.stream().filter(item -> "query".equalsIgnoreCase(item.getKey().split("#")[2])).collect(Collectors.toMap(i -> i.getKey().split("#")[3],
                    Function.identity(), (i1, i2) -> i2));
            for (JsonType param : dto.getInput().getParams()) {
                String renderParam = paramBuilderOptService.renderParam(param.getValue().toString());
                param.setValue(renderParam);
                if(Objects.nonNull(queryMap.get(param.getName()))){
                    param.setValue(queryMap.get(param.getName()).getValue());
                }
            }
            result.setRender(dto.getInput().getParams());

        }
        if (!CollectionUtils.isEmpty(dto.getInput().getPath())) {
            result.setParam(dto.getInput().getPath());
            for (JsonType path : dto.getInput().getPath()) {
                String renderParam = paramBuilderOptService.renderParam(path.getValue().toString());
                path.setValue(renderParam);
            }
            result.setRender(dto.getInput().getPath());
        }
        // 3、实际调用
        HttpOutput out = new HttpOutput();
        // 3.1 接口测试环境转发 ---当前为测试机器环境不需要转发
        boolean isTestMachine = "dev".equals(ssoEnv) || "test".equals(ssoEnv);
        if ("test".equals(dto.getSite()) && !isTestMachine) {
            transferCallTestHttp(dto, out);
        } else {

            address = resolveTargetAddress(dto, input);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(hostIp)) {
                host = RpcUtils.getHostFromUrl(address);
                address = address.replace(host, hostIp);
                log.info("新url:{}。hostName:{}. System.getProperty:{}", address, hostIp, System.getProperty("sun.net.http.allowRestrictedHeaders"));
                if (Objects.isNull(input.getHeaders())) {
                    input.setHeaders(Maps.newHashMap());
                    input.getHeaders().put(HttpHeaders.HOST, host);

                } else {
                    input.getHeaders().put(HttpHeaders.HOST, host);
                }
            }
            if (ReqType.file.equals(input.getReqType())) {
                out = RpcUtils.callHttpFile(dto, address, host);
            } else {
                out = RpcUtils.callHttp(input, address, host);
            }
        }
        paramBuilderOptService.replaceValue(0L, out.getBody(), ParamOptPositionEnum.other);
        BeanUtils.copyProperties(out, result);

        //4、后置操作&断言
        paramBuilderOptService.postOpt(dto.getPostOpt());
        List<AssertionResultDTO> assertionResult = paramBuilderOptService.assertionOpt(dto.getPostOpt(), out);
        result.setAssertionResult(assertionResult);
        long total = assertionResult.stream().count();
        long successCount = assertionResult.stream().filter(AssertionResultDTO::isResult).count();
        result.setAssertionStatistics(new AssertionStatisticsDTO(total == successCount, total, total - successCount, successCount));

        log.info("http debug 日志阶段 input {}", JsonUtils.toJSONString(input));
        HttpDebugDataDto.Input inputData = buildInputData(dto, input);
        inputData.setTargetAddress(address);

        FlowDebugLog log = new FlowDebugLog();
        HttpDebugDataDto debugDataDto = new HttpDebugDataDto();
        debugDataDto.setSite(dto.getSite());
        HttpDebugDataDto.Output output = new HttpDebugDataDto.Output();
        output.setHeaders(result.getHeaders());
        output.setBody(result.getBody());
        output.setException(result.getException());
        debugDataDto.setInput(inputData);
        debugDataDto.setOutput(output);
        debugDataDto.setDto(dto);
        // 冗余 todo 去掉
        debugDataDto.setType("http");
        log.setMethodTag(null != dto.getIsColor() && dto.getIsColor() ? 1 : 0);
        log.setSite(dto.getSite());
        log.setEnvName(dto.getEnvName());
        log.setDigest(getHttpDigest(debugDataDto));
        log.setSuccess(result.isSuccess() ? 1 : 0);
        log.setMethodId(dto.getMethodId());
        log.setLogContent(JsonUtils.toJSONString(debugDataDto));
        log.setYn(1);
        result.setDebugContent(log);
        Long id = saveOrUpdateLog(log);
        result.setLogId(id);
        result.setStepMsg(DataContextProvider.getContext().getParamStepMsgs());
        // 【指标度量】快捷调用http
        measureDataService.saveQuickCallLog(MeasureDataEnum.QUICK_CALL_HTTP.getCode(), log);
        return result;
    }

    /**
     * 注意，调用日志中 inputData 不做填充
     * 把color-header添加到header，把业务参数填充到color网关请求的body中
     * @param input
     */
    private void fillColorParamsAndHeader(HttpInput input,HttpDebugDto dto) {
        //color 接口业务参数 填充到body中
        //color 请求参数 url中参数包含colorInputParam+param+body（form场景下页面输入的body）
        //color 请求参数 form场景下，页面body参数按照url传递，填充到param中
        List<JsonType> colorInputParam=dto.getInput().getColorInputParam();
            if(ReqType.form.name().equals(input.getReqType())){
                if (null!=dto.getInput().getBodyData()) {
                    JSONObject param= (JSONObject) JSONObject.toJSON(input.getBody());
                    input.getParams().putAll(param);
                }
        }
        if (null == input.getParams()) {
            input.setParams(utils.buildInput(colorInputParam));
        } else {
            input.getParams().putAll(utils.buildInput(colorInputParam));
        }
        input.setHeaders(utils.buildInput(dto.getInput().getColorHeaders()));
        //color接口设置完成
    }

    private String checkHaveFile(HttpDebugDto dto) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dto.getInput().getParams())) {
            for (JsonType param : dto.getInput().getParams()) {
                if ("file".equals(param.getType())) {
                    return param.getValue().toString();
                }
            }
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dto.getInput().getBody())) {
            for (JsonType param : dto.getInput().getBody()) {
                if ("file".equals(param.getType())) {
                    return param.getValue().toString();
                }
            }
        }

        return "";

    }

    private Long saveOrUpdateLog(FlowDebugLog log) {
       return flowDebugLogService.saveOrUpdateLogByDigest(log);
    }

    private String getJsfDigest(JsfDebugData dto) {
        String params = JsonUtils.toJSONString(dto);
        return Md5Utils.md5(params);
    }

    private String getHttpDigest(HttpDebugDataDto dto) {
        String params = JsonUtils.toJSONString(dto);
        return Md5Utils.md5(params);
    }

    private String getAppName(Server server){
        if(server.getAppName()!=null){
            return server.getAppName();
        }
        String attrUrl = server.getAttrUrl();
        if(attrUrl!=null){
            String[] strs = attrUrl.split(",");
            for (String str : strs) {
                String[]  attrNameAndValue = StringUtils.split(str.trim(), '=');
                if(attrNameAndValue.length==2&&"appName".equals(attrNameAndValue[0])){
                    return attrNameAndValue[1];
                }
            }
        }
        return "";
    }

    /**
     * JSF快捷调用
     * 0522 增加前置后置操作
     * @param dto
     * @return
     */
    public JsfOutputExt debugJsfNew(NewJsfDebugDto dto) {
        JsfOutputExt result = new JsfOutputExt();
        Long start = System.currentTimeMillis();
        // 1、本地环境判断
        setLocalEnv(dto);
        if(Objects.nonNull(dto.getPreOpt())){
            dto.getPreOpt().sort(Comparator.comparing(ParamOptDto::getSort));
        }
        if(Objects.nonNull(dto.getPostOpt())){
            dto.getPostOpt().sort(Comparator.comparing(ParamOptDto::getSort));
        }
        // 2、前置操作&入参渲染
        DataContextProvider.getContext().setParamDepParse(dto.getParamDep());
        paramBuilderOptService.preOpt(dto.getPreOpt());
        if(JsfCallType.jar.equals(dto.getCallType())){
            result.setParam(dto.getJarInputData());
            String render = paramBuilderOptService.renderParam(dto.getJarInputData());
            dto.setJarInputData(render);
            result.setRender(render);
        }
        else {
            result.setParam(dto.getInputData());
            if(!CollectionUtils.isEmpty(dto.getInputData())){
                String render = paramBuilderOptService.renderParam(JsonUtils.toJSONString(dto.getInputData()));
                List<Object> renderList = JsonUtils.parseArray(render, Object.class);
                dto.setInputData(renderList);
                result.setRender(renderList);
            }
        }
        // 3、实际调用
        // 3.1 接口测试环境转发 ---当前为测试机器环境不需要转发
        boolean isTestMachine = "dev".equals(ssoEnv) || "test".equals(ssoEnv);
        JsfOutput output = new JsfOutput();
        if ((JsfRegistryEnvEnum.local.name().equals(dto.getEnv())
                ||JsfRegistryEnvEnum.test.name().equals(dto.getEnv()))
                && !isTestMachine) {
            transferCallTest(dto, output);
        }else {
            if(JsfCallType.jar.equals(dto.getCallType())){
                JarJsfDebugDto jsfJarDto = new JarJsfDebugDto();
                BeanUtils.copyProperties(dto,jsfJarDto);
                jsfJarDto.setInputData(dto.getJarInputData());
                jsfJarDto.setLocation(dto.getMavenLocation());
                output =defaultJsfCallService.jarCallJsf(jsfJarDto);
            }else{
                output = RpcUtils.callJsf(dto);
            }
        }
        // todo 方法接收 object
        paramBuilderOptService.replaceValue(0L, output.getBody(), ParamOptPositionEnum.other);

        //4、后置操作&断言
        paramBuilderOptService.postOpt(dto.getPostOpt());
        List<AssertionResultDTO> assertionResult = paramBuilderOptService.assertionOpt(dto.getPostOpt(), output);
        result.setAssertionResult(assertionResult);
        long total = assertionResult.stream().count();
        long successCount = assertionResult.stream().filter(AssertionResultDTO::isResult).count();
        result.setAssertionStatistics(new AssertionStatisticsDTO(total==successCount,total,total-successCount,successCount));
        // 5、结果日志
        BeanUtils.copyProperties(output, result);
        result.setTime((System.currentTimeMillis() - start) );
        if(output.isSuccess()){
            result.setStatus(200);
        }else{
            result.setStatus(0);
        }
        try {
            long size=getLength(output.getBody());
            result.setSize(size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FlowDebugLog log = new FlowDebugLog();
        JsfDebugData debugData = new JsfDebugData();
        debugData.setInput(dto);
        debugData.setOutput(result);
        log.setSite(dto.getSite());
        log.setEnvName(dto.getEnv());
        log.setDigest(getJsfDigest(debugData));
        log.setSuccess(result.isSuccess() ? 1 : 0);
        log.setMethodId(dto.getMethodId() + "");
        log.setLogContent(JsonUtils.toJSONString(debugData));
        log.setYn(1);
        Long logId=saveOrUpdateLog(log);
        result.setLogId(logId);
        result.setDebugContent(log);
        result.setStepMsg(DataContextProvider.getContext().getParamStepMsgs());
        // 【指标度量】快捷调用jsf
        measureDataService.saveQuickCallLog(MeasureDataEnum.QUICK_CALL_JSF.getCode(), log);

        return result;
    }

    /**
     * 转调测试环境
     * @param dto 入参
     * @param output 出参
     */
    private void transferCallTest(NewJsfDebugDto dto, JsfOutput output) {
        try{
            NewJsfDebugDto testDto = new NewJsfDebugDto();
            BeanUtils.copyProperties(dto, testDto);
            testDto.setPreOpt(null);
            testDto.setPostOpt(null);
            String url="http://test.data-flow.jd.com/debug/debugJsfNew?erp=sunchao81";
            String testResult = HttpRequest.post(url).body(JsonUtils.toJSONString(testDto)).execute().body();
            CommonResult<JsfOutputExt> bean = JSONUtil.toBean(testResult, new TypeReference<CommonResult<JsfOutputExt>>() {},true);
            if(Objects.nonNull(bean.getData())){
                BeanUtils.copyProperties(bean.getData(), output);
            }
            parseException(testResult, output);
        }catch (Exception e){
            log.error("[test env] error");
            e.printStackTrace();
            output.setException(new BizException("[测试环境]"+e.toString()));
        }
    }

    /**
     *
     * @param dto
     * @param output
     */
    private void transferCallTestHttp(HttpDebugDto dto, HttpOutput output) {
        try{
            HttpDebugDto testDto = new HttpDebugDto();
            BeanUtils.copyProperties(dto, testDto);
            testDto.setPreOpt(null);
            testDto.setPostOpt(null);
            String url="http://test.data-flow.jd.com/debug/debugHttpNew?erp=";
            String testResult = HttpRequest.post(url).body(JsonUtils.toJSONString(testDto)).execute().body();
            CommonResult<HttpOutputExt> bean = JSONUtil.toBean(testResult, new TypeReference<CommonResult<HttpOutputExt>>() {},true);
            if(!bean.isSuccess()){
                output.setException(new BizException("[测试环境]"+JSONUtil.toJsonStr(bean)));
                return;
            }
            if(Objects.nonNull(bean.getData())){
                BeanUtils.copyProperties(bean.getData(), output);
            }
            parseException(testResult, output);
        }catch (Exception e){
            log.error("[test env] error");
            e.printStackTrace();
            output.setException(new BizException("[测试环境]"+e.toString()));
        }
    }

    /**
     * 由于序列化问题，单独处理
     * @param testResult
     * @param output
     */
    private void parseException(String testResult, BaseOutput output) {
        try{
            Object stack = JSONUtil.getByPath(JSONUtil.parse(testResult), "$.data.exception");
            boolean isBlank = Objects.isNull(stack) || "null".equalsIgnoreCase(stack+"");
            if(Objects.isNull(output.getBody()) && !isBlank){
                output.setException(new BizException("[测试环境]"+stack));
            }
        } catch (Exception e){
            log.error("parseException error");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param dto
     */
    private void setLocalEnv(NewJsfDebugDto dto) {
        // 根据ip判断是否是本地环境，查询逻辑：jsf provider的AppName为空,代表为本地环境
        if (JsfRegistryEnvEnum.test.name().equals(dto.getEnv())&&StringUtils.isNotBlank(dto.getIp()) && StringUtils.isNotBlank(dto.getInterfaceName()) && StringUtils.isNotBlank(dto.getAlias())) {
            if (!localJsfIp.containsKey(dto.getIp())) {
                List<Server> servers = jsfOpenService.getProviders(dto.getInterfaceName(), dto.getAlias());
                String localIp= dto.getIp().split(":")[0];
                List<Server> targetServers = servers.stream().filter(item -> item.getIp().equals(localIp)).collect(Collectors.toList());

                if ( !ObjectHelper.isEmpty(targetServers) && StringUtils.isBlank(getAppName(targetServers.get(0)))) {
                    localJsfIp.put(dto.getIp(), 1);
                    dto.setEnv(JsfRegistryEnvEnum.local.name());
                    log.info("选择本地ip,环境设置为本地"+ dto.getIp());
                } else {
                    localJsfIp.put(dto.getIp(), 2);
                }
            } else {
                if (1 == localJsfIp.get(dto.getIp())) {
                    dto.setEnv(JsfRegistryEnvEnum.local.name());
                    log.info("查询缓存选择本地ip,环境设置为本地"+ dto.getIp());
                }
            }
        }
    }

    public long getLength (Object obj) {
        if(obj == null){
            return 0;
        }
        // 长度前端计算，后端不在计算
        return 0;

    }

    public Map debugJsf(JsfDebugDto dto) {
        Map<String, Object> ret = new LinkedHashMap<>();
        JsfProcessor processor = new JsfProcessor();
        Step current = new Step();
        try {
            processor.init(dto);

            final StepContext stepContext = new StepContext();
            stepContext.setInput(new WorkflowInput());
            current.setContext(stepContext);

            processor.process(current);
            ret.put("input", current.getInput());
            ret.put("output", current.getOutput());
            return ret;
        } catch (RuntimeException e) {
            log.error("jsf.err_debug_jsf", e);
            JsfOutput output = new JsfOutput();
            output.setSuccess(false);
            if (e instanceof StdException) {
                final Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    output.setException((RuntimeException) cause);
                } else {
                    output.setException(e);
                }

            } else {
                output.setException(e);
            }
            ret.put("input", current.getInput());
            ret.put("output", output);
            return ret;

        } finally {
            processor.stop();
        }

    }

    public FlowDebugResult debugFlow(FlowDebugDto dto, String cookie) {
        WorkflowDefinition def = null;
        if (dto.getMethodId() != null) {
            def = flowService.parseAndInitFlow(dto.getMethodId() + "", null);
            if (def == null) {
                throw new BizException("当前方法流程定义为空，请确认流程已保存");
            }
        } else {
            def = flowService.parseAndInitFlow(dto.getMethodId() + "", dto.getDefinition());
        }

        CamelRouteLoader routeLoader = new CamelRouteLoader();
        try {
            CamelContext camelContext = routeLoader.buildCamelContext(def);

            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                dto.replaceNullStringToEmptyString();
                WorkflowInput input = dto.getInput().toWorkflowInput();

                if (!StringUtils.isEmpty(cookie)) {
                    input.addHeader("Cookie", cookie);
                }
                Exchange exchange = new DefaultExchange(camelContext);
                StepContext stepContext = StepContextHelper.setInput(exchange, input); // 设置输入，返回执行上下文
                stepContext.setSubflowProcessor(new CamelSubflowProcessor(template));
                stepContext.setDebugMode(true);

                template.send("direct:start", exchange);// 执行代码

                Output output = (Output) exchange.getMessage().getBody();

                FlowDebugResult result = new FlowDebugResult();
                result.setStepContext(stepContext);
                result.setOutput(output);
                final Long flowId = flowDebugLogService.saveLog(stepContext, dto.getMethodId());
                stepDebugLogService.saveLog(stepContext, dto.getMethodId(), flowId);
                return result;
            } finally {
                camelContext.stop();

            }

        } catch (Exception e) {
            log.error("debug.err_build_route_context", e);
            throw new BizException("调试失败", e);
        }
    }

    public HttpOutput http2ws(WebServiceStepMetadata stepMetadata, String inputXml) {
        Http2WsStepProcessor processor = new Http2WsStepProcessor();
        processor.init(stepMetadata);
        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setBody(inputXml);

        Step currentStep = new Step();
        currentStep.setContext(stepContext);
        processor.process(currentStep);

        HttpOutput httpOutput = (HttpOutput) currentStep.getOutput();

        return httpOutput;
    }

    public List<ExprTreeNode> buildWorkflowExprTree(WorkflowTreeBuilderDto dto) {
        WorkflowDefinition definition = WorkflowParser.parse(dto.getDefinition(), false, true);


        List<ExprTreeNode> result = new ArrayList<>();

        WorkflowInputDefinition input = definition.getInput();
        ExprTreeNode inputNode = new ExprTreeNode("输入", "object", "workflow.input");
        inputNode.setKey("input");
        if (input != null) {
            ExprNodeUtils.buildExprNode(inputNode, input.getParams(), "params");
            ExprNodeUtils.buildExprNode(inputNode, input.getHeaders(), "headers");
            ExprNodeUtils.buildBodyExprNode(inputNode, input.getBody());
        }
        Map<String, WorkflowParam> publicParams = new HashMap<>();
        if (dto.getFlowId() != null) {
            MethodManage methodManage = methodManageService.getById(dto.getFlowId());
            QueryParamQuoteReqDTO reqDTO = new QueryParamQuoteReqDTO();
            reqDTO.setInterfaceId(methodManage.getInterfaceId());
            QueryParamQuoteResultDTO resultDTO = flowParamQuoteService.queryQuoteParam(reqDTO);
            for (FlowParamQuote flowParamQuote : resultDTO.getList()) {
                WorkflowParam workflowParam = new WorkflowParam();
                workflowParam.setName(flowParamQuote.getName());
                workflowParam.setValue(flowParamQuote.getValue());
                workflowParam.setEntityId(flowParamQuote.getId());
                //definition.getParams().add(workflowParam);
                publicParams.put(workflowParam.getName(), workflowParam);
            }
        }
        /*if(definition.getParams() != null && !definition.getParams().isEmpty()){
            ExprTreeNode paramsNode = new ExprTreeNode("公共参数", "object", "workflow.params");
            inputNode.setKey("input");
            for (WorkflowParam param : definition.getParams()) {
                ExprTreeNode current = new ExprTreeNode(param.getName(),"string","workflow.params."+param.getName());
                paramsNode.addChild(current);
                publicParams.put(param.getName(),param);
            }

            result.add(paramsNode);
        }*/
        if (!publicParams.isEmpty()) {
            ExprTreeNode paramsNode = new ExprTreeNode("公共参数", "object", "workflow.params");
            for (Map.Entry<String, WorkflowParam> entry : publicParams.entrySet()) {
                WorkflowParam param = entry.getValue();
                ExprTreeNode current = new ExprTreeNode(param.getName(), "string", "workflow.params." + param.getName());
                paramsNode.addChild(current);
                publicParams.put(param.getName(), param);
            }
            result.add(paramsNode);
        }
        result.add(inputNode);
        int index = 0;
        for (StepDefinition task : definition.getTasks()) {
            StepMetadata stepMetadata = task.getMetadata();
          /*  if(definition.isBefore(dto.getCurrentStepKey(), stepMetadata.getKey())){
                break;
            }*/

            if (stepMetadata.getKey() != null && stepMetadata.getKey().equals(dto.getCurrentStepKey())) { // 跳过当前步骤
                continue;
            }
           /* String name = (String) task.getConfig().get("name");
            if(StringUtils.isEmpty(name)){
                name = stepMetadata.getId();
            }
            ExprTreeNode stepNode = new ExprTreeNode(name,"object","steps."+stepMetadata.getId());
            stepNode.setKey(stepMetadata.getId()+"index");
            stepMetadata.buildTreeNode(stepNode);
            if(!CollectionUtils.isEmpty(stepNode.getChildren())){ // 有些节点比如choice需要跳过呢
                result.add(stepNode);
            }*/
            buildStepMetadata(definition, dto.getCurrentStepKey(), task, result);
            index++;
        }
        addPrefix(result);
        return result;
    }

    private void buildStepMetadata(WorkflowDefinition definition, String currentStepKey, StepDefinition stepDefinition, List<ExprTreeNode> result) {
        for (Map.Entry<Integer, List<StepDefinition>> entry : stepDefinition.getChildren().entrySet()) {
            for (StepDefinition childDefinition : entry.getValue()) {
                buildStepMetadata(definition, currentStepKey, childDefinition, result);
            }
        }
        StepMetadata stepMetadata = stepDefinition.getMetadata();
        if (definition.isBefore(currentStepKey, stepMetadata.getKey())) {
            return;
        }
        String name = (String) stepDefinition.getConfig().get("name");
        if (StringUtils.isEmpty(name)) {
            name = stepMetadata.getId();
        }
        ExprTreeNode stepNode = new ExprTreeNode(name, "object", "steps." + stepMetadata.getId());
        stepNode.setKey(stepMetadata.getId() + "index");
        stepMetadata.buildTreeNode(stepNode);
        if (!CollectionUtils.isEmpty(stepNode.getChildren())) { // 有些节点比如choice需要跳过呢
            result.add(stepNode);
        }

    }

    private void addPrefix(List<ExprTreeNode> nodes) {
        for (ExprTreeNode node : nodes) {
            if (StringUtils.isNotBlank(node.getExpr())) {
                node.setExpr("${" + node.getExpr() + "}");
            }
            if (node.getChildren() != null) {
                addPrefix(node.getChildren());
            }
        }
    }

    public String buildPseudoCode(Map map) {
        WorkflowDefinition definition = WorkflowParser.parse(map);
        return PseudoBuildUtils.buildPseudoCode(definition);
    }

    public String uploadFile(MultipartFile multipartFile) {
        return jfsUtils.uploadStreamWithFileName(multipartFile, "lht");
    }

    public JsfOutputExt debugJsfHttp(NewJsfDebugDto jsfDto, HttpServletRequest req) {
        HttpInput input=new HttpInput();
        input.setUrl(jsfDto.getUrl());

        input.setHeaders(utils.buildInput(jsfDto.getColorHeaders()));
        String address = jsfDto.getUrl();
        input.setReqType(ReqType.valueOf("json"));
        if (StringUtils.isBlank(jsfDto.getMethodType())||"get".equalsIgnoreCase(jsfDto.getMethodType())) {
            for (JsonType item : jsfDto.getColorInputParam()) {
                if(item.getName().equals("body")&&item.getValue()==null){
                    item.setValue(jsfDto.getInput());
                }
            }
            input.setMethod("GET");
            input.setParams(utils.buildInput(jsfDto.getColorInputParam()));
        } else {
            input.setMethod(jsfDto.getMethodType());
            input.setParams(utils.buildInput(jsfDto.getColorInputParam()));
            input.setBody(utils.buildInput(jsfDto.getInput()));
        }

        String host = RpcUtils.getHostFromUrl(address);
        HttpOutput out = RpcUtils.callHttp(input, address, host);


        JsfOutputExt result = new JsfOutputExt();
        result.setBody(out.getBody());
        result.setSuccess(out.isSuccess());

        FlowDebugLog log = new FlowDebugLog();
        JsfDebugData debugData = new JsfDebugData();
        debugData.setInput(jsfDto);
        debugData.setOutput(result);
        log.setSite(jsfDto.getSite());
        log.setEnvName(jsfDto.getEnv());
        log.setDigest(getJsfDigest(debugData));
        log.setSuccess(result.isSuccess() ? 1 : 0);
        log.setMethodId(jsfDto.getMethodId() + "");
        log.setLogContent(JsonUtils.toJSONString(debugData));
        log.setYn(1);
        log.setMethodTag(1);
        Long logId=saveOrUpdateLog(log);
        result.setLogId(logId);
        result.setDebugContent(log);

        // 【指标度量】快捷调用jsf
        measureDataService.saveQuickCallLog(MeasureDataEnum.QUICK_CALL_JSF.getCode(), log);
return result;
    }

    /**
     * 构建入参
     * @param dto
     * @param onlineCookie
     * @return
     */
    private HttpInput buildHttpInput(HttpDebugDto dto, String onlineCookie) {
        HttpInput input = new HttpInput();
        String httpMethod = dto.getInput().getMethod();
        if (httpMethod.indexOf(",") != -1) {
            httpMethod = StringHelper.firstPart(httpMethod, ',');
        }
        input.setMethod(httpMethod);
        String reqType = dto.getInput().getReqType();
        if (StringUtils.isBlank(reqType)) {
            reqType = "json";
        }
        input.setReqType(ReqType.valueOf(reqType));


        if (StringUtils.isNotEmpty(checkHaveFile(dto))) {
            input.setReqType(ReqType.file);
        }

        if (dto.getInput().getBodyData() != null) {
            input.setBody(dto.getInput().getBodyData());
        } else {
            input.setBody(utils.buildInput(dto.getInput().getBody()));
        }


        input.setHeaders(utils.buildInput(dto.getInput().getHeaders()));
        boolean isTestMachine = "dev".equals(ssoEnv) || "test".equals(ssoEnv);
        if ("test".equals(dto.getSite()) && isTestMachine) {
            if (Objects.nonNull(dto.getSso())) {
                String cookie = TestSsoCookieHelper.getUserCookie(dto.getSso().getUser(), dto.getSso().getPwd());
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(cookie)) {
                    if (Objects.nonNull(input.getHeaders())) {
                        input.getHeaders().put("Cookie", cookie);
                    } else {
                        Map<String, Object> header = Maps.newHashMap();
                        header.put("Cookie", cookie);
                        input.setHeaders(header);
                    }
                    log.info("debugHttp change cookie:{},HttpDebugDto:{}", cookie, JSON.toJSONString(dto));
                }
            }
        }
        if ("China".equals(dto.getSite()) && !isTestMachine) {
            UpLoginContextHelper.getUserPin();
            if (dto.getRequestCookie()) {
                if (Objects.nonNull(input.getHeaders())) {
                    input.getHeaders().put("Cookie", onlineCookie);
                } else {
                    Map<String, Object> header = Maps.newHashMap();
                    header.put("Cookie", onlineCookie);
                    input.setHeaders(header);
                }

                log.info("debugHttp change cookie:{},HttpDebugDto:{}", onlineCookie, JSON.toJSONString(dto));
            }
        }

        input.setParams(utils.buildInput(dto.getInput().getParams()));
        input.setUrl(dto.getInput().getUrl());

        // 处理路径参数并更新URL
        if (!ObjectHelper.isEmpty(dto.getInput().getPath())) {
            Map<String, Object> pathData = utils.buildInput(dto.getInput().getPath());
            input.setUrl(StringHelper.replacePlaceholder(dto.getInput().getUrl(), pathData));
        }
        ensureUrlStartsWithSlash(input);

        // 处理Color
        if (dto.getIsColor() != null && dto.getIsColor()) {
            fillColorParamsAndHeader(input, dto);
        }

        return input;
    }

    /**
     * inputData 用于记录
     * @param dto
     * @param input
     * @return
     */
    public HttpDebugDataDto.Input buildInputData(HttpDebugDto dto, HttpInput input) {
        HttpDebugDataDto.Input inputData = new HttpDebugDataDto.Input();

        BeanUtils.copyProperties(input,inputData);
        // 基础属性赋值
//        inputData.setBody(input.getBody());
//        inputData.setHeaders(input.getHeaders());
        inputData.setColorHeaders(utils.buildInput(dto.getInput().getColorHeaders()));
        inputData.setColorInPutParam(utils.buildInput(dto.getInput().getColorInputParam()));
        inputData.setParams(input.getParams());

        // 处理路径参数并更新URL
        if (!ObjectHelper.isEmpty(dto.getInput().getPath())) {
            Map<String, Object> pathData = utils.buildInput(dto.getInput().getPath());
            inputData.setPath(pathData);
        }

        return inputData;
    }

    /**
     *
     * @param input
     */
    private void ensureUrlStartsWithSlash(HttpInput input) {
        if (input.getUrl() != null && !input.getUrl().startsWith("/")) {
            input.setUrl("/" + input.getUrl());
        }
    }

    /**
     *
     * @param dto
     * @param input
     * @return
     */
    private String resolveTargetAddress(HttpDebugDto dto, HttpInput input) {
        String address = dto.getTargetAddress();

        if (StringUtils.isNotBlank(dto.getInput().getFullUrl())) {
            return dto.getInput().getFullUrl();
        } else {
            try {
                if (dto.getEnvModel() != null
                        && dto.getEnvModel().getEnvName().equals(dto.getEnvName())) {
                    dto.setSite(dto.getEnvModel().getType().equals(EnvTypeEnum.TEST) ? "test" : "China");
                    address = dto.getEnvModel().getUrl().get(0);

                    if (StringUtils.isNotEmpty(dto.getEnvModel().getHostIp())) {
                        // 假设hostIp处理逻辑需要应用到address或其它逻辑中
                        // 这里根据实际需求处理hostIp
                    }
                }
            } catch (Exception e) {
                log.error("处理envModel异常");
                e.printStackTrace();
            }
            // 组合最终地址
            if (StringUtils.isNotBlank(input.getUrl())) {
                address += input.getUrl();
            }
        }
        return address;
    }
}
