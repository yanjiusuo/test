package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Sets;
import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.PublishEnum;
import com.jd.workflow.console.base.enums.StringMatchEnum;
import com.jd.workflow.console.dao.mapper.MethodManageMapper;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.ConvertWebServiceBaseDto;
import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.HttpToWebServiceDTO;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.helper.ProjectHelper;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.IServiceConvertService;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.WebServiceStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.Ws2HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.processor.impl.Ws2HttpStepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.xml.SoapUtils;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.client.core.SoapClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.legacy.SoapVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.wsdl.Definition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


/**
 * 项目名称：example
 * 类 名 称：ServiceConvertServiceImpl
 * 类 描 述：webservice转换服务层
 * 创建时间：2022-05-26 21:06
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Service
public class ServiceConvertServiceImpl implements IServiceConvertService {

    @Resource
    private MethodManageMapper methodManageMapper;

    @Resource
    private ProjectHelper projectHelper;
    @Resource
    private IMethodManageService methodManageService;

    @Resource
    IInterfaceManageService manageService;

    /*@Resource
    private JssUtil jssUtil;*/

    /**
     * webservice body提交方式
     */
    private static Set<String> BODY_SUBMIT_TYPES = Sets.newHashSet("form","json");


    @Transactional
    @Override
    public Boolean addHttpToWebService(String basePath,HttpToWebServiceDTO httpToWebServiceDTO) {
        //基本数据校验
        baseInfoCheck(httpToWebServiceDTO);
        //校验重复的环境调用
        checkDuplicateEnv(httpToWebServiceDTO);
        //校验父方法是否存在以及是否是http类型的方法
        MethodManage httpMethod = checkAndGetExistedMethodManage(httpToWebServiceDTO.getMethodId()
                ,httpToWebServiceDTO.getInterfaceId(),true);
        if(!InterfaceTypeEnum.HTTP.getCode().equals(httpMethod.getType())){
            throw new BizException("非法操作,请基于http的接口方法转换webservice方法");
        }
        MethodManage entity = new MethodManage();


        //entity.setStoragePos(StoragePosEnum.DATA_BASE.getCode());
        //过滤超长文本
        //filterContentSize(entity,true);
        entity.setType(InterfaceTypeEnum.WEB_SERVICE.getCode());
        entity.setHttpMethod("post");
        entity.setPublished(PublishEnum.NO.getCode());
        entity.setInterfaceId(httpToWebServiceDTO.getInterfaceId());
        entity.setParentId(httpToWebServiceDTO.getMethodId());
        entity.setCallEnv(httpToWebServiceDTO.getEnv());
        entity.setYn(DataYnEnum.VALID.getCode());
        entity.setCreated(new Date());
        entity.setCreator(UserSessionLocal.getUser().getUserId());
        entity.setModified(new Date());
        entity.setModifier(UserSessionLocal.getUser().getUserId());

        int count = methodManageMapper.insert(entity);

        String webServiceCallUrl = WebServiceHelper.getWebServiceCallUrl(basePath, httpToWebServiceDTO.getInterfaceId(), entity.getId());
        httpToWebServiceDTO.setEndpointUrl(webServiceCallUrl);
        //处理wsdl信息封装
        String content = processWsdlInfo(httpToWebServiceDTO);

        if(entity.getId()!=null&&entity.getId()>0){
            MethodManage updateWsdlUrl = new MethodManage();
            updateWsdlUrl.setName(httpToWebServiceDTO.getMethodName());
            updateWsdlUrl.setPath(webServiceCallUrl);
            updateWsdlUrl.setDesc("HTTP转换成WebService");
            updateWsdlUrl.setId(entity.getId());
            updateWsdlUrl.setContent(content);
            count = methodManageMapper.updateById(updateWsdlUrl);
        }
        return count>0;
    }

    @Transactional
    @Override
    public Boolean modifyHttpToWebService(String basePath,HttpToWebServiceDTO httpToWebServiceDTO) {
        //基本数据校验
        baseInfoCheck(httpToWebServiceDTO);
        MethodManage methodManage = checkAndGetExistedMethodManage(httpToWebServiceDTO.getId()
                ,httpToWebServiceDTO.getInterfaceId(),true);
        httpToWebServiceDTO.setEndpointUrl(WebServiceHelper.getWebServiceCallUrl(basePath,httpToWebServiceDTO.getInterfaceId(),httpToWebServiceDTO.getId()));
        //处理wsdl信息封装
        String content = processWsdlInfo(httpToWebServiceDTO);
        MethodManage update = new MethodManage();
        update.setId(httpToWebServiceDTO.getId());
        update.setContent(content);
        update.setName(httpToWebServiceDTO.getMethodName());
        //过滤超长文本
        //filterContentSize(update,true);
        update.setModified(new Date());
        update.setModifier(UserSessionLocal.getUser().getUserId());
        int count = methodManageMapper.updateById(update);
        return count>0;
    }

    @Override
    public Boolean removeHttpToWebService(Long id, Long interfaceId) {
        MethodManage methodManage = checkAndGetExistedMethodManage(id,interfaceId,true);
        MethodManage update = new MethodManage();
        update.setId(id);
        update.setYn(DataYnEnum.INVALID.getCode());
        update.setModified(new Date());
        update.setModifier(UserSessionLocal.getUser().getUserId());
        int count = methodManageMapper.updateById(update);
        return count>0;
    }

    @Override
    public String getConvertWsdlContent(Long id, Long interfaceId) {
        HttpToWebServiceDTO dto = findHttpToWebService(id,interfaceId);
        return dto.getWsdl();
    }

    @Override
    public HttpToWebServiceDTO findHttpToWebService(Long id, Long interfaceId) {
        MethodManage httpMethod = checkAndGetExistedMethodManage(id,interfaceId,false);
        if(!InterfaceTypeEnum.WEB_SERVICE.getCode().equals(httpMethod.getType())){
            throw new BizException("根据请求条件查询不到有效的webservice方法");
        }
        //filterContentSize(httpMethod,false);
        HttpToWebServiceDTO dto = JsonUtils.parse(httpMethod.getContent(), HttpToWebServiceDTO.class);
        //拼装发布后的调用地址
        if(httpMethod.getPublished().equals(PublishEnum.YES.getCode())){
            dto.setEndpointUrl(projectHelper.getPublishUrl(projectHelper.getPublishMethodId(httpMethod,interfaceId)));
        }
        return dto;
    }

    @Override
    public List<ConvertWebServiceBaseDto> findHttpToWebServiceList(Long methodId, Long interfaceId) {
        LambdaQueryWrapper<MethodManage> queryWrapper = Wrappers.<MethodManage>lambdaQuery();
        queryWrapper.eq(MethodManage::getInterfaceId,interfaceId)
                .eq(MethodManage::getParentId,methodId)
                .eq(MethodManage::getYn,DataYnEnum.VALID.getCode()).orderByDesc(MethodManage::getModified);
        filterBigField(queryWrapper);
        List<MethodManage> methodManages = methodManageMapper.selectList(queryWrapper);
        List<ConvertWebServiceBaseDto> result = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(methodManages)){
            methodManages.stream().forEach(obj->{
                ConvertWebServiceBaseDto dto = new ConvertWebServiceBaseDto();
                dto.setId(obj.getId());
                dto.setInterfaceId(obj.getInterfaceId());
                dto.setMethodId(obj.getParentId());
                dto.setCallEnv(obj.getCallEnv());
                //dto.setReqType("post");
                dto.setPublished(obj.getPublished());
                dto.setModified(obj.getModified());
                dto.setMockWsdlUrlPath(projectHelper.getMockWsdlPath(obj.getId(),obj.getInterfaceId()));
                result.add(dto);
            });
        }
        return result;
    }

    @Override
    public Object callHttpToWebService(CallHttpToWebServiceReqDTO callHttpToWebServiceReqDTO) {
       /* MethodManage httpMethod = checkAndGetExistedMethodManage(callHttpToWebServiceReqDTO.getMethodId()
                ,callHttpToWebServiceReqDTO.getInterfaceId(),false);
        //filterContentSize(httpMethod,false);
        HttpToWebServiceDTO dto = JsonUtils.parse(httpMethod.getContent(), HttpToWebServiceDTO.class);

        //TODO callHttpToWebServiceReqDTO.getInputType  默认xml , json方式底层未提供
        JsonType schemaTypeInput = dto.getInput().getSchemaType();
        List<XNode> xNodes = schemaTypeInput.transformToXml(callHttpToWebServiceReqDTO.getInputType());

        String soapInput = xNodes.get(0).toXml();
        log.info("callHttpToWebService soapInput={}",soapInput);

        //TODO url取值？
        String endpointUrl = "http://127.0.0.1:8001/"+dto.getMethodName();
        SoapClient client = SoapClient.builder().endpointUri(endpointUrl).build();
        String response = client.post(soapInput);
        log.info("callHttpToWebService response={}",response);
        *//**
         * 协议转换
         *//*
        JsonType schemaTypeOutput = dto.getOutput().getSchemaType();
        Object responseJson = SoapUtils.soapXmlToJson(response, schemaTypeOutput);
        log.info("callHttpToWebService responseJson={}",responseJson);*/
        return null;
    }
    HttpOutput buildNoServerResponse(String env){
        HttpOutput output = new HttpOutput();
        String response = SoapMessageBuilder.buildFault("soap:Server","noserver found in env "+env, SoapVersion.Soap11, SoapContext.DEFAULT);
        output.setBody(response);
        return output;
    }
    @Override
    public HttpOutput ws2http(Long id,String content) {
        Ws2HttpStepProcessor processor = new Ws2HttpStepProcessor();
        //service层
        HttpToWebServiceDTO ref = findHttpToWebService(id,null);

        MethodManage manageDto = methodManageService.getById(ref.getMethodId());

        InterfaceManage manage = manageService.getById(manageDto.getInterfaceId());
        Ws2HttpStepMetadata ws2HttpStepMetadata = new Ws2HttpStepMetadata();
        try{
            List<EnvModel> models = JsonUtils.parseArray(manage.getEnv(), EnvModel.class);
            EnvModel envModel = null;
            for (EnvModel model : models) {
                if(ref.getEnv().equalsIgnoreCase(model.getEnvName())){
                    envModel = model;
                }
            }

            if(envModel == null){
                return buildNoServerResponse(ref.getEnv());
            }
            if(envModel == null || CollectionUtils.isEmpty(envModel.getUrl())){
                return buildNoServerResponse(ref.getEnv());
            }
            ws2HttpStepMetadata.setEndpointUrl(envModel.getUrl());
            ws2HttpStepMetadata.setUrl(manageDto.getPath());
        }catch (Exception e){
            log.error("service.err_process_env_model",e);
            return buildNoServerResponse(ref.getEnv());
        }
        Step current = new Step();
        try{
            ws2HttpStepMetadata.setHttpMethod(manageDto.getHttpMethod());
            ws2HttpStepMetadata.setOpName(ref.getMethodName());
            ws2HttpStepMetadata.setReqType(ReqType.valueOf(ref.getInput().getReqType()));
            WebServiceStepMetadata.Metadata input = new WebServiceStepMetadata.Metadata();
            input.setSchemaType(ref.getInput().getSchemaType());
            WebServiceStepMetadata.Metadata output = new WebServiceStepMetadata.Metadata();
            output.setSchemaType(ref.getOutput().getSchemaType());

            //ws2HttpStepMetadata.setEndpointUrl();
            ws2HttpStepMetadata.setInput(input);
            ws2HttpStepMetadata.setOutput(output);

            processor.init(ws2HttpStepMetadata);

            WorkflowInput workflowInput = new WorkflowInput();
            workflowInput.setBody(content);

            StepContext stepContext = new StepContext();
            stepContext.setInput(workflowInput);
            current.setContext(stepContext);

            processor.process(current);

            HttpOutput newOutput = (HttpOutput) current.getOutput();
            return newOutput;
        }catch (StdException e){
            log.error("logger.err_ws2http:id={},content={}",id,content,e);
            HttpOutput newOutput = (HttpOutput) current.getOutput();
            String msg = ErrorMessageFormatter.formatMsg((StepExecException) e);
            newOutput.setException(e);
            String  result = SoapMessageBuilder.buildFault("soap:Server",msg, SoapVersion.Soap11,SoapContext.DEFAULT);
            newOutput.setBody(result);
            return newOutput;
        }


    }


    /**
     * 校验环境是否重复
     * @param httpToWebServiceDTO
     */
    private void checkDuplicateEnv(HttpToWebServiceDTO httpToWebServiceDTO){
        LambdaQueryWrapper<MethodManage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MethodManage::getInterfaceId,httpToWebServiceDTO.getInterfaceId())
                .eq(MethodManage::getParentId,httpToWebServiceDTO.getMethodId())
                .eq(MethodManage::getCallEnv,httpToWebServiceDTO.getEnv())
                .eq(MethodManage::getYn,DataYnEnum.VALID.getCode());
        filterBigField(queryWrapper);

        MethodManage obj = methodManageService.getOne(queryWrapper);
        if(obj!=null){
            throw new BizException("已存在相同调用环境的webservice转换方法");
        }
    }

    /**
     * 校验并获得已经存在的http转换的webservice方法
     * @return
     */
    private MethodManage checkAndGetExistedMethodManage(Long id, Long interfaceId,boolean excludeBigField){
        LambdaQueryWrapper<MethodManage> queryWrapper = Wrappers.<MethodManage>lambdaQuery();
        queryWrapper.eq(MethodManage::getId,id).eq(MethodManage::getYn,DataYnEnum.VALID.getCode());
        if(interfaceId != null){
            queryWrapper.eq(MethodManage::getInterfaceId,interfaceId);
        }
        if(excludeBigField){
            filterBigField(queryWrapper);
        }
        MethodManage obj = methodManageMapper.selectOne(queryWrapper);
        if(obj==null){
            throw new BizException("查询有效的方法不存在");
        }
        return obj;
    }

    /**
     * 基本入参校验
     * @param httpToWebServiceDTO
     */
    private void baseInfoCheck(HttpToWebServiceDTO httpToWebServiceDTO){
       /* if(CollectionUtils.isEmpty(httpToWebServiceDTO.getInput().getParams())&&CollectionUtils.isEmpty(httpToWebServiceDTO.getInput().getBody())
                &&CollectionUtils.isEmpty(httpToWebServiceDTO.getInput().getHeaders())){
            throw new BizException("input参数内容不能为空");
        }*/
        if(StringUtils.isBlank(httpToWebServiceDTO.getPkgName())){
            throw new BizException("包名不可为空");
        }
        if(CollectionUtils.isEmpty(httpToWebServiceDTO.getOutput().getBody())
                &&CollectionUtils.isEmpty(httpToWebServiceDTO.getOutput().getHeaders())){
            throw new BizException("output参数内容不能为空");
        }
        if(CollectionUtils.isNotEmpty(httpToWebServiceDTO.getInput().getBody())){
            if(StringUtils.isBlank(httpToWebServiceDTO.getInput().getReqType())){
                throw new BizException("input中对应的body请求类型不能为空");
            }
            if(!BODY_SUBMIT_TYPES.contains(httpToWebServiceDTO.getInput().getReqType())){
                throw new BizException("input中对应的body请求类型值非法");
            }
        }
        if(!StringMatchEnum.JAVA_METHOD_NAME.doMatch(httpToWebServiceDTO.getMethodName())){
            throw new BizException("方法名校验不通过：字母开头，仅包含字母、数字");
        }
        if(!StringMatchEnum.JAVA_PK_NAME.doMatch(httpToWebServiceDTO.getPkgName())){
            throw new BizException("包名校验不通过：字母开头，仅包含字母、数字、小数点");
        }

        //设置服务方法
        httpToWebServiceDTO.setServiceName(httpToWebServiceDTO.getMethodName()+"Service");
    }

    /**
     * 处理wsdl等信息
     *  将webservice转换为http定义
     * @param httpToWebServiceDTO
     */
    private String processWsdlInfo(HttpToWebServiceDTO httpToWebServiceDTO){
        try {
            Definition definition = WebServiceHelper.generateWsdlDefinition(httpToWebServiceDTO);
            WebServiceHelper.assembleWsdlInfos(httpToWebServiceDTO,definition);
            return JsonUtils.toJSONString(httpToWebServiceDTO);
        } catch (Exception e) {
            log.error("process wsdl info occur exception , the msg = {}",e.getMessage());
            throw new BizException("转换服务失败，请检查请求参数是否正确！",e);
        }
    }

    /**
     * 过滤content大字段
     * @param queryWrapper
     */
    private void filterBigField(LambdaQueryWrapper<MethodManage> queryWrapper){
        queryWrapper.setEntityClass(MethodManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                return !tableFieldInfo.getColumn() .equalsIgnoreCase("content");
            }
        });
    }
    /**
     * 过滤文本长度
     * @param entity
     */
    /*private void filterContentSize(MethodManage entity,boolean isWrite){
        if(isWrite){
            if(entity.getContent().length()>1024){
                String ossPath = jssUtil.uploadString(entity.getContent(), UUID.randomUUID() + ".txt");
                entity.setContent(ossPath);
                entity.setStoragePos(StoragePosEnum.OSS.getCode());
            }else{
                entity.setStoragePos(StoragePosEnum.DATA_BASE.getCode());
            }
        }else{
            if(StoragePosEnum.OSS.getCode().equals(entity.getStoragePos())){
                String fileString = jssUtil.jssFile2String(entity.getContent());
                entity.setContent(fileString);
            }
        }
    }*/


}
