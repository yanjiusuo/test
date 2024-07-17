package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.jtfm.configcenter.ducc.manage.ConfigCenterManagerHelperOne2NDucc;
import com.jd.jtfm.configcenter.ducc.model.CodeConstant;
import com.jd.jtfm.configcenter.ducc.model.ConfigItem;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.PublishEnum;
import com.jd.workflow.console.dao.mapper.PublishManageMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.PublishCluster;
import com.jd.workflow.console.entity.PublishManage;
import com.jd.workflow.console.helper.ProjectHelper;

import com.jd.workflow.console.service.*;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.BeanStepDefinition;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.metadata.impl.Ws2HttpStepMetadata;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 项目名称：example
 * 类 名 称：PublishManageServiceImpl
 * 类 描 述：发布service
 * 创建时间：2022-06-01 10:09
 * 创 建 人：wangxiaofei8
 */
@Service
@Slf4j
public class PublishManageServiceImpl extends ServiceImpl<PublishManageMapper, PublishManage> implements IPublishManageService {


    @Resource
    private IInterfaceManageService interfaceManageService;

    @Resource
    private IMethodManageService methodManageService;

    @Resource
    private IServiceConvertService serviceConvertService;

    @Resource
    private ProjectHelper projectHelper;

    @Resource
    IRoutePublishService routePublishService;

    @Resource
    private PublishManageMapper publishManageMapper;

    @Resource
    private IPublishClusterService publishClusterService;

    /*@Resource
    private JssUtil jssUtil;*/

    /**
     * 推送ducc提前初始化的cjg应用id
     */
    @Value("${camel.config.appId:integration-paas}")
    private String camelConfigAppId;


    /**
     * http转换后的webservice发布
     * @param methodId
     * @param interfaceId
     * @return
     */
    @Transactional
    @Override
    public Boolean publishConvertWebService(Long methodId, Long interfaceId, Long clusterId) {
        //TODO后续通过加锁解决并发，目前量不大，优先级低
        //反查对应转换webservice的对象
        HttpToWebServiceDTO httpToWebServiceDTO = serviceConvertService.findHttpToWebService(methodId, interfaceId);
        //构建发布的内容
        PublishInfoDTO publishInfoDTO = buildPublishInfoDTO(httpToWebServiceDTO, null);
        //发布服务
        return publishVersion(methodId,interfaceId,clusterId,publishInfoDTO);
    }

    @Transactional
    @Override
    public Boolean republishService(Long id, Long methodId, Long interfaceId) {
        //校验发布记录
        PublishManage entity = getById(id);
        if(entity==null||!methodId.equals(entity.getRelatedMethodId())){
            throw new BizException("发布记录不存在!");
        }
        //校验是否已经是发布中版本
        if(entity.getIsLatest().equals(PublishEnum.YES.getCode())){
            throw new BizException("当前已经是发布中的版本!");
        }
        //校验方法
        MethodManage methodEntity = methodManageService.getBaseMapper().selectById(methodId);
        if(methodEntity==null||!methodEntity.getInterfaceId().equals(interfaceId)||!methodEntity.getYn().equals(DataYnEnum.VALID.getCode())){
            throw new BizException("待发布的服务方法不存在!");
        }
        //查询当前生效的版本
        Long replaceLatestPublishId = getPublishEntityByMethodId(methodId);
        //反序列化要发布的版本内容
        PublishInfoDTO publishInfoDTO = JsonUtils.parse(entity.getContent(),PublishInfoDTO.class);

        String publishedMethodId = projectHelper.getPublishMethodId(methodId,interfaceId);
        String clusterCode = null;
        if(entity.getClusterId()!=null&&entity.getClusterId()>0){
            PublishCluster cluster = publishClusterService.getById(entity.getClusterId());
            if(cluster==null||!DataYnEnum.VALID.getCode().equals(cluster.getYn())){
                throw new BizException("发布的集群不存在!");
            }
            clusterCode = cluster.getClusterCode();
        }
        //推送发布到camel
        pushItemToDucc(publishInfoDTO.getCamelData(),publishedMethodId,entity.getVersionId().toString(),clusterCode);
        //调用地址
        //String address = String.format(PUBLISH_ADDRESS_FORMAT, publishAddressPrefix,methodId);
        Date opTime = new Date();
        //更新状态
        PublishManage toPublish = new PublishManage();
        toPublish.setId(id);
        toPublish.setModified(opTime);
        toPublish.setModifier(UserSessionLocal.getUser().getUserId());
        toPublish.setIsLatest(PublishEnum.YES.getCode());
        //entity.setAddress(address);
        Boolean result = this.updateById(toPublish);
        if(!result){
            throw new BizException("重新发布历史版本后更新版本状态失败!");
        }
        //更新当前生效状态为非生效版本
        if(replaceLatestPublishId!=null){
            PublishManage replaceEntity = new PublishManage();
            replaceEntity.setId(replaceLatestPublishId);
            replaceEntity.setIsLatest(PublishEnum.NO.getCode());
            replaceEntity.setModifier(UserSessionLocal.getUser().getUserId());
            replaceEntity.setModified(opTime);
            result =  this.updateById(replaceEntity);
            if(!result){
                throw new BizException("重新发布后更新历史版本发布状态失败!");
            }
        }

        //更新webservice方法发布时间以及操作人
        MethodManage updateEntity = new MethodManage();
        updateEntity.setId(methodId);
        updateEntity.setModifier(UserSessionLocal.getUser().getUserId());
        updateEntity.setModified(opTime);
        result = methodManageService.updateById(updateEntity);
        if(!result){
            throw new BizException("重新发布后更新服务发布状态失败!");
        }
        return result;
    }

    @Transactional
    @Override
    public Boolean publishWorkflow(WorkFlowPublishReqDTO dto) {
        MethodManage methodManage = methodManageService.getById(dto.getMethodId());
        Guard.notEmpty(methodManage,"无效的方法id");
        Guard.assertTrue(InterfaceTypeEnum.ORCHESTRATION.getCode() == methodManage.getType(),"必须为编排接口才可以发布");
        String workFlowJson = methodManage.getContent();
        if(StringUtils.isBlank(workFlowJson)){
            throw new BizException("当前工作流为空，请保存后再发布");
        }
        //构建发布的内容
        PublishInfoDTO publishInfoDTO = buildPublishInfoDTO(null, workFlowJson);
        //发布版本
        return publishVersion(dto.getMethodId(),dto.getInterfaceId(),dto.getClusterId(),publishInfoDTO);
    }

    @Override
    public List<PublishManageDTO> findPublicVersionList(Long methodId, Long interfaceId) {
        List<PublishManageDTO> result = new ArrayList<>();
        LambdaQueryWrapper<PublishManage> queryWrapper = Wrappers.<PublishManage>lambdaQuery();
        queryWrapper.eq(PublishManage::getRelatedMethodId,methodId).orderByDesc(PublishManage::getIsLatest).orderByDesc(PublishManage::getModified);
        List<PublishManage> list = list(queryWrapper.setEntityClass(PublishManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                return !tableFieldInfo.getColumn() .equalsIgnoreCase("content");
            }
        }));
        if(CollectionUtils.isNotEmpty(list)){
            Set<Long> clusterIds = list.stream().filter(o -> o.getClusterId() != null && o.getClusterId() > 0).map(o -> o.getClusterId()).collect(Collectors.toSet());
            Map<Long, PublishCluster> clusterMap = Collections.emptyMap();
            if(clusterIds.size()>0){
                clusterMap = Optional.ofNullable(publishClusterService.list(Wrappers.<PublishCluster>lambdaQuery().in(PublishCluster::getId, clusterIds)))
                        .orElse(new ArrayList<>()).stream().collect(Collectors.toMap(PublishCluster::getId, Function.identity()));
            }
            for (PublishManage obj : list) {
                PublishManageDTO dto = new PublishManageDTO();
                dto.setId(obj.getId());
                if(clusterMap.containsKey(obj.getClusterId())){
                    dto.setAddress(projectHelper.getPublishUrl(clusterMap.get(obj.getClusterId()).getClusterDomain(),projectHelper.getPublishMethodId(obj.getRelatedMethodId(),interfaceId)));
                }else{
                    dto.setAddress(projectHelper.getPublishUrl(projectHelper.getPublishMethodId(obj.getRelatedMethodId(),interfaceId)));
                }
                dto.setVersionId(obj.getVersionId());
                dto.setClusterId(obj.getClusterId());
                dto.setIsLatest(obj.getIsLatest());
                dto.setRelatedMethodId(obj.getRelatedMethodId());
                dto.setModified(obj.getModified());
                dto.setModifier(obj.getModifier());
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public PublishManageDTO findPublishVersionDetail(Long id, Long methodId, Long interfaceId) {
        //校验发布记录
        PublishManage obj = getById(id);
        if(obj==null||!methodId.equals(obj.getRelatedMethodId())){
            throw new BizException("发布记录不存在!");
        }
        PublishManageDTO dto = new PublishManageDTO();
        dto.setId(obj.getId());
        String publishMethodId = projectHelper.getPublishMethodId(methodId,interfaceId);
        if(obj.getClusterId()!=null&&obj.getClusterId()>0){
            PublishCluster cluster = publishClusterService.getById(obj.getClusterId());
            dto.setAddress(projectHelper.getPublishUrl(cluster.getClusterDomain(),publishMethodId));
        }else{
            dto.setAddress(projectHelper.getPublishUrl(publishMethodId));
        }
        dto.setClusterId(obj.getClusterId());
        dto.setModified(obj.getModified());
        dto.setModifier(obj.getModifier());
        dto.setVersionId(obj.getVersionId());
        /**
        dto.setVersionId(obj.getVersionId());
        dto.setCamelId(obj.getCamelId());
        dto.setIsLatest(obj.getIsLatest());
        dto.setRelatedMethodId(obj.getRelatedMethodId());
        PublishInfoDTO publishInfoDTO = JsonUtils.parse(jssUtil.jssFile2String(obj.getContent())
                ,PublishInfoDTO.class);*/
        PublishInfoDTO publishInfoDTO = JsonUtils.parse(obj.getContent(),PublishInfoDTO.class);
        dto.setPublishInfoDTO(publishInfoDTO);
        return dto;
    }

    @Override
    public Page<PublishMethodDTO> queryPublishMethods(PublishMethodQueryDTO queryDTO) {
        Long count = publishManageMapper.queryPublishMethodCount(queryDTO);
        List<PublishMethodDTO> list = publishManageMapper.queryPublishMethodList(queryDTO);
        Page<PublishMethodDTO> page = new Page<>(queryDTO.getCurrentPage(),queryDTO.getPageSize());
        page.setRecords(list);
        page.setTotal(count);
        return page;
    }

    /**
     * 获得当前发布的id
     * @param methodId
     * @return
     */
    private Long getPublishEntityByMethodId(Long methodId){
        LambdaQueryWrapper<PublishManage> queryWrapper = Wrappers.<PublishManage>lambdaQuery();
        queryWrapper.eq(PublishManage::getRelatedMethodId,methodId).eq(PublishManage::getIsLatest, PublishEnum.YES.getCode());
        List<PublishManage> list = this.list(queryWrapper.setEntityClass(PublishManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                return !tableFieldInfo.getColumn() .equalsIgnoreCase("content");
            }
        }));
        if(CollectionUtils.isNotEmpty(list)){
            return list.get(0).getId();
        }
        return null;
    }

   

    /**
     * 发布新的服务
     * @param methodId
     * @param interfaceId
     * @param publishInfoDTO
     * @return
     */
    private Boolean publishVersion(Long methodId, Long interfaceId,Long clusterId,PublishInfoDTO publishInfoDTO){

        methodManageService.getEntityById(methodId);
        String publishedMethodId = projectHelper.getPublishMethodId(methodId,interfaceId);
        //调用地址
        //String address = String.format(PUBLISH_ADDRESS_FORMAT, publishAddressPrefix,methodId);
        //新增发布记录
        PublishManage entity = new PublishManage();
        //entity.setAddress(address);
        entity.setIsLatest(PublishEnum.YES.getCode());
        entity.setRelatedMethodId(methodId);
        entity.setClusterId(clusterId);
        //自增版本ID
        Integer startVersionId = 1;
        //是否需要更新历史发布记录中的isLatest字段
        Long replaceLatestPublishId = null;
        List<PublishManage> versionList = list(Wrappers.lambdaQuery(PublishManage.class).eq(PublishManage::getRelatedMethodId, methodId)
                .orderByDesc(PublishManage::getVersionId).last("limit 1").setEntityClass(PublishManage.class).select(new Predicate<TableFieldInfo>() {
                    @Override
                    public boolean test(TableFieldInfo tableFieldInfo) {
                        return !tableFieldInfo.getColumn() .equalsIgnoreCase("content");
                    }
                }));
        if(CollectionUtils.isNotEmpty(versionList)){
            startVersionId = versionList.get(0).getVersionId()+1;
            if(PublishEnum.YES.getCode().equals(versionList.get(0).getIsLatest())){
                replaceLatestPublishId = versionList.get(0).getId();
            }else{
                replaceLatestPublishId = getPublishEntityByMethodId(methodId);
            }
        }
        String clusterCode = null;
        if(clusterId!=null&&clusterId>0){
            PublishCluster cluster = publishClusterService.getById(clusterId);
            if(cluster==null||!DataYnEnum.VALID.getCode().equals(cluster.getYn())){
                throw new BizException("发布的集群不存在!");
            }
            clusterCode = cluster.getClusterCode();
        }
        //推送发布到camel
        pushItemToDucc(publishInfoDTO.getCamelData(),publishedMethodId,startVersionId.toString(),clusterCode);
        entity.setVersionId(startVersionId);
        entity.setContent(JsonUtils.toJSONString(publishInfoDTO));
        //entity.setContent(jssUtil.uploadString(JsonUtils.toJSONString(publishInfoDTO), UUID.randomUUID() + "_publicsh.txt"));
        entity.setYn(DataYnEnum.VALID.getCode());
        Date opTime = new Date();
        entity.setCreated(opTime);
        entity.setModified(opTime);
        entity.setCreator(UserSessionLocal.getUser().getUserId());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        Boolean result = this.save(entity);
        if(!result){
            throw new BizException("保存发布信息失败!");
        }

        //更新历史发布信息状态islatest
        if(replaceLatestPublishId!=null){
            PublishManage replaceEntity = new PublishManage();
            replaceEntity.setId(replaceLatestPublishId);
            replaceEntity.setIsLatest(PublishEnum.NO.getCode());
            replaceEntity.setModifier(UserSessionLocal.getUser().getUserId());
            replaceEntity.setModified(opTime);
            result =  this.updateById(replaceEntity);
            if(!result){
                throw new BizException("更新历史发布状态失败!");
            }
        }

        //更新webservice方法发布状态以及更新操作人和修改时间
        MethodManage updateEntity = new MethodManage();
        updateEntity.setId(methodId);
        updateEntity.setPublished(PublishEnum.YES.getCode());
        //updateEntity.setEndpointUrl(address);
        updateEntity.setModifier(UserSessionLocal.getUser().getUserId());
        updateEntity.setModified(opTime);
        result = methodManageService.updateById(updateEntity);
        if(!result){
            throw new BizException("更新服务发布状态失败!");
        }
        return result;
    }

    /**
     * 构建发布camel原始信息对象
     * @param httpToWebServiceDTO
     * @return
     */
    private PublishInfoDTO buildPublishInfoDTO(HttpToWebServiceDTO httpToWebServiceDTO,String workflowData){
        PublishInfoDTO dto = new PublishInfoDTO();
        if(httpToWebServiceDTO!=null){
            //校验输入和输出schemaType
            if(httpToWebServiceDTO.getInput()==null||httpToWebServiceDTO.getInput().getSchemaType()==null
                    ||httpToWebServiceDTO.getOutput()==null||httpToWebServiceDTO.getOutput().getSchemaType()==null){
                throw new BizException("http转换的webservice方法中设置的请求和返回值schemaType为空");
            }
            Map<String,Object> args = new HashMap<>();
            List<String> endponitUrls = new ArrayList<>();
            InterfaceManage interfaceObj = interfaceManageService.getById(httpToWebServiceDTO.getInterfaceId());
            //校验接口的调用环境地址
            if(interfaceObj==null|| StringUtils.isBlank(interfaceObj.getEnv())){
                throw new BizException("接口信息不存在");
            }
            List<EnvModel> models = JsonUtils.parseArray(interfaceObj.getEnv(), EnvModel.class);
            EnvModel foundModel = null;
            for (EnvModel model : models) {
                if(httpToWebServiceDTO.getEnv().equals(model.getEnvName())){
                    foundModel = model;
                }
            }
            if(foundModel == null ||CollectionUtils
                    .isEmpty(foundModel.getUrl() )){
                throw new BizException(httpToWebServiceDTO.getEnv()+"环境信息或远程服务地址不存在");
            }
            //构建调用地址
            args.put("endpointUrl", foundModel.getUrl());//callenv 对应多个path list集合
            Map<String,Object> input = new HashMap<>();
            input.put("schemaType", httpToWebServiceDTO.getInput().getSchemaType());
            args.put("input",input);
            args.put("id","ws2http");
            args.put("url","/json");
            args.put("httpMethod","post");
            args.put("reqType","json");
            args.put("type","ws2http");
            Map<String,Object> output = new HashMap<>();
            output.put("schemaType",httpToWebServiceDTO.getOutput().getSchemaType());
            args.put("output",output);
            Ws2HttpStepMetadata stepMetadata = (Ws2HttpStepMetadata) StepProcessorRegistry.parseMetadata(args);
            BeanStepDefinition beanDef = new BeanStepDefinition();
            beanDef.setMetadata(stepMetadata);
            WorkflowDefinition workflowDefinition = new WorkflowDefinition();
            workflowDefinition.setTasks(Collections.singletonList(beanDef));
            dto.setWorkflowData(JsonUtils.toJSONString(workflowDefinition));
            dto.setCamelData(RouteBuilder.buildRoute(workflowDefinition));
        }else{
            dto.setWorkflowData(workflowData);
            WorkflowDefinition workflowDefinition = WorkflowParser.parse(workflowData);
            dto.setCamelData(RouteBuilder.buildRoute(workflowDefinition));
        }
        return dto;
    }


    /**
     * 调用camelsdk推送ducc配置
     * @param camelData
     * @param methodId
     */
    private void pushItemToDucc(String camelData,String methodId,String version,String clusterCode){
        PublishRecordDto dto = new PublishRecordDto();
        dto.setConfig(camelData);
        dto.setMethodId(methodId);
        dto.setPublishVersion(version);
        dto.setClusterCode(clusterCode);
        routePublishService.publish(dto);
    }
}
