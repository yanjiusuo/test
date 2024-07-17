package com.jd.workflow.console.service.doc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dao.mapper.doc.InterfaceVersionMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.MethodSnapshot;
import com.jd.workflow.console.dto.doc.MethodSnapshotItem;
import com.jd.workflow.console.dto.doc.method.HttpMethodDocConfig;
import com.jd.workflow.console.dto.doc.method.JsfMethodDocConfig;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.dto.version.CompareMethodVersionDTO;
import com.jd.workflow.console.dto.version.CompareVersionDTO;
import com.jd.workflow.console.dto.version.InterfaceInfo;
import com.jd.workflow.console.dto.version.InterfaceInfoReq;
import com.jd.workflow.console.dto.version.MethodVersionDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import com.jd.workflow.console.service.doc.IMethodVersionModifyLogService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.console.utils.ReqDemoBuildUtils;
import com.jd.workflow.console.utils.VersionManager;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InterfaceVersionServiceImpl extends ServiceImpl<InterfaceVersionMapper, InterfaceVersion> implements IInterfaceVersionService {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private IInterfaceManageService interfaceManageService;

    @Resource
    private IMemberRelationService memberRelationService;

    @Resource
    private IInterfaceMethodGroupService interfaceMethodGroupService;

    @Autowired
    private MethodManageServiceImpl methodManageService;

    @Autowired
    private IInterfaceVersionService versionService;

    /*@Autowired
    private IMethodModifyLogService methodModifyLogService;*/

    @Autowired
    private IMethodVersionModifyLogService methodVersionModifyLogService;
    @Autowired
    IMethodModifyLogService methodModifyLogService;

    @Autowired
    private IAppInfoService appInfoService;
    @Autowired
    MethodModifyDeltaInfoService deltaInfoService;

    @Override
    public InterfaceInfo findInterfaceBaseInfo(InterfaceInfoReq req) {
        InterfaceInfo info = new InterfaceInfo();
        InterfaceManage interfaceObj = interfaceManageService.getById(req.getInterfaceId());
        if(interfaceObj==null){
            throw new BizException("接口不存在！");
        }
        info.setAppId(interfaceObj.getAppId());
        info.setInterfaceId(interfaceObj.getId());
        info.setName(interfaceObj.getName());
        info.setType(interfaceObj.getType());
        if(req.getVersion()!=null){
            info.setVersion(req.getVersion());
        }else{
            info.setVersion(interfaceObj.getLatestDocVersion());
        }
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceId(req.getInterfaceId());
        memberRelationDTO.setResourceType(getResourceType(interfaceObj.getType()).getCode());
        UserInfo admin = memberRelationService.getAdminWithUser(memberRelationDTO);
        if(admin!=null){
            info.setPin(admin.getUserCode());
            UserVo vo = userHelper.getUserBaseInfoByUserName(admin.getUserCode());
            if(vo!=null){
                info.setPinName(vo.getRealName());
                info.setDepartment(vo.getOrganizationFullName());
            }
        }
        //获取应用的负责人
        if(StringUtils.isBlank(info.getDepartment())&&interfaceObj.getAppId()!=null&&interfaceObj.getAppId()>0){
            AppInfo app = appInfoService.getById(interfaceObj.getAppId());
            if(app!=null&&StringUtils.isNotBlank(app.getMembers())){
                List<String> owners = AppUserTypeEnum.OWNER.splitErps(app.getMembers(), "-", ",");
                if(CollectionUtils.isNotEmpty(owners)){
                    info.setPin(owners.get(0));
                    UserVo vo = userHelper.getUserBaseInfoByUserName(info.getPin());
                    if(vo!=null){
                        info.setPinName(vo.getRealName());
                        info.setDepartment(vo.getOrganizationFullName());
                    }
                }
            }
        }
        if(info.getVersion()!=null){
            InterfaceVersion interfaceVersion = getInterfaceVersion(req.getInterfaceId(), info.getVersion());
            if(interfaceVersion==null){
                throw new BizException("接口版本信息不存在！");
            }
            info.setVersionCreated(interfaceVersion.getCreated());
            info.setVersionDesc(interfaceVersion.getDesc());
        }
        return info;
    }

    @Override
    public MethodGroupTreeDTO findMethodGroupTree(InterfaceInfoReq req) {
        InterfaceManage interfaceObj = interfaceManageService.getById(req.getInterfaceId());
        if(interfaceObj==null){
            throw new BizException("接口不存在！");
        }
        if(req.getVersion()==null||(req.getVersion()!=null&&req.getVersion().equals(interfaceObj.getLatestDocVersion()))){
            return interfaceMethodGroupService.findMethodGroupTree(req.getInterfaceId());
        }
        InterfaceVersion interfaceVersion = getInterfaceVersion(req.getInterfaceId(),req.getVersion());

        if(interfaceVersion==null){
            throw new BizException("接口版本信息不存在！");
        }
        MethodGroupTreeDTO dto = new MethodGroupTreeDTO();
         MethodGroupTreeModel treeModel = interfaceVersion.getGroupTreeSnapshot();
         dto.setTreeModel(treeModel);
         dto.setInterfaceId(interfaceObj.getId());
        return dto;
    }

    /**
     * 创建新版本后，把老版本方法的签名固化下来
     * @param req
     * @return
     */
    @Transactional
    @Override
    public String createInterfaceVersion(InterfaceInfoReq req) {
        InterfaceManage interfaceObj = interfaceManageService.getById(req.getInterfaceId());
        if(interfaceObj==null){
            throw new BizException("接口不存在！");
        }
        List<MethodManage> existMethods = methodManageService.getInterfaceMethods(interfaceObj.getId());
      //  List<MethodManage> allMethods = new ArrayList<>();

       /* List<MethodManage> hasDigestMethods = existMethods.stream().filter(vs->StringUtils.isNotBlank(vs.getDigest())).collect(Collectors.toList());
        List<MethodManage> noDigestMethods = existMethods.stream().filter(vs->StringUtils.isBlank(vs.getDigest())).collect(Collectors.toList());

        allMethods.addAll(hasDigestMethods);
        allMethods.addAll(methodManageService.fixNoContentMethodsDigest(noDigestMethods));*/
        InterfaceVersion oldVersion = null;

        InterfaceVersion interfaceVersion = new InterfaceVersion();
        interfaceVersion.setInterfaceId(interfaceObj.getId());
        interfaceVersion.setDesc(req.getAddVersionDesc());
        if(interfaceObj.getLatestDocVersion() == null){
            String version = "1.0.0";
            interfaceObj.setLatestDocVersion(version);
            interfaceVersion.setVersion(version);
        }else{
            interfaceVersion.setVersion(VersionManager.increaseVersion(interfaceObj.getLatestDocVersion(),req.getAddVersionType()));

            oldVersion = versionService.getInterfaceVersion(req.getInterfaceId(),interfaceObj.getLatestDocVersion());
        }

        MethodSnapshot snapshot = new MethodSnapshot();
        for (MethodManage existMethod : existMethods) {
            MethodSnapshotItem snapshotItem = new MethodSnapshotItem();
            snapshotItem.setMethodId(existMethod.getId());
           /* if(StringUtils.isBlank(existMethod.getDigest())){
                methodManageService.fillMethodDigest(existMethod);
            }*/
            snapshotItem.setDigest(existMethod.getMergedContentDigest());
            snapshotItem.setPath(existMethod.getPath());
            snapshot.getMethods().add(snapshotItem);
        }
        interfaceVersion.setMethodSnapshot(snapshot);
        MethodGroupTreeDTO methodGroupTree = interfaceMethodGroupService.findMethodGroupTree(interfaceObj.getId());
        interfaceVersion.setGroupTreeSnapshot(methodGroupTree.getTreeModel());

        if(oldVersion != null){
            oldVersion.setGroupTreeSnapshot(methodGroupTree.getTreeModel());
            oldVersion.setMethodSnapshot(snapshot);
            versionService.updateById(oldVersion);
        }

        interfaceVersion.setCreator(UserSessionLocal.getUser().getUserId());
        interfaceVersion.setModifier(UserSessionLocal.getUser().getUserId());
        boolean result = versionService.save(interfaceVersion);
        if(result){
            InterfaceManage obj = new InterfaceManage();
            obj.setId(req.getInterfaceId());
            obj.setLatestDocVersion(interfaceVersion.getVersion());
            obj.setModifier(UserSessionLocal.getUser().getUserId());
            result = interfaceManageService.updateById(obj);
        }
        if(!result){
            log.error("IInterfaceVersionService createInterfaceVersion save new version return false >>>>>>");
            throw new BizException("操作异常，请求稍后重试！");
        }
        return interfaceVersion.getVersion();
    }

    @Override
    public String viewNextInterfaceVersion(InterfaceInfoReq req) {
        InterfaceManage interfaceObj = interfaceManageService.getById(req.getInterfaceId());
        if(interfaceObj==null){
            throw new BizException("接口不存在！");
        }
        //TODO 是否必须有版本 才能新增迭代
        if(interfaceObj.getLatestDocVersion() == null){
            return "1.0.0";
        }else{
            return VersionManager.increaseVersion(interfaceObj.getLatestDocVersion(),req.getAddVersionType());
        }
    }

    @Override
    public List<InterfaceVersion> findInterfaceVersion(InterfaceInfoReq req) {
        LambdaQueryWrapper<InterfaceVersion> qw = Wrappers.<InterfaceVersion>lambdaQuery().orderByDesc(InterfaceVersion::getId);
        qw.eq(req.getInterfaceId()!=null,InterfaceVersion::getInterfaceId,req.getInterfaceId());
        qw.eq(InterfaceVersion::getYn,1);

        qw.select(InterfaceVersion.class, x -> !(x.getColumn().equals("method_snapshot")||
                x.getColumn().equals("group_tree_snapshot")));
        return this.list(qw);
    }

    @Override
    public CompareVersionDTO compareInterfaceVersion(InterfaceInfoReq req) {
        InterfaceManage interfaceObj = interfaceManageService.getById(req.getInterfaceId());
        if(interfaceObj==null){
            throw new BizException("接口不存在！");
        }
        List<InterfaceVersion> interfaceVersions = this.getBaseMapper().selectList(Wrappers.<InterfaceVersion>lambdaQuery().eq(InterfaceVersion::getInterfaceId, req.getInterfaceId()).in(InterfaceVersion::getVersion, req.getVersion(), req.getCompareVersion()));
        if(CollectionUtils.isEmpty(interfaceVersions)){
            throw new BizException("接口版本信息不存在！");
        }
        if(interfaceVersions.size()!=2 ){
            throw new BizException("同版本不可比较！");
        }
        CompareVersionDTO dto = new CompareVersionDTO();
        if(req.getVersion().equals(interfaceVersions.get(0).getVersion())){
            dto.setBaseVersion(interfaceVersions.get(0));
            dto.setCompareVersion(interfaceVersions.get(1));
        }else{
            dto.setBaseVersion(interfaceVersions.get(1));
            dto.setCompareVersion(interfaceVersions.get(0));
        }
        interfaceMethodGroupService.findGroupTreeVersionDiff(req,interfaceObj,dto);
        dto.getBaseVersion().setGroupTreeSnapshot(null);
        dto.getBaseVersion().setMethodSnapshot(null);
        dto.getCompareVersion().setGroupTreeSnapshot(null);
        dto.getCompareVersion().setMethodSnapshot(null);
        return dto;
    }
    @Override
    public MethodManageDTO getVersionMethod(String version,Long methodId){
        MethodManage methodManage = methodManageService.getById(methodId);
        MethodVersionDTO versionDTO = new MethodVersionDTO();
        if(StringUtils.isBlank(version)){
            versionDTO = getLatestMethodVersion(methodManage,version,false);
        }else{
            InterfaceManage interfaceManage = interfaceManageService.getById(methodManage.getInterfaceId());
            versionDTO = getMethodVersionContent(interfaceManage,methodManage,version,false);
        }
        if(versionDTO == null) return null;
        MethodManageDTO dto = new MethodManageDTO();
        BeanUtils.copyProperties(methodManage,dto);
        dto.setId(methodManage.getId()+"");
        dto.setDocInfo(versionDTO.getDesc());
        MethodDocConfig methodDocConfig = new MethodDocConfig();
        methodDocConfig.setInputExample(versionDTO.getInputExample());
        methodDocConfig.setOutputExample(versionDTO.getOutputExample());
        dto.setDocConfig(methodDocConfig);
        dto.setContent(versionDTO.getContent());
        dto.setContentObject(versionDTO.getContentObject());
        dto.setHttpMethod(versionDTO.getHttpMethod());
        dto.setVersion(version);
        return dto;
    }
    @Override
    public CompareMethodVersionDTO compareMethodVersion(InterfaceInfoReq req) {
        InterfaceManage interfaceObj = interfaceManageService.getById(req.getInterfaceId());
        if(interfaceObj==null){
            throw new BizException("接口不存在！");
        }
        MethodManage method = methodManageService.getOne(Wrappers.<MethodManage>lambdaQuery().eq(MethodManage::getInterfaceId, req.getInterfaceId())
                .eq(MethodManage::getId, req.getMethodId()));
        if(method==null){
            throw new BizException("接口对应方法不存在！");
        }
        CompareMethodVersionDTO dto = new CompareMethodVersionDTO();
        dto.setInterfaceId(interfaceObj.getId());
        dto.setMethodId(method.getId());
        dto.setMethodCode(method.getMethodCode());
        dto.setMethodName(method.getName());
        dto.setDesc(method.getDesc());
        dto.setType(method.getType());
        dto.setHttpMethod(method.getHttpMethod());
        dto.setVersionDto(getMethodVersionContent(interfaceObj,method,req.getVersion(), BooleanUtils.isTrue(req.getDemonCompare())));
        if(req.getVersion().equals(req.getCompareVersion())){
            dto.setCompareVersionDto(dto.getVersionDto());
        }else if(req.getCompareVersion()!=null){
            dto.setCompareVersionDto(getMethodVersionContent(interfaceObj,method,req.getCompareVersion(), BooleanUtils.isTrue(req.getDemonCompare())));
        }
        return dto;
    }
    public  MethodVersionDTO getLatestMethodVersion( MethodManage method,String version,boolean compareDemon){
        methodManageService.initContentObject(method);
        deltaInfoService.initMethodDeltaInfo(Collections.singletonList(method));

        MethodVersionDTO dto = new MethodVersionDTO();
        dto.setContent(JsonUtils.toJSONString(method.getContent()));


        dto.setVersionDesc("当前最新版本");
        dto.setVersion(version);
        dto.setDesc(method.getDocInfo());
        dto.setHttpMethod(method.getHttpMethod());
        dto.setModified(method.getModified());
        dto.setModifier(method.getModifier());
        if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
            JsfStepMetadata model = (JsfStepMetadata) method.getContentObject();
            dto.setContentObject(model);
            if(compareDemon){
                dto.setContentObject(JsfMethodDemonModel.convertDemonByJsfMethodModel(model));
            }
            MethodDocConfig config =  method.getDocConfig();
            if(config==null||StringHelper.isEmpty(config.getInputExample())){
                dto.setInputExample(ReqDemoBuildUtils.getJsfInputDemoValue(model));
            }else{
                dto.setInputExample(config.getInputExample());
            }
            if(config==null||StringHelper.isEmpty(config.getOutputExample())){
                dto.setOutputExample(ReqDemoBuildUtils.getJsfOutputDemoValue(model));
            }else{
                dto.setOutputExample(config.getOutputExample());
            }
        }else if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
            HttpMethodModel model = (HttpMethodModel) method.getContentObject();
            dto.setContentObject(model);
            if(compareDemon){
                dto.setContentObject(HttpMethodDemonModel.convertDemonByHttpMethodModel(model));
            }
            MethodDocConfig config =  method.getDocConfig();
            if(config==null||StringHelper.isEmpty(config.getInputExample())){
                dto.setInputExample(ReqDemoBuildUtils.buildHttpInput(model));
            }else{
                dto.setInputExample(config.getInputExample());
            }
            if(config==null||StringHelper.isEmpty(config.getOutputExample())){
                dto.setOutputExample(ReqDemoBuildUtils.buildHttpOutput(model));
            }else{
                dto.setOutputExample(config.getOutputExample());
            }

        }
        return dto;
    }

    // 判断2个日期是否差值在1秒内
    private boolean isEqualsOrBefore(Date date1,Date date2){
        if(date1==null||date2==null){
            return false;
        }
        long time1 = date1.getTime();
        long time2 = date2.getTime();
        if(time1  <= time2 ) return true;
        if(Math.abs(time1 - time2) < 1000) return true;
        return false;
    }
    private InterfaceVersion getInterfaceNextVersion(Long interfaceId,Long versionId){
        return getOne(Wrappers.<InterfaceVersion>lambdaQuery().eq(InterfaceVersion::getInterfaceId, interfaceId)
                .gt(InterfaceVersion::getId, versionId).orderByAsc(InterfaceVersion::getId).last("limit 1"));
    }
    private MethodVersionDTO getMethodVersionContent(InterfaceManage interfaceObj,MethodManage method,String version,boolean compareDemon){
        MethodVersionDTO dto = new MethodVersionDTO();
        InterfaceVersion interfaceVersion = getInterfaceVersion(interfaceObj.getId(), version);
        if(interfaceVersion==null){
            log.error("InterfaceVersionServiceImpl.getMethodVersionContent InterfaceVersion by interfaceId={} , version={} get result is null>>>>>>>>>>>>>>",interfaceObj.getId(),version);
            throw new BizException("接口版本不存在！");
        }
        dto.setVersionDesc(interfaceVersion.getDesc());
        if(version.equals(interfaceObj.getLatestDocVersion())){
            return getLatestMethodVersion( method, version, compareDemon);
        }else{
            dto.setVersion(version);
            MethodVersionModifyLog lastObj = methodVersionModifyLogService.getOne(Wrappers.<MethodVersionModifyLog>lambdaQuery().eq(MethodVersionModifyLog::getInterfaceId, interfaceObj.getId())
                    .eq(MethodVersionModifyLog::getMethodId, method.getId())
                    .eq(MethodVersionModifyLog::getVersion, version));
            if(lastObj==null){ // 如果指定版本的记录未找到，就找该版本之后的第一条修改记录

                List<MethodModifyLog> list = methodModifyLogService.list(Wrappers.<MethodModifyLog>lambdaQuery().eq(MethodModifyLog::getInterfaceId, interfaceObj.getId())
                        .eq(MethodModifyLog::getMethodId, method.getId()).ge(MethodModifyLog::getModified, interfaceVersion.getCreated())
                        .orderByAsc(MethodModifyLog::getId).last("LIMIT 1"));
                if(CollectionUtils.isNotEmpty(list)){
                    MethodModifyLog modifyLog = list.get(0);
                    lastObj = new MethodVersionModifyLog();
                    BeanUtils.copyProperties(modifyLog,lastObj);
                }else {
                    InterfaceVersion nextVersion = getInterfaceNextVersion(interfaceObj.getId(), interfaceVersion.getId());
                    log.warn("interfaceVersion.miss_change_log:InterfaceId={} , methodId={} , version={} have no change log>>>>>>>>>",interfaceObj.getId(),method.getId(),version);
                    if(isEqualsOrBefore(method.getCreated(),nextVersion.getCreated() ) ){ // 在下一个版本之前创建的接口，没有修改记录，直接返回最新版本
                        return getLatestMethodVersion( method, version, compareDemon);
                    }else{ // 说明当前版本，该方法还未创建
                       lastObj = null;
                    }

                }
            }
            if(lastObj!=null){
                dto.setContent(lastObj.getMethodContentSnapshot().getContent());
                if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                    JsfStepMetadata model = JsonUtils.parse(dto.getContent(),JsfStepMetadata.class);
                    dto.setContentObject(model);
                    if(compareDemon){
                        dto.setContentObject(JsfMethodDemonModel.convertDemonByJsfMethodModel(model));
                    }
                }else if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                    HttpMethodModel model = JsonUtils.parse(dto.getContent(), HttpMethodModel.class);
                    dto.setContentObject(model);
                    if(compareDemon){
                        dto.setContentObject(HttpMethodDemonModel.convertDemonByHttpMethodModel(model));
                    }
                }
                dto.setDesc(lastObj.getMethodContentSnapshot().getDesc());
                dto.setHttpMethod(lastObj.getMethodContentSnapshot().getHttpMethod());
                dto.setInputExample(lastObj.getMethodContentSnapshot().getInputExample());
                dto.setOutputExample(lastObj.getMethodContentSnapshot().getOutputExample());
                dto.setModified(lastObj.getModified());
                dto.setModifier(lastObj.getModifier());
            }
            return dto;
        }
    }


    private ResourceTypeEnum getResourceType(Integer code){
        InterfaceTypeEnum type = InterfaceTypeEnum.getByCode(code);
        ResourceTypeEnum resourceType=null;
        if(type==null){
            return null;
        }
        switch (type){

            case ORCHESTRATION:
                resourceType=ResourceTypeEnum.ORCHESTRATION;
                break;
            default:
                resourceType=ResourceTypeEnum.INTERFACE;
                break;
        }
        return resourceType;
    }
    @Override
    public InterfaceVersion getInterfaceVersion(Long interfaceId, String version) {
        LambdaQueryWrapper<InterfaceVersion> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceVersion::getInterfaceId,interfaceId);
        lqw.eq(InterfaceVersion::getVersion,version);
         List<InterfaceVersion> list = list(lqw);
        if(!list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

    @Override
    public InterfaceVersion initInterfaceVersion(InterfaceManage manage) {
        InterfaceVersion version = new InterfaceVersion();
        version.setVersion("1.0.0");
        version.setInterfaceId(manage.getId());
        version.setCreated(new Date());
        version.setModified(new Date());
        version.setYn(1);
        manage.setLatestDocVersion(version.getVersion());
        save(version);
        return version;
    }

    @Override
    public void updateInterfaceVersion(InterfaceVersion interfaceVersion) {
        if(Objects.isNull(interfaceVersion.getId())){
            throw new BizException("参数不正确！");
        }
        InterfaceVersion interfaceVersionDB;
        if(Objects.isNull(interfaceVersionDB = this.getById(interfaceVersion.getId()))){
            throw new BizException("未找到对应的版本信息");
        }
        interfaceVersionDB.setFinalVersion(interfaceVersion.getFinalVersion());
        interfaceVersionDB.setDesc(interfaceVersion.getDesc());
        this.updateById(interfaceVersion);
    }

    @Override
    public void removeInterfaceVersion(Long interfaceId) {
        LambdaUpdateWrapper<InterfaceVersion> luw = new LambdaUpdateWrapper<>();
        luw.eq(InterfaceVersion::getInterfaceId,interfaceId);
        luw.set(InterfaceVersion::getYn,1);
    }
}
