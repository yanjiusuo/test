package com.jd.workflow.console.service.param.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.workflow.console.base.DataContextProvider;
import com.jd.workflow.console.base.JsonPathUtil;
import com.jd.workflow.console.base.enums.AssertionOptEnum;
import com.jd.workflow.console.base.enums.ParamOptPositionEnum;
import com.jd.workflow.console.base.enums.ParamOptTypeEnum;
import com.jd.workflow.console.dto.ParamDepDto;
import com.jd.workflow.console.dto.ParamOptDto;
import com.jd.workflow.console.dto.ParamStepDto;
import com.jd.workflow.console.dto.requirement.AssertionResultDTO;
import com.jd.workflow.console.entity.param.ParamBuilderScript;
import com.jd.workflow.console.service.impl.OpenApiService;
import com.jd.workflow.console.service.param.IParamBuilderOptService;
import com.jd.workflow.console.service.param.IParamBuilderScriptService;
import com.jd.workflow.console.utils.TemplateUtils;
import com.jd.workflow.console.worker.param.ParamWorker;
import com.jd.workflow.flow.core.output.BaseOutput;
import com.jd.workflow.soap.common.util.JsonUtils;
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import groovy.lang.Tuple4;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 前置后置操作
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-21
 */
@Service
@Slf4j
public class ParamBuilderOptServiceImpl implements IParamBuilderOptService {

    /**
     *
     */
    @Resource
    ParamWorker paramWorker;

    /**
     *
     */
    @Resource
    IParamBuilderScriptService scriptService;

    /**
     *
     */
    @Resource
    OpenApiService openApiService;

    /**
     * 1、前置操作
     *    入参命中依赖关系中的key
     *    替换并调用
     * @param
     * @return
     */
    public void preOpt(List<ParamOptDto> preOpt){
        if(CollectionUtils.isEmpty(preOpt)){
            return;
        }
        for (ParamOptDto paramOptDto : preOpt) {
            log.info("preOpt start opt {} script {}", paramOptDto.getName(),paramOptDto.getReferId());
            if(Objects.nonNull(paramOptDto.getReferId())){
                try{
                    exc(paramOptDto,ParamOptTypeEnum.wlTool,ParamOptPositionEnum.preOpt);
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("preOpt {} msg:{}", paramOptDto.getReferId(), e.getMessage());
                    ParamStepDto paramStepDto = new ParamStepDto(e.getMessage(),"前置操作",paramOptDto.getName());
                    DataContextProvider.getContext().getParamStepMsgs().add(paramStepDto);
                }
            }
            log.info("preOpt end opt {} script {}", paramOptDto.getName(),paramOptDto.getReferId());
        }
    }

    /**
     * 2、渲染入参
     * @param
     * @return
     */
    public List renderParam(List inputData){
        try{
            List list = Lists.newArrayList();
            List<String> keyList = TemplateUtils.getKeyList(JsonUtils.toJSONString(inputData));
            for (Object input : inputData) {
                String text = getRenderString(JsonUtils.toJSONString(input), keyList);
                list.add(text);
            }
            List<ParamDepDto> paramDepDtos = hitParamDep(0L,ParamOptTypeEnum.other,ParamOptPositionEnum.other);
            cn.hutool.json.JSON content = JSONUtil.parse(list);
            for (ParamDepDto paramDepDto : paramDepDtos) {
                JSONUtil.putByPath(content, getJsonPath(paramDepDto.getKey()),paramDepDto.getValue());
            }
            return JSONUtil.toBean(content,new TypeReference<List>() {},true);
        } catch (Exception e){
            e.printStackTrace();
            log.error("渲染入参 msg:{}", e.getMessage());
            ParamStepDto paramStepDto = new ParamStepDto(e.getMessage(),"渲染入参","渲染入参");
            DataContextProvider.getContext().getParamStepMsgs().add(paramStepDto);
            return inputData;
        }
    }

    /**
     * 2、渲染入参
     * @param inputData
     * @return
     */
    @Override
    public String renderParam(String inputData) {
        try{
            List<String> keyList = TemplateUtils.getKeyList(JsonUtils.toJSONString(inputData));
            String text = getRenderString(inputData, keyList);
            List<ParamDepDto> paramDepDtos = hitParamDep(0L,ParamOptTypeEnum.other,ParamOptPositionEnum.other);
            if(CollectionUtils.isNotEmpty(paramDepDtos) && JSONUtil.isJson(text)){
                cn.hutool.json.JSON content = JSONUtil.parse(text);
                for (ParamDepDto paramDepDto : paramDepDtos) {
                    JSONUtil.putByPath(content, getJsonPath(paramDepDto.getKey()),paramDepDto.getValue());
                }
                return JSONUtil.toBean(content,new TypeReference<String>() {},true);
            }
            return text;
        } catch (Exception e){
            e.printStackTrace();
            log.error("渲染入参 msg:{}", e.getMessage());
            ParamStepDto paramStepDto = new ParamStepDto(e.getMessage(),"渲染入参","渲染入参");
            DataContextProvider.getContext().getParamStepMsgs().add(paramStepDto);
            return inputData;
        }
    }


    /**
     * 1、占位符渲染
     * 2、参数依赖渲染
     * @param inputData
     * @param keyList
     * @return
     */
    private String getRenderString(String inputData, List<String> keyList) {
        String text = inputData;
        if(CollectionUtils.isNotEmpty(keyList)){
            Map<String, String> contextMap = paramWorker.parseKey(keyList);
            log.info("renderParam contextMap:{}", JsonUtils.toJSONString(contextMap));
            text = TemplateUtils.renderTpl(inputData, contextMap);
            log.info("renderParam txt:{}", text);
        }
        return text;
    }

    /**
     * 3、后置操作
     * @param
     * @return
     */
    public void postOpt(List<ParamOptDto> postOpt){
        if(CollectionUtils.isEmpty(postOpt)){
            return;
        }
        List<ParamOptDto> wlTools = postOpt.stream().filter(dto -> ParamOptTypeEnum.wlTool.getCode().equalsIgnoreCase(dto.getType())).collect(Collectors.toList());
        for (ParamOptDto paramOptDto : wlTools) {
            if(ParamOptTypeEnum.wlTool.getCode().equalsIgnoreCase(paramOptDto.getType()) && Objects.nonNull(paramOptDto.getReferId())){
                try{
                    exc(paramOptDto,ParamOptTypeEnum.wlTool,ParamOptPositionEnum.postOpt);
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("postOpt {} msg:{}", paramOptDto.getReferId(), e.getMessage());
                    ParamStepDto paramStepDto = new ParamStepDto(e.getMessage(),"后置操作",paramOptDto.getName());
                    DataContextProvider.getContext().getParamStepMsgs().add(paramStepDto);
                }
            }
        }
    }

    /**
     * 4、 断言
     * paramType 类型
     * 1-jsonPath是精准匹配
     * 2-文本是模糊匹配
     * @param
     * @return Tuple4  位置、key、value、是否匹配
     */
    public List<AssertionResultDTO> assertionOpt(List<ParamOptDto> postOpt, BaseOutput output){
        List<AssertionResultDTO> resultDTOS = Lists.newArrayList();
        try{
            List<Tuple4> list = new ArrayList();
            if(CollectionUtils.isEmpty(postOpt)){
                return Lists.newArrayList();
            }
            List<ParamOptDto> assertionList = postOpt.stream().filter(dto -> ParamOptTypeEnum.assertion.getCode().equalsIgnoreCase(dto.getType())).collect(Collectors.toList());
            Map<String, List<Tuple2>> textMap = parseJsonToMap(JSONUtil.toJsonStr(output.getBody()));
            for (ParamOptDto paramOptDto : assertionList) {
                if("1".equalsIgnoreCase(paramOptDto.getParamType())){
                    List<Map> maps = JSONUtil.toList(JSONUtil.parseArray(paramOptDto.getJsonPath()), Map.class);
                    for (Map map : maps) {
                        String key = MapUtil.getStr(map, "key");
                        String opt = MapUtil.getStr(map, "opt");
                        String expectedValue = MapUtil.getStr(map, "value");
                        Object actualValue = JSONUtil.getByPath(JSONUtil.parse(output.getBody()), key+"");
                        Tuple4 tuple = new Tuple4(JSONUtil.toJsonStr(key),
                                JSONUtil.toJsonStr(key),
                                AssertionOptEnum.getByCode(opt).getSymbol()+""+expectedValue,
                                astOpt(actualValue+"",opt,JSONUtil.toJsonStr(expectedValue)));
                        list.add(tuple);
                    }

                }else if("2".equalsIgnoreCase(paramOptDto.getParamType())){
                    JSONObject jsonObject = JSONUtil.parseObj(paramOptDto.getParamText());
                    for (String key : jsonObject.keySet()) {
                        Object expectedValue = jsonObject.get(key);
                        List<Tuple2> tuple2List = textMap.get(key);
                        if(CollectionUtils.isEmpty(tuple2List)){
                            Tuple4 tuple = new Tuple4("",
                                    key,
                                    "="+expectedValue,
                                    false);
                            list.add(tuple);
                        }else {
                            List<String> valueList = tuple2List.stream().map(tuple2 -> tuple2.getV2()+"").collect(Collectors.toList());
                            if(ObjectUtils.containsElement(valueList.toArray(), expectedValue+"")){
                                Map<Object, Object> map = tuple2List.stream().collect(Collectors.toMap(Tuple2::getV2, Tuple2::getV1, (i1, i2) -> i1));
                                Tuple4 tuple = new Tuple4(map.get(expectedValue),
                                        key,
                                        "="+expectedValue,
                                        true);
                                list.add(tuple);
                            }else {
                                Tuple4 tuple = new Tuple4(tuple2List.get(0).getV1(),
                                        key,
                                        "="+expectedValue,
                                        false);
                                list.add(tuple);
                            }
                        }
                    }
                }
            }
            resultDTOS = list.stream().map(o -> AssertionResultDTO.fromTuple(o)).collect(Collectors.toList());
        }catch (Exception e){
            log.error("assertionOpt msg {}",e.getMessage());
            e.printStackTrace();
            ParamStepDto paramStepDto = new ParamStepDto(e.getMessage(),"断言","断言错误");
            DataContextProvider.getContext().getParamStepMsgs().add(paramStepDto);
        }
        return resultDTOS;
    }

    /**
     * 验证操作符
     */
    private boolean astOpt(String actualValue,Object opt,String expectedValue) {
        AssertionOptEnum byCode = AssertionOptEnum.getByCode(opt+"");
        switch (Objects.requireNonNull(byCode)) {
            case ge:
                if (actualValue instanceof Comparable && ((Comparable<String>) actualValue).compareTo(expectedValue) < 0) {
                    return false;
                }
                break;
            case le:
                if (actualValue instanceof Comparable && ((Comparable<String>) actualValue).compareTo(expectedValue) > 0) {
                    return false;
                }
                break;
            case gt:
                if (actualValue instanceof Comparable && ((Comparable<String>) actualValue).compareTo(expectedValue) <= 0) {
                    return false;
                }
                break;
            case lt:
                if (actualValue instanceof Comparable && ((Comparable<String>) actualValue).compareTo(expectedValue) >= 0) {
                    return false;
                }
                break;
            case eq:
                if (!ObjectUtils.nullSafeEquals(actualValue, expectedValue)) {
                    return false;
                }
                break;
            default:
                log.error("Unsupported operator: " + opt);
                return false;
        }
        return true;
    }


    /**
     * 文本匹配的可能是多个
     * @param jsonString
     * @return
     */
    public static Map<String, List<Tuple2>> parseJsonToMap(String jsonString){
        List<Tuple3<String, String, Object>> tuple3s = JsonPathUtil.parseJsonToList(jsonString, "");
        Map<String, List<Tuple2>> map = Maps.newHashMap();
        for (Tuple3<String, String, Object> tuple3 : tuple3s) {
            if(CollectionUtils.isEmpty(map.get(tuple3.getV1()))){
                Tuple2 tuple2 = new Tuple2(tuple3.getV2(), tuple3.getV3());
                List<Tuple2> list = new ArrayList<>();
                list.add(tuple2);
                map.put(tuple3.getV1(),list);
            }else {
                map.get(tuple3.getV1()).add(new Tuple2(tuple3.getV2(),tuple3.getV3()));
            }
        }
        return map;
    }

    /**
     * 入参命中参数依赖关系
     * @return
     */
    public List<ParamDepDto> hitParamDep(Long referId, ParamOptTypeEnum type, ParamOptPositionEnum position) {
        List<ParamDepDto> paramDepParse = DataContextProvider.getContext().getParamDepParse();
        if(CollectionUtils.isNotEmpty(paramDepParse)){

            Map<String, ParamDepDto> map = paramDepParse.stream().collect(Collectors.toMap(ParamDepDto::getKey, Function.identity(), (i1, i2) -> i2));
            List<ParamDepDto> resultList = Lists.newArrayList();
            List<String> keyList;
            if(ParamOptPositionEnum.other.equals(position)){
                keyList = paramDepParse.stream().map(ParamDepDto::getKey).filter(dto -> dto.startsWith(position.getCode()+"#"+type.getCode()))
                        .collect(Collectors.toList());
            }else {
                keyList = paramDepParse.stream().map(ParamDepDto::getKey).filter(dto -> dto.startsWith(position.getCode()+"#"+type.getCode()))
                        .filter(dto -> dto.contains("#"+type.getCode()+"-"+referId+"#"))
                        .collect(Collectors.toList());
            }
            for (String key : keyList) {
                if(Objects.nonNull(map.get(key))){
                    resultList.add(map.get(key));
                }
            }
            return resultList;
        }
        return Lists.newArrayList();
    }


    /**
     *
     * @param paramDep
     */
    private String getJsonPath(String paramDep) {
        int length = paramDep.split("#").length;
        String expression = paramDep.split("#")[length-1];
        if(expression.startsWith("$")){
            return expression;
        }else {
            return "";
        }
    }

    /**
     *
     * @param paramDep
     */
    private String getInType(String paramDep) {
        int length = paramDep.split("#").length;
        String type = paramDep.split("#")[length-2];
        return type;
    }

    /**
     * 参数依赖-2填充返回值
     * 2种模式
     * 简单模式 $<{1255}> 物料工具
     * 复杂模式 替换jsonPath 如 10#wlTool-255#out#$.data.productId  或  20#other#out#$.data.id
     * @param referId
     * @param result
     */
    @Override
    public void replaceValue(Long referId, Object result, ParamOptPositionEnum position) {
        List<ParamDepDto> paramDepParse = DataContextProvider.getContext().getParamDepParse();
        if(CollectionUtils.isEmpty(paramDepParse)){
            return;
        }
        for (ParamDepDto paramDepDto : paramDepParse) {
            if(ParamOptPositionEnum.other.equals(position)){
                if(paramDepDto.getValue().startsWith("20#other#out#")){
                    if(JSONUtil.isJson(result+"")){
                        Object byPath = JSONUtil.getByPath(JSONUtil.parse(result), getJsonPath(paramDepDto.getValue()));
                        paramDepDto.setValue(byPath+"");
                    }else {
                        paramDepDto.setValue(result+"");
                    }
                }
            }else {
                if(TemplateUtils.genKey(referId).equalsIgnoreCase(paramDepDto.getValue())){
                    //TODO 特定提取规则
                    paramDepDto.setValue(result+"");
                }
                if(paramDepDto.getValue().contains("#"+ParamOptTypeEnum.wlTool.getCode()+"-"+referId+"#")){
                    Object byPath = JSONUtil.getByPath(JSONUtil.parse(result), getJsonPath(paramDepDto.getValue()));
                    paramDepDto.setValue(byPath+"");
                }
            }
        }
    }

    /**
     * 执行脚本
     * @param paramOptDto
     */
    private void exc(ParamOptDto paramOptDto, ParamOptTypeEnum type, ParamOptPositionEnum position) {
        ParamBuilderScript script = scriptService.getById(paramOptDto.getReferId());
        if(Objects.isNull(script) || StringUtils.isBlank(script.getScriptContent())){
            log.info("执行物料 {} 为空",paramOptDto.getReferId());
            return;
        }
        List<ParamDepDto> paramDepDtos = hitParamDep(paramOptDto.getReferId(),type,position);
        if(CollectionUtils.isNotEmpty(paramDepDtos) && JSONUtil.isJson(script.getScriptContent())){
            cn.hutool.json.JSON content = JSONUtil.parse(script.getScriptContent());
            for (ParamDepDto paramDepDto : paramDepDtos) {
                JSONUtil.putByPath(content, getJsonPath(paramDepDto.getKey()),paramDepDto.getValue());
            }
            log.info("执行物料 {} {} 入参为 {}",position.getDescription(),JSONUtil.toJsonStr(paramOptDto),JSONUtil.toJsonStr(content));
            String result = openApiService.testDataExecute(JSON.parseObject(JSONUtil.toJsonStr(content)));
            replaceValue(paramOptDto.getReferId(), result,position);
        }else {
            log.info("执行物料 {} {} 入参为 {}",position.getDescription(),JSONUtil.toJsonStr(paramOptDto),script.getScriptContent());
            String result = openApiService.testDataExecute(JSON.parseObject(script.getScriptContent()));
            replaceValue(paramOptDto.getReferId(), result,position);
        }
    }
}
