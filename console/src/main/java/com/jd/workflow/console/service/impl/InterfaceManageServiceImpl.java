package com.jd.workflow.console.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.businessworks.domain.FlowBeanInfo;
import com.jd.cjg.kg.vo.KgBusinessDomainVo;
import com.jd.jsf.gd.util.StringUtils;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.PageParam;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.dao.mapper.InterfaceManageMapper;
import com.jd.workflow.console.dao.mapper.MethodManageMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.auth.InterfaceAuthFilter;
import com.jd.workflow.console.dto.datasource.DataSourceDto;
import com.jd.workflow.console.dto.dept.QueryDeptReqDTO;
import com.jd.workflow.console.dto.dept.QueryDeptResultDTO;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.jsf.JsfImportDto;
import com.jd.workflow.console.dto.manage.InterfaceAppSearchDto;
import com.jd.workflow.console.dto.manage.InterfaceOrMethodResult;
import com.jd.workflow.console.dto.manage.MethodSearchResult;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.helper.MaskPinHelper;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.jme.JdMEMessageUtil;
import com.jd.workflow.console.jme.JdMENoticeMessage;
import com.jd.workflow.console.jme.JdMEResult;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import com.jd.workflow.console.service.doc.IMethodVersionModifyLogService;
import com.jd.workflow.console.service.doc.importer.JapiHttpDataImporter;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.utils.SafeUtil;
import com.jd.workflow.flow.bean.BeanStepDefinitionLoader;
import com.jd.workflow.flow.bean.utils.ValidateUtils;
import com.jd.workflow.flow.core.bean.IBeanStepProcessor;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * <p>
 * 接口管理 服务实现类
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Slf4j
@Service
public class InterfaceManageServiceImpl extends ServiceImpl<InterfaceManageMapper, InterfaceManage> implements IInterfaceManageService {
    private static final String ERP_PATTERN = "[a-z0-9.]+";
    /**
     * 数据表
     *
     * @date: 2022/5/16 11:09
     * @author wubaizhao1
     */
    @Resource
    InterfaceManageMapper interfaceManageMapper;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    UserHelper userHelper;
    @Autowired
    IInterfaceVersionService interfaceVersionService;


    /**
     * 资源关系表
     *
     * @date: 2022/5/16 11:09
     * @author wubaizhao1
     */
    @Resource
    IMemberRelationService memberRelationService;
    @Autowired
    IMethodModifyLogService methodModifyLogService;
    @Autowired
    IMethodVersionModifyLogService methodVersionModifyLogService;

    /**
     * 方法表
     *
     * @date: 2022/6/7 17:35
     * @author wubaizhao1
     */
    @Resource
    IMethodManageService methodManageService;

    @Resource
    MethodManageMapper methodManageMapper;
    /**
     * 用户表
     *
     * @date: 2022/6/1 16:57
     * @author wubaizhao1
     */
    @Resource
    IUserInfoService userInfoService;
    @Autowired
    JapiHttpDataImporter japiHttpDataImporter;

    /**
     * 校验接口是否有权限
     *
     * @date: 2022/6/19 10:09
     * @author wubaizhao1
     */
    @Resource
    UserPrivilegeHelper userPrivilegeHelper;

    /**
     * @date: 2022/6/21 16:45
     * @author wubaizhao1
     */
    @Resource
    MaskPinHelper maskPinHelper;

    @Autowired
    CjgHelper cjgHelper;

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthDetailService httpAuthDetailService;
    @Autowired
    private InterfaceFollowListService interfaceFollowListService;
    @Autowired
    List<InterfaceChangeListener> listeners;

    @Resource(name = "statisticExecutor")
    ScheduledThreadPoolExecutor statisticExecutor;

    private static final Integer JSF_TYPE = 3;

    private static final Integer HTTP_TYPE = 1;

    /**
     * 新增
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/6/20 18:37
     * @author wubaizhao1
     */
    @Transactional
    @Override
    public Long add(InterfaceManageDTO interfaceManageDTO) {
        //参数校验
        addBeforeCheck(interfaceManageDTO);
        // 查询是否已存在 校验：租户id name type
        addCheckDuplicate(interfaceManageDTO);
        // 校验serviceCode重复
        addCheckServiceCodeDuplicate(interfaceManageDTO);
        //添加到数据表
        InterfaceManage manage = addExecute(interfaceManageDTO);
        //添加权限-默认负责人
        addRight(interfaceManageDTO, manage.getId());
        //拓展处理
        addAfterExtra(interfaceManageDTO, manage.getId());
        for (InterfaceChangeListener listener : listeners) {
            listener.onInterfaceAdd(Collections.singletonList(manage));
        }
        return manage.getId();
    }

    @Override
    public InterfaceManage getOneExcludeBigTextField(Long id) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getId, id);
        excludeBigTextFiled(lqw);
        return getOne(lqw);
    }


    @Override
    public void addListener(InterfaceChangeListener listener) {
        listeners.add(listener);
    }

    @Transactional
    @Override
    public Long importJsfInterface(JsfImportDto jsfImportDto) {
        InterfaceManage manage = new InterfaceManage();
        String deptName = getDeptName(jsfImportDto.getAppId(), jsfImportDto.getAdminCode());
        if (StringUtils.isNotBlank(deptName)) {
            manage.setDeptName(deptName);
        }
        if (jsfImportDto.getId() != null) {
            manage = getById(jsfImportDto.getId());
            manage.init();
            jsfImportDto.setInterfaceId(manage.getName());
            jsfImportDto.setServiceCode(manage.getServiceCode());
            jsfImportDto.setDocInfo(manage.getDesc());
            jsfImportDto.setGroupId(manage.getGroupId());
            jsfImportDto.setArtifactId(manage.getArtifactId());
            jsfImportDto.setVersion(manage.getVersion());
            jsfImportDto.setAppId(manage.getAppId());
        }
        manage.setAppId(jsfImportDto.getAppId());
        manage.setType(InterfaceTypeEnum.JSF.getCode());
        manage.setName(jsfImportDto.getInterfaceId());
        manage.setServiceCode(jsfImportDto.getServiceCode());
        manage.setVisibility(jsfImportDto.getVisibility());
        manage.setDocConfig(jsfImportDto.getDocConfig());
        manage.setLevel(jsfImportDto.getLevel());
        manage.setDesc(jsfImportDto.getDocInfo());
        manage.setUserCode(jsfImportDto.getAdminCode());
        manage.setId(jsfImportDto.getId());
        manage.setPath(jsfImportDto.getGroupId() + ":" + jsfImportDto.getArtifactId() + ":" + jsfImportDto.getVersion());
        manage.setTenantId(UserSessionLocal.getUser().getTenantId());
        if (manage.getId() == null) {
            save(manage);
        } else {
            updateById(manage);
        }
        InterfaceManageDTO dto = new InterfaceManageDTO();
        dto.setAdminCode(jsfImportDto.getAdminCode());
        dto.setType(ResourceTypeEnum.INTERFACE.getCode());
        if (jsfImportDto.getId() == null) {
            addRight(dto, manage.getId());
        }
        if (!jsfImportDto.skipListener()) {
            if (jsfImportDto.getId() == null) {
                for (InterfaceChangeListener listener : listeners) {
                    listener.onInterfaceAdd(Collections.singletonList(manage));
                }
            } else {
                InterfaceManage exist = getById(jsfImportDto.getId());
                for (InterfaceChangeListener listener : listeners) {
                    listener.onInterfaceUpdate(exist, manage);
                }
            }
        }
        methodManageService.updateJsfMethods(jsfImportDto, manage);
        return manage.getId();
    }

    public void updateAppTenantId(String targetDeptName, String tenantId) {
        int pageNo = 1;
        while (true) {
            LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AppInfo::getYn, 1);

            Page<AppInfo> page = appInfoService.page(new Page<>(pageNo, 1000));
            pageNo++;
            if (page.getRecords().isEmpty()) {
                break;
            }

            for (AppInfo app : page.getRecords()) {
                AppInfoDTO appInfoDTO = new AppInfoDTO();
                appInfoDTO.splitMembers(app.getMembers());
                if (appInfoDTO.getOwner() != null && !appInfoDTO.getOwner().isEmpty()) {

                    String deptName = userInfoService.getUserDeptNameByErp(appInfoDTO.getOwner().get(0));
                    if (StringUtils.isNotBlank(deptName) && deptName.startsWith(targetDeptName)) {
                        if (!tenantId.equals(app.getTenantId())) {

                            AppInfo appInfo = new AppInfo();
                            appInfo.setId(app.getId());
                            appInfo.setTenantId(tenantId);
                            appInfo.setYn(1);
                            appInfoService.updateById(appInfo);
                        }


                    }
                }

            }
          /*  if(!updated.isEmpty()){
                updateBatchById(updated);
            }*/
        }
    }

    public void updateInterfaceDept() {
        int pageNo = 1;
        while (true) {
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(InterfaceManage::getYn, 1);

            lqw.select(InterfaceManage::getId, InterfaceManage::getDeptName, InterfaceManage::getAppId, InterfaceManage::getType);
            Page<InterfaceManage> page = page(new Page<>(pageNo, 1000));
            pageNo++;
            if (page.getRecords().isEmpty()) {
                break;
            }
            fixInterfaceAdminInfo(page.getRecords(), ResourceTypeEnum.INTERFACE.getCode());
            List<InterfaceManage> updated = new ArrayList<>();
            for (InterfaceManage manage : page.getRecords()) {
                if (StringUtils.isBlank(manage.getUserCode())) continue;
                String deptName = userInfoService.getUserDeptNameByErp(manage.getUserCode());
                if (StringUtils.isNotBlank(deptName) && !deptName.equals(manage.getDeptName())) {
                    manage.setDeptName(deptName);
                    LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
                    luw.eq(InterfaceManage::getId, manage.getId());
                    luw.set(InterfaceManage::getDeptName, deptName);
                    update(luw);


                }
            }
          /*  if(!updated.isEmpty()){
                updateBatchById(updated);
            }*/
        }
    }

    public void updateInterfaceTenant(String deptNamePrefix, String tenantId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.likeRight(InterfaceManage::getDeptName, deptNamePrefix);
        lqw.eq(InterfaceManage::getYn, 1);
        lqw.select(InterfaceManage::getId, InterfaceManage::getDeptName, InterfaceManage::getAppId, InterfaceManage::getType);
        List<InterfaceManage> list = list(lqw);
        List<Long> appIds = list.stream().map(item -> item.getAppId()).collect(Collectors.toList());
        //List<AppInfo> appManages = appInfoService.listByIds(appIds);

        for (InterfaceManage interfaceManage : list) {
            interfaceManage.setTenantId(tenantId);
            updateById(interfaceManage);
            if (interfaceManage.getAppId() != null) {
                AppInfo app = new AppInfo();
                app.setId(interfaceManage.getAppId());
                app.setTenantId(interfaceManage.getTenantId());
                appInfoService.updateById(app);
            }
        }
    }


    @Override
    public Integer getInterfaceNoticeStatus(Long interfaceId) {
        Guard.notEmpty(interfaceId, "无效的接口id");
        InterfaceManage manage = getById(interfaceId);
        Guard.notEmpty(manage, "无效的接口id");
        InterfaceDocConfig docConfig = manage.getDocConfig();
        if (docConfig == null) {
            return 0;
        }
        return docConfig.getNoticeStatus();
    }

    @Override
    public Integer updateInterfaceNoticeStatus(Long interfaceId, int status) {

        Guard.notEmpty(interfaceId, "无效的接口id");
        InterfaceManage manage = getById(interfaceId);
        Guard.notEmpty(manage, "无效的接口id");
        InterfaceDocConfig docConfig = manage.getDocConfig();
        if (docConfig == null) {
            docConfig = new InterfaceDocConfig();
            manage.setDocConfig(docConfig);
        }
        docConfig.setNoticeStatus(status);
        updateById(manage);
        return status;
    }

    @Override
    public Boolean saveJavaBean(List<FlowBeanInfo> beanInfos, Long appId) {
        Guard.notEmpty(beanInfos, "无效的bean信息");
        Guard.notEmpty(appId, "无效的appId:" + appId);
        AppInfo app = appInfoService.getById(appId);
        Guard.notEmpty(app, "无效的appId:" + appId);
        for (FlowBeanInfo beanInfo : beanInfos) {
            Guard.notEmpty(beanInfo.getFullClassName(), "类名不可为空");
            Guard.notEmpty(beanInfo.getServiceType(), "serviceType不可为空");

        }
        Boolean result = false;
        for (FlowBeanInfo beanInfo : beanInfos) {

            InterfaceManage existBean = getJavaBean(beanInfo.getFullClassName(), appId);
            Map<String, Object> beanConfig = new HashMap<>();
            beanConfig.put("serviceType", beanInfo.getServiceType());
            beanConfig.put("beanName", beanInfo.getBeanName());
            if (existBean == null) {
                existBean = new InterfaceManage();
                existBean.setName(beanInfo.getFullClassName());
                existBean.setServiceCode(beanInfo.getFullClassName());
                existBean.setYn(1);
                existBean.setAppId(appId);
                existBean.setDesc(beanInfo.getDesc());
                existBean.setType(InterfaceTypeEnum.BEAN.getCode());
                existBean.setTenantId(app.getTenantId());
                existBean.setConfig(beanConfig);
                save(existBean);
                for (InterfaceChangeListener listener : listeners) {
                    listener.onInterfaceAdd(Collections.singletonList(existBean));
                }
                result = true;
            } else {
                existBean.setDesc(existBean.getDesc());
                existBean.setConfig(beanConfig);
                updateById(existBean);
                for (InterfaceChangeListener listener : listeners) {
                    listener.onInterfaceUpdate(existBean, existBean);
                }
            }
            List<MethodManage> addOrUpdateMethods = new ArrayList<>();
            for (MethodMetadata method : beanInfo.getMethods()) {
                MethodManage manage = new MethodManage();
                manage.setMethodCode(method.getMethodName());
                manage.setName(method.getMethodName());
                manage.setInterfaceId(existBean.getId());
                manage.setType(InterfaceTypeEnum.BEAN.getCode());
                manage.setParamCount(method.getInput().size());
                manage.setContent(JsonUtils.toJSONString(method));
                addOrUpdateMethods.add(manage);
            }
            methodManageService.mergeMethods(addOrUpdateMethods, existBean.getId(), false);
        }
        return result;
    }


    /**
     * 获取部门名称
     * appId不为空，取应用负责人的部门名称
     * appId为空，取接口负责人的部门名称
     *
     * @param appId
     * @param adminCode
     * @return
     */
    public String getDeptName(Long appId, String adminCode) {
        String deptName = null;
        if (Objects.isNull(appId) && StringUtils.isNotBlank(adminCode)) {
            //应用id为空，未绑定应用，查询接口负责人的部门信息
            deptName = userInfoService.getUserDeptNameByErp(adminCode);
        } else if (Objects.nonNull(appId)) {
            deptName = appInfoService.getDeptNameFoyAppMember(appId);
        }
        return deptName;
    }

    /**
     * 编辑
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/6/20 18:38
     * @author wubaizhao1
     */
    @Override
    @Transactional
    public Long edit(InterfaceManageDTO interfaceManageDTO) {
        //参数校验
        editBeforeCheck(interfaceManageDTO);
        //数据库存在校验
        InterfaceManage interfaceManage = editEmptyCheck(interfaceManageDTO.getId());
        validateAppIdHasChange(interfaceManage, interfaceManageDTO);

        //修改负责人
        editAdmin(interfaceManageDTO, interfaceManage);
//        if (StringUtils.isNotBlank(interfaceManage.getServiceCode())) { // 服务编码不允许更新
//            interfaceManageDTO.setServiceCode(null);
//        }

        if (InterfaceTypeEnum.JSF.getCode().equals(interfaceManageDTO.getType())
                && StringUtils.isNotBlank(interfaceManageDTO.getGroupId())
                && StringUtils.isNotBlank(interfaceManageDTO.getArtifactId())
                && StringUtils.isNotBlank(interfaceManageDTO.getVersion())
        ) {
            interfaceManageDTO.setPath(interfaceManageDTO.getGroupId() + ":" + interfaceManageDTO.getArtifactId() + ":" + interfaceManageDTO.getVersion());
        }
        //修改数据表
        InterfaceManage updated = editExecute(interfaceManageDTO);

        for (InterfaceChangeListener listener : listeners) {
            listener.onInterfaceUpdate(interfaceManage, updated);
        }
        return updated.getId();
    }


    /**
     * 已经开启鉴权的接口不允许修改appId
     */
    private void validateAppIdHasChange(InterfaceManage oldData, InterfaceManageDTO newData) {
        if (oldData.getAppId() != null && newData.getAppId() != null && !oldData.getAppId().equals(newData.getAppId())) {
            if (isAuthInterface(oldData)) {
                throw new BizException("已鉴权接口不允许修改所属应用");
            }
        }
    }

    private boolean isAuthInterface(InterfaceManage manage) {
        if (InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) {
            if (StringUtils.isNotBlank(manage.getCjgAppId())) {
                return true;
            }


        } else if (InterfaceTypeEnum.HTTP.getCode().equals(manage.getType())) {
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.inSql(InterfaceManage::getId, " select auth.interface_id interfaceId from http_auth_detail auth ");
            lqw.eq(InterfaceManage::getId, manage.getId());
            return list(lqw).size() > 0;
        }
        return false;
    }

    /**
     * 是否已经开启了鉴权并且有人申请
     *
     * @param manage
     * @return
     */
    private boolean isEnabledAuth(InterfaceManage manage) {
        if (InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) {
            if (StringUtils.isNotBlank(manage.getCjgAppId())) {
                return true;
            }
        } else if (InterfaceTypeEnum.HTTP.getCode().equals(manage.getType())) {
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.inSql(InterfaceManage::getId, " select auth.interface_id interfaceId from http_auth_apply_detail auth ");
            lqw.eq(InterfaceManage::getId, manage.getId());
            return list(lqw).size() > 0;
        }
        return false;
    }

    /**
     * 删除
     *
     * @param interfaceId
     * @return
     * @date: 2022/6/20 18:38
     * @author wubaizhao1
     */
    @Override
    @Transactional
    public Boolean remove(Long interfaceId) {
        //参数校验
        Guard.notEmpty(interfaceId, "接口id不可为空");
        //检查从属的方法数量
        /*int count = removeCountSubMethod(interfaceManageDTO.getId());
        //存在就不能删除
        if (count > 0) {
            return false;
        }*/
        InterfaceManage interfaceManage = getById(interfaceId);
        if (isEnabledAuth(interfaceManage)) {
            throw new BizException("已开启鉴权接口不允许删除");
        }

        //删除
        InterfaceManage removeEntity = new InterfaceManage();
        removeEntity.setId(interfaceId);
        removeEntity.setYn(DataYnEnum.INVALID.getCode());
        int remove = interfaceManageMapper.updateById(removeEntity);
        for (InterfaceChangeListener listener : listeners) {
            listener.onInterfaceRemove(Collections.singletonList(removeEntity));
        }
        methodManageService.removeByInterfaceId(interfaceId);
        interfaceVersionService.removeInterfaceVersion(interfaceId);
        httpAuthDetailService.removeInterfaceAuthDetail(interfaceId);
        methodModifyLogService.removeByInterfaceId(interfaceId);
        methodVersionModifyLogService.removeByInterfaceId(interfaceId);
        return remove > 0;
    }

    @Override
    public boolean setInterfaceAppId(Long interfaceId, Long appId) {
        Guard.notEmpty(interfaceId, "接口id不可为空");
        Guard.notEmpty(appId, "应用id不可为空");
        boolean hasAuth = userPrivilegeHelper.hasInterfaceRole(interfaceId, UserSessionLocal.getUser().getUserId());
        if (!hasAuth) {
            throw new BizException("无权限");
        }
        LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
        luw.set(InterfaceManage::getAppId, appId);
        luw.eq(InterfaceManage::getId, interfaceId);

        boolean result = update(luw);
        japiHttpDataImporter.initInterfaceProjectMembers(interfaceId);
        return result;

    }

    /**
     * 填充接口的应用信息字段
     *
     * @param interfaces
     */
    public void fillInterfaceAppInfo(List<InterfaceManage> interfaces) {
        List<Long> appIds = interfaces.stream().filter(item -> item.getAppId() != null).map(item -> item.getAppId()).collect(Collectors.toList());
        if (ObjectHelper.isEmpty(appIds)) return;
        List<AppInfo> appInfos = appInfoService.listByIds(appIds);
        Map<Long, List<AppInfo>> id2Apps = appInfos.stream().collect(Collectors.groupingBy(AppInfo::getId));
        for (InterfaceManage anInterface : interfaces) {
            if (anInterface.getAppId() == null || id2Apps.get(anInterface.getAppId()) == null) continue;
            AppInfo app = id2Apps.get(anInterface.getAppId()).get(0);
            anInterface.setAppName(app.getAppName());
            anInterface.setAppCode(app.getAppCode());
        }
    }

    /**
     * 接口分页查询
     * 入参: 必传租户id ,搜索条件有 接口名称(模糊),类型,负责人
     * 出参: Page<InterfaceManage>
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/12 17:48
     * @author wubaizhao1
     */
    @Override
    public Page<InterfaceManage> pageList(InterfacePageQuery interfaceManageDTO) {
        //参数校验
        Guard.notEmpty(interfaceManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(interfaceManageDTO.getTenantId(), "租户id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        if(org.apache.commons.lang.StringUtils.isEmpty(interfaceManageDTO.getErp())){
            interfaceManageDTO.setErp(UserSessionLocal.getUser().getUserId());
        }
        //是否为租户管理员
        Boolean tenantAdmin = memberRelationService.checkTenantAdmin(interfaceManageDTO.getErp());
        long total = 0L;
        List<InterfaceManage> records = null;
        InterfaceQueryDto dto = new InterfaceQueryDto();
        dto.setOffset((interfaceManageDTO.getCurrent() - 1) * interfaceManageDTO.getSize());
        dto.setLimit(interfaceManageDTO.getSize());
        dto.setAdminCode(interfaceManageDTO.getAdminCode());
        dto.setCurrentUser(interfaceManageDTO.getErp());
        dto.setTenantId(interfaceManageDTO.getTenantId() + "");
        dto.setAppId(interfaceManageDTO.getAppId());
        dto.setLevel(interfaceManageDTO.getLevel());

        Integer[] types = null;
        if (StringHelper.isNotBlank(interfaceManageDTO.getType())) {
            List<String> strs = StringHelper.split(interfaceManageDTO.getType(), ",");
            types = new Integer[strs.size()];
            for (int i = 0; i < strs.size(); i++) {
                types[i] = Integer.valueOf(strs.get(i));
                if (getInterfaceTypeList().contains(types[i])) {
                    dto.setResourceType(ResourceTypeEnum.INTERFACE.getCode());
                } else {
                    dto.setResourceType(ResourceTypeEnum.ORCHESTRATION.getCode());
                    dto.setPublicInterface(true);
                }
            }
        } else {
            types = getInterfaceTypeList().toArray(new Integer[0]);
            dto.setResourceType(ResourceTypeEnum.INTERFACE.getCode());
        }
        dto.setTypes(types);

        dto.setName(interfaceManageDTO.getName());
        dto.setNodeType(interfaceManageDTO.getNodeType());
        dto.setAutoReport(interfaceManageDTO.getAutoReport());
        if (!tenantAdmin) {
            total = interfaceManageMapper.queryListCount(dto);
            records = interfaceManageMapper.queryList(dto);
        } else {
            total = interfaceManageMapper.selectAdminListCount(dto);
            records = interfaceManageMapper.selectAdminList(dto);
        }
        //分页
        Page<InterfaceManage> page = new Page<>(interfaceManageDTO.getCurrent(), interfaceManageDTO.getSize());
        page.setRecords(records);
        page.setTotal(total);
        if (page.getRecords().isEmpty()) {
            return page;
        }
        fixInterfaceAdminInfo(page.getRecords(), dto.getResourceType());
        fillInterfaceAppInfo(page.getRecords());
        //接口的权限人以及默认处理
        page.getRecords().forEach(x -> {
            pageEachHandle(x, dto.getResourceType());
        });
        return page;
    }

    @Override
    public boolean hasNoAppInterface() {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.isNotNull(InterfaceManage::getAppId);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.inSql(InterfaceManage::getId, "select resource_id from member_relation where yn = 1 and resource_type = 1 and user_code = '" + UserSessionLocal.getUser().getUserId() + "'");
        lqw.last("LIMIT 1");
        return getOne(lqw) != null;
    }

    @Override
    public List<InterfaceManage> getNoAppInterfaces(Integer interfaceType) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.isNull(InterfaceManage::getAppId);
        lqw.eq(interfaceType != null, InterfaceManage::getType, interfaceType);
        excludeBigTextFiled(lqw);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.inSql(InterfaceManage::getId, "select resource_id from member_relation where yn = 1 and resource_type = 1 and user_code = '" + UserSessionLocal.getUser().getUserId() + "'");
        //lqw.last("LIMIT 1");
        return list(lqw);
    }

    public void fixHasLicense(List<InterfaceManage> interfaces) {
        if (interfaces.isEmpty()) return;
        List<InterfaceManage> httpInterfaceIds = interfaces.stream().filter(item -> {
            return InterfaceTypeEnum.HTTP.getCode().equals(item.getType());
        }).collect(Collectors.toList());
        Set<Long> authIds = httpAuthDetailService.queryExists(httpInterfaceIds.stream().map(item -> item.getId()).collect(Collectors.toList()));
        for (InterfaceManage anInterface : interfaces) {
            if (InterfaceTypeEnum.HTTP.getCode().equals(anInterface.getType())) {
                anInterface.setHasLicense(authIds.contains(anInterface.getId()));
            } else if (InterfaceTypeEnum.JSF.getCode().equals(anInterface.getType())) {
                anInterface.setHasLicense(StringUtils.isNotBlank(anInterface.getCjgAppId()));
            }
        }
    }

    @Override
    public Page<InterfaceManage> pageMarketInterface(InterfaceAuthFilter filter) {
        if (filter.getOnlySelf() == null) {
            filter.setOnlySelf(0);
        }
        if (filter.getType() == null) {
            filter.setType(InterfaceTypeEnum.JSF.getCode());
        }
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(InterfaceManage::getScore);
        UserVo erpUser = getUserErp(filter.getName());
        if (erpUser != null) {

            filter.setAdminCode(filter.getName());
            filter.setName(null);
        }
        if (StringUtils.isNotBlank(filter.getName())) {
            if (SafeUtil.sqlValidate(filter.getName())) {
                log.info("sql注入安全检查失败");
            }
            lqw.and(childWrapper -> {
                childWrapper.or().like(StringUtils.isNotBlank(filter.getName()), InterfaceManage::getName, filter.getName())
                        .or().like(StringUtils.isNotBlank(filter.getName()), InterfaceManage::getServiceCode, filter.getName())
                        .or().like(StringUtils.isNotBlank(filter.getName()), InterfaceManage::getDocInfo, filter.getName())
                        .or().apply("app_id in (select id from app_info where app_code like concat('%',{0},'%') or app_name like concat('%',{1},'%') ) ", filter.getName(), filter.getName());
                ;
            });

        }

        lqw.eq(InterfaceManage::getType, filter.getType());
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.isNull(filter.getNullDept() != null && filter.getNullDept(), InterfaceManage::getDeptName);
        lqw.eq(InterfaceManage::getTenantId, UserSessionLocal.getUser().getTenantId());

        if (StringUtils.isNotBlank(filter.getDeptName())) {
            lqw.likeRight(InterfaceManage::getDeptName, filter.getDeptName());
        }
        if (filter.getScoreMin() != null) {
            lqw.ge(InterfaceManage::getScore, filter.getScoreMin());
        }
        if (filter.getScoreMax() != null) {
            lqw.le(InterfaceManage::getScore, filter.getScoreMax());
        }
        lqw.eq(InterfaceManage::getVisibility, 0);
        lqw.like(StringUtils.isNotBlank(filter.getCjgBusinessDomainTrace()), InterfaceManage::getCjgBusinessDomainTrace, "," + filter.getCjgBusinessDomainTrace());
        lqw.like(StringUtils.isNotBlank(filter.getCjgProductTrace()), InterfaceManage::getCjgProductTrace, "," + filter.getCjgProductTrace());
        lqw.select(InterfaceManage.class, x -> {
            String[] bigTextFields = new String[]{"sort_group_tree", "content"};
            return Arrays.asList(bigTextFields).indexOf(x.getColumn()) == -1;
        });
        if (CollectionUtils.isNotEmpty(filter.getAppIds())) {
            lqw.in(InterfaceManage::getAppId, filter.getAppIds());
        }
        lqw.eq(filter.getAppId() != null, InterfaceManage::getAppId, filter.getAppId());
        if (Objects.nonNull(filter.getLevel())) {
            lqw.eq(InterfaceManage::getLevel, filter.getLevel());
        }
       /* if (filter.getAppId() != null) {
            lqw.eq(InterfaceManage::getAppId, filter.getAppId());
        }*/
        if (filter.getIsFollow() != null) {
            String sql = "select interface_id from interface_follow_list where erp='" + UserSessionLocal.getUser().getUserId() + "'";
            if (filter.getIsFollow() == 1) { // 已关注
                lqw.inSql(InterfaceManage::getId, sql);

            } else {// 未关注
                //lqw.notInSql(InterfaceManage::getId, sql);
            }
        }
        if (filter.getHasLicense() != null) {
            if (InterfaceTypeEnum.JSF.getCode().equals(filter.getType())) {
                if (filter.getHasLicense()) {
                    lqw.isNotNull(InterfaceManage::getCjgAppId);
                } else {
                    //lqw.isNull(InterfaceManage::getCjgAppId);
                }
            } else if (InterfaceTypeEnum.HTTP.getCode().equals(filter.getType())) {
                if (filter.getHasLicense()) {
                    lqw.and(child -> {
                        child.or(wrapper -> {
                            wrapper.inSql(InterfaceManage::getId, " select auth.interface_id interfaceId from http_auth_detail auth ");
                        });
                    });
                }
            }
        }
        if (!StringUtils.isBlank(filter.getAdminCode())) {
            lqw.and(child -> {
                child.or(wrapper -> {
                    wrapper.inSql(InterfaceManage::getId, " select resource_id from member_relation relation where relation.resource_type=1 and relation.resource_role=2 and relation.user_code='" + filter.getAdminCode() + "'");
                }).or(wrapper -> {
                    interfaceManageInAppSql(wrapper, filter.getAdminCode());
                });
            });
        }
        if (filter.getOnlySelf() == 1) {
            lqw.and(child -> {
                child.or(wrapper -> {
                    wrapper.inSql(InterfaceManage::getId, " select resource_id from member_relation relation where relation.resource_type=1   and relation.user_code='" + UserSessionLocal.getUser().getUserId() + "'");
                }).or(wrapper -> {
                    interfaceManageInAppSql(wrapper, filter.getAdminCode());
                });
            });
        }
//
//        if (CollectionUtils.isNotEmpty(filter.getAppAlias())) {
//            lqw.and(child -> {
//                child.or(wrapper -> {
//                    wrapper.inSql(InterfaceManage::getAppId, " select id from app_info app where app.yn=1   and app.jdos_app_code in (" + String.join(",",filter.getAppAlias())+ ")");
//                });
//            });
//        }

        Page<InterfaceManage> page = page(new Page<>(filter.getCurrent(), filter.getSize()), lqw);
        if (StringUtils.isNotBlank(filter.getAdminCode())) {
            for (InterfaceManage record : page.getRecords()) {
                record.setUserCode(filter.getAdminCode());
                if (erpUser != null) {
                    record.setUserName(erpUser.getUserName());
                }
            }
        } else {
            fixInterfaceAdminInfo(page.getRecords(), ResourceTypeEnum.INTERFACE.getCode());
        }

        fillInterfaceAppInfo(page.getRecords());
        updateFollowStatus(page.getRecords());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            fixHasLicense(page.getRecords());

        }
        return page;
    }


    @Override
    public Map<Long, List<String>> getAppInterfaceTypes(List<AppInfoDTO> appInfos) {
        List<Long> appIds = appInfos.stream().map(AppInfoDTO::getId).collect(Collectors.toList());
//        Map<Long, String> id2JdosCode = appInfos.stream().collect(Collectors.toMap(AppInfoDTO::getId, AppInfoDTO::getJdosAppCode));

        //查询接口信息
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.isNotNull(InterfaceManage::getAppId);
        lqw.in(InterfaceManage::getAppId, appIds);
        Integer[] typeCodes = {InterfaceTypeEnum.HTTP.getCode(), InterfaceTypeEnum.JSF.getCode()};
        lqw.in(InterfaceManage::getType, typeCodes);

//        Page<InterfaceManage> page = page(new Page<>(current, size), lqw);
        List<InterfaceManage> records = list(lqw);
        Map<Integer, List<InterfaceManage>> pp = records.stream().collect(Collectors.groupingBy(InterfaceManage::getType));

        List<InterfaceManage> https = pp.get(InterfaceTypeEnum.HTTP.getCode());
        List<InterfaceManage> jsfs = pp.get(InterfaceTypeEnum.JSF.getCode());
        Map<Long, List<String>> ss = new HashMap<Long, List<String>>();
        if (CollectionUtils.isNotEmpty(https)) {
            Set<Long> haveHttpAppIds = https.stream().map(InterfaceManage::getAppId).collect(Collectors.toSet());
            for (Long haveHttpAppId : haveHttpAppIds) {
//                String appCode = StringUtils.isEmpty(id2JdosCode.get(haveHttpAppId)) ? "" : id2JdosCode.get(haveHttpAppId);
                List<String> types = new ArrayList<>();
                types.add("http");
                ss.put(haveHttpAppId, types);
//                if(StringUtils.isNotBlank(appCode)){
//                    ss.put(haveHttpAppId, types);
//                }
            }

        }
        if (CollectionUtils.isNotEmpty(jsfs)) {
            Set<Long> haveJsfAppIds = jsfs.stream().map(InterfaceManage::getAppId).collect(Collectors.toSet());
            for (Long haveJsfAppId : haveJsfAppIds) {
//                String appCode = StringUtils.isEmpty(id2JdosCode.get(haveJsfAppId)) ? "" : id2JdosCode.get(haveJsfAppId);
                if (null == ss.get(haveJsfAppId)) {
                    List<String> types = new ArrayList<>();
                    types.add("jsf");
                    ss.put(haveJsfAppId, types);
//                    if(StringUtils.isNotBlank(appCode)) {
//                        ss.put(appCode, types);
//                    }
                } else {
                    ss.get(haveJsfAppId).add("jsf");
                }
            }
        }
        return ss;

    }

    @Override
    public InterfaceOrMethodResult searchInterfaceOrMethod(String search, int current, int size) {
        List<Future> futures = new ArrayList<>();
        boolean isErp = isErp(search);
        InterfaceOrMethodResult result = new InterfaceOrMethodResult();
        {
            Future future = statisticExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    Integer type = InterfaceTypeEnum.HTTP.getCode();
                    InterfaceOrMethodResult.SearchResult httpSearchResult = new InterfaceOrMethodResult.SearchResult();
                    Page<InterfaceManage> interfaces = searchInterface(type, search, current, size, isErp);
                    Page<MethodSearchResult> methods = searchMethod(type, search, current, size, isErp);
                    httpSearchResult.setType(type);
                    httpSearchResult.setTotal(interfaces.getTotal() + methods.getTotal());
                    httpSearchResult.setInterfaceData(interfaces);
                    httpSearchResult.setMethodData(methods);
                    result.getResult().add(httpSearchResult);
                }
            });
            futures.add(future);
        }
        {
            Future future = statisticExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    Integer type = InterfaceTypeEnum.JSF.getCode();
                    InterfaceOrMethodResult.SearchResult httpSearchResult = new InterfaceOrMethodResult.SearchResult();
                    Page<InterfaceManage> interfaces = searchInterface(type, search, current, size, isErp);
                    Page<MethodSearchResult> methods = searchMethod(type, search, current, size, isErp);
                    httpSearchResult.setType(type);
                    httpSearchResult.setTotal(interfaces.getTotal() + methods.getTotal());
                    httpSearchResult.setInterfaceData(interfaces);
                    httpSearchResult.setMethodData(methods);
                    result.getResult().add(httpSearchResult);
                }
            });
            futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new BizException("搜索失败", e);
            }
        }


        return result;
    }

    public Page<MethodSearchResult> searchMethod(int type, String search, int current, int size, boolean isErp) {

        LambdaQueryWrapper<MethodManage> lqw = methodSearchCondition(search, isErp);
        lqw.eq(MethodManage::getType, type);


        Page<MethodManage> page = methodManageService.page(new Page<>(current, size), lqw);
        Page<MethodSearchResult> result = new Page<>(page.getCurrent(), page.getSize());
        List<MethodSearchResult> records = page.getRecords().stream().map(vs -> {
            MethodSearchResult searchResult = new MethodSearchResult();
            BeanUtils.copyProperties(vs, searchResult);
            return searchResult;
        }).collect(Collectors.toList());
        result.setRecords(records);
        result.setTotal(page.getTotal());
        fillInterfaceAndAppInfo(result.getRecords());

        return result;
    }

    @Override
    public Page<MethodSearchResult> searchMethod(int type, String search, int current, int size) {
        boolean isErp = isErp(search);
        return searchMethod(type, search, current, size, isErp);
    }

    private void fillInterfaceAndAppInfo(List<MethodSearchResult> results) {
        List<Long> interfaceIds = results.stream().filter(vs -> vs.getInterfaceId() != null).map(vs -> vs.getInterfaceId()).collect(Collectors.toList());
        List<InterfaceManage> interfaces = list(interfaceIds);
        fillInterfaceAppInfo(interfaces);
        fixInterfaceAdminInfo(interfaces, ResourceTypeEnum.INTERFACE.getCode());
        Map<Long, List<InterfaceManage>> id2Interfaces = interfaces.stream().collect(Collectors.groupingBy(InterfaceManage::getId));

        for (MethodSearchResult result : results) {
            if (result.getInterfaceId() == null) continue;
            List<InterfaceManage> its = id2Interfaces.get(result.getInterfaceId());
            if (its == null) continue;
            InterfaceManage item = its.get(0);
            result.setInterfaceCode(item.getServiceCode());
            result.setInterfaceName(item.getName());
            result.setAppCode(item.getAppCode());
            result.setAppName(item.getAppName());
            result.setDeptName(item.getDeptName());
            result.setAdminCode(item.getUserCode());
            result.setAdminName(item.getUserName());
        }
    }

    @Override
    public Page<InterfaceManage> searchInterface(int type, String search, int current, int size) {
        boolean isErp = isErp(search);
        return searchInterface(type, search, current, size, isErp);
    }

    public Page<InterfaceManage> searchInterface(int type, String search, int current, int size, boolean isErp) {
        LambdaQueryWrapper<InterfaceManage> lqw = interfaceSearchCondition(search, isErp);
        lqw.eq(InterfaceManage::getType, type);
        Page<InterfaceManage> page = page(new Page<>(current, size), lqw);
        fixInterfaceAdminInfo(page.getRecords(), ResourceTypeEnum.INTERFACE.getCode());
        fillInterfaceAppInfo(page.getRecords());
        for (InterfaceManage record : page.getRecords()) {
            record.init();
        }
        return page;
    }

    private LambdaQueryWrapper<MethodManage> methodSearchCondition(String search, boolean isErp) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getYn, ValidEnum.VALID.getCode());
        try {
            if (SafeUtil.sqlValidate(search)) {
                throw new Exception("您发送请求中的参数中含有非法字符");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isErp) {
            String erpSql = "interface_id in (select o.id from interface_manage o where o.yn=1 and o.app_id in\n" +
                    " (select app_id from app_info_members where erp = {0} ) \n" +
                    " or id in ( select resource_id from member_relation relation where relation.resource_type=1 and relation.resource_role=2 and relation.user_code= {1} ) )";
            lqw.apply(erpSql, search, search);

        } else {
            lqw.and(vs -> {
                String sql = "interface_id in (select o.id from interface_manage o left join app_info app on o.app_id = app.id where o.service_code = {0} or o.name={1} or app.app_code={2} or app.app_name={3})";
                vs.like(MethodManage::getName, search)
                        .or().like(MethodManage::getMethodCode, search)
                        .or().like(MethodManage::getPath, search)
                        .or().like(MethodManage::getDesc, search)
                        .or().like(MethodManage::getDocInfo, search)
                        .or().apply(sql, search, search, search, search)
                ;

            });
        }

        lqw.select(MethodManage.class, x -> {
            String[] bigTextFields = new String[]{"content"};
            return Arrays.asList(bigTextFields).indexOf(x.getColumn()) == -1;
        });
        return lqw;
    }

    private UserVo getUserErp(String erp) {
        if (StringUtils.isBlank(erp)) return null;
        boolean matches = erp.matches(ERP_PATTERN);
        if (!matches) return null;
        UserVo userBaseInfoByUserName = userHelper.getUserBaseInfoByUserName(erp);
        if (userBaseInfoByUserName == null || userBaseInfoByUserName.getUserCode() == null) return null;
        return userBaseInfoByUserName;
    }

    //erp的属性： 字母数字、字符串
    private boolean isErp(String erp) {
        return getUserErp(erp) != null;
    }

    private void adminCondition(LambdaQueryWrapper<InterfaceManage> lqw, String adminCode) {
        if (SafeUtil.sqlValidate(adminCode)) {
            log.info("sql注入安全检查失败");
        }
        lqw.and(child -> {
            child.or(wrapper -> {
                wrapper.inSql(InterfaceManage::getId, " select resource_id from member_relation relation where relation.resource_type=1 and relation.resource_role=2 and relation.user_code='" + adminCode + "'");
            }).or(wrapper -> {
                wrapper.apply("app_id in ( select id  from app_info_members where erp = {0} and role_type =1 )", adminCode);
            });
        });
    }

    private void interfaceManageInAppSql(LambdaQueryWrapper<InterfaceManage> lqw, String erp) {
        if (SafeUtil.sqlValidate(erp)) {
            log.info("sql注入安全检查失败");
        }
        lqw.apply("app_id in (select app_id from app_info_members where erp={0})", erp);
    }

    private LambdaQueryWrapper<InterfaceManage> interfaceSearchCondition(String search, boolean isErp) {

        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getVisibility, 1);
        if (isErp) {
            adminCondition(lqw, search);
        } else {
            if (SafeUtil.sqlValidate(search)) {
                log.info("sql注入安全检查失败");
            }
            String sql = " app_id in ( select id from app_info where  app_code = {0} or app_name = {1} )";
            lqw.eq(InterfaceManage::getYn, ValidEnum.VALID.getCode()).and(vs -> {
                vs.like(InterfaceManage::getName, search)
                        .or().like(InterfaceManage::getServiceCode, search)
                        .or().like(InterfaceManage::getDesc, search)
                        .or().like(InterfaceManage::getDocInfo, search)
                        .or().like(InterfaceManage::getDeptName, search)
                        .or().apply(sql, search, search);

            });
        }

        lqw.select(InterfaceManage.class, x -> {
            String[] bigTextFields = new String[]{"sort_group_tree"};
            return Arrays.asList(bigTextFields).indexOf(x.getColumn()) == -1;
        });
        return lqw;
    }


    @Override
    public boolean updateInterfaceDomain(UpdateBusinessDomainDto dto) {
        List<Long> interfaceIds = dto.getInterfaceIds();
        String cjgBusinessDomainTrace = dto.getCjgBusinessDomainTrace();
        if (ObjectHelper.isEmpty(interfaceIds)) return false;
        for (Long interfaceId : interfaceIds) {
            boolean hasAuth = userPrivilegeHelper.hasInterfaceRole(interfaceId, UserSessionLocal.getUser().getUserId());
            if (!hasAuth) {
                throw new BizException("该接口无操作权限：" + interfaceId);
            }
        }
        for (Long interfaceId : interfaceIds) {
            LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
            luw.set(InterfaceManage::getCjgBusinessDomainTrace, cjgBusinessDomainTrace);
            luw.eq(InterfaceManage::getId, interfaceId);
            update(luw);
        }
        return true;
    }
	/*@Override
	public Page<InterfaceManage> pageMarketInterface(InterfaceAuthFilter filter) {
		InterfaceQueryDto dto = new InterfaceQueryDto();
		dto.setOffset((filter.getCurrent()-1)*filter.getSize());
		dto.setLimit(filter.getSize());
		dto.setName(filter.getName());
		dto.setAppId(filter.getAppId());
		dto.setAuthInterface(true);
		dto.setTenantId(UserSessionLocal.getUser().getTenantId());
		dto.setAdminCode(filter.getAdminCode());
		dto.setResourceType(ResourceTypeEnum.INTERFACE.getCode());

		if(filter.isHasLicense()){
			dto.setAuthInterface(true);
		}

		if(filter.getType() == null){
			dto.setTypes(new Integer[]{InterfaceTypeEnum.JSF.getCode()});
			//dto.setTypes(getInterfaceTypeList().toArray(new Integer[0]));
		}else{
			dto.setTypes(new Integer[]{filter.getType()});
		}

		dto.setCurrentUser(UserSessionLocal.getUser().getUserId());
		if(filter.getOnlySelf() == null){
			filter.setOnlySelf(0);
		}
		if(filter.getOnlySelf() == 0){
			dto.setPublicInterface(true);
		}


		List<InterfaceManage> interfaceManages = interfaceManageMapper.queryList(dto);
		Long total = interfaceManageMapper.queryListCount(dto);
		Page page = new Page(filter.getCurrent(),filter.getSize());
		page.setTotal(total);
		if(CollectionUtils.isNotEmpty(interfaceManages)){
			interfaceManages.stream().forEach(obj->{
				obj.setHasLicense(StringUtils.isNotBlank(obj.getCjgAppId()));
				QueryHttpAuthDetailReqDTO authDetailReqDTO = new QueryHttpAuthDetailReqDTO();
				authDetailReqDTO.setInterfaceId(obj.getId());
				Long count = httpAuthDetailService.queryListCount(authDetailReqDTO);
				if (NumberUtils.toLong(count)>0){
					obj.setNeedApply(true);
				}
			});
		}
		fixInterfaceAdminInfo(interfaceManages,ResourceTypeEnum.INTERFACE.getCode());
		if(CollectionUtils.isNotEmpty(interfaceManages)){
			interfaceManages.stream().forEach(obj->{
				obj.setHasLicense(StringUtils.isNotBlank(obj.getCjgAppId()));
			});
		}
		page.setRecords(interfaceManages);

		return page;
	}*/

    private boolean checkPass(Long interfaceId, String userCode) {
        return userPrivilegeHelper.hasInterfaceRole(interfaceId, userCode);
    }

    public InterfaceManage getJavaBean(String name, Long appId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getName, name);
        lqw.eq(InterfaceManage::getAppId, appId);
        lqw.eq(InterfaceManage::getYn, 1);
        lqw.eq(InterfaceManage::getType, InterfaceTypeEnum.BEAN.getCode());
        List<InterfaceManage> list = list(lqw);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<Long> selectIsPublic(InterfaceManageDTO interfaceManageDTO) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.select(InterfaceManage.class, x -> !x.getColumn().equals("env"));
        lqw.select(InterfaceManage.class, x -> !x.getColumn().equals("desc"));
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(interfaceManageDTO.getTenantId() != null, InterfaceManage::getTenantId, interfaceManageDTO.getTenantId());
        lqw.eq(InterfaceManage::getIsPublic, 1);
        //类型筛选
        if (EmptyUtil.isNotEmpty(interfaceManageDTO.getType())) {
            lqw.eq(EmptyUtil.isNotEmpty(interfaceManageDTO.getType()), InterfaceManage::getType, interfaceManageDTO.getType());
        } else {
            lqw.in(InterfaceManage::getType, getInterfaceTypeList());
        }
        if (interfaceManageDTO.getNodeType() != null) {
            lqw.eq(InterfaceManage::getNodeType, interfaceManageDTO.getNodeType());
        }
        List<InterfaceManage> interfaceManages = interfaceManageMapper.selectList(lqw);
        List<Long> publicIds = interfaceManages.stream().map(x -> x.getId()).collect(Collectors.toList());
        return publicIds;
    }

    /**
     * @date: 2022/5/30 17:29
     * @author wubaizhao1
     * @param interfaceManageDTO
     * @return
     */
	/*@Override
	public Page<InterfaceManage> pageListByUser(InterfaceManageDTO interfaceManageDTO) {
		checkPageListByUser(interfaceManageDTO);
		*//**
     * 如果用户是租户管理员,则不需要过滤ids
     *//*
		Boolean isTenantAdmin = memberRelationService.checkTenantAdmin(interfaceManageDTO.getUserCode());
		boolean checkIdsFlag = false;
		Set<Long> resourceIdSetFilter = new HashSet<>();
		Set<Long> ResourceIdSet = new HashSet<>();
		if (!isTenantAdmin && EmptyUtil.isNotEmpty(interfaceManageDTO.getUserCode())) {
			MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
			ResourceTypeEnum resourceType = getResourceType(interfaceManageDTO.getType());
			memberRelationDTO.setResourceType(resourceType==null?null:resourceType.getCode());
			memberRelationDTO.setResourceRoleList(Arrays.asList(ResourceRoleEnum.ADMIN.getCode(),ResourceRoleEnum.MEMBER.getCode()));
			memberRelationDTO.setUserCode(interfaceManageDTO.getUserCode());
			log.info("InterfaceManageServiceImpl pageList getUserCode is not empty memberRelationDTO={}", JsonUtils.toJSONString(memberRelationDTO));
			List<Long> resourceIds = memberRelationService.listResourceIds(memberRelationDTO);
			resourceIdSetFilter.addAll(resourceIds);
			checkIdsFlag = true;
		}

		if (!isTenantAdmin && EmptyUtil.isNotEmpty(interfaceManageDTO.getAdminCode())) {
			MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
			ResourceTypeEnum resourceType = getResourceType(interfaceManageDTO.getType());
			memberRelationDTO.setResourceType(resourceType==null?null:resourceType.getCode());
			memberRelationDTO.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
			memberRelationDTO.setUserCode(interfaceManageDTO.getAdminCode());
			log.info("InterfaceManageServiceImpl pageList admindId is not empty memberRelationDTO={}", JsonUtils.toJSONString(memberRelationDTO));
			List<Long> resourceIds = memberRelationService.listResourceIds(memberRelationDTO);
			ResourceIdSet=resourceIds.stream().filter(resourceIdSetFilter::contains).collect(Collectors.toSet());
			checkIdsFlag = true;
		}
		List<Long> resourceIds = new ArrayList<>(ResourceIdSet);

		//查询条件
		LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper();
		lqw.eq(InterfaceManage::getYn,DataYnEnum.VALID.getCode());
		lqw.eq(interfaceManageDTO.getTenantId()!=null,InterfaceManage::getTenantId,interfaceManageDTO.getTenantId());
		lqw.eq(EmptyUtil.isNotEmpty(interfaceManageDTO.getType()), InterfaceManage::getType,interfaceManageDTO.getType());
		lqw.like(EmptyUtil.isNotEmpty(interfaceManageDTO.getName()), InterfaceManage::getName,interfaceManageDTO.getName());
		lqw.orderByDesc(InterfaceManage::getCreated);
		if(checkIdsFlag){
			if(EmptyUtil.isEmpty(resourceIds)){
				return new Page<>(interfaceManageDTO.getCurrent(), interfaceManageDTO.getSize());
			}
			lqw.in(InterfaceManage::getId,resourceIds);
		}
		//分页
		Page<InterfaceManage> interfaceManagePage = null;
		if(interfaceManageDTO.getCurrent()==null || interfaceManageDTO.getSize()==null){
			interfaceManagePage = new Page<>(1, 100);
		}else {
			interfaceManagePage = new Page<>(interfaceManageDTO.getCurrent(), interfaceManageDTO.getSize());
		}
		Page<InterfaceManage> page = interfaceManageMapper.selectPage(interfaceManagePage, lqw);
		if(page.getRecords().isEmpty()){
			return page;
		}
		page.getRecords().forEach(x -> {
			pageEachHandle(x,interfaceManageDTO.getType());
		});

		return page;
	}
	private void checkPageListByUser(InterfaceManageDTO interfaceManageDTO){
		Guard.notEmpty(interfaceManageDTO,ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
		Guard.notEmpty(interfaceManageDTO.getTenantId(),"租户id不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
		Guard.notEmpty(interfaceManageDTO.getUserCode(),"用户Code不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
		log.info("InterfaceManageServiceImpl pageListByUser query={}", JsonUtils.toJSONString(interfaceManageDTO));
	}*/

    /**
     * @param id
     * @return
     * @date: 2022/5/24 17:34
     * @author wubaizhao1
     */
    @Override
    public InterfaceManage getOneById(Long id) {
        Guard.notEmpty(id, "接口id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(id);
        Guard.notEmpty(interfaceManage, "数据为空！");
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceId(id);
        memberRelationDTO.setResourceType(getResourceType(interfaceManage.getType()).getCode());
        UserInfo admin = memberRelationService.getAdminWithUser(memberRelationDTO);
        if (EmptyUtil.isNotEmpty(admin)) {
            interfaceManage.setUserCode(maskPinHelper.maskUserCode(admin.getUserCode()));
            interfaceManage.setUserName(admin.getUserName());
        }
        //对大字段的校验
        String env = interfaceManage.getEnv();
        List<EnvModel> envModelList = null;
        if (EmptyUtil.isNotEmpty(env)) {
            try {
                envModelList = JsonUtils.parseArray(env, EnvModel.class);
                interfaceManage.setEnvList(envModelList);
                //interfaceManage.setEnv(env);
                //忽略
            } catch (Exception e) {
            }
        }
        Boolean hasInterfaceRole = userPrivilegeHelper.hasInterfaceRole(id, UserSessionLocal.getUser().getUserId());
        Boolean hasAppRole = userPrivilegeHelper.hasAppRole(interfaceManage.getAppId(), UserSessionLocal.getUser().getUserId());
        interfaceManage.setEditable(hasInterfaceRole || hasAppRole);
        //判断用户是否有操作权限（对demo）
        interfaceManage.setHasAuth(0);
        fillInterfaceAppInfo(Collections.singletonList(interfaceManage));
        updateFollowStatus(Collections.singletonList(interfaceManage));
        if (checkPass(id, UserSessionLocal.getUser().getUserId())) {
            interfaceManage.setHasAuth(1);
        }
        initDocConfig(interfaceManage);

        if (interfaceManage.getCjgBusinessDomainTrace() != null) {
            KgBusinessDomainVo domain = cjgHelper.getDomainByTrace(interfaceManage.getCjgBusinessDomainTrace());
            if (domain != null) {
                String traceName = domain.getTraceName();
                if (traceName == null) {
                    traceName = domain.getName();
                }
                interfaceManage.setCjgBusinessDomainTraceName(StringHelper.replace(traceName, "^", "-"));
            }

        }

        return interfaceManage;
    }

    private String getPomConfig(InterfaceManage interfaceManage) {
        interfaceManage.init();
        String pomConfigTemplate = "<dependency>\n" +
                "            <groupId>%s</groupId>\n" +
                "            <artifactId>%s</artifactId>\n" +
                "            <version>%s</version>\n" +
                "        </dependency>";
        if (interfaceManage.getPath() == null) return null;
        return String.format(pomConfigTemplate, interfaceManage.getGroupId(), interfaceManage.getArtifactId(), interfaceManage.getVersion());
    }

    private String getInvokeConfig(InterfaceManage interfaceManage) {
        interfaceManage.init();
        String invokeConfigTemplate = "<jsf:consumer id=\"%s\" interface=\"%s\"\n" +
                "                  protocol=\"jsf\" alias=\"${jsf.alias}\" timeout=\"10000\"/>";
        if (StringUtils.isBlank(interfaceManage.getServiceCode())) return null;
        String className = interfaceManage.getServiceCode();
        String simpleClassName = StringHelper.lastPart(className, '.');
        return String.format(invokeConfigTemplate, StringHelper.capitalize(simpleClassName), className);
    }

    private void initDocConfig(InterfaceManage manage) {
        InterfaceDocConfig docConfig = manage.getDocConfig();
        if (InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) {
            if (docConfig == null) {
                JsfDocConfig jsfDocConfig = new JsfDocConfig();
                manage.setDocConfig(jsfDocConfig);
                if (StringUtils.isBlank(jsfDocConfig.getPomConfig())) {
                    jsfDocConfig.setPomConfig(getPomConfig(manage));
                }
                if (StringUtils.isBlank(jsfDocConfig.getInvokeConfig())) {
                    jsfDocConfig.setInvokeConfig(getInvokeConfig(manage));
                }

            }
        }
        if (docConfig == null) {
            docConfig = new InterfaceDocConfig();
        }
    }

    /**
     * 接口成员 添加该类型下的成员
     *
     * @date: 2022/5/16 11:01
     * @author wubaizhao1
     */
    @Override
    public Boolean addMember(InterfaceManageDTO interfaceManageDTO) {
        Guard.notEmpty(interfaceManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(interfaceManageDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getTenantId(), "租户id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getUserCode(), "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(interfaceManageDTO.getId());
        if (interfaceManage == null || !Objects.equals(interfaceManage.getTenantId(), interfaceManageDTO.getTenantId())) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        ResourceTypeEnum resourceType = getResourceType(interfaceManage.getType());
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(interfaceManageDTO.getId());
        memberRelationDTO.setUserCode(interfaceManageDTO.getUserCode());
        memberRelationDTO.setResourceRole(ResourceRoleEnum.MEMBER.getCode());
        Boolean binding = memberRelationService.binding(memberRelationDTO);
        return binding;
    }

    /**
     * 列出成员
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/16 14:55
     * @author wubaizhao1
     */
    @Override
    public Page<MemberRelationWithUser> listMember(InterfaceManageDTO interfaceManageDTO) {
        Guard.notEmpty(interfaceManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(interfaceManageDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getTenantId(), "租户id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //接口数据
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(interfaceManageDTO.getId());
        if (interfaceManage == null || !Objects.equals(interfaceManage.getTenantId(), interfaceManageDTO.getTenantId())) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        Page<MemberRelationWithUser> page = new Page<>(interfaceManageDTO.getCurrent(), interfaceManageDTO.getSize());
        //权限数据+用户数据
        ResourceTypeEnum resourceType = getResourceType(interfaceManage.getType());
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(interfaceManageDTO.getId());
        memberRelationDTO.setSize(interfaceManageDTO.getSize());
        memberRelationDTO.setCurrent(interfaceManageDTO.getCurrent());
        memberRelationDTO.setResourceRoleList(Arrays.asList(ResourceRoleEnum.MEMBER.getCode(), ResourceRoleEnum.READONLY_MEMBER.getCode(), ResourceRoleEnum.ADMIN.getCode()));

        Page<MemberRelationWithUser> memberRelationWithUserPage = memberRelationService.pageListRelationWithUserInfoByResource(memberRelationDTO);
        return memberRelationWithUserPage;
    }

    /**
     * 列出应用的所有成员
     *
     * @param interfaceId
     * @return
     * @date: 2022/5/16 14:55
     * @author wubaizhao1
     */
    @Override
    public List<MemberRelationWithUser> listAllAppMember(Long interfaceId) {
        Guard.notEmpty(interfaceId, "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //接口数据
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(interfaceId);
        if (interfaceManage == null) {//|| !Objects.equals(interfaceManage.getTenantId(), UserSessionLocal.getUser().getTenantId())
            throw new BizException("无效的接口id");
        }
        //权限数据+用户数据
        ResourceTypeEnum resourceType = getResourceType(interfaceManage.getType());
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(interfaceId);
        memberRelationDTO.setResourceRoleList(Arrays.asList(ResourceRoleEnum.MEMBER.getCode(), ResourceRoleEnum.ADMIN.getCode()));
        List<MemberRelationWithUser> withUserList = memberRelationService.listRelationWithUserInfoByResource(memberRelationDTO);
        List<MemberRelationWithUser> appUser = getAllAppUser(interfaceManage.getAppId());
        withUserList.addAll(appUser);
        List<MemberRelationWithUser> noDuplicated = new ArrayList<>();
        Set<String> userCode = new HashSet<>();
        for (MemberRelationWithUser memberRelationWithUser : withUserList) {
            if (!userCode.contains(memberRelationWithUser.getUserCode())) {
                userCode.add(memberRelationWithUser.getUserCode());
                noDuplicated.add(memberRelationWithUser);
            }
        }
        return noDuplicated;
    }

    private List<MemberRelationWithUser> getAllAppUser(Long appId) {
        List<MemberRelationWithUser> result = new ArrayList<>();
        if (appId == null) return result;
        AppInfo app = appInfoService.getById(appId);
        if (app == null) return result;
        AppInfoDTO dto = new AppInfoDTO();
        List<String> members = dto.splitMembers(app.getMembers());
        for (String member : members) {
            MemberRelationWithUser relationUser = new MemberRelationWithUser();
            relationUser.setUserCode(member);
            result.add(relationUser);
        }
        List<UserInfo> users = userInfoService.getUsers(members);
        Map<String, List<UserInfo>> userCode2Users = users.stream().collect(Collectors.groupingBy(UserInfo::getUserCode));
        for (MemberRelationWithUser user : result) {
            List<UserInfo> userInfos = userCode2Users.get(user.getUserCode());
            if (userInfos == null) {
                user.setUserName(user.getUserCode());
                continue;
            }
            UserInfo userInfo = userInfos.get(0);
            user.setUserName(userInfo.getUserName());
        }
        return result;
    }

    /**
     * @param interfaceManageDTO
     * @return
     * @date: 2022/6/1 17:16
     * @author wubaizhao1
     */
    public List<UserForAddDTO> listMemberForAdd(InterfaceManageDTO interfaceManageDTO) {
        Guard.notEmpty(interfaceManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(interfaceManageDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getTenantId(), "租户id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getUserCode(), "用户编码不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //接口数据
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(interfaceManageDTO.getId());
        if (interfaceManage == null || !Objects.equals(interfaceManage.getTenantId(), interfaceManageDTO.getTenantId())) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        //模糊搜索用户数据
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserCode(interfaceManageDTO.getUserCode());
        List<UserInfo> userInfos = userInfoService.listByCode(userInfoDTO);
        //权限数据+用户数据
        ResourceTypeEnum resourceType = getResourceType(interfaceManage.getType());
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(interfaceManageDTO.getId());
        memberRelationDTO.setResourceRoleList(Arrays.asList(ResourceRoleEnum.MEMBER.getCode(), ResourceRoleEnum.ADMIN.getCode()));
        List<String> userCodeList = memberRelationService.listUserCodeByResource(memberRelationDTO);
        Set<String> userCodeSet = new HashSet<>(userCodeList);
        List<UserForAddDTO> result = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            UserForAddDTO userForAddDTO = new UserForAddDTO();
            BeanUtils.copyProperties(userInfo, userForAddDTO);
            if (userCodeSet.contains(userInfo.getUserCode())) {
                userForAddDTO.setExist(true);
            } else {
                userForAddDTO.setExist(false);
            }
            result.add(userForAddDTO);
        }
        return result;
    }

    @Override
    public Long copy(InterfaceCopyDto dto) {
        InterfaceManage interfaceManage = getOneById(dto.getInterfaceId());
        Guard.notEmpty(interfaceManage, "接口id无效");
        interfaceManage.setName(dto.getName());
        interfaceManage.setServiceCode(dto.getServiceCode());
        interfaceManage.setDesc(dto.getDesc());
        InterfaceManageDTO interfaceManageDTO = new InterfaceManageDTO();
        interfaceManageDTO.setType(InterfaceTypeEnum.ORCHESTRATION.getCode());
        BeanUtils.copyProperties(interfaceManage, interfaceManageDTO);
        BeanUtils.copyProperties(dto, interfaceManageDTO);
        interfaceManageDTO.setId(null);
        interfaceManageDTO.setIsPublic(0);
        final Long added = add(interfaceManageDTO);
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper();
        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(MethodManage::getInterfaceId, dto.getInterfaceId());
//		lqw.eq(MethodManage::getCreator, user.getUserId());//用户权限过滤
        List<MethodManage> methodList = methodManageService.list(lqw);
        for (MethodManage methodManage : methodList) {
            methodManage.setInterfaceId(added);
            methodManage.setPublished(0);
            methodManage.setId(null);
            methodManageMapper.insert(methodManage);
        }
        return added;
    }

    private InterfaceManage validateGetInterfaceManage(DataSourceDto dto) {
        Guard.notEmpty(dto.getType(), "类型不可为空");
        final InterfaceTypeEnum interfaceTypeEnum = InterfaceTypeEnum.getByCode(dto.getType());
        Guard.notEmpty(interfaceTypeEnum, "无效的类型");
        Guard.notEmpty(dto.getConfig(), "配置不可为空");
        IBeanStepProcessor beanProcessor = BeanStepDefinitionLoader.getBeanProcessor(interfaceTypeEnum.getDesc());
        Class initConfigClass = beanProcessor.getInitConfigClass();
        Object initData = JsonUtils.cast(dto.getConfig(), initConfigClass);
        String[] errors = ValidateUtils.validateInitValue(initData);
        if (errors != null && errors.length > 0) {
            throw new BizException(org.apache.commons.lang.StringUtils.join(errors, ","));
        }
        InterfaceManage interfaceManage = new InterfaceManage();
        interfaceManage.setName(dto.getName());
        interfaceManage.setDesc(dto.getDesc());
        interfaceManage.setType(interfaceTypeEnum.getCode());
        interfaceManage.setConfig(dto.getConfig());
        interfaceManage.setServiceCode(dto.getServiceCode());
        return interfaceManage;
    }

    @Transactional
    @Override
    public Long addDataSource(DataSourceDto dto) {
        InterfaceManage interfaceManage = validateGetInterfaceManage(dto);
        interfaceManage.setTenantId(UserSessionLocal.getUser().getTenantId());
        save(interfaceManage);
        addRight(ResourceTypeEnum.INTERFACE, dto.getAdminCode(), interfaceManage.getId());
        return interfaceManage.getId();
    }

    @Transactional
    @Override
    public Long updateDatasource(DataSourceDto dto) {
        Guard.notEmpty(dto.getId(), "id不可为空");
        InterfaceManage interfaceManage = validateGetInterfaceManage(dto);
        InterfaceManage exist = getById(dto.getId());
        BeanUtils.copyProperties(interfaceManage, exist);
        exist.setId(dto.getId());
        updateById(exist);
        return interfaceManage.getId();
    }

    @Override
    public List<InterfaceManage> getCjgRelatedList(String cjgAppId) {
        LambdaQueryWrapper<InterfaceManage> wrapper = new LambdaQueryWrapper();
        wrapper.eq(InterfaceManage::getCjgAppId, cjgAppId);
        return list(wrapper);
    }

    /**
     * 根据接口的类型，映射权限管理的资源类型。
     *
     * @param code
     * @return
     * @date: 2022/5/16 14:32
     * @author wubaizhao1
     */
    private ResourceTypeEnum getResourceType(Integer code) {
        InterfaceTypeEnum type = InterfaceTypeEnum.getByCode(code);
        ResourceTypeEnum resourceType = null;
        if (type == null) {
            return null;
        }
        switch (type) {
            case ORCHESTRATION:
                resourceType = ResourceTypeEnum.ORCHESTRATION;
                break;
            default:
                resourceType = ResourceTypeEnum.INTERFACE;
                break;
        }
        return resourceType;
    }

    private List<Integer> getInterfaceTypeList() {
        return Arrays.asList(InterfaceTypeEnum.HTTP.getCode(), InterfaceTypeEnum.WEB_SERVICE.getCode()
                , InterfaceTypeEnum.JSF.getCode(), InterfaceTypeEnum.DUCC.getCode(), InterfaceTypeEnum.JIMDB.getCode(), InterfaceTypeEnum.BEAN.getCode());
    }

    private void checkEnv(String env) {
        if (EmptyUtil.isNotEmpty(env)) {
            try {
                List<EnvModel> envModelList = JsonUtils.parseArray(env, EnvModel.class);
                for (EnvModel envModel : envModelList) {
                    if (EmptyUtil.isAnyEmpty(envModel.getEnvName(), envModel.getUrl(), envModel.getType())) {
                        throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "env字段格式校验不通过");
                    }
                    if (!envModel.getEnvName().matches(StringMatchEnum.NUMBER_LETTER_CN.getMatch()) || envModel.getEnvName().length() > 10) {
                        throw ServiceException.withCommon("env.Name仅支持汉字、字母和数字,不超过10位。");
                    }
                }
            } catch (Exception e) {
                log.error("env字段格式校验不通过", e);
                throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "env字段格式校验不通过");
            }
        }
    }

    private void addBeforeCheck(InterfaceManageDTO interfaceManageDTO) {
        Guard.notEmpty(interfaceManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(interfaceManageDTO.getName(), "接口名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getType(), "接口类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //Guard.notEmpty(interfaceManageDTO.getDesc(),"接口描述不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getTenantId(), "租户id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getAdminCode(), "负责人Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
		/*if(!interfaceManageDTO.getName().matches(StringMatchEnum.NUMBER_LETTER_CN.getMatch()) || interfaceManageDTO.getName().length()>30){
			throw ServiceException.withCommon("请输入汉字、字母和数字，不超过30位。");
		}*/
        String tenantId = interfaceManageDTO.getTenantId();
        //对大字段的校验
        checkEnv(interfaceManageDTO.getEnv());
        //WebService的校验
        if (InterfaceTypeEnum.WEB_SERVICE.getCode().equals(interfaceManageDTO.getType()) && EmptyUtil.isEmpty(interfaceManageDTO.getPath())) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "类型为WebService时,path地址不能为空");
        }
        // 编排管理 服务编码 唯一校验
        if (InterfaceTypeEnum.ORCHESTRATION.getCode().equals(interfaceManageDTO.getType())) {
            Guard.notEmpty(interfaceManageDTO.getServiceCode(), "服务编码不可为空");
//            Guard.notEmpty(interfaceManageDTO.getNodeType(), "节点类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
            Guard.notEmpty(interfaceManageDTO.getServiceCode(), "服务编码", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
            LambdaQueryWrapper<InterfaceManage> serviceCodeLqw = new LambdaQueryWrapper<>();
            serviceCodeLqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode())
                    .eq(tenantId != null, InterfaceManage::getTenantId, tenantId)
                    .eq(interfaceManageDTO.getAppId() != null, InterfaceManage::getAppId, interfaceManageDTO.getAppId())
                    .isNull(interfaceManageDTO.getAppId() == null, InterfaceManage::getAppId)
                    .eq(InterfaceManage::getServiceCode, interfaceManageDTO.getServiceCode());
            if (interfaceManageMapper.selectCount(serviceCodeLqw) > 0) {
                throw ServiceException.withCommon("服务编码重复，请重新输入。");
            }
        }
    }

    /**
     * 防重复校验
     *
     * @param interfaceManageDTO
     * @date: 2022/6/20 17:55
     * @author wubaizhao1
     */
    private void addCheckDuplicate(InterfaceManageDTO interfaceManageDTO) {
        String tenantId = interfaceManageDTO.getTenantId();
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode())
                .eq(tenantId != null, InterfaceManage::getTenantId, tenantId).eq(InterfaceManage::getName, interfaceManageDTO.getName())
                .eq(interfaceManageDTO.getAppId() != null, InterfaceManage::getAppId, interfaceManageDTO.getAppId())
                .isNull(interfaceManageDTO.getAppId() == null, InterfaceManage::getAppId)
                .eq(InterfaceManage::getType, interfaceManageDTO.getType());
        int count = interfaceManageMapper.selectCount(lqw);
        if (count > 0) {
            throw ServiceException.withCommon("该名称已存在");
        }
    }

    private void addCheckServiceCodeDuplicate(InterfaceManageDTO interfaceManageDTO) {
        if (EmptyUtil.isEmpty(interfaceManageDTO.getServiceCode())) {
            //默认附上唯一值，保证唯一索引
            interfaceManageDTO.setServiceCode("[Only]" + UUID.randomUUID().toString());
        }
        String tenantId = interfaceManageDTO.getTenantId();
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode())
                .eq(tenantId != null, InterfaceManage::getTenantId, tenantId)
                .eq(interfaceManageDTO.getAppId() != null, InterfaceManage::getAppId, interfaceManageDTO.getAppId())
                .isNull(interfaceManageDTO.getAppId() == null, InterfaceManage::getAppId)
                .eq(InterfaceManage::getServiceCode, interfaceManageDTO.getServiceCode());
        int count = interfaceManageMapper.selectCount(lqw);
        if (count > 0) {
            throw ServiceException.with(ServiceErrorEnum.DATA_DUPLICATION_ERROR_INFO, "服务编码重复!");
        }
    }

    private InterfaceManage addExecute(InterfaceManageDTO interfaceManageDTO) {
        InterfaceManage interfaceManage = new InterfaceManage();
        if (interfaceManageDTO.getDocConfig() != null) {
            InterfaceDocConfig docConfig = new InterfaceDocConfig();
            BeanUtils.copyProperties(interfaceManageDTO.getDocConfig(), docConfig);
            interfaceManage.setDocConfig(docConfig);
        }
        BeanUtils.copyProperties(interfaceManageDTO, interfaceManage);
        String deptName = getDeptName(interfaceManageDTO.getAppId(), interfaceManageDTO.getAdminCode());
        if (StringUtils.isNotBlank(deptName)) {
            interfaceManage.setDeptName(deptName);
        }
        interfaceManage.setYn(DataYnEnum.VALID.getCode());
        interfaceManage.setTenantId(interfaceManageDTO.getTenantId());
        interfaceManage.setId(null);
        int add = interfaceManageMapper.insert(interfaceManage);
        return interfaceManage;
    }

    private void addRight(ResourceTypeEnum resourceType, String adminCode, Long id) {
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(id);
        memberRelationDTO.setUserCode(adminCode);
        memberRelationDTO.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
        memberRelationService.binding(memberRelationDTO);
    }

    private void addRight(InterfaceManageDTO interfaceManageDTO, Long id) {
        ResourceTypeEnum resourceType = getResourceType(interfaceManageDTO.getType());
        addRight(resourceType, interfaceManageDTO.getAdminCode(), id);
    }

    private void addAfterExtra(InterfaceManageDTO interfaceManageDTO, Long id) {
        //如果是webservice 触发更新方法列表
        if (InterfaceTypeEnum.WEB_SERVICE.getCode().equals(interfaceManageDTO.getType())) {
            MethodManageDTO methodManageDTO = new MethodManageDTO();
            methodManageDTO.setInterfaceId(id);
            methodManageService.updateWebService(methodManageDTO);
        }
    }

    private void editBeforeCheck(InterfaceManageDTO interfaceManageDTO) {
        Guard.notEmpty(interfaceManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(interfaceManageDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getTenantId(), "租户id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
		/*if (EmptyUtil.isNotEmpty(interfaceManageDTO.getName())){
			if(!interfaceManageDTO.getName().matches(StringMatchEnum.NUMBER_LETTER_CN.getMatch()) || interfaceManageDTO.getName().length()>30){
				throw ServiceException.withCommon("请输入汉字、字母和数字，不超过30位。");
			}
		}*/
        //对大字段的校验
        checkEnv(interfaceManageDTO.getEnv());
    }

    private InterfaceManage editEmptyCheck(Long id) {
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(id);
        if (EmptyUtil.isEmpty(interfaceManage)) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        return interfaceManage;
    }

    private void editAdmin(InterfaceManageDTO interfaceManageDTO, InterfaceManage interfaceManage) {
        if (EmptyUtil.isNotEmpty(interfaceManageDTO.getAdminCode())) {
            ResourceTypeEnum resourceType = getResourceType(interfaceManage.getType());
            MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
            memberRelationDTO.setResourceType(resourceType.getCode());
            memberRelationDTO.setResourceId(interfaceManageDTO.getId());
            memberRelationDTO.setUserCode(interfaceManageDTO.getAdminCode());
            memberRelationService.changeAdminCode(memberRelationDTO);
        }
    }

    private InterfaceManage editExecute(InterfaceManageDTO interfaceManageDTO) {
        InterfaceManage interfaceManageForUpdate = new InterfaceManage();
        if (interfaceManageDTO.getDocConfig() != null) {
            InterfaceDocConfig docConfig = new InterfaceDocConfig();
            BeanUtils.copyProperties(interfaceManageDTO.getDocConfig(), docConfig);
            interfaceManageForUpdate.setDocConfig(docConfig);

        }
        BeanUtils.copyProperties(interfaceManageDTO, interfaceManageForUpdate);
        int update = interfaceManageMapper.updateById(interfaceManageForUpdate);
        return interfaceManageForUpdate;
    }

    private void removeBeforeCheck(InterfaceManageDTO interfaceManageDTO) {
        Guard.notEmpty(interfaceManageDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(interfaceManageDTO.getId(), "id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(interfaceManageDTO.getTenantId(), "租户id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
    }

    private int removeCountSubMethod(Long interfaceId) {
        LambdaQueryWrapper<MethodManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MethodManage::getInterfaceId, interfaceId);
        lqw.eq(MethodManage::getYn, DataYnEnum.VALID.getCode());
        int count = methodManageService.count(lqw);
        return count;
    }

    /**
     * 补全接口的负责人code以及名称信息
     */
    private void updateFollowStatus(List<InterfaceManage> interfaces) {
        List<Long> interfaceIds = interfaces.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        if (interfaceIds.isEmpty()) return;
        LambdaQueryWrapper<InterfaceFollowList> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceFollowList::getInterfaceId, interfaceIds);
        lqw.eq(InterfaceFollowList::getErp, UserSessionLocal.getUser().getUserId());
        List<InterfaceFollowList> followLists = interfaceFollowListService.list(lqw);
        Map<Long, List<InterfaceFollowList>> followMap = followLists.stream().collect(Collectors.groupingBy(InterfaceFollowList::getInterfaceId));
        for (InterfaceManage manage : interfaces) {
            if (followMap.containsKey(manage.getId())) {
                manage.setFollowStatus(1);
            } else {
                manage.setFollowStatus(0);
            }
        }
    }

    /**
     * 补全接口的负责人code以及名称信息
     */
    public void fixInterfaceAdminInfo(List<InterfaceManage> interfaces, Integer interfaceType) {
        //final List<Long> interfaceIds = interfaces.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        memberRelationService.fixInterfaceAdminInfo(interfaces, interfaceType);
    }

    private void pageEachHandle(InterfaceManage interfaceManage, Integer interfaceType) {
        Long id = interfaceManage.getId();
		/*MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
		memberRelationDTO.setResourceId(id);
		ResourceTypeEnum resourceType = getResourceType(interfaceType);
		Integer defaultType=resourceType==null?ResourceTypeEnum.INTERFACE.getCode():resourceType.getCode();
		memberRelationDTO.setResourceType(defaultType);*/
		/*UserInfo admin = memberRelationService.getAdminWithUser(memberRelationDTO);
		if (EmptyUtil.isNotEmpty(admin)) {
			interfaceManage.setUserCode(maskPinHelper.maskUserCode(admin.getUserCode()));
			interfaceManage.setUserName(admin.getUserName());
		}*/
        interfaceManage.setHasAuth(0);
        if (interfaceManage.getIsPublic() != 1) { // 非公开就有权限
            interfaceManage.setHasAuth(1);
        } else {
            if (checkPass(interfaceManage.getId(), UserSessionLocal.getUser().getUserId())) {
                interfaceManage.setHasAuth(1);
            }
        }

    }

    private List<Long> pageListFilteByLoginUserCode(InterfaceManageDTO interfaceManageDTO) {
        List<Long> resourceIds = new ArrayList<>();
        String userCode = UserSessionLocal.getUser().getUserId();
        if (EmptyUtil.isNotEmpty(userCode)) {
            MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
            ResourceTypeEnum resourceType = getResourceType(interfaceManageDTO.getType());
            memberRelationDTO.setResourceType(resourceType == null ? null : resourceType.getCode());
//			memberRelationDTO.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
            memberRelationDTO.setUserCode(userCode);
            log.info("InterfaceManageServiceImpl pageList userCode is not empty memberRelationDTO={}", JsonUtils.toJSONString(memberRelationDTO));
            resourceIds = memberRelationService.listResourceIds(memberRelationDTO);
        }
        return resourceIds;
    }

    // 找到负责人是指定人员的所有接口
    private List<Long> pageListFilteByAdmin(InterfaceManageDTO interfaceManageDTO) {
        List<Long> resourceIds = new ArrayList<>();
        if (EmptyUtil.isNotEmpty(interfaceManageDTO.getAdminCode())) {
            MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
            ResourceTypeEnum resourceType = getResourceType(interfaceManageDTO.getType());
            memberRelationDTO.setResourceType(resourceType == null ? null : resourceType.getCode());
            memberRelationDTO.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
            memberRelationDTO.setUserCode(interfaceManageDTO.getAdminCode());
            log.info("InterfaceManageServiceImpl pageList admindId is not empty memberRelationDTO={}", JsonUtils.toJSONString(memberRelationDTO));
            resourceIds = memberRelationService.listResourceIds(memberRelationDTO);
        }
        return resourceIds;
    }

    private Page stdPage(PageParam dto) {
        Page page = null;
        if (dto.getCurrent() == null || dto.getSize() == null) {
            page = new Page<>(1, 100);
        } else {
            page = new Page<>(dto.getCurrent(), dto.getSize());
        }
        return page;
    }

    private LambdaQueryWrapper<InterfaceManage> pageListCondition(InterfaceManageDTO interfaceManageDTO, List<Long> resourceIds) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper();
        lqw.select(InterfaceManage.class, x -> !x.getColumn().equals("env"));
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(interfaceManageDTO.getTenantId() != null, InterfaceManage::getTenantId, interfaceManageDTO.getTenantId());
        //类型筛选
        if (EmptyUtil.isNotEmpty(interfaceManageDTO.getType())) {
            lqw.eq(EmptyUtil.isNotEmpty(interfaceManageDTO.getType()), InterfaceManage::getType, interfaceManageDTO.getType());
        } else {
            //默认接口管理
            lqw.in(InterfaceManage::getType, getInterfaceTypeList());
        }
        if (interfaceManageDTO.getNodeType() != null) {
            lqw.eq(InterfaceManage::getNodeType, interfaceManageDTO.getNodeType());
        }
        //模糊筛选
        lqw.like(EmptyUtil.isNotEmpty(interfaceManageDTO.getName()), InterfaceManage::getName, interfaceManageDTO.getName());
        lqw.orderByDesc(InterfaceManage::getModified);
        lqw.in(EmptyUtil.isNotEmpty(resourceIds), InterfaceManage::getId, resourceIds);
        return lqw;
    }

    public void excludeBigTextFiled(LambdaQueryWrapper<InterfaceManage> lqw) {
        lqw.select(InterfaceManage.class, x -> {
            String[] bigTextFields = new String[]{"sort_group_tree", "content", "doc_info"};
            return Arrays.asList(bigTextFields).indexOf(x.getColumn()) == -1;
        });

    }

    private List<InterfaceManage> list(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper();
        lqw.in(InterfaceManage::getId, ids);
        lqw.eq(InterfaceManage::getYn, 1);
        excludeBigTextFiled(lqw);
        return list(lqw);
    }

    public InterfaceManage getAppInterface(String appId, String interfaceName, boolean autoReport) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        //excludeBigTextFiled(lqw);
        if (autoReport) {
            lqw.eq(InterfaceManage::getAutoReport, 1);
        }
        lqw.eq(InterfaceManage::getServiceCode, interfaceName);
        lqw.eq(InterfaceManage::getAppId, appId);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        List<InterfaceManage> list = list(lqw);
        if (!list.isEmpty()) return list.get(0);
        return null;
    }

    @Override
    public List<InterfaceManage> geInterfaceByName(String interfaceName) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceManage::getAutoReport, Arrays.asList(1, 0));
        lqw.eq(InterfaceManage::getServiceCode, interfaceName);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        List<InterfaceManage> list = list(lqw);
        return list;
    }

    private LambdaQueryWrapper<InterfaceManage> listCondition(InterfaceAppSearchDto dto) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(dto.getName())) {
            lqw.and(child -> {
                child.or(wrapper -> {
                    wrapper.like(InterfaceManage::getName, dto.getName());
                }).or(wrapper -> {
                    wrapper.like(InterfaceManage::getServiceCode, dto.getName());
                });
            });
        }
        if (!ObjectHelper.isEmpty(dto.getInterfaceIds())) {
            lqw.in(InterfaceManage::getId, dto.getInterfaceIds());
        }
        excludeBigTextFiled(lqw);
        if (dto.getAppId() != null) {
            if (dto.getAppId().equals(0L)) {
                lqw.isNull(InterfaceManage::getAppId);
                lqw.inSql(InterfaceManage::getId, "select resource_id from member_relation where yn = 1 and resource_type = 1 and user_code = '" + UserSessionLocal.getUser().getUserId() + "'");
            } else {
                lqw.eq(dto.getAppId() != null, InterfaceManage::getAppId, dto.getAppId());
            }
        }


        lqw.eq(dto.getType() != null, InterfaceManage::getType, dto.getType());
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        return lqw;
    }

    @Override
    public List<InterfaceManage> getAppInterfaces(Long appId, String interfaceName, int type) {
        InterfaceAppSearchDto dto = new InterfaceAppSearchDto();
        dto.setAppId(appId);
        dto.setName(interfaceName);
        dto.setType(type);
        LambdaQueryWrapper<InterfaceManage> lqw = listCondition(dto);
        List<InterfaceManage> list = list(lqw);
        return list;
    }

    public List<InterfaceManage> getAppInterface(Long appId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        //excludeBigTextFiled(lqw);

        lqw.eq(InterfaceManage::getAppId, appId);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        List<InterfaceManage> list = list(lqw);
        return list;
    }

    @Override
    public Page<InterfaceManage> listInterface(InterfaceAppSearchDto dto) {
        LambdaQueryWrapper<InterfaceManage> lqw = listCondition(dto);
        return page(new Page<>(dto.getCurrent(), dto.getSize()), lqw);
    }

    @Override
    public List<InterfaceManage> getAppInterfaces(Long appId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getAppId, appId);
        lqw.eq(InterfaceManage::getYn, 1);
        return list(lqw);
    }

    @Override
    public List<InterfaceManage> getAppInterfaceIncludeInvalids(Long appId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getAppId, appId);
        excludeBigTextFiled(lqw);
        return list(lqw);
    }

    public Page<InterfaceManage> findInterfaceList(String appCode, String search, Long current, Long size, Integer autoReport) {
        AppInfo appInfo = appInfoService.findApp(appCode);
        Guard.notEmpty(appInfo, "无效的应用code:" + appCode);
        InterfacePageQuery interfaceManageDTO = new InterfacePageQuery();
        if (Objects.nonNull(current) && current > 0) {
            interfaceManageDTO.setCurrent(current);
        }
        if (Objects.nonNull(size) && size > 0) {
            interfaceManageDTO.setSize(size);
        }
        interfaceManageDTO.setName(search);
        interfaceManageDTO.setAppId(appInfo.getId());
        String tenantId = UserSessionLocal.getUser().getTenantId();
        interfaceManageDTO.setTenantId(tenantId);
        interfaceManageDTO.setAutoReport(autoReport);
        return pageList(interfaceManageDTO);
    }

    @Override
    public InterfaceDocConfig updateDocConfig(UpdateInterfaceConfigDto dto) {
        InterfaceManage interfaceManage = getById(dto.getInterfaceId());
        Guard.notEmpty(interfaceManage, "无效的接口id");
        InterfaceDocConfig docConfig = interfaceManage.getDocConfig();
        if (InterfaceTypeEnum.JSF.getCode().equals(interfaceManage.getType())) {
            if (docConfig == null) {
                docConfig = new JsfDocConfig();
                interfaceManage.setDocConfig(docConfig);
            }
        } else if (InterfaceTypeEnum.HTTP.equals(interfaceManage.getType())) {
        }
		/*if (interfaceManage.getDocConfig() == null) {
			interfaceManage.setDocConfig(docConfig);
		}*/
        try {
            org.apache.commons.beanutils.BeanUtils.setProperty(docConfig, dto.getField(), dto.getFieldValue());

        } catch (Exception e) {
            throw new BizException("设置失败");
        }
        updateById(interfaceManage);
        return docConfig;
    }

    @Override
    public UserInterfaceCountDto statisticUserInterfaceCount() {
        InterfaceCountQueryDto dto = new InterfaceCountQueryDto();
        dto.setCurrentUser(UserSessionLocal.getUser().getUserId());
        dto.setResourceType(ResourceTypeEnum.INTERFACE.getCode());
        List<Map<String, Object>> result = getBaseMapper().queryUserInterfaceCount(dto);
        UserInterfaceCountDto ret = new UserInterfaceCountDto();
        for (Map<String, Object> map : result) {
            if (0 == (Integer) map.get("autoReport")) {
                ret.setNonAutoReportCount(Variant.valueOf(map.get("size")).toLong());
            } else {
                ret.setAutoReportCount(Variant.valueOf(map.get("size")).toLong());
            }
        }
        return ret;
    }

    @Override
    public QueryDeptResultDTO queryDeptList(QueryDeptReqDTO query) {
        QueryDeptResultDTO dto = new QueryDeptResultDTO();
        query.initPageParam(1000);
        dto.setCurrentPage(query.getCurrentPage());
        dto.setPageSize(query.getPageSize());
        InterfaceQueryDto querydto = new InterfaceQueryDto();
        querydto.setOffset((long) ((dto.getCurrentPage() - 1) * dto.getPageSize()));
        querydto.setDeptName(query.getDeptName());
        dto.setList(interfaceManageMapper.queryDeptNameList(querydto));
        dto.setTotalCnt(interfaceManageMapper.queryDeptNameCount(querydto));
        return dto;
    }

    /**
     * 批量更新接口信息
     *
     * @param interfaceManages
     */
    public void batchUpdateInterfaceInfo(List<InterfaceManage> interfaceManages) {
        updateBatchById(interfaceManages, interfaceManages.size());
    }

    @Override
    public List<InterfaceManage> queryListByAppId(Long appId) {
        return interfaceManageMapper.queryListByAppId(appId);
    }

    @Override
    public JsfAndHttpInterfaceCountDto queryNumsByType() {
        JsfAndHttpInterfaceCountDto dto = new JsfAndHttpInterfaceCountDto();
        dto.setHttpInterfaceCount(interfaceManageMapper.queryNumsByType(HTTP_TYPE));
        dto.setJsfInterfaceCount(interfaceManageMapper.queryNumsByType(JSF_TYPE));
        return dto;
    }

    /**
     * 批量更新接口部门信息
     *
     * @param interfaceManages
     */
    public int batchUpdateInterfaceDeptName(List<InterfaceManage> interfaceManages) {
        return interfaceManageMapper.batchUpdateInterfaceDeptName(interfaceManages);
    }

    private String NOTICETEMPLATE = "（%s）接口信息已发生变化，请于接口详情页查看接口变更记录。";
    private String NOTICEURL = "http://console.paas.jd.com/idt/online/interface/interfaceDetail/%d/%d";

    @Override
    public void sendMessage(InterfaceManage manage) {
        Set<String> interfaceFollowUser = interfaceFollowListService.getInterfaceFollowUser(manage.getId());
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(interfaceFollowUser)) return;
        JdMENoticeMessage noticeMessage = new JdMENoticeMessage();
        noticeMessage.setTitle("接口文档变更通知");
        noticeMessage.setContent(String.format(NOTICETEMPLATE, manage.getName()));
        noticeMessage.setTos(interfaceFollowUser.toArray(new String[interfaceFollowUser.size()]));
        JdMENoticeMessage.Extend extend = new JdMENoticeMessage.Extend();
        extend.setUrl(String.format(NOTICEURL, manage.getType(), manage.getId()));
        noticeMessage.setExtend(extend);
        JdMEResult<String> jdMeResult = JdMEMessageUtil.sendMessage(noticeMessage);
        log.info("京东ME消息发送结果为：{}", com.jd.fastjson.JSON.toJSONString(jdMeResult));
    }

    public List<InterfaceManage> listInterfaceByIds(List<Long> ids) {


        return listInterfaceByIds(ids, null);
    }

    public List<InterfaceManage> listInterfaceByIds(List<Long> ids, String search) {
        if (ids.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceManage::getId, ids);
        lqw.eq(InterfaceManage::getYn, 1);
        lqw.like(StringUtils.isNotBlank(search), InterfaceManage::getName, search);
        lqw.like(StringUtils.isNotBlank(search), InterfaceManage::getServiceCode, search);
        excludeBigTextFiled(lqw);

        return list(lqw);
    }

    public List<InterfaceManage> listInterfaceByIdsOnlyOne(List<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceManage::getId, ids);
        lqw.eq(InterfaceManage::getYn, 1);
        lqw.last("limit 1");
        excludeBigTextFiled(lqw);
        return list(lqw);
    }

    @Override
    public List<AppInfoDTO> getInterfaceApps(List<Long> interfaceIds) {
        if (interfaceIds.isEmpty()) return Collections.emptyList();
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceManage::getId, interfaceIds);
        lqw.select(InterfaceManage.class, column -> {
            return column.getColumn().equals("app_id");
        });
        List<InterfaceManage> interfaceManages = list(lqw);
        List<Long> appIds = interfaceManages.stream().map(item -> item.getAppId()).filter(item -> item != null).collect(Collectors.toList());
        if (appIds.isEmpty()) return Collections.emptyList();
        List<AppInfo> appInfos = appInfoService.listByIds(appIds);
        return appInfos.stream().map(app -> {
            AppInfoDTO dto = new AppInfoDTO();

            BeanUtils.copyProperties(app, dto);
            dto.setAppSecret(null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void initInterfaceAppAndAdminInfo(List<InterfaceManage> interfaceManages) {
        fixInterfaceAdminInfo(interfaceManages, ResourceTypeEnum.INTERFACE.getCode());
        fillInterfaceAppInfo(interfaceManages);
    }

    @Override
    public List<InterfaceManage> listHttpInterfaceByAppId(Long appId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getAppId, appId);
        lqw.eq(InterfaceManage::getYn, 1);
        lqw.eq(InterfaceManage::getType, 1);
        excludeBigTextFiled(lqw);

        return list(lqw);

    }

    @Override
    public boolean updateCloudFile(Long id, String path, String tags) {
        LambdaUpdateWrapper<InterfaceManage> interfaceWrapper = new LambdaUpdateWrapper<>();
        interfaceWrapper.eq(InterfaceManage::getId, id);
        interfaceWrapper.eq(InterfaceManage::getYn, 1);
        interfaceWrapper.set(InterfaceManage::getCloudFilePath, path);
        interfaceWrapper.set(InterfaceManage::getCloudFileTags, tags);
        interfaceWrapper.set(InterfaceManage::getUnionFile, 1);
        update(null, interfaceWrapper);

        LambdaUpdateWrapper<MethodManage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MethodManage::getInterfaceId, id);
        wrapper.eq(MethodManage::getYn, 1);
        wrapper.set(MethodManage::getCloudFilePath, path);
        wrapper.set(MethodManage::getCloudFileTags, tags);
        wrapper.set(MethodManage::getUnionFile, 1);

        return methodManageService.update(wrapper);


    }

    @Override
    public List<InterfaceManage> getAppInterfaceByAppIdList(List<Long> appIds) {
        List<InterfaceManage> retData = new ArrayList<>();
        if (!CollectionUtils.isEmpty(appIds)) {
            LambdaUpdateWrapper<InterfaceManage> interfaceWrapper = new LambdaUpdateWrapper<>();
            interfaceWrapper.in(InterfaceManage::getAppId, appIds);
            interfaceWrapper.eq(InterfaceManage::getYn, 1);
            interfaceWrapper.in(InterfaceManage::getAutoReport, Arrays.asList(0, 1));
            interfaceWrapper.last("limit 1");
            retData = list(interfaceWrapper);
        }
        return retData;
    }


    public static void main(String[] args) {
        System.out.println("ext.123".matches(ERP_PATTERN));
    }


}