package com.jd.workflow.console.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.google.common.collect.Sets;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.jd.common.util.StringUtils;
import com.jd.jim.cli.Cluster;
import com.jd.jim.cli.protocol.ZSetTuple;
import com.jd.workflow.console.base.*;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.auth.InterfaceAuthFilter;
import com.jd.workflow.console.dto.datasource.DataSourceInvokeDto;
import com.jd.workflow.console.dto.doc.AppInterfaceCount;
import com.jd.workflow.console.dto.doc.InterfaceTypeCount;
import com.jd.workflow.console.dto.doc.UpdateMethodConfigDto;
import com.jd.workflow.console.dto.doc.method.HttpMethodDocConfig;
import com.jd.workflow.console.dto.doc.method.JsfMethodDocConfig;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.dto.errorcode.BindPropParam;
import com.jd.workflow.console.dto.errorcode.MethodPropParam;
import com.jd.workflow.console.dto.jsf.JsfImportDto;
import com.jd.workflow.console.dto.manage.CountQueryDto;
import com.jd.workflow.console.dto.mock.HttpDemoValue;
import com.jd.workflow.console.dto.mock.JsfDemoValue;
import com.jd.workflow.console.dto.requirement.DemandDetailDTO;
import com.jd.workflow.console.dto.test.jagile.DemandDetail;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.debug.FlowDebugLog;
import com.jd.workflow.console.entity.doc.MethodDocDto;
import com.jd.workflow.console.entity.errorcode.REnumMethodProp;
import com.jd.workflow.console.entity.logic.BizLogicInfo;
import com.jd.workflow.console.entity.method.MethodModifyDeltaInfo;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.helper.ProjectHelper;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.debug.FlowDebugLogService;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import com.jd.workflow.console.service.doc.IMethodVersionModifyLogService;
import com.jd.workflow.console.service.doc.*;
import com.jd.workflow.console.service.errorcode.IEnumsService;
import com.jd.workflow.console.service.errorcode.IREnumMethodPropService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.service.logic.IBizLogicInfoService;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.console.service.remote.MockDataBuildService;
import com.jd.workflow.console.service.requirement.*;
import com.jd.workflow.console.service.test.JagileRemoteCaller;
import com.jd.workflow.console.utils.CjgJfsHelper;
import com.jd.workflow.console.utils.DigestUtils;
import com.jd.workflow.console.utils.ReqDemoBuildUtils;
import com.jd.workflow.console.utils.SafeUtil;
import com.jd.workflow.flow.bean.BeanStepDefinitionLoader;
import com.jd.workflow.flow.bean.BeanStepMetadata;
import com.jd.workflow.flow.bean.BeanStepProcessor;
import com.jd.workflow.flow.bean.BeanTemplateDefinition;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.processor.impl.HttpStepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.flow.xml.SoapUtils;
import com.jd.workflow.jsf.analyzer.JarParser;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.metrics.client.DemandUserResponse;
import com.jd.workflow.server.dto.InterfaceAndMethodInfo;
import com.jd.workflow.soap.SoapBuilderException;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.method.ColorApiEnum;
import com.jd.workflow.soap.common.method.ColorGatewayParamDto;
import com.jd.workflow.soap.common.method.ColorTypeEnum;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.*;
import com.jd.workflow.soap.legacy.SoapVersion;
import com.jd.workflow.soap.legacy.WsdlUtils;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.console.dao.mapper.MethodManageMapper;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.console.dto.requirement.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;


import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import javax.annotation.Resource;
import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 方法管理
 *
 * @author wubaizhao1
 * @date: 2022/5/16 18:24
 */
@Service
@Slf4j
public class MethodManageServiceImpl extends ServiceImpl<MethodManageMapper, MethodManage> implements IMethodManageService {

    /**
     * 方法表
     *
     * @date: 2022/5/16 18:28
     * @author wubaizhao1
     */
    @Resource
    MethodManageMapper methodManageMapper;

    @Resource
    IInterfaceManageService interfaceManageService;
    @Autowired
    RequirementInfoService requirementInfoService;


    @Resource
    IColorGatewayServiceImpl colorGatewayService;
    @Resource
    IInterfaceMethodGroupService methodGroupService;
    @Resource
    private ProjectHelper projectHelper;

    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    FlowDebugLogService flowDebugLogService;

    @Autowired
    MethodModifyDeltaInfoService deltaInfoService;
    @Autowired
    MockDataBuildService mockDataBuildService;

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthDetailService httpAuthDetailService;
    @Autowired
    IMethodModifyLogService methodModifyLogService;
    @Autowired
    MethodModifyDeltaInfoService methodModifyDeltaInfoService;
    @Autowired
    IMethodVersionModifyLogService methodVersionModifyLogService;
    @Autowired
    List<InterfaceChangeListener> listeners;
    @Autowired
    IFlowService flowService;
    @Autowired
    EasyMockRemoteService testEasyMockRemoteService;

    @Autowired
    private UserPrivilegeHelper userPrivilegeHelper;
    @Autowired
    private RefJsonTypeService refJsonTypeService;
    @Resource
    private RelationMethodTagService relationMethodTagService;

    @Resource
    private TagService tagService;

    @Autowired
    private IEnumsService enumsService;
    @Autowired
    private IREnumMethodPropService irEnumMethodPropService;
    @Autowired
    private JagileRemoteCaller jagileRemoteCaller;
    @Autowired
    private  InterfaceFollowListService interfaceFollowListService;

    @Autowired
    private IAppInfoMembersService appInfoMembersService;

    private static final String BODY = "Body";
    private static final String HEADER = "Header";

    /**
     * redis 客户端
     */
    @Resource(name = "jimClient")
    private Cluster jimClient;

    private static final String APP_SORTEDSET_FORMAT = "debug_tool_app_sortedset_%s";
    private static final String PROP_KEY_FORMAT = "%s:%s:%s";

    @Autowired
    private IBizLogicInfoService bizLogicInfoService;

    @Autowired
    private CjgJfsHelper cjgJfsHelper;

    @Transactional
    @Override
    public Long add(MethodManageDTO methodManageDTO) {
        log.info("MethodManageServiceImpl.add methodManageDTO={}", JsonUtils.toJSONString(methodManageDTO));
        Guard.notEmpty(methodManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(methodManageDTO.getName(), "方法名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
//		Guard.notEmpty(methodManageDTO.getDesc(),"方法描述不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(methodManageDTO.getHttpMethod(), "调用方式不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        Guard.notEmpty(methodManageDTO.getType(), "方法类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(methodManageDTO.getInterfaceId(), "所属的接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        //Guard.notEmpty(methodManageDTO.getMethodCode(),"方法编码不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
		/*if(!methodManageDTO.getMethodCode().matches(StringMatchEnum.NUMBER_LETTER.getMatch()) ){
			throw ServiceException.withCommon("方法编码只支持字母、数字");
		}*/
        if(StringUtils.isNotBlank(methodManageDTO.getFunctionId())&&!methodManageDTO.getFunctionId().matches(StringMatchEnum.NUMBER_LETTER.getMatch()) ){
			throw ServiceException.withCommon("functionId只支持字母、数字");
		}

        if (!InterfaceTypeEnum.ORCHESTRATION.getCode().equals(methodManageDTO.getType())) {
            Guard.notEmpty(methodManageDTO.getPath(), "路径不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
            Guard.notEmpty(methodManageDTO.getContent(), "内容不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
            Boolean contentCheck = checkMethodContent(methodManageDTO.getType(), methodManageDTO.getContent());
            if (!contentCheck) {
                throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "content格式校验不通过");
            }
        }
        // 查询是否已存在
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode())
                .eq(MethodManage::getMethodCode, methodManageDTO.getMethodCode())
                .eq(MethodManage::getInterfaceId, methodManageDTO.getInterfaceId());
        int count = methodManageMapper.selectCount(lqw);
        if (count > 0) {
            throw new BizException("该方法编码在该接口【" + methodManageDTO.getInterfaceId() + "】下已存在" + methodManageDTO.getMethodCode());
        }
        //添加
        MethodManage methodManage = new MethodManage();
        if (StringUtils.isNotEmpty(methodManageDTO.getName())) {
            methodManageDTO.setName(methodManageDTO.getName().trim());
        }
        if (StringUtils.isNotEmpty(methodManageDTO.getMethodCode())) {
            methodManageDTO.setMethodCode(methodManageDTO.getMethodCode().trim());
        }
        BeanUtils.copyProperties(methodManageDTO, methodManage);
        methodManage.setYn(DataYnEnum.VALID.getCode());
        methodManage.setPublished(PublishEnum.NO.getCode());
        if(StringUtils.isNotBlank(methodManageDTO.getFunctionId())){
            methodManage.setMethodTag(MethodTagEnum.COLOR.getCode());
            methodManage.setZoneInfo(JSONObject.toJSONString(methodManageDTO.getZoneInfo()));
        }

        initContentObject(methodManage);
        fillMethodDigest(methodManage);
        methodManage.setMergedContentDigest(methodManage.getDigest());
        MethodGroupTreeDTO dto = null;
        if (methodManageDTO.getGroupId() != null) {
            dto = methodGroupService.findMethodGroupTree(methodManage.getInterfaceId());

        }

        int add = methodManageMapper.insert(methodManage);
        //保存标签关系
        Set<Long> methodIds=new HashSet<>();
        methodIds.add(methodManage.getId());
        InterfaceManage interfaceInfo = interfaceManageService.getOneById(methodManage.getInterfaceId());
        relationMethodTagService.saveBatchTags(interfaceInfo.getAppId(), methodIds, methodManageDTO.getTags());
        tagService.saveBatchTags(interfaceInfo.getAppId(), methodManageDTO.getTags());

        InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());

        { // 插入方法差量信息
            MethodManage before = new MethodManage();
            before.setId(methodManage.getId());
            before.setType(methodManage.getType());
            before.setContent(methodManage.getContent());
            initContentObject(before);
            refJsonTypeService.initMethodRefInfos(before, interfaceManage.getAppId());
            before.setContent(JsonUtils.toJSONString(before.getContentObject()));
            deltaInfoService.saveDelta(before, methodManage, true);
        }
        if (dto != null) {
            dto.insertMethod(methodManageDTO.getGroupId(), methodManage.getId());

            interfaceManage.setSortGroupTree(dto.getTreeModel());
            interfaceManage.setGroupLastVersion(DateUtil.getCurrentDateMillTime());
            interfaceManageService.updateById(interfaceManage);
        }

        for (InterfaceChangeListener listener : listeners) {
            listener.onMethodAdd(interfaceManage, Collections.singletonList(methodManage));
        }

        return methodManage.getId();
    }

    @Override
    public void addColorInfo(String ss){
        JSONArray ssArray=JSONArray.parseArray(ss);
        for (Object item : ssArray) {
            ColorGatewayParam entity= JSONObject.parseObject(JSONObject.toJSONString(item),ColorGatewayParam.class);
            colorGatewayService.insertParam(entity);
        }

    }

    public boolean getMergedMethod(MethodManage method, InterfaceManage manage) {
        if (method == null) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        refJsonTypeService.initMethodRefInfos(method, manage.getAppId());
        if (isAuthReportMethod(manage, method)) {
            boolean inited = deltaInfoService.initDeltaInfo(method);
            if (inited) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthReportMethod(InterfaceManage interfaceManage, MethodManage methodManage) {
        if ((InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(methodManage.getType()) || InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType()) || InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType()))) {
            if (interfaceManage.getAutoReport() != null && interfaceManage.getAutoReport() == 1) {
                return true;
            }
        }


        return false;

    }

    /**
     * 前端有bug,可能会导致修改A方法,但id传的是B方法的id
     * @return
     */
    private void validateValidModify(MethodManageDTO methodManageDTO,MethodManage exist,InterfaceManage manage){
        long start = System.currentTimeMillis();
        Guard.notEmpty(exist,"无效的id");
        if(/*!ObjectHelper.equals(methodManageDTO.getName()) &&
         !ObjectHelper.equals(methodManageDTO.getHttpMethod(),exist.getHttpMethod())
                &&*/ !ObjectHelper.equals(methodManageDTO.getPath(),exist.getPath()) // 路径发生修改可能是前端bug导致的-
        ){ // 修改了path,exist

            List<InterfaceManage> manages = interfaceManageService.getAppInterfaces(manage.getAppId(),null,exist.getType());
            LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
            lqw.in(MethodManage::getInterfaceId,manages.stream().map(item->item.getId()).collect(Collectors.toList()));
            lqw.eq(MethodManage::getYn,1);
            lqw.eq(MethodManage::getPath,methodManageDTO.getPath());
            lqw.eq(MethodManage::getName,methodManageDTO.getName());
            lqw.eq(MethodManage::getHttpMethod,methodManageDTO.getHttpMethod());
            lqw.select(MethodManage::getId,MethodManage::getYn,MethodManage::getPath,MethodManage::getInterfaceId);
            List<MethodManage> methods = list(lqw);
            if(methods.size() > 1
             || methods.size() == 1 && !ObjectHelper.equals(
                    methods.get(0).getId(),exist.getId())
            ){
                throw new BizException("校验失败:当前应用下发现重复路径、http method、名称的http接口，请检查编辑是否有问题");
            }
            log.info("method.validate_method_modify_cost={}",System.currentTimeMillis() - start);
        }
    }
    /**
     * 更新方法信息，需要注意的是，针对自动上报的方法，自动上报的信息存到MethodManage表里，用户手动维护的存到MethodModifyDeltaInfo表里。界面展示的是合并后的结果。
     * digest里存的是自动上报的部分的签名。
     *
     * @param methodManageDTO
     * @return
     */
    @Override
    public Long edit(MethodManageDTO methodManageDTO) {
        Guard.notEmpty(methodManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(methodManageDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
		/*if(EmptyUtil.isNotEmpty(methodManageDTO.getName())){
			if(!methodManageDTO.getName().matches(StringMatchEnum.NUMBER_LETTER.getMatch()) || methodManageDTO.getName().length()>30){
				throw ServiceException.withCommon("请输入字母和数字，不超过30位。");
			}
		}*/
        if(StringUtils.isNotBlank(methodManageDTO.getFunctionId())&&!methodManageDTO.getFunctionId().matches(StringMatchEnum.NUMBER_LETTER.getMatch()) ){
            throw ServiceException.withCommon("functionId 只支持字母、数字");
        }
        MethodManage exist = methodManageMapper.selectById(methodManageDTO.getId());
        Guard.notEmpty(exist,"无效的方法id");
        InterfaceManage interfaceManage = interfaceManageService.getById(exist.getInterfaceId());
        validateValidModify(methodManageDTO,exist,interfaceManage);

        MethodManage original = exist.clone();
        initContentObject(exist);
        initContentObject(original);
        if (exist == null) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        if (methodManageDTO.getType() == null) {
            methodManageDTO.setType(exist.getType());
        }
        if (EmptyUtil.isNotEmpty(methodManageDTO.getContent())) {
            Boolean contentCheck = checkMethodContent(methodManageDTO.getType(), methodManageDTO.getContent());
            if (!contentCheck) {
                throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "content格式校验不通过");
            }
        }
		/*if(StringUtils.isNotBlank(exist.getMethodCode())){ //方法编码不允许更新
			methodManageDTO.setMethodCode(null);
		}*/

        getMergedMethod(exist, interfaceManage);
        // 这里由于计算digest的规则变了，因此重新计算下digest的值
        fillMethodDigest(exist);
        // desc不允许编辑，这里用前desc
        methodManageDTO.setDesc(exist.getDesc());
        MethodManage after = dtoToMethod(methodManageDTO);
        if (methodManageDTO.getDocConfig() != null) {
            if (StringUtils.isBlank(methodManageDTO.getDocConfig().getInputExample()) && exist.getDocConfig() != null) { // 入参为空不修改
                methodManageDTO.getDocConfig().setInputExample(exist.getDocConfig().getInputExample());
            }

            if (StringUtils.isBlank(methodManageDTO.getDocConfig().getOutputExample())
                    && exist.getDocConfig() != null) { // 入参为空不修改
                methodManageDTO.getDocConfig().setOutputExample(exist.getDocConfig().getOutputExample());
            }
        }

        Set<Long> methodIds = new HashSet<>();
        methodIds.add(Long.valueOf(methodManageDTO.getId()));
        //保存标签关系
        relationMethodTagService.saveBatchTags(methodManageDTO.getAppId(), methodIds, methodManageDTO.getTags());
        tagService.saveBatchTags(methodManageDTO.getAppId(), methodManageDTO.getTags());
        initContentObject(after);
        for (InterfaceChangeListener listener : listeners) {
            //不相同的时候生成变更记录
            if (after.getDigest() != null && !after.getDigest().equals(exist.getDigest())) {
                exist.setContent(JsonUtils.toJSONString(exist.getContentObject()));
                listener.onMethodBeforeUpdate(interfaceManage, Collections.singletonList(exist));
            }
        }
        MethodManage beUpdated = after.clone();

        refJsonTypeService.initMethodRefInfos(original, interfaceManage.getAppId());
        original.setContent(JsonUtils.toJSONString(original.getContentObject()));
        if (isAuthReportMethod(interfaceManage, original)) { // 将修改信息存到delta里。被delta的信息不需要变更
            // 每次都是与初始上报的数据进行比对
            deltaInfoService.saveDelta(original, after, false);
            beUpdated.setContent(null);
            beUpdated.setDigest(null); // digest清理
            beUpdated.setDocInfo(null);
            beUpdated.setName(null);
            beUpdated.setMethodCode(null);
        } else {
            deltaInfoService.saveDelta(original, after, true);
        }

        int update = methodManageMapper.updateById(beUpdated);

        after = getById(methodManageDTO.getId());
        for (InterfaceChangeListener listener : listeners) {
            listener.onMethodUpdate(interfaceManage, Collections.singletonList(beUpdated));
            listener.onMethodAfterUpdate(interfaceManage, Collections.singletonList(beUpdated), Collections.singletonList(exist));
        }
        return after.getId();
    }

    @Override
    public Boolean updateCloudFile(Long id, String path, String tags){
        LambdaUpdateWrapper<MethodManage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MethodManage::getId, id);
        wrapper.eq(MethodManage::getYn, 1);
        wrapper.set(MethodManage::getCloudFilePath, path);
        wrapper.set(MethodManage::getCloudFileTags, tags);
        wrapper.set(MethodManage::getUnionFile, 1);
        return update(null,wrapper);

    }

    private MethodManage dtoToMethod(MethodManageDTO dto) {
        MethodManage after = new MethodManage();

        BeanUtils.copyProperties(dto, after);
        initContentObject(after);
        fillMethodDigest(after);
        after.setMergedContentDigest(getContentObjectDigest(after));
        after.setId(Long.valueOf(dto.getId()));
        return after;
    }


    @Override
    public Long createDoc(MethodDocDto dto) {
        MethodManage manage = new MethodManage();
        manage.setYn(1);
        manage.setName(dto.getName());
        manage.setType(InterfaceTypeEnum.DOC.getCode());
        manage.setDocInfo(dto.getDocInfo());
        manage.setDocConfig(dto.getDocConfig());
        manage.setInterfaceId(dto.getInterfaceId());
        save(manage);
        return manage.getId();
    }

    @Override
    public Long updateReportStatus(Long methodId, Integer reportStatus) {
        Guard.notEmpty(methodId, "方法id不可为空");
        Guard.notEmpty(reportStatus, "reportStatus不可为空");
        MethodManage method = getById(methodId);
        Guard.notEmpty(method, "无效的方法id");
        if (reportStatus != 1 && reportStatus != 0) {
            throw new BizException("reportStatus只能为1或0");
        }
        method.setReportSyncStatus(reportStatus);
        LambdaUpdateWrapper<MethodManage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MethodManage::getId, methodId);
        updateWrapper.set(MethodManage::getReportSyncStatus, reportStatus);
        update(updateWrapper);
        return method.getId();
    }

    @Override
    public boolean updateStatus(Long methodId, Integer status) {
        Guard.notEmpty(methodId, "方法id不可为空");
        Guard.notEmpty(status, "status不可为空");
        MethodManage method = getById(methodId);
        Guard.notEmpty(method, "无效的方法id");
        method.setStatus(status);
        LambdaUpdateWrapper<MethodManage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MethodManage::getId, methodId);
        updateWrapper.set(MethodManage::getStatus, status);
        update(updateWrapper);
        return true;
    }

    @Override
    public boolean updateFunctionId(Long methodId, String zone, String functionId, String type) {

        Guard.notEmpty(methodId, "方法id不可为空");
        Guard.notEmpty(functionId, "functionId不可为空");
        MethodManage method = getById(methodId);
        Guard.notEmpty(method, "无效的方法id");

        LambdaUpdateWrapper<MethodManage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MethodManage::getId, methodId);
        updateWrapper.eq(MethodManage::getYn, 1);
        if(type.equals(ColorApiEnum.API_OFFLINE)||type.equals(ColorApiEnum.API_DELETE)){
            functionId="";
        }
        updateWrapper.set(MethodManage::getFunctionId, functionId);
        updateWrapper.set(MethodManage::getColorApiStatus, type);
        List<String> zoneInfoText = Arrays.asList(zone.split(":"));
        Map<String, String> zoneInfo = new HashMap<>();
        zoneInfo.put(zoneInfoText.get(0),zoneInfoText.get(1));
        updateWrapper.set(MethodManage::getZoneInfo, JSONObject.toJSONString(zoneInfo));
        update(updateWrapper);
        return true;
    }

    @Override
    public String getMethodIdByDoc(String docUrl, String key) {
        String[] params = docUrl.split("\\?")[1].split("&");
        for (String param : params) {
            if (param.contains(key)) {
                return param.split("=")[1];
            }
        }
        return null;
    }

    @Transactional
    @Override
    public Boolean remove(MethodManageDTO methodManageDTO) {
        Guard.notEmpty(methodManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(methodManageDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        MethodManage manage = getById(methodManageDTO.getId());
        Guard.notEmpty(manage, "id无效", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        if (PublishEnum.YES.getCode().equals(manage.getPublished())) {
            throw new BizException("已发布接口不可删除");
        }
        MethodManage removeEntity = new MethodManage();
        removeEntity.setId(Long.valueOf(methodManageDTO.getId()));
        removeEntity.setYn(DataYnEnum.INVALID.getCode());
        int remove = methodManageMapper.updateById(removeEntity);
        InterfaceManage interfaceObj = interfaceManageService.getById(manage.getInterfaceId());
        onMethodRemove(interfaceObj, Collections.singletonList(manage));
        return remove > 0;
    }

    private void onMethodRemove(InterfaceManage interfaceObj, List<MethodManage> methods) {
        List<Long> ids = methods.stream().map(item -> item.getId()).collect(Collectors.toList());
        httpAuthDetailService.removeMethodAuthDetail(ids);
		/*methodModifyLogService.removeByMethodIds(ids);
		methodVersionModifyLogService.removeByMethodIds(ids);*/

        for (InterfaceChangeListener listener : listeners) {
            listener.onMethodRemove(interfaceObj, methods);
        }

    }

    @Transactional
    @Override
    public Boolean removeByInterfaceId(Long interfaceId) {

        Guard.notEmpty(interfaceId, "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        List<MethodManage> methods = getInterfaceMethods(interfaceId);
        InterfaceManage interfaceObj = interfaceManageService.getById(interfaceId);
        for (InterfaceChangeListener listener : listeners) {
            listener.onMethodRemove(interfaceObj, methods);
        }

        LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper();
        luw.eq(MethodManage::getInterfaceId, interfaceId);
        luw.set(MethodManage::getYn, DataYnEnum.INVALID.getCode());
        return update(luw);

    }

    @Transactional
    public boolean removeMethodByIds(List<Long> ids) {
        if (ObjectHelper.isEmpty(ids)) return false;
        List<MethodManage> methods = listMethods(ids);
        InterfaceManage interfaceManage = interfaceManageService.getById(methods.get(0).getInterfaceId());
        onMethodRemove(interfaceManage, methods);
        LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper<>();
        luw.in(MethodManage::getId, ids);
        luw.set(MethodManage::getYn, DataYnEnum.INVALID.getCode());
        boolean result = update(luw);
        for (InterfaceChangeListener listener : listeners) {
            listener.onMethodRemove(interfaceManage, methods);
        }
        return result;
    }

    @Override
    public void initMethodDeltaInfos(List<MethodManage> methods) {
        if (ObjectHelper.isEmpty(methods)) return;
        List<Long> ids = methods.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<MethodModifyDeltaInfo> deltas = deltaInfoService.getMethodDeltas(ids);
        final Map<Long, List<MethodManage>> id2Methods = methods.stream().collect(Collectors.groupingBy(MethodManage::getId));
        for (MethodModifyDeltaInfo delta : deltas) {
            List<MethodManage> found = id2Methods.get(delta.getMethodId());
            if (found == null) continue;
            deltaInfoService.initDeltaInfo(found.get(0), delta);
        }
    }

    @Override
    public List<MethodManage> listMethods(List<Long> ids) {
        if (ObjectHelper.isEmpty(ids)) return Collections.emptyList();
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(MethodManage::getId, ids);
        lqw.eq(MethodManage::getYn, ValidEnum.VALID.getCode());

        excludeBigTextFiled(lqw);
        List<MethodManage> methods = list(lqw);
        initMethodDeltaInfos(methods);
        return methods;
    }

    private boolean isBeanMethod(InterfaceManage manage) {
        final InterfaceTypeEnum interfaceTypeEnum = InterfaceTypeEnum.getByCode(manage.getType());
        return interfaceTypeEnum != null && BeanStepDefinitionLoader.getBeanDefinition(interfaceTypeEnum.getDesc()) != null;
    }

    /**
     * content查出来的时候应当还原文本
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/23 18:28
     * @author wubaizhao1
     */
    @Override
    public Page<MethodManageDTO> pageMethod(MethodManageDTO methodManageDTO) {
        Guard.notEmpty(methodManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(methodManageDTO.getInterfaceId(), "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        InterfaceManage interfaceManage = interfaceManageService.getOneById(methodManageDTO.getInterfaceId());
        if (isBeanMethod(interfaceManage)) {
            return listDataSourceMethods(interfaceManage, methodManageDTO);
        }

        //查询条件
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper();

        excludeBigTextFiled(lqw);

        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(MethodManage::getInterfaceId, methodManageDTO.getInterfaceId());
        lqw.eq(EmptyUtil.isNotEmpty(methodManageDTO.getType()), MethodManage::getType, methodManageDTO.getType());
        lqw.like(EmptyUtil.isNotEmpty(methodManageDTO.getName()), MethodManage::getName, methodManageDTO.getName());

        //分页
        Page<MethodManage> methodManagePage = null;
        if (methodManageDTO.getCurrent() == null || methodManageDTO.getSize() == null) {
            methodManagePage = new Page<>(1, 100);
        } else {
            methodManagePage = new Page<>(methodManageDTO.getCurrent(), methodManageDTO.getSize());
        }

        Page<MethodManage> page = methodManageMapper.selectPage(methodManagePage, lqw);
        for (MethodManage record : page.getRecords()) {
            if (StringUtils.isNotBlank(record.getName())) {
                record.setName(record.getName().trim());
            }
            if (StringUtils.isNotBlank(record.getMethodCode())) {
                record.setMethodCode(record.getMethodCode().trim());
            }
        }
        initMethodDeltaInfos(page.getRecords());
        fillPublishedPath(page, interfaceManage);
        boolean hasAuth = checkPass(interfaceManage.getId(), UserSessionLocal.getUser().getUserId());
        //判断用户是不是对 方法 有权限的操作
        for (MethodManage methodManage : page.getRecords()) {
            methodManage.setHasAuth(0);
            if (hasAuth) {
                methodManage.setHasAuth(1);
            }
			/*if(InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())){
				methodManage.setJsfMockAlias(EasyMockRemoteService.DEFAULT_ALIAS);
			}
			if(InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType())){
				methodManage.setHttpMockPath(EasyMockRemoteService.buildUrlPath(interfaceManage,methodManage));
			}*/
            initMockInfo(methodManage, interfaceManage);

        }
        Page<MethodManageDTO> retPage = new Page<>(page.getCurrent(), page.getSize());
        retPage.setTotal(page.getTotal());
        List<MethodManageDTO> dtoRecords = page.getRecords().stream().map(methodManage -> {
            MethodManageDTO dto = new MethodManageDTO();
            BeanUtils.copyProperties(methodManage, dto);
            dto.setId(methodManage.getId() + "");
            return dto;
        }).collect(Collectors.toList());
        retPage.setRecords(dtoRecords);
        return retPage;
    }

    @Override
    public MethodManage getMethodExcludeBigTextField(Long id) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getId, id);
        excludeBigTextFiled(lqw);
        return getOne(lqw);
    }

    @Override
    public void updateMethodInterfaceId(List<Long> methodIds, Long interfaceId) {
        LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper();
        luw.set(MethodManage::getInterfaceId, interfaceId);
        luw.in(MethodManage::getId, methodIds);
        update(luw);

    }

    private void initMockInfo(MethodManage methodManage, InterfaceManage interfaceManage) {
        if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
            methodManage.setJsfMockAlias(EasyMockRemoteService.DEFAULT_ALIAS);
        }
        if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(methodManage.getType())) {
            methodManage.setHttpMockPath(testEasyMockRemoteService.buildUrlPath(interfaceManage, methodManage));
        }
    }

    public Page<MethodManageDTO> listDataSourceMethods(InterfaceManage manage, PageParam param) {
        InterfaceTypeEnum interfaceTypeEnum = InterfaceTypeEnum.getByCode(manage.getType());
        BeanTemplateDefinition beanDefinition = BeanStepDefinitionLoader.getBeanDefinition(interfaceTypeEnum.getDesc());

        List<MethodManageDTO> methods = beanDefinition.getMethods().stream().map(metadata -> {
            MethodManageDTO methodManage = new MethodManageDTO();
            methodManage.setName(metadata.getMethodName());
            methodManage.setDesc(metadata.getDesc());
            methodManage.setId(interfaceTypeEnum.getDesc() + "_" + metadata.getMethodName() + "_" + metadata.getInput().size());

            Map<String, Object> contentObject = buildBeanContent(beanDefinition.getInitConfigClass(), metadata);

            methodManage.setContentObject(contentObject);
            return methodManage;
        }).collect(Collectors.toList());
        Page page = new Page(param.getCurrent(), param.getSize());
        page.setTotal(methods.size());

        int start = Variant.valueOf((param.getCurrent() - 1) * param.getSize()).toInt();
        int end = Variant.valueOf((param.getCurrent()) * param.getSize()).toInt();

        if (start >= methods.size()) {
            page.setRecords(new ArrayList());
        } else {
            page.setRecords(methods.subList(start, Math.min(end, methods.size())));
        }


        return page;
    }

    private Map<String, Object> buildBeanContent(String initConfigClassName, MethodMetadata metadata) {
        Map<String, Object> contentObject = new HashMap<>();
        contentObject.put("input", metadata.getInput());
        contentObject.put("output", metadata.getOutput());
        contentObject.put("initConfigClassName", initConfigClassName);

        return contentObject;
    }

    @Override
    public void addListener(InterfaceChangeListener listener) {
        listeners.add(listener);
    }

    private MethodManageDTO getBeanMethod(String id) {
        String[] result = id.split("_");
        String type = result[0];
        String methodName = result[1];
        String methodCount = result[2];
        BeanTemplateDefinition beanDefinition = BeanStepDefinitionLoader.getBeanDefinition(type);
        MethodMetadata method = beanDefinition.getMethod(methodName, methodCount);
        if (method == null) return null;
        MethodManageDTO dto = new MethodManageDTO();
        dto.setId(id);
        dto.setDesc(method.getDesc());
        Map<String, Object> contentObject = buildBeanContent(beanDefinition.getInitConfigClass(), method);
        dto.setContentObject(contentObject);
        dto.setName(methodName);
        return dto;
    }
    @Override
    public MethodManageDTO getEntity(String methodIdOrBeanId){
        return getEntity(methodIdOrBeanId,null);
    }

    @Override
    public MethodManageDTO getEntity(String methodIdOrBeanId,FilterParam filter) {
        Guard.notEmpty(methodIdOrBeanId, "方法id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        if (!StringHelper.isNumber(methodIdOrBeanId)) {
            return getBeanMethod(methodIdOrBeanId);
        }
        Long id = Long.valueOf(methodIdOrBeanId);
        return getEntityByIdAndModel(id,filter);
    }

    /**
     * 获取流程编排的示例body值
     *
     * @param methodId
     * @return
     */
    public Object getFlowReqBodyDemoValue(Long methodId) {
        WorkflowDefinition def = flowService.loadFlowDef(methodId);
        if (def == null || def.getInput() == null) return null;
        if (ReqType.json.equals(def.getInput().getReqType())) {
            List<JsonType> body = def.getInput().getBody();
            if (!ObjectHelper.isEmpty(body)) {
                return body.get(0).toExprValue();
            }
        }
        return null;
    }

    public Object getMethodReqBodyDemoValue(Long methodId) {
        MethodManage manage = getById(methodId);
        if (InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) {
            JsfStepMetadata model = JsonUtils.parse(manage.getContent(), JsfStepMetadata.class);
            return ReqDemoBuildUtils.getJsfInputExampleValue(model);
        } else if (InterfaceTypeEnum.HTTP.getCode().equals(manage.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(manage.getType())) {
            HttpMethodModel model = JsonUtils.parse(manage.getContent(), HttpMethodModel.class);

            if (model.getInput() != null && "json".equals(model.getInput().getReqType())) {
                if (!ObjectHelper.isEmpty(model.getInput().getBody())) {
                    return model.getInput().getBody().get(0).toExprValue();
                }

            }

        }
        return null;
    }

    public Object getMethodExampleValue(Long methodId) {
        MethodManage manage = getById(methodId);
        FlowDebugLog debugLog = flowDebugLogService.getNewestDebugLog(methodId);
        if (debugLog != null) {
            return JsonUtils.parse(debugLog.getLogContent());
        }
        MethodManageDTO methodDto = getEntityById(methodId);
        Map<String, Object> map = new HashMap<>();
        if (InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) {
            Map<String, Object> inputData = new HashMap<>();
            Object child = null;
            try {
                child = JsonUtils.parse(methodDto.getDocConfig().getInputExample());
            } catch (Exception e) {

            }
            inputData.put("inputData", child);
            map.put("input", inputData);

        } else if (InterfaceTypeEnum.HTTP.getCode().equals(manage.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(manage.getType())) {
			/*Map<String,Object> inputData = new HashMap<>();
			inputData.put("inputData",methodDto.getDocConfig().getInputExample());
			map.put("input",inputData);*/
            Object input = null;
            try {
                input = JsonUtils.parse(methodDto.getDocConfig().getInputExample());
            } catch (Exception e) {

            }
            map.put("input", input);
        }


        return map;
    }

    public void initMethodDocConfig(MethodManage manage) {
        initMethodDocConfig(manage, false);
    }

    public void initMethodDocConfig(MethodManage manage, boolean onlyMockValue) {


        if (InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) {
            MethodDocConfig config = manage.getDocConfig();
            if (manage.getDocConfig() == null) {
                config = new MethodDocConfig();
                manage.setDocConfig(config);
            }
            JsfStepMetadata model = JsonUtils.parse(manage.getContent(), JsfStepMetadata.class);
            JsfDemoValue jsfDemoValue = null;

            if (StringHelper.isEmpty(config.getInputExample())
                    || StringHelper.isEmpty(config.getOutputExample())
            ) {
                jsfDemoValue = mockDataBuildService.buildJsfDemoValue(model, false, onlyMockValue);
            }

            if (StringHelper.isEmpty(config.getInputExample())) {
                config.setInputExample(JsonUtils.toJSONString(jsfDemoValue.getInputMockValue()));
            }
            if (StringHelper.isEmpty(config.getOutputExample())) {
                config.setOutputExample(JsonUtils.toJSONString(jsfDemoValue.getOutputMockValue()));
            }

        } else if (InterfaceTypeEnum.HTTP.getCode().equals(manage.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(manage.getType())) {
            HttpMethodModel model = JsonUtils.parse(manage.getContent(), HttpMethodModel.class);
            HttpDemoValue httpDemoValue = null;


            MethodDocConfig config = manage.getDocConfig();
            if (config == null) {
                config = new MethodDocConfig();
                manage.setDocConfig(config);
            }
            if (StringHelper.isEmpty(config.getInputExample())
                    || StringHelper.isEmpty(config.getOutputExample())
            ) {
                httpDemoValue = mockDataBuildService.buildHttpDemoValue(model, false, onlyMockValue);
            }

            if (StringHelper.isEmpty(config.getInputExample())) {
                config.setInputExample(ReqDemoBuildUtils.buildHttpInputDemoValue(model, httpDemoValue,manage.getFunctionId()));
            }
            if (StringHelper.isEmpty(config.getOutputExample())) {
                config.setOutputExample(JsonUtils.toJSONString(httpDemoValue.getOutputBody()));
            }
            config.setInputTypeScript(ReqDemoBuildUtils.buildInputTypeScript(manage.getContent()));
            config.setOutputTypeScript(ReqDemoBuildUtils.buildOutputTypeScript(manage.getContent()));
        }
    }


    public void initContentObject(IMethodInfo methodManageDTO) {
        initContentObject(methodManageDTO,0,null);
    }

    /**
     *
     * @param methodManageDTO
     * @param filter  source=2 color客户端必传信息
     */
    public void initContentObject(IMethodInfo methodManageDTO,Integer gateWayFlag,FilterParam filter) {
        String content = methodManageDTO.getContent();
        InterfaceTypeEnum type = InterfaceTypeEnum.getByCode(methodManageDTO.getType());
        switch (type) {
            case HTTP:
                HttpMethodModel httpMethodModel = JsonUtils.parse(content, HttpMethodModel.class);
                httpMethodModel.setType("http");
                httpMethodModel.initEmptyValue();
                if (null!=gateWayFlag&&gateWayFlag==1) {
                   setColorGateWayParam(httpMethodModel,methodManageDTO,filter);
                }

                if(methodManageDTO instanceof MethodManageDTO){
                    httpMethodModel.getInput().setUrl(((MethodManageDTO)methodManageDTO).getPath());
                }else  if(methodManageDTO instanceof MethodManage){
                    httpMethodModel.getInput().setUrl(((MethodManage)methodManageDTO).getPath());
                }
                methodManageDTO.setContentObject(httpMethodModel);

                //methodManageDTO.setContent(null);
                break;
            case EXTENSION_POINT:
                HttpMethodModel httpMethodModel2 = JsonUtils.parse(content, HttpMethodModel.class);
                httpMethodModel2.setType("http");
                httpMethodModel2.initEmptyValue();
                methodManageDTO.setContentObject(httpMethodModel2);
                break;
            case WEB_SERVICE:
                WebServiceMethod webServiceMethod = JsonUtils.parse(content, WebServiceMethod.class);
                methodManageDTO.setContentObject(webServiceMethod);
                //methodManageDTO.setContent(null);
                break;
            case JSF:
                JsfStepMetadata contentJSFObject = JsonUtils.parse(content, JsfStepMetadata.class);
                contentJSFObject.initEmptyValue();
                fillParentType(contentJSFObject.getInput(), null);
                List<JsonType> outPuts = new ArrayList();
                if (Objects.nonNull(contentJSFObject.getOutput())) {
                    outPuts.add(contentJSFObject.getOutput());
                }
                fillParentType(outPuts, null);
                if (null!=gateWayFlag&&gateWayFlag==1) {
                   setColorGateWayParam(contentJSFObject,methodManageDTO,filter);
                }
                methodManageDTO.setContentObject(contentJSFObject);
                //methodManageDTO.setContent(null);
                break;
            case ORCHESTRATION:
                Object contentORCHESTRATIONObject = JsonUtils.parse(content, Map.class);
                methodManageDTO.setContentObject(contentORCHESTRATIONObject);

                break;
            default:
                methodManageDTO.setContentObject(JsonUtils.parse(content, Map.class));
                break;
        }
    }

    public void fillParentType(List<? extends JsonType> body, List<String> parentType) {
        if (CollectionUtils.isEmpty(body)) {
            return;
        }
        if (null == parentType) {
            parentType = new ArrayList<>();
        }
        for (JsonType jsonType : body) {
            jsonType.setParentTypeName(parentType);
            List<String> parentTypes = new ArrayList<>();
            parentTypes.addAll(parentType);
            if (jsonType.getType().equals("ref")) {
                RefObjectJsonType refType = (RefObjectJsonType) jsonType;
                List<JsonType> child = ((RefObjectJsonType) jsonType).getChildren();
                parentTypes.add(refType.getRefName());
                fillParentType(child, parentTypes);
            }
            if (jsonType.getType().equals("object") || jsonType.getType().equals("array")) {
                List<JsonType> child = ((ComplexJsonType) jsonType).getChildren();
                fillParentType(child, parentTypes);
            }
        }
    }

    public void setColorGateWayParam(Object param,IMethodInfo info,FilterParam filter){
        String id = StringUtils.EMPTY;
        if (info instanceof MethodManageDTO) {
            MethodManageDTO dto = (MethodManageDTO) info;
            id = dto.getFunctionId();
        } else if (info instanceof MethodManage) {
            MethodManage dto = (MethodManage) info;
            id = dto.getFunctionId();
        }
        List<JsonType> gateWayParam = colorGatewayService.querytParamByFunctionId(id, filter);
        //查询默认网关参数
//        if (CollectionUtils.isEmpty(gateWayParam)) {
//            gateWayParam = colorGatewayService.queryColorGateParam(null, filter);
//        }
        if(CollectionUtils.isEmpty(gateWayParam)){
            return;
        }
        Map<Integer, List<JsonType>> params = gateWayParam.stream().collect(Collectors.groupingBy(JsonType::getColorType));
        ColorGatewayParamDto input = new ColorGatewayParamDto();
        input.setHeaders(sorted(params.get(ColorTypeEnum.requestHeader.getCode())));
        input.setParams(sorted(params.get(ColorTypeEnum.requestParam.getCode())));
        for (JsonType item : input.getParams()) {
            if (item.getName().equals("functionId")) {
                item.setValue(id);
            }
            if (item.getName().equals("t")) {
                item.setValue(System.currentTimeMillis());
            }
        }
        ColorGatewayParamDto outPut = new ColorGatewayParamDto();
        outPut.setHeaders(sorted(params.get(ColorTypeEnum.responseHeader.getCode())));
        if (param instanceof HttpMethodModel) {
            HttpMethodModel httpMethodModel = (HttpMethodModel) param;
            httpMethodModel.setColorInput(input);
            httpMethodModel.setColorOutput(outPut);
        }
        if (param instanceof JsfStepMetadata) {
            JsfStepMetadata jsfData = (JsfStepMetadata) param;
            jsfData.setColorInput(input);
            jsfData.setColorOutput(outPut);
        }
    }

    private List<JsonType> sorted(List<JsonType> params){
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(params)) {
            params.sort((o1,o2)->{
                //优先展示排序
                if (o1.getDefaultShow() == 1 && o2.getDefaultShow() != 1) {
                    return -1;
                } else if (o1.getDefaultShow() != 1 && o2.getDefaultShow() == 1) {
                    return 1;
                } else if (o1.getMark().getValue() == 2 && o2.getMark().getValue() != 2) {
                    return -1;
                } else if (o1.getMark().getValue() != 2 && o2.getMark().getValue() == 2) {
                    return 1;
                }
                return Integer.compare(o2.getIsAppNecessary().getValue(), o1.getIsAppNecessary().getValue());
            });
        }
        return params;
    }

    public void initMethodRefAndDelta(List<MethodManage> methodManages, Long appId) {

        for (MethodManage methodManage : methodManages) {
            initContentObject(methodManage);
        }
        refJsonTypeService.initMethodRefInfos(methodManages, appId);
        initMethodDeltaInfos(methodManages);
    }

    public List<MethodManage> listScoreFields(List<Long> ids, Long appId) {
        if (ids.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.select(MethodManage::getId, MethodManage::getType, MethodManage::getContent, MethodManage::getDocInfo);
        lqw.in(MethodManage::getId, ids);
        List<MethodManage> methodManages = list(lqw);
        initMethodRefAndDelta(methodManages, appId);
        return methodManages;
    }



    public Page<DemandDetailDTO> getDemandByInterfaceId(Long interfaceId, long current, long size){
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Page<DemandDetailDTO> res = new Page<>();
        res.setSize(size);
        res.setCurrent(current);
        if (null == interfaceManage) {
            return res;
        }
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getYn,1);
        lqw.isNotNull(RequirementInfo::getRelatedRequirementCode);
        lqw.inSql(RequirementInfo::getId, "select requirement_id from requirement_interface_group where interface_id='" + interfaceManage.getId() + "'");
        Page<RequirementInfo> infos = requirementInfoService.page(new Page(current,size),lqw);
        res.setTotal(infos.getTotal());
        List<DemandDetailDTO> data = new ArrayList<>();
        for (RequirementInfo info : infos.getRecords()) {
            if(StringUtils.isBlank(info.getRelatedRequirementCode())){
                continue;
            }
            CommonResult<Map<String,Object>> detail = jagileRemoteCaller.getDemandByCodeT(info.getRelatedRequirementCode());
            log.info("test-->{}",JSONObject.toJSONString(detail));
            DemandDetail detail1=JSONObject.parseObject(JSONObject.toJSONString(detail.getData()),DemandDetail.class);
            DemandUserResponse relatedUsers = detail1.getRelatedUsers();
            String projectName = detail1.getPmpProjectName();
            String projectCode = detail1.getPmpProjectCode();
            Date cTime = detail1.getCTime();
            DemandDetailDTO dto=new DemandDetailDTO();
            dto.setName(info.getName());
            dto.setCode(info.getRelatedRequirementCode());
            dto.setProject(projectName);
            dto.setProjectCode(projectCode);
            List<UserInfoDTO> dtos=new ArrayList();
            UserInfoDTO recipientor=new UserInfoDTO();
            recipientor.setErp(relatedUsers.getRecipient().getErp());
            recipientor.setUserName(relatedUsers.getRecipient().getName());
            dtos.add(recipientor);
            dto.setMembers(dtos);
            dto.setCreated(cTime);
            dto.setLevel("1");
            data.add(dto);
        }
        res.setRecords(data);
        return res;
    }

    @Override
    public MethodManageDTO getEntityById(Long id) {
        return  getEntityByIdAndModel(id,null);
    }


    /**
     * color客户端必传信息
     * @param id
     * @param filter
     * @return
     */
    public MethodManageDTO getEntityByIdAndModel(Long id,FilterParam filter) {
        MethodManage methodManage = methodManageMapper.selectById(id);
        if (methodManage == null) {
            throw new BizException("该记录不存在");
        }
        Integer gateWayFlag=StringUtils.isNotBlank(methodManage.getFunctionId())?1:0;
        methodManage.initKey();
        InterfaceManage interfaceManage = interfaceManageService.getOneById(methodManage.getInterfaceId());
        Set<String> tags = relationMethodTagService.queryTagNames(id, interfaceManage.getAppId());
        initMockInfo(methodManage, interfaceManage);
        initMethodDocConfig(methodManage);
        MethodManageDTO methodManageDTO = new MethodManageDTO();
        BeanUtils.copyProperties(methodManage, methodManageDTO);
        methodManageDTO.setId(methodManage.getId() + "");
        methodManageDTO.setTags(tags);
        methodManageDTO.setInterfaceName(interfaceManage.getServiceCode());
        log.info("MethodManageServiceImpl.getById id={},code={},name={}", methodManageDTO.getId(), methodManageDTO.getMethodCode(), methodManageDTO.getName());

        InterfaceTypeEnum type = InterfaceTypeEnum.getByCode(methodManageDTO.getType());
        Guard.notEmpty(type, "类型未知!");
//        String content = methodManageDTO.getContent();
        initContentObject(methodManageDTO,gateWayFlag,filter);
        boolean hasRef = refJsonTypeService.initMethodRefInfos(methodManageDTO, interfaceManage.getAppId());
        if (isAuthReportMethod(interfaceManage, methodManage) || hasRef) {
            deltaInfoService.initDeltaInfo(methodManageDTO);
        }

        enumsService.initContentEnums(methodManageDTO);

        methodManageDTO.setContent(JsonUtils.toJSONString(methodManageDTO.getContentObject()));
        //转换的webservice必须是发布成功后才展示path
        if (methodManageDTO.getParentId() != null && methodManageDTO.getParentId() > 0
                && PublishEnum.NO.getCode().equals(methodManageDTO.getPublished())) {
            methodManageDTO.setPath(null);
        }
        //判断方法是否有被操作的权限
        methodManageDTO.setHasAuth(0);
        final Long appId = interfaceManage.getAppId();
        if (appId != null) {
            methodManageDTO.setAppId(appId);
            final AppInfo app = appInfoService.getById(appId);

            if (app != null) {
                methodManageDTO.setAppCode(app.getAppCode());
                methodManageDTO.setAppName(app.getAppName());
            }


        }

        if (checkPass(interfaceManage.getId(), UserSessionLocal.getUser().getUserId())) {
            methodManageDTO.setHasAuth(1);
        }
        methodManageDTO.setAutoReport(interfaceManage.getAutoReport());
        methodManageDTO.setIsPublic(interfaceManage.getIsPublic());

        return methodManageDTO;
    }

    @Override
    public boolean updateRelatedId(Long interfaceId, Long relatedId) {
        Guard.notEmpty(interfaceId, "无效的接口id");
        LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper<>();
        luw.eq(MethodManage::getId, interfaceId);
        luw.set(MethodManage::getRelatedId, relatedId);
        update(luw);
        return true;
    }

    private boolean checkPass(Long interfaceId, String userCode) {
        return userPrivilegeHelper.hasInterfaceRole(interfaceId, userCode);
    }

    private void deepForChild(Map map, JsonType schemaType) {
        if (schemaType == null)
            return;
        if (BODY.equals(schemaType.getName())) {
            map.put(BODY, schemaType);
            return;
        } else if (HEADER.equals(schemaType.getName())) {
            map.put(HEADER, schemaType);
            return;
        }

        List<JsonType> children = null;
        if (JsonTypeEnum.OBJECT.getName().equals(schemaType.getType())) {
            ObjectJsonType objectJsonType = (ObjectJsonType) schemaType;
            children = objectJsonType.getChildren();
        } else if (JsonTypeEnum.ARRAY.getName().equals(schemaType.getType())) {
            ArrayJsonType arrayJsonType = (ArrayJsonType) schemaType;
            children = arrayJsonType.getChildren();
        }
        if (children == null) {
            return;
        }
        for (JsonType child : children) {
            deepForChild(map, child);
        }
    }

    @Transactional
    @Override
    public Boolean updateWebService(MethodManageDTO methodManageDTO) {
        Guard.notEmpty(methodManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(methodManageDTO.getInterfaceId(), "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        InterfaceManage interfaceManage = interfaceManageService.getOneById(methodManageDTO.getInterfaceId());
        String wsdlPath = interfaceManage.getPath();

        try {
            List<EnvModel> envList = new ArrayList<>();
            if (StringUtils.isNotBlank(interfaceManage.getEnv())) {
                envList = JsonUtils.parseArray(interfaceManage.getEnv(), EnvModel.class);
            }

            List<MethodManage> list = wsdlToMethod(methodManageDTO.getInterfaceId(), wsdlPath, envList);
            mergeMethods(list, methodManageDTO.getInterfaceId());
            if (EmptyUtil.isEmpty(list)) {
                return Boolean.FALSE;
            }
            //删除原接口
		/*	MethodManageDTO methodManageForRemove = new MethodManageDTO();
			methodManageForRemove.setInterfaceId(methodManageDTO.getInterfaceId());
			removeByInterfaceId(methodManageForRemove);*/

            interfaceManage.setEnv(JsonUtils.toJSONString(envList));
            interfaceManageService.updateById(interfaceManage);
            //批量保存
            /*saveBatch(list);*/
        } catch (Exception e) {
            log.error("method.err_update_web_service:wsdl={}", methodManageDTO.getPath(), e);
            throw StdException.adapt(e);
            //return false;
        }
        return Boolean.TRUE;
    }

    public void updateJsfMethods(JsfImportDto dto, InterfaceManage interfaceManage) {
        MavenJarLocation location = new MavenJarLocation();
        location.setGroupId(dto.getGroupId());
        location.setArtifactId(dto.getArtifactId());
        location.setVersion(dto.getVersion());
        final List<JsfStepMetadata> jsfStepMetadata = JarParser.parseJsfInterface(location, dto.getServiceCode());

        List<MethodManage> addOrUpdateMethods = new ArrayList<>(jsfStepMetadata.size());
        for (JsfStepMetadata jsfMetadata : jsfStepMetadata) {
            MethodManage manage = new MethodManage();
            manage.setMethodCode(jsfMetadata.getMethodName());
            manage.setName(jsfMetadata.getMethodName());
            manage.setInterfaceId(interfaceManage.getId());
            manage.setType(InterfaceTypeEnum.JSF.getCode());
            manage.setFunctionId(dto.getFunctionId());
            if(StringUtils.isNotBlank(dto.getFunctionId())){
                manage.setMethodTag(MethodTagEnum.COLOR.getCode());
            }
            manage.setParamCount(jsfMetadata.getInput().size());
            manage.setContent(JsonUtils.toJSONString(jsfMetadata));
            fillMethodDigest(manage);


            //initMethodRefAndDelta(Collections.singletonList(cloned),interfaceManage.getAppId());


            addOrUpdateMethods.add(manage);
        }


        mergeMethods(addOrUpdateMethods, interfaceManage.getId(), dto.skipListener());


    }


    public void excludeBigTextFiled(LambdaQueryWrapper<MethodManage> lqw) {
        lqw.select(MethodManage.class, x -> {
            String[] bigTextFields = new String[]{"content", "doc_info", "doc_config"};
            return Arrays.asList(bigTextFields).indexOf(x.getColumn()) == -1;
        });

    }

    public List<MethodManage> getInterfaceMethods(Long interfaceId) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper();
        lqw.eq(MethodManage::getInterfaceId, interfaceId);
        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode());
        excludeBigTextFiled(lqw);
        final List<MethodManage> methods = list(lqw);
        initMethodDeltaInfos(methods);
        return methods;
    }

    private void updateMethodDigest(MethodManage methodManage) {
        try {
            initContentObject(methodManage);
        } catch (Exception e) {
            log.error("json.err_init_content_object:id={}", methodManage.getId());
            return;
        }
        String digest = null;
        if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
            digest = DigestUtils.getJsfMethodMd5(methodManage, (JsfStepMetadata) methodManage.getContentObject());
        } else if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(methodManage.getType())) {
            digest = DigestUtils
                    .getHttpMethodMd5(methodManage, (HttpMethodModel) methodManage.getContentObject());
        }
        if (methodManage.getDigest() == null || !methodManage.getDigest().equals(digest)) {
            methodManage.setDigest(digest);
            LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper<>();
            luw.set(MethodManage::getDigest, methodManage.getDigest());
            luw.eq(MethodManage::getId, methodManage.getId());
            update(luw);
        }

    }

    @Override
    public void updateMethodDigest() {
        int count = 0;
        {
            LambdaQueryWrapper<MethodManage> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.in(MethodManage::getType, Arrays.asList(InterfaceTypeEnum.HTTP.getCode(), InterfaceTypeEnum.JSF.getCode()));
            countWrapper.eq(MethodManage::getYn, 1);
            count = count(countWrapper);
        }

        int pageSize = 1000;
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getYn, 1);
        lqw.in(MethodManage::getType, Arrays.asList(InterfaceTypeEnum.HTTP.getCode(), InterfaceTypeEnum.JSF.getCode()));
        lqw.select(MethodManage.class, x -> {
            String[] bigTextFields = new String[]{"content", "id", "digest", "type", "http_method",
                    "desc", "doc_info", "name", "method_code", "path"};
            String column = x.getColumn().replace("`", "");
            return Arrays.asList(bigTextFields).indexOf(column) != -1;
        });
        for (int i = 0; i < count / pageSize + 1; i++) {
            Long pageNo = Long.valueOf(i + 1);
            Page<MethodManage> page = page(new Page<>(pageNo, Long.valueOf(pageSize)), lqw);
            for (MethodManage record : page.getRecords()) {
                updateMethodDigest(record);
            }
        }


    }

    @Override
    public List<MethodManage> fixNoContentMethodsDigest(List<MethodManage> noContentMethods) {
        if (ObjectHelper.isEmpty(noContentMethods)) return Collections.emptyList();
        List<Long> ids = noContentMethods.stream().map(item -> item.getId()).collect(Collectors.toList());
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
		 /*lqw.select(MethodManage.class, x -> {
		 	return "id".equals(x.getColumn()) || "content".equals(x.getColumn()) || "path".equals(x.getColumn());
		 });*/
        lqw.in(MethodManage::getId, ids);
        List<MethodManage> methods = list(lqw);
        for (MethodManage method : methods) {
            initContentObject(method);
            fillMethodDigest(method);
            MethodManage newMethod = new MethodManage();
            newMethod.setId(method.getId());
            newMethod.setDigest(method.getDigest());
            updateById(newMethod);
        }
        return methods;
    }

    void mergeMethods(List<MethodManage> newMethods, Long interfaceId) {
        mergeMethods(newMethods, interfaceId, false);
    }

    public void mergeMethods(List<MethodManage> newMethods, Long interfaceId, boolean skipListener) {

        List<Long> removedMethodIds = new ArrayList<>();
        List<MethodManage> added = new ArrayList<>();
        List<MethodManage> updated = new ArrayList<>();
        List<MethodManage> oldMethods = getInterfaceMethods(interfaceId);
        Map<String, List<MethodManage>> newMethodsMap = newMethods.stream().collect(Collectors.groupingBy(MethodManage::getMethodCode));
        Map<String, List<MethodManage>> oldMethodsMap = oldMethods.stream().collect(Collectors.groupingBy(MethodManage::getMethodCode));
        for (MethodManage newMethod : newMethods) {
            List<MethodManage> matchMethods = oldMethodsMap.get(newMethod.getMethodCode());
            if (CollectionUtils.isEmpty(matchMethods)) {
                added.add(newMethod);
            } else {
                newMethod.setId(matchMethods.get(0).getId());
                updated.add(newMethod);
            }
        }
        List<MethodManage> remvedMethods = new ArrayList<>();
        for (MethodManage oldMethod : oldMethods) {
            if (!newMethodsMap.containsKey(oldMethod.getMethodCode())) {
                removedMethodIds.add(oldMethod.getId());
                remvedMethods.add(oldMethod);
            }
        }
        removeMethodByIds(removedMethodIds);
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        if (!remvedMethods.isEmpty()) {
            for (InterfaceChangeListener listener : listeners) {
                listener.onMethodRemove(interfaceManage, remvedMethods);
            }
        }


        for (MethodManage methodManage : updated) {
            updateById(methodManage);


        }
        if (!skipListener) {
            for (InterfaceChangeListener listener : listeners) {
                listener.onMethodUpdate(interfaceManage, updated);
            }
        }
        saveBatch(added);

        if (!skipListener) {
            for (InterfaceChangeListener listener : listeners) {
                listener.onMethodAdd(interfaceManage, added);
            }
        }

    }

    @Override
    public Long copyJsfMethod(Long groupId, MethodManageDTO methodManageDTO) {
        MethodGroupTreeDTO dto = null;
        if (null != groupId) {
            dto = methodGroupService.findMethodGroupTree(methodManageDTO.getInterfaceId());
        }

        InterfaceManage interfaceManage = interfaceManageService.getById(methodManageDTO.getInterfaceId());
        MethodManage newMethod = new MethodManage();
        BeanUtils.copyProperties(methodManageDTO, newMethod);
        newMethod.setYn(DataYnEnum.VALID.getCode());
        newMethod.setPublished(PublishEnum.NO.getCode());
        initContentObject(newMethod);
        fillMethodDigest(newMethod);
        newMethod.setMergedContentDigest(newMethod.getDigest());
        save(newMethod);
        if (dto != null) {
            if (null != dto) {
                dto.insertMethod(groupId, newMethod.getId());
                interfaceManage.setSortGroupTree(dto.getTreeModel());
                interfaceManage.setGroupLastVersion(DateUtil.getCurrentDateMillTime());
                interfaceManageService.updateById(interfaceManage);
            }
        }
        List<MethodManage> added = new ArrayList<>();
        added.add(newMethod);
        for (InterfaceChangeListener listener : listeners) {
            listener.onMethodAdd(interfaceManage, added);
        }
        return newMethod.getId();
    }


    @Override
    public Boolean checkWsdlPath(String path) {
        log.info("checkWsdlPath path={}", path);
        try {
            if (EmptyUtil.isEmpty(path)) {
                return Boolean.FALSE;
            }
            List<MethodManage> methodManages = wsdlToMethod(0L, path, new ArrayList<>());
            if (EmptyUtil.isEmpty(methodManages)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (StdException e) {
            throw e;
        } catch (Exception e) {
            log.error("checkWsdlPath happen error!", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 根据wsdl生成方法
     *
     * @param interfaceId
     * @param wsdlPath
     * @throws Exception
     * @date: 2022/5/25 17:53
     * @author wubaizhao1
     */
    @Transactional
    @Override
    public List<MethodManage> wsdlToMethod(Long interfaceId, String wsdlPath, List<EnvModel> envModels) throws Exception {
        if (EmptyUtil.isEmpty(wsdlPath)) {
            return new ArrayList<>();
        }
        //解析出xml的结构
        WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature("javax.wsdl.verbose", false);
        Definition definition = reader.readWSDL(wsdlPath);
        //xml内容转换
        SoapContext context = SoapContext.DEFAULT;
        SoapMessageBuilder soapMessageBuilder = new SoapMessageBuilder(new URL(wsdlPath));
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlPath));
        //解析出除基础url以外的路径
        String documentBaseURI = definition.getDocumentBaseURI();
        /**
         * url = http://127.0.0.1:7001/FullTypedWebService
         * ServiceName = FullTypedWebService
         * env = http://127.0.0.1:7001
         */
        String url = documentBaseURI.substring(0, documentBaseURI.lastIndexOf('?'));
        String env = url.substring(0, url.lastIndexOf('/'));
        String ServiceName = url.substring(url.lastIndexOf('/') + 1);

        //输出一个方法list
        List<MethodManage> result = new ArrayList<>();
        //每个方法
        Map<Binding, String> bindingUrl = getBindingUrl(definition);
        if (bindingUrl.isEmpty()) {
            throw new BizException("wsdl无效,不存在http绑定协议");
        }

        String baseUrl = getBaseUrl(bindingUrl.entrySet().iterator().next().getValue());
        Optional<EnvModel> any = envModels.stream().filter(envModel -> {
            if (envModel.getUrl().isEmpty()) return false;
            return baseUrl.equals(envModel.getUrl().get(0));
        }).findAny();
        if (!any.isPresent()) {
            envModels.add(new EnvModel("正式环境", baseUrl, EnvTypeEnum.RELEASE));
        }

        for (Object o : definition.getAllBindings().entrySet()) {
            Map.Entry<QName, Binding> entry = (Map.Entry<QName, Binding>) o;
            Binding binding = entry.getValue();
            try {
                SoapVersion soapVersion = SoapMessageBuilder.getSoapVersion(binding);
                if (!SoapVersion.Soap11.equals(soapVersion)) {
                    continue;
                }
            } catch (SoapBuilderException e) {
                continue;
            }

            String serviceUrl = bindingUrl.get(binding);
            String path = serviceUrl.substring(baseUrl.length());// 服务path
            List<BindingOperation> operations = binding.getBindingOperations();

            for (BindingOperation operation : operations) {
                String methodName = operation.getName();
                log.info("wsdlToSoap.methodName={}", methodName);
                //组装入参
                String inputMsg = soapMessageBuilder.buildSoapMessageFromInput(entry.getValue(), operation, context); // 构造示例输入数据
                BuilderJsonType schemaTypeInput = transformer.buildSoapMessageFromInput(binding, operation, SoapContext.DEFAULT);
                Map<String, Object> schemaTypeInputJson = schemaTypeInput.toJson();
                String schemaTypeInputStr = JsonUtils.toJSONString(schemaTypeInputJson);
                log.info("wsdlToSoap.inputMsg={}", inputMsg);
                log.info("wsdlToSoap.schemaTypeInputStr={}", schemaTypeInputStr);
                //组装出参
                String outMsg = soapMessageBuilder.buildSoapMessageFromOutput(entry.getValue(), operation, context); // 构造示例输出数据
                BuilderJsonType schemaTypeOutput = transformer.buildSoapMessageFromOutput(binding, operation, SoapContext.DEFAULT);
                Map<String, Object> schemaTypeOutputJson = schemaTypeOutput.toJson();
                String schemaTypeOutputStr = JsonUtils.toJSONString(schemaTypeOutputJson);
                log.info("wsdlToSoap.outMsg={}", outMsg);
                log.info("wsdlToSoap.schemaTypeOutputStr={}", schemaTypeOutputStr);
                SOAPOperation soapOperation = (SOAPOperation) WsdlUtils.getExtensiblityElement(operation.getExtensibilityElements(), SOAPOperation.class);
                //生成出方法体
                WebServiceMethod webServiceMethod = WebServiceMethod.builder()
                        .type(InterfaceTypeEnum.WEB_SERVICE.getDesc())
                        .methodName(methodName)
                        .soapAction(soapOperation.getSoapActionURI())
                        .input(WebServiceMethod.WebServiceMethodIO.builder().demoXml(inputMsg).schemaType(schemaTypeInput).build())
                        .output(WebServiceMethod.WebServiceMethodIO.builder().demoXml(outMsg).schemaType(schemaTypeOutput).build())
                        .build();

                String Methodcontent = JsonUtils.toJSONString(webServiceMethod);

                Map parts = operation.getOperation().getInput().getMessage().getParts();
                /**
                 * 添加到方法表中
                 */
                MethodManage methodManage = new MethodManage();
                methodManage.setName(methodName);
                methodManage.setContent(Methodcontent);
                methodManage.setDesc(WsdlUtils.getOperationDesc(operation.getOperation()));
                methodManage.setMethodCode(methodName);
                methodManage.setType(InterfaceTypeEnum.WEB_SERVICE.getCode());
                methodManage.setInterfaceId(interfaceId);
                methodManage.setHttpMethod("post");
                methodManage.setPath(path);//methodManageDTO.setPath(url);
                methodManage.setParamCount(parts.size());
                //调用地址 发布后才能调用
                //调用环境 http转webservice才有调用环境
                methodManage.setCallEnv(null);
                //父方法id
                methodManage.setParentId(null);
                //直接生成的webservice接口是否有发布状态?
                methodManage.setPublished(PublishEnum.NO.getCode());
                //直接生成的webservice接口是否有拓展属性
                methodManage.setExtConfig("");
                result.add(methodManage);
            }
        }
        return result;
    }

    private Map<Binding, String/* url*/> getBindingUrl(Definition definition) throws MalformedURLException {
        Map<Binding, String/* url*/> result = new LinkedHashMap<>();

        for (Object o : definition.getServices().entrySet()) {
            Map.Entry<QName, javax.wsdl.Service> entry = (Map.Entry<QName, javax.wsdl.Service>) o;
            for (Object o1 : entry.getValue().getPorts().entrySet()) {
                Map.Entry<String, Port> portEntry = (Map.Entry) o1;
                List extensibilityElements = portEntry.getValue().getExtensibilityElements();
                SOAPAddress address = WsdlUtils.getExtensiblityElement(extensibilityElements, SOAPAddress.class);
                if (address == null) {
                    continue;
                }
                result.put(portEntry.getValue().getBinding(), address.getLocationURI());
            }
        }
        String baseUrl = null;
        for (Map.Entry<Binding, String> bindingStringEntry : result.entrySet()) {
            String newUrl = getBaseUrl(bindingStringEntry.getValue());
            if (baseUrl != null && !newUrl.equals(baseUrl)) {
                throw new BizException("服务的域名必须一致，发现存在2个地址：" + newUrl + "," + baseUrl);
            }
            baseUrl = newUrl;
        }
        return result;
    }

    private String getBaseUrl(String location) throws MalformedURLException {
        URL url = new URL(location);
        String ret = url.getProtocol() + "://" + url.getHost();
        if (url.getPort() != -1) {
            ret += ":" + url.getPort();
        }
        return ret;
    }


    /**
     * 方法调试
     *
     * @param invokeMethodDTO
     * @return
     * @date: 2022/5/19 17:23
     * @author wubaizhao1
     */
    @Override
    public Object invokeMethod(InvokeMethodDTO invokeMethodDTO) {
        try {
            log.info("MethodManageServiceImpl.invokeMethod param={}", invokeMethodDTO);
            InterfaceTypeEnum interfaceType = InterfaceTypeEnum.getByName(invokeMethodDTO.getType());
            Guard.notEmpty(interfaceType, "类型未知!");

            Object result = null;
            if (interfaceType == InterfaceTypeEnum.HTTP || interfaceType == InterfaceTypeEnum.EXTENSION_POINT) {
                result = invokeHttpMethodNew(invokeMethodDTO);
            }
            return result;
        } catch (Exception e) {
            log.info("MethodManageServiceImpl.invokeMethod unknown error! param={}", invokeMethodDTO, e);
            throw e;
        }
    }

    @Override
    public Object invokeDataSourceMethod(DataSourceInvokeDto dto) {
        Guard.notEmpty(dto.getInterfaceId(), "接口id不可为空");
        InterfaceManage interfaceManage = interfaceManageService.getById(dto.getInterfaceId());
        Guard.notEmpty(interfaceManage, "无效的接口id");

        String desc = InterfaceTypeEnum.getByCode(interfaceManage.getType()).getDesc();

        List<String> beanInfo = StringHelper.split(dto.getMethodId(), "_");
        BeanTemplateDefinition beanDefinition = BeanStepDefinitionLoader.getBeanDefinition(beanInfo.get(0));
        MethodMetadata methodMetadata = beanDefinition.getMethod(beanInfo.get(1), beanInfo.get(2));

        BeanStepProcessor processor = new BeanStepProcessor();
        BeanStepMetadata metadata = new BeanStepMetadata();
        metadata.setInput(dto.getInput());
        metadata.setMethodName(methodMetadata.getMethodName());
        metadata.setBeanType(desc);

        metadata.setInitConfigClass(beanDefinition.getInitConfigClass());
        StepContext stepContext = new StepContext();
        ParametersUtils utils = new ParametersUtils();
        //final Map<String, Object> initValue = utils.getJsonInputValue(dto.getInput(), stepContext);

        metadata.setInitConfigValue(interfaceManage.getConfig());

        metadata.setInput(dto.getInput());

        processor.init(metadata);

        Step current = new Step();
        current.setContext(stepContext);
        processor.process(current);
        return current.getOutput();
    }


    public HttpOutput invokeWebService(String basePath, String cookie, CallHttpToWebServiceReqDTO dto) {
        Guard.notEmpty(dto.getMethodId(), "方法id不可为空");
        Guard.notEmpty(dto.getEnvName(), "环境不可为空");
        MethodManage methodManage = getById(dto.getMethodId());
        Guard.notEmpty(methodManage, "无效的方法id");
        if (!Objects.equals(InterfaceTypeEnum.WEB_SERVICE.getCode(), methodManage.getType())) {
            throw new BizException("非webservice接口");
        }
        InterfaceManage interfaceManage = interfaceManageService.getOneById(methodManage.getInterfaceId());

        List<EnvModel> envList = interfaceManage.getEnvList();
        EnvModel envModel = envList.stream().filter(x -> x.getEnvName().equals(dto.getEnvName())).findFirst().get();
        List<String> urlList = envModel.getUrl();
        String callUrl = "";
        if (methodManage.getParentId() != null) { // parent id不为空，调用的是父节点,说明是转换接口
            callUrl = WebServiceHelper.getWebServiceCallUrl(basePath, methodManage.getInterfaceId(), methodManage.getId());
        } else {
            if (urlList == null || urlList.isEmpty()) {
                throw new BizException("该环境下没有对应的服务提供者");
            }
            String endpointUrl = urlList.get(0);
            String path = methodManage.getPath();
            callUrl = endpointUrl + path;
        }

        WebServiceMethod method = JsonUtils.parse(methodManage.getContent(), WebServiceMethod.class);

        HttpOutput output = WebServiceHelper.debugWebServiceMethod(dto, cookie, callUrl,
                method);
        if (output.isSuccess() && output.getBody() != null) {
            if (output.getBody().toString().startsWith("<")) {
                WebServiceMethod.WebServiceMethodIO methodOutput = method.getOutput();
                JsonType schemaTypeOutput = methodOutput.getSchemaType();
                Object responseJson = SoapUtils.soapXmlToJson(output.getBody().toString(), schemaTypeOutput);

                HttpOutput.HttpResponse response = new HttpOutput.HttpResponse();
                response.setBody(responseJson);
                output.setResponse(response);
            }

        }

        return output;
    }

    @Override
    public MethodDocConfig updateDocConfig(UpdateMethodConfigDto dto) {
        MethodManage entity = getById(dto.getMethodId());
        Guard.notEmpty(entity, "无效的方法id");
        MethodDocConfig docConfig = entity.getDocConfig();
        if (InterfaceTypeEnum.HTTP.getCode().equals(entity.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(entity.getType())) {
            if (docConfig == null) {
                docConfig = new HttpMethodDocConfig();
            }
        } else if (InterfaceTypeEnum.JSF.getCode().equals(entity.getType())) {
            if (docConfig == null) {
                docConfig = new JsfMethodDocConfig();
            }
        }
        entity.setDocConfig(docConfig);
        try {
            org.apache.commons.beanutils.BeanUtils.setProperty(docConfig, dto.getField(), dto.getFieldValue());
        } catch (Exception e) {
            throw new BizException("设置失败");
        }

        updateById(entity);
        return docConfig;
    }

    /**
     * 填充摘要信息
     *
     * @param methodManage
     */
    @Override
    public void fillMethodDigest(IMethodInfo methodManage) {
        if (StringUtils.isNotBlank(methodManage.getContent())
                && methodManage.getContentObject() == null
        ) {
            initContentObject(methodManage);
        }

        methodManage.setDigest(getContentObjectDigest(methodManage));
    }

    public String getContentObjectDigest(IMethodInfo methodManage) {
        if (StringUtils.isNotBlank(methodManage.getContent())
                && methodManage.getContentObject() == null
        ) {
            initContentObject(methodManage);
        }
        if (InterfaceTypeEnum.JSF.getCode().equals(methodManage.getType())) {
            return DigestUtils.getJsfMethodMd5(methodManage, (JsfStepMetadata) methodManage.getContentObject());
        } else if (InterfaceTypeEnum.HTTP.getCode().equals(methodManage.getType()) || InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(methodManage.getType())) {
            return DigestUtils
                    .getHttpMethodMd5(methodManage, (HttpMethodModel) methodManage.getContentObject());
        }
        return null;
    }


    private Object invokeHttpMethodNew(InvokeMethodDTO invokeMethodDTO) {
        String envName = invokeMethodDTO.getEnvName();
        Long methodId = invokeMethodDTO.getMethodId();
        Guard.notEmpty(envName, "环境不能为空");
        Guard.notEmpty(methodId, "方法id不能为空");
        HttpMethodModel.HttpMethodInput httpInput = JsonUtils.parse(JsonUtils.toJSONString(invokeMethodDTO.getInput()), HttpMethodModel.HttpMethodInput.class);
        String method = httpInput.getMethod();
        method = method.trim().toUpperCase();
        String url = httpInput.getUrl();

        List<SimpleJsonType> path = httpInput.getPath();
        List<JsonType> params = httpInput.getParams();
        List<JsonType> headers = httpInput.getHeaders();

        List<JsonType> body = new ArrayList<>();
        body = httpInput.getBody();

        String reqType = httpInput.getReqType();

        //组装路径
        MethodManageDTO methodManageDTO = getEntityById(methodId);
        InterfaceManage interfaceManage = interfaceManageService.getOneById(methodManageDTO.getInterfaceId());

        List<EnvModel> envList = interfaceManage.getEnvList();
        Optional<EnvModel> any = envList.stream().filter(x -> x.getEnvName().equals(envName)).findAny();
        EnvModel envModel = any.orElse(null);
        if (envModel == null || EmptyUtil.isEmpty(envModel.getUrl())) {
            throw ServiceException.withCommon("没有对应的环境");
        }

        List<String> urlList = envModel.getUrl();
        String baseUrl = "";
        if (!urlList.isEmpty()) {
            baseUrl = urlList.get(0);
        }
        String endPointUrl = baseUrl;
        //组装httpstepMetadata数据 调用http 流程处理器
        log.info("invokeHttpMethodNew->envModel={},endPointUrl={}", JsonUtils.toJSONString(envModel), endPointUrl);
        HttpStepProcessor processor = new HttpStepProcessor();
        HttpStepMetadata httpMetadata = new HttpStepMetadata();//(HttpStepMetadata) StepProcessorRegistry.parseMetadata(JsonUtils.parse(httpDef, Map.class));
        httpMetadata.setId("httpInvoke");
        httpMetadata.setEndpointUrl(Arrays.asList(endPointUrl));
        //输入：
        HttpStepMetadata.Input metaInput = new HttpStepMetadata.Input();
        metaInput.setMethod(method);
        metaInput.setPath(path);
        metaInput.setParams(params);
        metaInput.setHeaders(headers);
        metaInput.setBody(body);
        metaInput.setReqType(reqType);
        metaInput.setUrl(url);

        httpMetadata.setInput(metaInput);
        httpMetadata.init();
        processor.init(httpMetadata);
        //构建一个空step，执行的时候，metadata会填充到里面去
        WorkflowInput workflowInput = new WorkflowInput();
        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);
        Step step = new Step();
        step.setContext(stepContext);
        long beginTimeMillis = System.currentTimeMillis();
        try {
            processor.process(step);
        } catch (Exception e) {
            log.info("invokeHttpMethodNew-> processor.process error", e);
            long endTimeMillis = System.currentTimeMillis();
            Long diffTime = endTimeMillis - beginTimeMillis;
            Map<String, Object> map = new HashMap();
            map.put("time", diffTime);
            map.put("error", e.getMessage());
            return map;
        }
        HttpOutput httpOutput = (HttpOutput) step.getOutput();
        Object result = httpOutput.getResponse();
        return result;
    }


    /**
     * 检查方法内容json是否符合约定
     * true 符合通过
     * false 不符合
     *
     * @param content
     * @return
     */
    private Boolean checkMethodContent(Integer type, String content) {
        InterfaceTypeEnum interfaceTypeEnum = InterfaceTypeEnum.getByCode(type);
        Guard.notEmpty(interfaceTypeEnum, "类型未知!");
        try {
            switch (interfaceTypeEnum) {
                case HTTP:
                    HttpMethodModel httpMethodModel = JsonUtils.parse(content, HttpMethodModel.class);
                    break;
                case EXTENSION_POINT:
                    httpMethodModel = JsonUtils.parse(content, HttpMethodModel.class);
                    break;
                case WEB_SERVICE:
                    WebServiceMethod webServiceMethod = JsonUtils.parse(content, WebServiceMethod.class);
                    break;
            }
        } catch (Exception e) {
            log.info("checkMethodContent error! type={},content={}", type, content);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public static void main(String[] args) {
        String ssst = "{\"input\":{\"body\":[{\"attrs\":{},\"children\":[{\"attrs\":{},\"desc\":\"测试字段1\",\"extAttrs\":{},\"fullTagName\":\"param1\",\"name\":\"param1\",\"rawNameDefaultName\":\"param1\",\"required\":true,\"simpleType\":true,\"type\":\"String\"}],\"desc\":\"测试字段2-object\",\"extAttrs\":{},\"fullTagName\":\"param3\",\"name\":\"param3\",\"rawNameDefaultName\":\"param3\",\"required\":true,\"simpleType\":false,\"type\":\"object\",\"typeClass\":\"java.util.Map\"}]},\"type\":\"http\"}";
        HttpMethodModel httpMethodModel = JsonUtils.parse(ssst, HttpMethodModel.class);
        Boolean sst = Boolean.TRUE;
    }

    /**
     * 方法列表发布返回path
     *
     * @param page
     */
    private void fillPublishedPath(Page<MethodManage> page, InterfaceManage interfaceManage) {
        if (Objects.nonNull(page) && CollectionUtils.isEmpty(page.getRecords())) {
            page.getRecords().forEach(m -> {
                if (StringUtils.isBlank(m.getPath()) && PublishEnum.YES.getCode().equals(m.getPublished())) {
                    m.setPath(projectHelper.getPublishUrl(projectHelper.getPublishMethodId(m, interfaceManage)));
                }
            });
        }
    }


//	/**
//	 * 旧方法
//	 * @param JsonMap
//	 * @return
//	 */
//	private Object invokeHttpMethod(Map JsonMap){
//		Map<String,Object> map = (Map<String, Object>) JsonMap.get("input");
//		String method = (String) map.get("method");
//		method = method.trim().toUpperCase();
//		String url = (String) map.get("url");
//		String envName = (String) map.get("envName");
//		Long methodId =Long.valueOf( (Integer) JsonMap.get("methodId"));
//		log.info("map={}",map.get("path"));
//		List<Map> path = (List)map.get("path");
//		List<Map> params = (List)map.get("params");
//		List<Map> headers = (List)map.get("headers");
//
//		Object result = null;
//		switch (method){
//			case "GET":
//				result=doGetHttp(path,params,headers,url);
//				break;
//			case "POST":
//				result= doPostAndSomeHttp(map,path,params,headers,url,HttpMethod.POST);
//				break;
//		}
//		return result;
//	}
//	/**
//	 * @date: 2022/5/20 16:36
//	 * @author wubaizhao1
//	 * @param path
//	 * @param params
//	 * @param headers
//	 * @param url
//	 * @return
//	 */
//	private Object doGetHttp(List<Map> path,List<Map> params,List<Map> headers,String url){
//		Object result=null;
//		RestTemplate restTemplate=new RestTemplate();
//		HttpHeaders httpHeaders = new HttpHeaders();
//		headers.forEach(x->httpHeaders.add(String.valueOf(x.get("name")), String.valueOf(x.get("value"))));
//		HttpEntity httpEntity = new HttpEntity(null, httpHeaders);
//		Map<String,Object> matchParams = new HashMap();
//		//如果路径参数跟普通参数冲突，则有问题
//		path.forEach(x->matchParams.put(String.valueOf(x.get("name")), String.valueOf(x.get("value"))));
//		StringBuffer stringBuffer=new StringBuffer();
//
//		for (Map x : params) {
//			matchParams.put(String.valueOf(x.get("name")), String.valueOf(x.get("value")));
//			if (stringBuffer.length()==0){
//				stringBuffer.append("?");
//			}else{
//				stringBuffer.append("&");
//			}
//			stringBuffer.append(x.get("name")).append("={").append(x.get("name")).append('}');
//		}
//		String finalURL=url+stringBuffer;
//		log.info("MethodManageServiceImpl.invokeHttpMethod finalURL={}",finalURL);
//		//对象自动映射
//		result = restTemplate.exchange(finalURL, HttpMethod.GET, httpEntity,Object.class, matchParams).getBody();
//		log.info("MethodManageServiceImpl.invokeHttpMethod result={}",JsonUtils.toJSONString(result));
//		return result;
//	}
//	private Object doPostAndSomeHttp(Map<String,Object> map,List<Map> path,List<Map> params,List<Map> headers,String url,HttpMethod method){
//		Object result=null;
//		String reqType = (String) map.get("reqType");
//		reqType = reqType.trim().toLowerCase();
//
//		//组装headers和body
//		Object body = null;
//		RestTemplate restTemplate=new RestTemplate();
//		HttpHeaders httpHeaders = new HttpHeaders();
//		if("form".equals(reqType)){
//			httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//			String bodyStr = JsonUtils.toJSONString(map.get("body"));
//			body=buildFormBody(bodyStr);
//		}else if("json".equals(reqType)){
//			httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
//			String bodyStr = JsonUtils.toJSONString(map.get("body"));
//			body=buildJsonBody(bodyStr);
//		}
//		headers.forEach(x->httpHeaders.add(String.valueOf(x.get("name")), String.valueOf(x.get("value"))));
//		HttpEntity httpEntity = new HttpEntity(body, httpHeaders);
//
//		Map<String,Object> pathParams = new HashMap();
//		//如果路径参数跟普通参数冲突，则有问题
//		path.forEach(x->pathParams.put(String.valueOf(x.get("name")), String.valueOf(x.get("value"))));
//		StringBuffer stringBuffer=new StringBuffer();
//
//		for (Map x : params) {
//			pathParams.put(String.valueOf(x.get("name")), String.valueOf(x.get("value")));
//			if (stringBuffer.length()==0){
//				stringBuffer.append("?");
//			}else{
//				stringBuffer.append("&");
//			}
//			stringBuffer.append(x.get("name")).append("={").append(x.get("name")).append('}');
//		}
//		String finalURL=url+stringBuffer;
//		log.info("MethodManageServiceImpl.invokeHttpMethod finalURL={}",finalURL);
//		//对象自动映射
//		result = restTemplate.exchange(finalURL,method, httpEntity,Object.class, pathParams).getBody();
//		log.info("MethodManageServiceImpl.invokeHttpMethod result={}",JsonUtils.toJSONString(result));
//		return result;
//	}
//	private Object buildFormBody(String bodyStr){
//		List<Map> list = JsonUtils.parseArray(bodyStr,Map.class);
//		MultiValueMap<String,Object> result= new LinkedMultiValueMap<>();
//		for (Map map : list) {
//			JsonType bodyJson = JsonTypeUtils.from(map);
//			log.info("MethodManageServiceImpl.buildJsonBody bodyJson={}",JsonUtils.toJSONString(bodyJson));
//			Object jsonValue = null;
//			if (bodyJson.isSimpleType()) {
//				jsonValue = bodyJson.toJsonValue();
//			}else{
//				jsonValue = JsonUtils.toJSONString(bodyJson.toJsonValue());
//			}
//			result.add(bodyJson.getName(),jsonValue);
//		}
//		return result;
//	}
//	private Object buildJsonBody(String bodyStr){
//		List<Map> list = JsonUtils.parseArray(bodyStr,Map.class);
//		String result=null;
//		//只有一个，只取第一个
//		for (Map map : list) {
//			JsonType bodyJson = JsonTypeUtils.from(map);
//			log.info("MethodManageServiceImpl.buildJsonBody bodyJson={}",JsonUtils.toJSONString(bodyJson));
//			result = JsonUtils.toJSONString(bodyJson.toJsonValue());
//			return result;
//		}
//		return result;
//	}


    @Override
    public boolean exportInterface(List<MethodManageDTO> list) {
        try {
            // 加载模板文件并编译
            Handlebars handlebars = new Handlebars(new ClassPathTemplateLoader());
            Template template = handlebars.compile("api.template");
            // 应用数据到模板并生成Markdown文本
            String markdown = template.apply(list);
            // 写入文件
            FileWriter writer = new FileWriter("output.md");
            writer.write(markdown);
            writer.close();
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public List<MethodManage> searchMethod(List<Integer> types, String search, List<Long> interfaceIds) {
        if (interfaceIds.isEmpty()) return new ArrayList<>();
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(MethodManage::getInterfaceId, interfaceIds);
        lqw.eq(MethodManage::getYn, 1);
        lqw.in(MethodManage::getType, types);
        excludeBigTextFiled(lqw);
        if (StringUtils.isNotBlank(search)) {
            lqw.and(wrapper -> {
                wrapper.like(MethodManage::getName, search).or().like(MethodManage::getMethodCode, search).or().like(MethodManage::getPath, search)
                        .or().apply("id in(select method_id from method_modify_delta_info where interface_id in (" + StringHelper.join(interfaceIds, ",") + ") and name like concat('%',{0},'%') or method_code like concat('%',{1},'%') ) ", search, search);
            });
        }

        Page<MethodManage> page = page(new Page<>(0, 50), lqw);
        initMethodDeltaInfos(page.getRecords());
        return page.getRecords();
    }


    @Override
    public List<MethodManage> queryMethodByPath(Long interfaceId, String path, Integer type) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getInterfaceId, interfaceId);
        lqw.eq(MethodManage::getPath, path);
        lqw.in(MethodManage::getType, type);
        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode());
        excludeBigTextFiled(lqw);
        return list(lqw);

    }

    public Page<MethodManage> getInterfaceMethods(Long interfaceId, Long pageNo, Long size, String search) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        Page<MethodManage> page = new Page<>(pageNo, size);
        lqw.and(StringUtils.isNotBlank(search), child -> {
            child.or().like(MethodManage::getName, search)
                    .or().like(MethodManage::getPath, search)
                    .or().apply("id in(select method_id from method_modify_delta_info where interface_id = {0} and name like concat('%',{1},'%') or method_code like concat('%',{2},'%') ) ", interfaceId, search, search)
                    .or().like(MethodManage::getMethodCode, search);
        });
        excludeBigTextFiled(lqw);
        lqw.eq(MethodManage::getYn, 1);
        lqw.eq(MethodManage::getInterfaceId, interfaceId);
        lqw.ne(MethodManage::getType, 20);
        final Page<MethodManage> result = page(page, lqw);
        initMethodDeltaInfos(result.getRecords());
        return result;
    }

    public Page<MethodManage> getInterfaceMethodsIncludeContent(Long interfaceId, Long pageNo, Long size, boolean containsDeleted) {
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceManage, "接口不存在");
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        Page<MethodManage> page = new Page<>(pageNo, size);
        if (!containsDeleted) {
            lqw.eq(MethodManage::getYn, 1);
        }

        lqw.eq(MethodManage::getInterfaceId, interfaceId);
        final Page<MethodManage> result = page(page, lqw);
        for (MethodManage record : result.getRecords()) {
            initContentObject(record);
        }
        refJsonTypeService.initMethodRefInfos(result.getRecords(), interfaceManage.getAppId());
        initMethodDeltaInfos(result.getRecords());
        for (MethodManage record : result.getRecords()) {
            initMethodDocConfig(record, true);

            record.setContent(JsonUtils.toJSONString(record.getContentObject()));
        }

        return result;
    }

    public Page<MethodManage> listMethodsByIds(String search, Integer status, List<Long> ids, long pageNo, long size) {
        Page<MethodManage> page = new Page<>(pageNo, size);
        page.setRecords(Collections.emptyList());


        if (ids.isEmpty()) return page;

        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        excludeBigTextFiled(lqw);
        lqw.and(StringUtils.isNotBlank(search), child -> {
            child.or().like(MethodManage::getName, search)
                    .or().like(MethodManage::getPath, search)
                    .or().apply("id in(select method_id from method_modify_delta_info where method_id in (" + StringHelper.join(ids, ",") + ") and name like concat('%',{0},'%') or method_code like concat('%',{1},'%') ) ", search, search)
                    .or().like(MethodManage::getMethodCode, search);
        });
        lqw.eq(status != null, MethodManage::getStatus, status);
        lqw.eq(MethodManage::getYn, 1);

        lqw.in(MethodManage::getId, ids);
        Page<MethodManage> result = page(new Page<>(pageNo, size), lqw);

        initMethodDeltaInfos(result.getRecords());
        return result;
    }

    @Override
    public void updateObject(Long id,String content){
        LambdaUpdateWrapper<MethodManage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MethodManage::getId, id);
        updateWrapper.set(MethodManage::getContent, content);
        update(updateWrapper);
    }

    @Override
    public List<InterfaceTypeCount> queryInterfaceTypeCount(Long appId) {
        return baseMapper.queryInterfaceTypeCount(new CountQueryDto(appId, UserSessionLocal.getUser().getUserId()));
    }

    @Override
    public List<AppInterfaceCount> queryInterfaceMethodCount(Long appId) {
        return baseMapper.queryInterfaceMethodCount(new CountQueryDto(appId, UserSessionLocal.getUser().getUserId()));
    }

    @Override
    public void initAllMethodProps() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("clearAllProps start:{}", stopWatch.getStartTime());
        clearAllProps();
        stopWatch.stop();
        log.info("clearAllProps start:{},end:{}ms", stopWatch.getStartTime(), stopWatch.getTime());

        stopWatch.reset();
        stopWatch.start();

        log.info("initAllMethodProps start:{}", stopWatch.getStartTime());
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        List<Integer> typeList = Lists.newArrayList();
        typeList.add(1);
        typeList.add(3);
        lqw.eq(BaseEntity::getYn, 1).in(MethodManage::getType, typeList);
        lqw.select(MethodManage::getContent, MethodManage::getId, MethodManage::getType, MethodManage::getInterfaceId);
        lqw.orderByDesc(MethodManage::getId);

        Page<MethodManage> result = page(new Page<>(1, 20), lqw);
        log.info("initAllMethodProps result:{}", result.getTotal());
        for (long i = 1; i < result.getPages(); i++) {
            Page<MethodManage> curPage = page(new Page<>(i, 20), lqw);
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(curPage.getRecords())) {
                break;
            }
            List<Long> interfaceIdList = curPage.getRecords().stream().map(MethodManage::getInterfaceId).collect(Collectors.toList());
            LambdaQueryWrapper<InterfaceManage> lqwInterface = new LambdaQueryWrapper<>();
            lqwInterface.in(InterfaceManage::getId, interfaceIdList).isNotNull(InterfaceManage::getAppId);
            List<InterfaceManage> interfaceManageList = interfaceManageService.list(lqwInterface);
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(interfaceManageList)) {
                continue;
            }
            Map<Long, Long> interfaceAppMap = interfaceManageList.stream().collect(Collectors.toMap(InterfaceManage::getId, InterfaceManage::getAppId));
            for (MethodManage record : curPage.getRecords()) {
                if (!interfaceAppMap.containsKey(record.getInterfaceId())) {
                    continue;
                }
                Set<String> propSet = null;


                if (record.getType() == 1) {
                    try {
                        propSet = allHttpProps(record);
                    } catch (Exception ex) {
                        log.error("allHttpProps error", ex);
                    }

                } else if (record.getType() == 3) {
                    try {
                        propSet = allJsfProps(record);
                    } catch (Exception ex) {
                        log.error("allJsfProps error", ex);
                    }
                }

                if (Objects.nonNull(propSet)) {

                    initSortedSet(interfaceAppMap.get(record.getInterfaceId()), propSet);

                }
            }


        }

        stopWatch.stop();
        log.info("initAllMethodProps start:{} end:{}ms", stopWatch.getStartTime(), stopWatch.getTime());


    }

    private void initSortedSet(Long appId, Set<String> propSet) {

        String sortedKey = String.format(APP_SORTEDSET_FORMAT, appId);
        log.info("initSortedSet key:{}", sortedKey);
        for (String prop : propSet) {
            log.info("initSortedSet key:{},prop:{}", sortedKey, prop);
            jimClient.zIncrBy(sortedKey, 1, prop);
        }

    }

    private void deleteSortedSet(Long appId) {
        String sortedKey = String.format(APP_SORTEDSET_FORMAT, appId);
        log.info("deleteSortedSet key:{}", sortedKey);
        jimClient.del(sortedKey);
    }

    private Set<String> allHttpProps(MethodManage record) {
        Set<String> propSet = Sets.newHashSet();
        HttpMethodModel model = JsonUtils.parse(record.getContent(), HttpMethodModel.class);
        if (Objects.nonNull(model)) {
            if (Objects.nonNull(model.getInput())) {
                if (Objects.nonNull(model.getInput().getParams())) {
                    for (JsonType param : model.getInput().getParams()) {
                        addJsonTypeProp(param, propSet);
                    }
                }
                if (Objects.nonNull(model.getInput().getBody())) {
                    for (JsonType jsonType : model.getInput().getBody()) {
                        addJsonTypeProp(jsonType, propSet);
                    }
                }
            }
            if (Objects.nonNull(model.getOutput())) {
                if (Objects.nonNull(model.getOutput().getBody())) {
                    for (JsonType jsonType : model.getOutput().getBody()) {
                        addJsonTypeProp(jsonType, propSet);
                    }
                }
            }

        }

        return propSet;
    }


    private Set<String> allJsfProps(MethodManage record) {
        Set<String> propSet = Sets.newHashSet();
        JsfStepMetadata jsfStepMetadata = JsonUtils.parse(record.getContent(), JsfStepMetadata.class);
        if (Objects.nonNull(jsfStepMetadata)) {
            if (Objects.nonNull(jsfStepMetadata.getInput())) {
                for (JsonType jsonType : jsfStepMetadata.getInput()) {
                    addJsonTypeProp(jsonType, propSet);
                }
            }
            if (Objects.nonNull(jsfStepMetadata.getOutput())) {
                addJsonTypeProp(jsfStepMetadata.getOutput(), propSet);
            }
        }
        return propSet;
    }

    private void addJsonTypeProp(JsonType param, Set<String> propSet) {
        if (!"root".equals(param.getName())) {
            propSet.add(String.format(PROP_KEY_FORMAT, param.getName(), param.getType(), param.getDesc()));
        }
        if (param instanceof ComplexJsonType) {
            ComplexJsonType complexJsonType = (ComplexJsonType) param;
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(complexJsonType.getChildren())) {
                for (JsonType child : complexJsonType.getChildren()) {
                    addJsonTypeProp(child, propSet);
                }
            }
        }
    }


    @Override
    public List<MethodPropDTO> getTopProp(MethodPropParam methodPropParam) {
        List<MethodPropDTO> methodPropDTOList = Lists.newArrayList();
        boolean hasData = false;
        String sortedKey = String.format(APP_SORTEDSET_FORMAT, methodPropParam.getAppId());
        if (org.apache.commons.lang3.StringUtils.isEmpty(methodPropParam.getProp())) {
            Set<ZSetTuple<String>> zSetTupleSet = jimClient.zRevRangeWithScores(sortedKey, (methodPropParam.getCurrent() - 1) * methodPropParam.getSize(), methodPropParam.getCurrent() * methodPropParam.getSize() - 1);
            for (ZSetTuple<String> stringZSetTuple : zSetTupleSet) {
                MethodPropDTO methodPropDTO = new MethodPropDTO();
                methodPropDTO.setCount(stringZSetTuple.getScore());
                initMethodPropDTO(stringZSetTuple, methodPropDTO);
                methodPropDTOList.add(methodPropDTO);
            }
        } else {
            int i = 0;
            Long total = jimClient.zCard(sortedKey);
            log.info("getTopProp sortedKey:{},total:{}", sortedKey, total);
            while (true) {
                if (i * methodPropParam.getSize() > total) {
                    break;
                }
                Set<ZSetTuple<String>> zSetTupleSet = jimClient.zRevRangeWithScores(sortedKey, i * methodPropParam.getSize(), (i + 1) * methodPropParam.getSize() - 1);
                for (ZSetTuple<String> stringZSetTuple : zSetTupleSet) {
                    if (stringZSetTuple.getValue().contains(methodPropParam.getProp())) {
                        MethodPropDTO methodPropDTO = new MethodPropDTO();
                        methodPropDTO.setCount(stringZSetTuple.getScore());
                        initMethodPropDTO(stringZSetTuple, methodPropDTO);
                        methodPropDTOList.add(methodPropDTO);
                        if (methodPropDTOList.size() == (methodPropParam.getCurrent() * methodPropParam.getSize())) {
                            break;
                        }
                    }
                }
                i++;
            }
            if (methodPropDTOList.size() > ((methodPropParam.getCurrent() - 1) * methodPropParam.getSize())) {
                methodPropDTOList.subList((int) ((methodPropParam.getCurrent() - 1) * methodPropParam.getSize()), methodPropDTOList.size() - 1);
            }
        }
        if (methodPropDTOList.size() > 0) {
            hasData = true;
        }

        //去除已经绑定的属性。
        BindPropParam bindPropParam = new BindPropParam();
        bindPropParam.setAppId(methodPropParam.getAppId());
        List<REnumMethodProp> enumMethodPropList = irEnumMethodPropService.bindEnumList(bindPropParam);
        Set<String> propSet = new HashSet<>();
        for (REnumMethodProp rEnumMethodProp : enumMethodPropList) {
            propSet.add(rEnumMethodProp.getPropName());
        }
        methodPropDTOList.removeIf(methodPropDTO -> propSet.contains(methodPropDTO.getPropName()));

        if (methodPropDTOList.size() == 0 && hasData) {
            methodPropParam.setCurrent(methodPropParam.getCurrent() + 1);
            return getTopProp(methodPropParam);
        }

        return methodPropDTOList;
    }

    private void initMethodPropDTO(ZSetTuple<String> stringZSetTuple, MethodPropDTO methodPropDTO) {
        String[] props = stringZSetTuple.getValue().split(":");
        methodPropDTO.setPropName(props[0]);
        methodPropDTO.setPropType(props[1]);
        if (props.length > 2) {
            methodPropDTO.setPropDesc(props[2]);
        }
    }

    @Override
    public void clearAllProps() {
        log.info("clearAllProps start");
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.select(AppInfo::getId);
        Page<AppInfo> result = appInfoService.page(new Page<>(1, 20), lqw);
        for (long i = 1; i < result.getPages(); i++) {
            try {
                Page<AppInfo> curPage = appInfoService.page(new Page<>(i, 20), lqw);
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(curPage.getRecords())) {
                    break;
                }
                for (AppInfo record : curPage.getRecords()) {
                    deleteSortedSet(record.getId());
                }
            } catch (Exception ex) {
                log.error("clearAllProps error", ex);
            }
        }


    }

    @Override
    public List<Long> getExistIds(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(MethodManage::getId, ids);
        lqw.eq(MethodManage::getYn, 1);
        lqw.select(MethodManage::getId);
        return list(lqw).stream().map(MethodManage::getId).collect(Collectors.toList());
    }

    @Override
    public List<MethodManage> searchMethod(List<Integer> types, String search, List<Long> interfaceIds, List<Long> methodIds) {
        if (interfaceIds.isEmpty()) return new ArrayList<>();
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(MethodManage::getInterfaceId, interfaceIds);
        lqw.eq(MethodManage::getYn, 1);
        lqw.in(MethodManage::getType, types);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(methodIds)) {
            lqw.in(MethodManage::getId, methodIds);
        }
        excludeBigTextFiled(lqw);
        if (StringUtils.isNotBlank(search)) {
            lqw.and(wrapper -> {
                wrapper.like(MethodManage::getName, search).or().like(MethodManage::getMethodCode, search).or().like(MethodManage::getPath, search)
                        .or().apply("id in(select method_id from method_modify_delta_info where interface_id in (" + StringHelper.join(interfaceIds, ",") + ") and name like concat('%',{0},'%') or method_code like concat('%',{1},'%') ) ", search, search);
            });
        }

        Page<MethodManage> page = page(new Page<>(0, 50), lqw);
        initMethodDeltaInfos(page.getRecords());
        return page.getRecords();
    }

    /**
     * 之前文档上报的时候，接口描述字段放到desc上了，现在需要
     * 放到docInfo上
     */
    public void updateReportDelta() {
        int pageNo = 1;
        while (true) {
            LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(MethodManage::getYn, 1);
            lqw.inSql(MethodManage::getInterfaceId, "select id from interface_manage where yn = 1 and auto_report = 1");
            lqw.in(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode(), InterfaceTypeEnum.JSF.getCode());
            lqw.and(wrapper -> {
                wrapper.isNotNull(MethodManage::getDocInfo).or()
                        .isNotNull(MethodManage::getDesc);
            });
            lqw.select(MethodManage::getId, MethodManage::getDesc, MethodManage::getDocInfo, MethodManage::getType);
            Page<MethodManage> page = page(new Page<>(pageNo, 1000), lqw);
            if (page.getRecords().isEmpty()) break;
            List<MethodModifyDeltaInfo> saved = new ArrayList<>();
            for (MethodManage methodManage : page.getRecords()) {
                if (StringUtils.isEmpty(methodManage.getDocInfo())) {
                    if (StringUtils.isNotBlank(methodManage.getDesc())) {
                        methodManage.setDocInfo(methodManage.getDesc());
                        methodManage.setDesc(null);
                        log.info("method.update_doc_info:id={},method={}", methodManage.getId(), methodManage);
                        updateById(methodManage);
                    }

                } else {

                    MethodModifyDeltaInfo deltaInfo = new MethodModifyDeltaInfo();
                    Map<String, Object> deltaContent = new HashMap<>();
                    Map<String, Object> deltaAttrs = new HashMap<>();
                    deltaContent.put("deltaAttrs", deltaAttrs);
                    deltaInfo.setMethodId(methodManage.getId());
                    deltaAttrs.put("docInfo", methodManage.getDocInfo());
                    deltaInfo.setDeltaContent(JsonUtils.toJSONString(deltaContent));
                    saved.add(deltaInfo);
                    if (!ObjectHelper.equals(methodManage.getDocInfo(), methodManage.getDesc())) {
                        methodManage.setDocInfo(methodManage.getDesc());
                        updateById(methodManage);
                    }

                    log.info("method.saveDelta:id={},method={}", methodManage.getId(), methodManage);

                }
            }
            if (!saved.isEmpty()) {
                List<Long> methodIds = saved.stream().map(item -> item.getMethodId()).collect(Collectors.toList());
                List<MethodModifyDeltaInfo> methodDeltas = deltaInfoService.getMethodDeltas(methodIds);
                Set<Long> existMethodIds = methodDeltas.stream().map(item -> item.getMethodId()).collect(Collectors.toSet());
                saved = saved.stream().filter(item -> !existMethodIds.contains(item.getMethodId())).collect(Collectors.toList());
                if (!saved.isEmpty()) {
                    methodModifyDeltaInfoService.saveBatch(saved);
                }

            }
            pageNo++;
        }

    }


    public void updateMergedDigest() {
        int pageNo = 1;


        while (true) {
            LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(MethodManage::getYn, 1);
            lqw.inSql(MethodManage::getInterfaceId, "select id from interface_manage where yn = 1 and auto_report = 1");
            lqw.in(MethodManage::getType, InterfaceTypeEnum.HTTP.getCode(), InterfaceTypeEnum.JSF.getCode());

            lqw.select(MethodManage::getId, MethodManage::getDesc, MethodManage::getName, MethodManage::getHttpMethod, MethodManage::getDocInfo, MethodManage::getContent, MethodManage::getDigest, MethodManage::getMergedContentDigest, MethodManage::getType);

            Page<MethodManage> page = page(new Page<>(pageNo, 1000), lqw);
            if (page.getRecords().isEmpty()) break;
            for (MethodManage record : page.getRecords()) {
                initContentObject(record);
            }
            initMethodDeltaInfos(page.getRecords());

            for (MethodManage record : page.getRecords()) {
                String beforeDigest = record.getDigest();
                String afterDigest = getContentObjectDigest(record);
                if (beforeDigest.equals(afterDigest)) continue;
                record.setMergedContentDigest(afterDigest);
                LambdaUpdateWrapper<MethodManage> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(MethodManage::getId, record.getId());
                updateWrapper.set(MethodManage::getMergedContentDigest, record.getMergedContentDigest());
                update(updateWrapper);
            }

            pageNo++;
        }

    }

    public void updateAllJsfInterface() {
        long start = 1;
        log.info("method.update_jsf_interface_start:begin={}", System.currentTimeMillis());
        while (true) {
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode());
            lqw.eq(InterfaceManage::getYn, 1);
            Page<InterfaceManage> page = interfaceManageService.page(new Page<>(start, 200), lqw);

            for (InterfaceManage record : page.getRecords()) {
                updateEmptyJsfNameToServiceCode(record);
            }
            if (page.getRecords().isEmpty()) {
                log.info("method.update_jsf_interface_end:end={},cost={}", System.currentTimeMillis(), System.currentTimeMillis() - start);
                return;
            }
            start++;
        }
    }

    public void updateEmptyJsfNameToServiceCode(InterfaceManage interfaceManage) {
        Guard.assertTrue(InterfaceTypeEnum.JSF.getCode().equals(interfaceManage.getType()));
        if (StringUtils.isBlank(interfaceManage.getName())) {
            interfaceManage.setName(interfaceManage.getServiceCode());
            LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper();
            luw.set(InterfaceManage::getName, interfaceManage.getName());
            luw.set(InterfaceManage::getServiceCode, interfaceManage.getServiceCode());
            luw.eq(InterfaceManage::getId, interfaceManage.getId());

            interfaceManageService.update(luw);
        }

    }

    public void changeJsfDescriptionInfo(InterfaceManage interfaceManage) {
        Guard.assertTrue(InterfaceTypeEnum.JSF.getCode().equals(interfaceManage.getType()));
        if (StringUtils.isBlank(interfaceManage.getServiceCode())) {
            interfaceManage.setServiceCode(interfaceManage.getName());
            interfaceManage.setName(interfaceManage.getDesc());
        } else if (interfaceManage.getServiceCode().equals(interfaceManage.getName())) {
            if (StringUtils.isNotBlank(interfaceManage.getDesc())
                    && interfaceManage.getDesc().contains("<") // 如果是富文本，则取消掉
            ) {
                return;
            }
            interfaceManage.setName(interfaceManage.getDesc());
        } else {
            return;
        }
        LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper();
        luw.set(InterfaceManage::getName, interfaceManage.getName());
        luw.set(InterfaceManage::getServiceCode, interfaceManage.getServiceCode());
        luw.eq(InterfaceManage::getId, interfaceManage.getId());

        interfaceManageService.update(luw);
    }

    public void updateAllJsfMethod() {
        long start = 1;
        log.info("method.update_method_start:begin={}", System.currentTimeMillis());
        while (true) {
            LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(MethodManage::getType, InterfaceTypeEnum.JSF.getCode());
            lqw.eq(MethodManage::getYn, 1);
            Page<MethodManage> page = page(new Page<>(start, 200), lqw);

            for (MethodManage record : page.getRecords()) {
                changeJsfMethodNameToMethodCode(record);
            }
            if (page.getRecords().isEmpty()) {
                log.info("method.update_method_end:end={},cost={}", System.currentTimeMillis(), System.currentTimeMillis() - start);
                return;
            }
            start++;
        }
    }

    public void changeJsfMethodNameToMethodCode(MethodManage jsfMethod) {
        Guard.assertTrue(InterfaceTypeEnum.JSF.getCode().equals(jsfMethod.getType()));
        if (StringUtils.isBlank(jsfMethod.getMethodCode())) {
            jsfMethod.setMethodCode(jsfMethod.getName());
        }
        if (StringUtils.isNotEmpty(jsfMethod.getDesc()) && !jsfMethod.getDesc().contains("<")) {

            jsfMethod.setName(SwaggerParserService.truncateStr(jsfMethod.getDesc(), 128));
        }
        LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper();
        luw.set(MethodManage::getName, jsfMethod.getName());
        luw.set(MethodManage::getMethodCode, jsfMethod.getMethodCode());
        luw.eq(MethodManage::getId, jsfMethod.getId());

        update(luw);
    }

    @Override
    public List<Long> getMethodByCode(String methodCode, Long interfaceId) {
        // 查询是否已存在
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode())
                .eq(MethodManage::getMethodCode, methodCode)
                .eq(MethodManage::getInterfaceId, interfaceId).select(MethodManage::getId);
        List<MethodManage> rest = methodManageMapper.selectList(lqw);
        if (!CollectionUtils.isEmpty(rest)) {
            return rest.stream().map(MethodManage::getId).collect(Collectors.toList());
        }
        return new ArrayList<Long>();
    }



    @Override
    public MethodManageDTO getMetaMethodInfo(Long id) {
        MethodManage methodManage = methodManageMapper.selectById(id);
        if (methodManage == null) {
            throw new BizException("该记录不存在");
        }
        methodManage.initKey();
        InterfaceManage interfaceManage = interfaceManageService.getOneById(methodManage.getInterfaceId());
        Set<String> tags = relationMethodTagService.queryTagNames(id, interfaceManage.getAppId());
        initMockInfo(methodManage, interfaceManage);
        initMethodDocConfig(methodManage);
        MethodManageDTO methodManageDTO = new MethodManageDTO();
        BeanUtils.copyProperties(methodManage, methodManageDTO);
        methodManageDTO.setId(methodManage.getId() + "");
        methodManageDTO.setTags(tags);
        methodManageDTO.setInterfaceName(interfaceManage.getServiceCode() + "#");
        log.info("MethodManageServiceImpl.getById id={},code={},name={}", methodManageDTO.getId(), methodManageDTO.getMethodCode(), methodManageDTO.getName());

        methodManageDTO.setContent("");
        methodManageDTO.setContentObject(null);


        final Long appId = interfaceManage.getAppId();
        if (appId != null) {
            methodManageDTO.setAppId(appId);
            final AppInfo app = appInfoService.getById(appId);

            if (app != null) {
                methodManageDTO.setAppCode(app.getAppCode());
                methodManageDTO.setAppName(app.getAppName());
            }
            List<AppInfoMembers> appInfoMemberList = appInfoMembersService.listErpByAppCode(app.getAppCode());
            for (AppInfoMembers appInfoMember : appInfoMemberList) {
                if (AppUserTypeEnum.OWNER.getType().equals(appInfoMember.getRoleType())) {
                    methodManageDTO.setAppOwner(appInfoMember.getErp());
                    break;
                }
            }

        }


        methodManageDTO.setAutoReport(interfaceManage.getAutoReport());
        methodManageDTO.setIsPublic(interfaceManage.getIsPublic());
        return methodManageDTO;
    }


    public IPage<InterfaceAndMethodInfo> queryInterfaceAndMethodInfo(Integer type, String appCode, Long current, Long size) {
        Guard.notEmpty(type, "type无效");
        Guard.notEmpty(appCode, "appCode无效");

        Guard.notEmpty(current, "current无效");
        Guard.notEmpty(size, "size无效");

        AppInfo appInfo = appInfoService.findByJdosAppCode(appCode);
        if (appInfo == null) {
            return new Page<>(current, size);
        }
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.apply(" interface_id in (select id from interface_manage where yn = 1 and app_id = {0})", appInfo.getId());
        lqw.eq(MethodManage::getYn, 1);
        lqw.eq(MethodManage::getType, type);
        excludeBigTextFiled(lqw);
        Page<MethodManage> methods = page(new Page(current, size), lqw);
        if (methods == null || methods.getRecords().isEmpty()) {
            return new Page<>(current, size);
        }
        Set<Long> intefaceIds = methods.getRecords().stream().map(item -> item.getInterfaceId()).collect(Collectors.toSet());
        LambdaQueryWrapper<InterfaceManage> interfaceLqw = new LambdaQueryWrapper<>();
        interfaceLqw.in(InterfaceManage::getId, intefaceIds);
        interfaceLqw.eq(InterfaceManage::getYn, 1);
        interfaceLqw.select(InterfaceManage::getId, InterfaceManage::getAppId, InterfaceManage::getName, InterfaceManage::getServiceCode);
        List<InterfaceManage> interfaces = interfaceManageService.list(interfaceLqw);
        Map<Long, InterfaceManage> id2InterfaceManage = interfaces.stream().collect(Collectors.toMap(InterfaceManage::getId, item -> item));

        IPage<InterfaceAndMethodInfo> page = methods.convert(method -> {
            InterfaceAndMethodInfo info = new InterfaceAndMethodInfo();
            info.setId(method.getId());
            info.setPath(method.getPath());
            info.setHttpMethod(method.getHttpMethod());
            info.setCnName(method.getName());
            info.setEnName(method.getMethodCode());
            InterfaceManage interfaceManage = id2InterfaceManage.get(method.getInterfaceId());
            if (interfaceManage != null) {
                info.setInterfaceName(interfaceManage.getName());
                info.setInterfaceId(interfaceManage.getId());
                info.setAppId(interfaceManage.getAppId());
            }
            info.setUrl("http://console.paas.jd.com/idt/fe-app-view/demandManage/" + interfaceManage.getAppId() + "?methodId=" + method.getId());
            return info;
        });
        return page;

    }


    @Override
    public Page<MethodManageDTO> marketMethod (InterfaceAuthFilter filter){
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper();
        excludeBigTextFiled(lqw);

//        if (null == filter.getType()) {
//            if(StringUtils.isBlank(filter.getCjgProductTrace())&&StringUtils.isBlank(filter.getCjgBusinessDomainTrace())){
//                filter.setType(InterfaceTypeEnum.JSF.getCode());
//            }
//        }

        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(EmptyUtil.isNotEmpty(filter.getType()), MethodManage::getType, filter.getType());

        if (StringUtils.isNotBlank(filter.getName())) {
            lqw.and(childWrapper -> {
                childWrapper.or().like(StringUtils.isNotBlank(filter.getName()), MethodManage::getName, filter.getName())
                        .or().like(StringUtils.isNotBlank(filter.getName()), MethodManage::getMethodCode, filter.getName())
                ;
            });
        }
        //筛选jsf鉴权
        Boolean isLicense = filter.getHasLicense() != null && filter.getHasLicense();

        //使用校验方法校验用户输入的合法性
        if (SafeUtil.sqlValidate(filter.getCjgBusinessDomainTrace(), filter.getCjgProductTrace(), filter.getDeptName(), filter.getType() + "")) {
        }

        StringBuilder sql = new StringBuilder(" interface_id in (select id from interface_manage where yn=1");
        if (1 == filter.getQueryType()) {
            sql.append(" and cjg_product_trace is not null");
        }
        if (2 == filter.getQueryType()) {
            sql.append(" and cjg_business_domain_trace is not null");
        }
        if (StringUtils.isNotBlank(filter.getCjgBusinessDomainTrace())) {
            sql.append(" and cjg_business_domain_trace like '%" + "," + filter.getCjgBusinessDomainTrace() + "%'");
        }
        if (StringUtils.isNotBlank(filter.getCjgProductTrace())) {
            sql.append(" and cjg_product_trace like '%" + "," + filter.getCjgProductTrace() + "%'");
        }
        if (StringUtils.isNotBlank(filter.getDeptName())) {
            sql.append(String.format(" and dept_name = %s", "'" + filter.getDeptName() + "'"));
        }
        if (!CollectionUtils.isEmpty(filter.getAppIds())) {
            sql.append(" and app_id in ( " + filter.getAppIds().get(0) + ")");
        }
        if (null != filter.getType()) {
            sql.append(" and type=" + filter.getType());
            if (isLicense && InterfaceTypeEnum.JSF.getCode().equals(filter.getType())) {
                sql.append(" and cjg_app_id is not null ");
            }
        } else if (isLicense) {
            sql.append(" and ((cjg_app_id is not null and type=3) or type=1)");
        }

        if (!StringUtils.isBlank(filter.getAdminCode())) {
            sql.append(" and");
            sql.append(" (");
            sql.append(" id in ( select resource_id from member_relation relation where (relation.resource_type=1 or relation.resource_role=2) and relation.user_code='" + filter.getAdminCode() + "')");
            sql.append(" or app_id in (select app_id from app_info_members where erp='" + filter.getAdminCode() + "')");
            sql.append(" )");
        }
        if (null != filter.getOnlySelf() && filter.getOnlySelf() == 1) {
            sql.append(" and");
            sql.append(" (");
            sql.append(" id in ( select resource_id from member_relation relation where (relation.resource_type=1 or relation.resource_role=2) and relation.user_code='" + UserSessionLocal.getUser().getUserId() + "')");
            sql.append(" or app_id in (select app_id from app_info_members where erp='" + UserSessionLocal.getUser().getUserId() + "')");
            sql.append(" )");
        }


        sql.append(")");
        lqw.apply(sql.toString());

//        if (!StringUtils.isBlank(filter.getAdminCode())) {
//            lqw.and(child -> {
//                child.or(wrapper -> {
//                    wrapper.inSql(MethodManage::getInterfaceId, " select resource_id from member_relation relation where (relation.resource_type=1 or relation.resource_role=2) and relation.user_code='" + filter.getAdminCode() + "'");
//                }).or(wrapper -> {
//                    methodManageInAppSql(wrapper, filter.getAdminCode());
//                });
//            });
//        }
//        if (null!=filter.getOnlySelf()&&filter.getOnlySelf() == 1) {
//            lqw.and(child -> {
//                child.or(wrapper -> {
//                    wrapper.inSql(MethodManage::getInterfaceId, " select resource_id from member_relation relation where relation.resource_type=1   and relation.user_code='" + UserSessionLocal.getUser().getUserId() + "'");
//                }).or(wrapper -> {
//                    methodManageInAppSql(wrapper, filter.getAdminCode());
//                });
//            });
//        }

        if (filter.getHasLicense() != null && filter.getHasLicense()) {
            if (null == filter.getType()) {
                lqw.and(child -> {
                    child.or(wrapper -> {
                        wrapper.eq(MethodManage::getType, 1);
                        wrapper.inSql(MethodManage::getInterfaceId, " select auth.interface_id interfaceId from http_auth_detail auth ");
                    });
                    child.or(wrapper -> {
                        wrapper.eq(MethodManage::getType, 3);
                    });
                });
            } else if (InterfaceTypeEnum.HTTP.getCode().equals(filter.getType())) {
                lqw.and(child -> {
                    child.or(wrapper -> {
                        wrapper.inSql(MethodManage::getInterfaceId, " select auth.interface_id interfaceId from http_auth_detail auth ");
                    });
                });
            }
        }

        if (filter.getIsFollow() != null) {
            String sql1 = "select method_id from interface_follow_list where erp='" + UserSessionLocal.getUser().getUserId() + "'";
            if (filter.getIsFollow() == 1) { // 已关注
                lqw.inSql(MethodManage::getId, sql1);
            } else {// 未关注
                //lqw.notInSql(InterfaceManage::getId, sql);
            }
        }

        Page<MethodManage> page = page(new Page<>(filter.getCurrent(), filter.getSize()), lqw);
        Page<MethodManageDTO> retPage = new Page<>(filter.getCurrent(), filter.getSize());
        retPage.setTotal(page.getTotal());
        List<MethodManageDTO> dtoRecords = page.getRecords().stream().map(methodManage -> {
            MethodManageDTO dto = new MethodManageDTO();
            BeanUtils.copyProperties(methodManage, dto);
            dto.setId(methodManage.getId() + "");
            return dto;
        }).collect(Collectors.toList());
        fillInterfaceAppInfo(dtoRecords);

        if (!CollectionUtils.isEmpty(dtoRecords)) {
            fixHasLicense(dtoRecords);
        }
        updateFollowStatus(dtoRecords);
        retPage.setRecords(dtoRecords);
        return retPage;
    }

    @Override
    public MethodManageDTO getMethodManageDTOById(String id, FilterParam filter) {
        MethodManageDTO methodManageDTO = getEntity(id, filter);
        String interfaceMarkDown = obtainInterfaceMarkDown(methodManageDTO.getInterfaceName());
        methodManageDTO.setInterfaceText(interfaceMarkDown);
        List<BizLogicInfo> bizLogicMethodInfoList = bizLogicInfoService.obtainInfoListByInterfaceAndMethod(methodManageDTO.getInterfaceName()
                , methodManageDTO.getMethodCode());
        //设置方法 调用示例和业务说明
        if (!CollectionUtils.isEmpty(bizLogicMethodInfoList)) {
            for (BizLogicInfo bizLogicInfo : bizLogicMethodInfoList) {
                String doc_key = bizLogicInfo.getComponentId() + "_" + bizLogicInfo.getType();
                String methodMarkDown = cjgJfsHelper.firstFromCacheDownloadJss("cjg-interfaceword", doc_key);
                if (Objects.equals(bizLogicInfo.getType(), 2)) {
                    methodManageDTO.setExplanationText(methodMarkDown);
                }
                if (Objects.equals(bizLogicInfo.getType(), 3)) {
                    methodManageDTO.setBizLogicText(methodMarkDown);
                }
            }
        }
        return methodManageDTO;
    }

    public  String obtainInterfaceMarkDown(String interFaceName) {
        String interfaceMarkDown = null;
        List<BizLogicInfo> bizLogicInfoList = bizLogicInfoService.obtainInfoListByInterfaceName(interFaceName);
        //获取接口markdown
        if (!CollectionUtils.isEmpty(bizLogicInfoList)) {
            String doc_key = bizLogicInfoList.get(0).getComponentId() + "_" + bizLogicInfoList.get(0).getType();
            interfaceMarkDown = cjgJfsHelper.firstFromCacheDownloadJss("cjg-interfaceword", doc_key);
        }
        return interfaceMarkDown;
    }

    private void methodManageInAppSql(LambdaQueryWrapper<MethodManage> lqw, String erp) {
        if (SafeUtil.sqlValidate(erp)){
            log.info("sql注入安全检查失败");
        }
        lqw.apply("app_id in (select app_id from app_info_members where erp={0})", erp);
    }

    private void fixHasLicense(List<MethodManageDTO> records) {
        if (records.isEmpty()) {
            return;
        }
        //http接口处理
        List<MethodManageDTO> httpMethods = records.stream().filter(item -> {
            return InterfaceTypeEnum.HTTP.getCode().equals(item.getType());
        }).collect(Collectors.toList());
        Set<Long> authIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(httpMethods)) {
            authIds = httpAuthDetailService.queryExists(httpMethods.stream().map(item -> item.getInterfaceId()).collect(Collectors.toSet()).stream().collect(Collectors.toList()));
        }
        //jsf接口处理
        List<Long> jsfInterfaceIds = records.stream().filter(item -> {
            return InterfaceTypeEnum.JSF.getCode().equals(item.getType());
        }).map(MethodManageDTO::getInterfaceId).collect(Collectors.toSet()).stream().collect(Collectors.toList());

        Map<Long, String> jdosAppId = new HashMap<>();
        if (!CollectionUtils.isEmpty(jsfInterfaceIds)) {
            List<InterfaceManage> jsfInterface = interfaceManageService.listInterfaceByIds(jsfInterfaceIds);
            jdosAppId = jsfInterface.stream().filter(item->StringUtils.isNotBlank(item.getCjgAppId())).collect(Collectors.toMap(InterfaceManage::getId, InterfaceManage::getCjgAppId));
        }

        for (MethodManageDTO method : records) {
            if (InterfaceTypeEnum.HTTP.getCode().equals(method.getType())) {
                method.setHasLicense(authIds.contains(method.getInterfaceId()));
            } else if (InterfaceTypeEnum.JSF.getCode().equals(method.getType())) {
                method.setHasLicense(StringUtils.isNotBlank(jdosAppId.get(method.getInterfaceId())));
            }
        }
    }

    private void updateFollowStatus(List<MethodManageDTO> records) {
        if (records.isEmpty()) {
            return;
        }
        Set<Long> metIds = records.stream().map(vs -> Long.valueOf(vs.getId())).collect(Collectors.toSet());
        if (metIds.isEmpty()) return;
        LambdaQueryWrapper<InterfaceFollowList> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceFollowList::getMethodId, metIds);
        lqw.eq(InterfaceFollowList::getErp, UserSessionLocal.getUser().getUserId());
        List<InterfaceFollowList> followLists = interfaceFollowListService.list(lqw);
        if(!CollectionUtils.isEmpty(followLists)){
            Map<Long, List<InterfaceFollowList>> followMap = followLists.stream().collect(Collectors.groupingBy(InterfaceFollowList::getMethodId));
            for (MethodManageDTO manage : records) {
                if (followMap.containsKey(Long.valueOf(manage.getId()))) {
                    manage.setFollowStatus(1);
                } else {
                    manage.setFollowStatus(0);
                }
            }
        }
    }


    public void fillInterfaceAppInfo(List<MethodManageDTO> methodManages) {
        Map<String, Long> methodID2InterfaceId = methodManages.stream().collect(Collectors.toMap(item -> item.getId(), item -> item.getInterfaceId(), (s1, s2) -> s1));
        List<Long> ids = methodManages.stream().map(MethodManageDTO::getInterfaceId).collect(Collectors.toSet()).stream().collect(Collectors.toList());
        List<InterfaceManage> interfaces = interfaceManageService.listInterfaceByIds(ids);
        Map<Long, Long> interfaceId2appId = interfaces.stream().filter(item -> item.getAppId() != null).collect(Collectors.toMap(item->item.getId(),item->item.getAppId(),(s1, s2) -> s1));
        log.info("test11--》{}",interfaceId2appId);
        List<Long> appIds = interfaces.stream().filter(item -> item.getAppId() != null).map(InterfaceManage::getAppId).collect(Collectors.toList());
        if (ObjectHelper.isEmpty(appIds)) return;
        List<AppInfo> appInfos = appInfoService.listByIds(appIds);
        Map<Long, List<AppInfo>> id2Apps = appInfos.stream().collect(Collectors.groupingBy(AppInfo::getId));
        log.info("test--》{}",JSONObject.toJSONString(id2Apps));
        for (MethodManageDTO methodManage : methodManages) {
            Long interfaceId = methodID2InterfaceId.get(methodManage.getId());
            if (interfaceId == null) continue;
            Long appId = interfaceId2appId.get(interfaceId);
            if (appId == null) continue;
            AppInfo app = id2Apps.get(appId).get(0);
            methodManage.setAppId(appId);
            methodManage.setAppName(app.getAppName());
            methodManage.setAppCode(app.getAppCode());
            List<String> owners = AppUserTypeEnum.OWNER.splitErps(app.getMembers(), "-", ",");
            if(!CollectionUtils.isEmpty(owners)) {
                methodManage.setErp(owners.get(0));
            }
        }
    }
}
