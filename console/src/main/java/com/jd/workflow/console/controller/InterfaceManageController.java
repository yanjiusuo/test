package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.businessworks.domain.FlowBeanInfo;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceSimpleTypeEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.datasource.DataSourceDto;
import com.jd.workflow.console.dto.dept.QueryDeptReqDTO;
import com.jd.workflow.console.dto.dept.QueryDeptResultDTO;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.jingme.UserDTO;
import com.jd.workflow.console.dto.jsf.JsfImportDto;
import com.jd.workflow.console.dto.requirement.DemandDetailDTO;
import com.jd.workflow.console.elastic.entity.InterfaceManageDoc;
import com.jd.workflow.console.elastic.service.EsInterfaceService;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 接口管理 前端控制器
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Slf4j
@RestController
@UmpMonitor
@RequestMapping("/interfaceManage")
@Api(tags = "接口管理", value = "接口管理")
public class InterfaceManageController {

    /**
     * @date: 2022/5/13 11:18
     * @author wubaizhao1
     */
    @Resource
    IInterfaceManageService interfaceManageService;

    @Resource
    IInterfaceVersionService interfaceVersionService;
    @Autowired
    InterfaceFollowListService interfaceFollowListService;

    @Resource
    JavaBeanService javaBeanService;
    @Autowired
    IMethodManageService methodManageService;

    @Autowired
    EsInterfaceService esInterfaceService;
    @Autowired
    IAppInfoService appInfoService;

    @Resource
    private IApproveService approveService;

    @Autowired
    private IAppInfoMembersService appInfoMembersService;

    @Autowired
    private IUserInfoService userInfoService;

    /**
     * 新增
     * 入参:
     * 出参: id
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/12 17:55
     * @author wubaizhao1
     */
    @PostMapping("/addInterface")
    @ApiOperation(value = "新增接口")
    public CommonResult<Long> add(@RequestBody InterfaceManageDTO interfaceManageDTO) {
        log.info("InterfaceManageController add query={}", JsonUtils.toJSONString(interfaceManageDTO));
        //1.判空
        //2.入参封装
        String tenantId = UserSessionLocal.getUser().getTenantId();
        String operator = UserSessionLocal.getUser().getUserId();
        interfaceManageDTO.setTenantId(tenantId);
        //3.service层
        Long ref = interfaceManageService.add(interfaceManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("importJsfInterface")
    @ApiOperation(value = "导入jsf接口 & 重新导入jsf接口")
    public CommonResult importJsfInterface(@Valid @RequestBody JsfImportDto dto) {
        if(dto.getId() == null){
            Guard.notEmpty(dto.getAdminCode(),"负责人不可为空");
            Guard.notEmpty(dto.getGroupId(),"groupId不可为空");
            Guard.notEmpty(dto.getArtifactId(),"artifactId不可为空");
            Guard.notEmpty(dto.getVersion(),"version不可为空");
            Guard.notEmpty(dto.getServiceCode(),"serviceCode不可为空");
        }
        return CommonResult.buildSuccessResult(interfaceManageService.importJsfInterface(dto));
    }

    @PostMapping("updateInterfaceBusinessDomain")
    @ApiOperation(value = "更新接口业务域")
    public CommonResult<Boolean> updateInterfaceBusinessDomain(@Valid @RequestBody UpdateBusinessDomainDto dto) {
        return CommonResult.buildSuccessResult(interfaceManageService.updateInterfaceDomain(dto));
    }

    /**
     *
     * @param file java bean jar包
     * @return 解析后的java bean信息
     */
    @PostMapping("parseJavaBean")
    @ApiOperation(value = "解析java bean")
    public CommonResult<List<FlowBeanInfo>> parseJavaBean(MultipartFile file) {
        return CommonResult.buildSuccessResult(javaBeanService.parseJavaBean(file));
    }
    @PostMapping("saveJavaBean")
    @ApiOperation(value = "保存java bean")
    public CommonResult<Boolean> saveJavaBean(@RequestBody List<FlowBeanInfo> beanInfos,Long appId) {
        interfaceManageService.saveJavaBean(beanInfos,appId);
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * 用来关注接口
     * @param methodId 接口id
     * @return
     */
    @GetMapping("followInterface")
    @ApiOperation(value = "关注接口")
    public CommonResult followInterface( Long methodId) {
        return CommonResult.buildSuccessResult(interfaceFollowListService.followInterfaceByMethodId(methodId));
    }
    /**
     * 用来取消关注接口
     * @param methodId 接口id
     * @return
     */
    @GetMapping("unfollowInterface")
    @ApiOperation(value = "取消关注接口")
    public CommonResult unfollowInterface( Long methodId) {
        interfaceFollowListService.unFollowInterfaceByMethodId(methodId);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 获取接口通知状态
     * @param interfaceId 接口id
     * @return 1-开启 非1为关闭
     */
    @GetMapping("getInterfaceNoticeStatus")
    @ApiOperation(value = "获取接口通知状态")
    public CommonResult getInterfaceNoticeStatus( Long interfaceId) {
        return CommonResult.buildSuccessResult(interfaceManageService.getInterfaceNoticeStatus(interfaceId));
    }

    /**
     * 获取接口通知状态
     * @param interfaceId 接口id
     * @param status 状态：1-开启 0-关闭
     * @return
     */
    @GetMapping("updateInterfaceNoticeStatus")
    @ApiOperation(value = "更新接口通知状态")
    public CommonResult updateInterfaceNoticeStatus( Long interfaceId,int status) {
        return CommonResult.buildSuccessResult(interfaceManageService.updateInterfaceNoticeStatus(interfaceId,status));
    }


    /**
     * 修改
     * 入参:
     * 出参: id
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/12 17:54
     * @author wubaizhao1
     */
    @PostMapping("/editInterface")
    @Authorization(key = "id", parseType = ParseType.BODY)
    @ApiOperation(value = "接口修改")
    public CommonResult<Long> editInterface(@RequestBody InterfaceManageDTO interfaceManageDTO) {
        log.info("InterfaceManageController edit query={}", JsonUtils.toJSONString(interfaceManageDTO));
        //1.判空
        //2.入参封装
        String tenantId = UserSessionLocal.getUser().getTenantId();
        String operator = UserSessionLocal.getUser().getUserId();
        interfaceManageDTO.setTenantId(tenantId);
        //3.service层
        Long ref = interfaceManageService.edit(interfaceManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 设置接口所属应用
     * @param interfaceId 接口id
     * @param appId 应用id
     * @return
     */
    @RequestMapping("/setInterfaceAppId")
    public CommonResult<Boolean> setInterfaceAppId(Long interfaceId,Long appId){
        return CommonResult.buildSuccessResult(interfaceManageService.setInterfaceAppId(interfaceId,appId));
    }

    /**
     * 删除
     * 入参: id
     * 出参: boolean
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/12 17:50
     * @author wubaizhao1
     */
    @PostMapping("/removeInterface")
    @Authorization(key = "id", parseType = ParseType.BODY)
    @ApiOperation(value = "接口删除")
    public CommonResult<Boolean> removeInterface(@RequestBody InterfaceManageDTO interfaceManageDTO) {
        log.info("InterfaceManageController pageList query={}", JsonUtils.toJSONString(interfaceManageDTO));
        //1.判空
        //2.入参封装
        String tenantId = UserSessionLocal.getUser().getTenantId();
        String operator = UserSessionLocal.getUser().getUserId();
        interfaceManageDTO.setTenantId(tenantId);
        //3.service层
        Boolean ref = interfaceManageService.remove(interfaceManageDTO.getId());
        //4.出参
        if (ref) {
            return CommonResult.buildSuccessResult(ref);
        } else {
            return new CommonResult(ServiceErrorEnum.SUCCESS.getCode(), "该接口下存在可用方法，拒绝删除接口", ref);
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
    @GetMapping("/pageListInterface")
    @ApiOperation(value = "接口列表")
    public CommonResult<Page<InterfaceManage>> pageList(InterfacePageQuery interfaceManageDTO) {
        log.info("InterfaceManageController pageList query={}", JsonUtils.toJSONString(interfaceManageDTO));
        //1.判空
        //2.入参封装
        //		String operator=UserSessionLocal.getUser().getUserId();
        String tenantId = UserSessionLocal.getUser().getTenantId();
        interfaceManageDTO.setTenantId(tenantId);
        //3.service层
        Page<InterfaceManage> interfaceManagePage = interfaceManageService.pageList(interfaceManageDTO);
        for (InterfaceManage record : interfaceManagePage.getRecords()) {
            record.init();
        }
        //4.出参
        return CommonResult.buildSuccessResult(interfaceManagePage);
    }
    /**
     * 接口分页查询(我有权限的接口)
     * 提供支持天空之城bff
     * 入参: 解析出必填的 租户id+用户code
     * 出参: Page<InterfaceManage>
     * @date: 2022/5/12 17:48
     * @author wubaizhao1
     * @param interfaceManageDTO
     * @return
     */
    /*@GetMapping("/pageListMyInterface")
	public CommonResult pageListMyInterface( InterfaceManageDTO interfaceManageDTO){
		log.info("InterfaceManageController pageList query={}", JsonUtils.toJSONString(interfaceManageDTO));
		//1.判空
		//2.入参封装
		String operator=UserSessionLocal.getUser().getUserId();
		String tenantId = UserSessionLocal.getUser().getTenantId();
		interfaceManageDTO.setTenantId(tenantId);
		interfaceManageDTO.setUserCode(operator);
		//3.service层
		Page<InterfaceManage> interfaceManagePage = interfaceManageService.pageListByUser(interfaceManageDTO);
		//4.出参
		return CommonResult.buildSuccessResult(interfaceManagePage);
	}*/

    /**
     * 添加成员
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @PostMapping("/addMember")
    @Authorization(key = "id", parseType = ParseType.BODY)
    @ApiOperation(value = "添加接口成员")
    public CommonResult<Boolean> addMember(@RequestBody InterfaceManageDTO interfaceManageDTO) {
        log.info("InterfaceManageController addMember query={}", JsonUtils.toJSONString(interfaceManageDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        String tenantId = UserSessionLocal.getUser().getTenantId();
        interfaceManageDTO.setTenantId(tenantId);
        //3.service层
        Boolean result = interfaceManageService.addMember(interfaceManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 成员列表
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @GetMapping("/listMember")
    @ApiOperation(value = "list 查看成员")
    public CommonResult<Page<MemberRelationWithUser>> listMember(InterfaceManageDTO interfaceManageDTO) {
        log.info("InterfaceManageController listMember query={}", JsonUtils.toJSONString(interfaceManageDTO));
        //1.判空
        String tenantId = UserSessionLocal.getUser().getTenantId();
        interfaceManageDTO.setTenantId(tenantId);
        //3.service层
        Page<MemberRelationWithUser> result = interfaceManageService.listMember(interfaceManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 获取全部成员列表
     *
     * @param interfaceId 接口id
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @GetMapping("/listAllAppMember")
    @ApiOperation(value = "list 查看成员")
    public CommonResult<List<MemberRelationWithUser>> listAllAppMember(Long interfaceId) {
        //interfaceManageService.listAllAppMember()
        //4.出参
        return CommonResult.buildSuccessResult(interfaceManageService.listAllAppMember(interfaceId));
    }

    /**
     * 成员列表
     *
     * @param interfaceManageDTO
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @GetMapping("/listMemberForAdd")
    @ApiOperation(value = "接口下搜索成员")
    public CommonResult<List<UserForAddDTO>> listMemberForAdd(InterfaceManageDTO interfaceManageDTO) {
        log.info("InterfaceManageController listMember query={}", JsonUtils.toJSONString(interfaceManageDTO));
        //1.判空
        String tenantId = UserSessionLocal.getUser().getTenantId();
        interfaceManageDTO.setTenantId(tenantId);
        //3.service层
        List<UserForAddDTO> result = interfaceManageService.listMemberForAdd(interfaceManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 接口详情
     *
     * @param id
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    @GetMapping("/getOneById")
    @ApiOperation(value = "查询接口详情")
    public CommonResult<InterfaceManage> getOneById(Long id) {
        log.info("InterfaceManageController getOneById id={}", id);
        //3.service层
        InterfaceManage result = interfaceManageService.getOneById(id);
        result.init();
        //赋值接口上报时间
        if (result.getLatestDocVersion() != null) {
            InterfaceVersion interfaceVersion = interfaceVersionService.getInterfaceVersion(id, result.getLatestDocVersion());
            if (interfaceVersion != null) {
                result.setLatestReportTime(interfaceVersion.getModified());
            }
        }
        if(Objects.equals(InterfaceTypeEnum.JSF.getCode() ,result.getType())){
            result.setHasLicense(Objects.nonNull(result.getCjgAppId()));
        }
        String interfaceMarkDown = methodManageService.obtainInterfaceMarkDown(result.getServiceCode());
        result.setInterfaceText(interfaceMarkDown);
        AppInfoDTO appInfoDto = appInfoService.findApp(result.getAppId());
        if(Objects.nonNull(appInfoDto)){
            SimpleAppInfo simpleAppInfo = new SimpleAppInfo();
            simpleAppInfo.setDepartment(appInfoDto.getDepartment());
            UserDTO userDTO = obtainUserDTO(appInfoDto.getJdosOwner());
            simpleAppInfo.setJdosOwner(userDTO);
            simpleAppInfo.setJdosMembers(obtainErpList(appInfoDto.getJdosMembers()));
            simpleAppInfo.setProductor(obtainErpList(appInfoDto.getProductor()));
            result.setSimpleAppInfo(simpleAppInfo);
        }
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

    private List<UserDTO> obtainErpList(List<String> erpList) {
        List<UserDTO> retList = null;
        if(CollectionUtils.isNotEmpty(erpList)){
            retList = erpList.stream().map(erp -> {
                return obtainUserDTO(erp);
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        return retList;
    }

    private UserDTO obtainUserDTO(String erp) {
        UserDTO userDTO = null;
        UserInfo user = userInfoService.getUser(erp);
        if(Objects.nonNull(user)){
            userDTO = new UserDTO();
            userDTO.setErp(erp);
            userDTO.setRealName(user.getUserName());
        }
        return userDTO ;
    }

    @PostMapping("/copy")
    @ApiOperation(value = "复制保存服务")
    public CommonResult<Long> copy(@RequestBody @Valid InterfaceCopyDto dto) throws InvocationTargetException, IllegalAccessException {
        log.info("InterfaceManageController copy id={}", dto);
        //service层
        Long newId = interfaceManageService.copy(dto);
        //4.出参
        return CommonResult.buildSuccessResult(newId);
    }

    /**
     * 添加数据
     *
     * @param dto
     * @return
     */
    @PostMapping("/addDataSource")
    @ApiOperation(value = "添加ducc或者jimdb")
    public CommonResult<Long> addDataSource(@RequestBody @Valid DataSourceDto dto) {
        log.info("InterfaceManageController add id={}", dto);
        //service层
        Long newId = interfaceManageService.addDataSource(dto);
        //4.出参
        return CommonResult.buildSuccessResult(newId);
    }

    @PostMapping("/updateDataSource")
    @ApiOperation(value = "更新ducc或者jimdb")
    public CommonResult<Long> updateDataSource(@RequestBody @Valid DataSourceDto dto) {
        log.info("InterfaceManageController update id={}", dto);
        //service层
        Long newId = interfaceManageService.updateDatasource(dto);
        //4.出参
        return CommonResult.buildSuccessResult(newId);
    }

    @PostMapping("/updateDocConfig")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "修改接口描述")
    public CommonResult<InterfaceDocConfig> updateDocConfig(@RequestBody @Valid UpdateInterfaceConfigDto dto) {
        //4.出参
        return CommonResult.buildSuccessResult(interfaceManageService.updateDocConfig(dto));
    }

    @GetMapping("/statisticUserInterfaceCount")
    @ApiOperation(value = "统计用户接口数量")
    public CommonResult<UserInterfaceCountDto> statisticUserInterfaceCount(HttpServletRequest req) {
        log.info("interfaceManage.stat:params={}", JsonUtils.toJSONString(req.getParameterMap()));
        String pin = null;
        if (StringUtils.isEmpty(pin)) {
            String body = req.getParameter("body");
            if (StringUtils.isNotBlank(body)) {
                try {
                    Map map = JsonUtils.parse(body, Map.class);
                    pin = (String) map.get("pin");
                } catch (Exception e) {
                    log.error("interface.err_get_body", e);
                }
            }

        }
        if (StringUtils.isEmpty(pin)) {
            pin = req.getParameter("pin");
        }
        log.info("interfaceManage.getPin:pin={}", pin);
        UserInfoInSession user = UserSessionLocal.getUser();
        //if(user == null){
        user = new UserInfoInSession();
        user.setUserId(pin);
        UserSessionLocal.setUser(user);
        //}
        //4.出参
        return CommonResult.buildSuccessResult(interfaceManageService.statisticUserInterfaceCount());
    }

    @GetMapping("/statisticJsfAndHttpInterfaceCount")
    @ApiOperation(value = "统计jsf接口数量和http接口数量")
    public CommonResult<JsfAndHttpInterfaceCountDto> statisticJsfAndHttpInterfaceCount() {
        return CommonResult.buildSuccessResult(interfaceManageService.queryNumsByType());
    }


    @GetMapping("/getDefaultMdTemplate")
    @ApiOperation(value = "获取项目默认的文档模板")
    public CommonResult<String> getDefaultMdTemplate() {
        String desc = "### 项目说明\n\n> xxx项目是用于...\n\n### 接口统一说明\n\n> 1. 统一调用方式：\n> 2. 测试环境和开发环境说明：\n> 3. **其他**\n\n### 项目统一规范\n\n> * xxx\n> * xxx\n> * xxx\n\n### 统一状态码说明\n\n| 状态码 | 含义 | 说明 |\n| --- | --- | --- |\n| 1001 | 服务器内部错误 | 服务器发生内部逻辑错误 |\n| 1002 | 用户校验失败 | cookie过期 |";
        return CommonResult.buildSuccessResult(desc);
    }


    /**
     * 查询部门列表
     * 发布过接口个人&应用的部门
     *
     * @param query
     * @return
     */
    @PostMapping("/queryDeptList")
    public CommonResult<QueryDeptResultDTO> queryDept(@RequestBody QueryDeptReqDTO query) {
        log.info("InterfaceManageController queryDeptList requestBody={} ", JSON.toJSONString(query));
        return CommonResult.buildSuccessResult(interfaceManageService.queryDeptList(query));
    }




    @GetMapping("/esSearchDoc")
    public CommonResult<org.springframework.data.domain.Page<InterfaceManageDoc>> esSearchDoc(String search,int current,int pageSize) {
        log.info("InterfaceManageController esSearchDoc search={},current={},pageSize={} ",search,current,pageSize);
        return CommonResult.buildSuccessResult(esInterfaceService.searchInterface(search,InterfaceTypeEnum.HTTP.getCode(), current,pageSize));
    }

    @PostMapping("/createInterfaceDoc")
    public CommonResult<InterfaceManageDoc> createInterfaceDoc(@RequestBody InterfaceManageDoc doc) {
        InterfaceManageDoc result = esInterfaceService.indexMethodDoc(doc);
        return CommonResult.buildSuccessResult(result);
    }

    @GetMapping("/httpList")
    @ApiOperation(value = "获取http分组列表")
    public CommonResult<List<InterfaceManage>> pageList(String appCode) {
        log.info("InterfaceManageController pageList appId={}", appCode);
        AppInfo app = appInfoService.findApp(appCode);
        if(app == null){
            return CommonResult.buildSuccessResult(new ArrayList<>());
        }
        Long appId = app.getId();
        //1.判空
        //2.入参封装
        //		String operator=UserSessionLocal.getUser().getUserId();

        //3.service层
        List<InterfaceManage> interfaceManagePage = interfaceManageService.listHttpInterfaceByAppId(appId);

        //4.出参
        return CommonResult.buildSuccessResult(interfaceManagePage);
    }

    @GetMapping("/queryDocUrl")
    public CommonResult<JdosJsfDocInfo> queryDocUrl(String jdosAppCode, String jsfInterfaceName) {
        Guard.notEmpty(jdosAppCode,"应用code不可为空");
        Guard.notEmpty(jsfInterfaceName,"jsf接口名称不可为空");
        JdosJsfDocInfo docInfo = new JdosJsfDocInfo();

        AppInfo appInfo = appInfoService.findByJdosAppCode(jdosAppCode);
        if(appInfo == null){
            return CommonResult.buildSuccessResult(docInfo);
        }
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getServiceCode,jsfInterfaceName);
        lqw.eq(InterfaceManage::getType,InterfaceTypeEnum.JSF.getCode());
        lqw.eq(InterfaceManage::getYn,1);
        lqw.orderByDesc(InterfaceManage::getId);
        Page<InterfaceManage> page = interfaceManageService.page(new Page<>(1, 1), lqw);

        if(!page.getRecords().isEmpty()){
            Long id = page.getRecords().get(0).getId();
            docInfo.setId(id);
            List<MethodManage> methods = methodManageService.getInterfaceMethods(id);
            List<MethodIdAndName> idAndNames = methods.stream().map(
                    vs -> {
                        MethodIdAndName idAndName = new MethodIdAndName();
                        idAndName.setId(vs.getId());
                        idAndName.setName(vs.getMethodCode());
                        return idAndName;
                    }
            ).collect(Collectors.toList());
            docInfo.setMethods(idAndNames);
        }

        return CommonResult.buildSuccessResult(docInfo);

    }

    /**
     * https://joyspace.jd.com/pages/KXWR9Z5dDAzVOUtp1S1o
     * @return
     */
    @PostMapping("queryDemandInfo")
    @ApiOperation(value = "查询需求列表信息根据接口id")
    public CommonResult<IPage<DemandDetailDTO>> queryDemanInfo(Long current, Long pageSize, Long interfaceId) {
        log.info("InterfaceSpaceController queryDemanInfo query={}", JsonUtils.toJSONString(interfaceId));
        //1.判空
        Guard.notNull(interfaceId, "id不能为空");
        Page<DemandDetailDTO> data=methodManageService.getDemandByInterfaceId(interfaceId,current,pageSize);
        //4.出参
        return CommonResult.buildSuccessResult(data);
    }


    /**
     *
     * @param id 接口id/方法id
     * @param type 0接口 1方法
     * @param path 云文档路径
     * @param tags 关键字
     * @return 0 不需要审批 1 需要审批
     */
    @GetMapping("/saveCloudFile")
    @ApiOperation(value = "保存云文件")
    public CommonResult<Integer> updateCloudFile(Long id,Integer type,String path,String tags) {
        //接口 关联云文档
        if (InterfaceSimpleTypeEnum.Interface.getCode().equals(type)) {
            InterfaceManage interfaceManage = interfaceManageService.getOneById(id);
            Guard.notNull(interfaceManage, "当前id不存在接口信息");
            List<AppInfoMembers> info = new ArrayList<>();
            if (null != interfaceManage.getAppId()) {
                info = appInfoMembersService.getMemberByAppId(interfaceManage.getAppId());
            }

            List<String> erps=info.stream().map(AppInfoMembers::getErp).collect(Collectors.toList());
            Boolean noNeedApprove = erps.contains(UserSessionLocal.getUser().getUserId());
            ApproveCreateDTO manage= approveService.converManage(interfaceManage.getId(),interfaceManage.getServiceCode(),type,interfaceManage.getAppId(),interfaceManage.getAppCode(),path,noNeedApprove,tags);
            approveService.createApprove(manage);
            if (noNeedApprove) {
                interfaceManageService.updateCloudFile(id, path, tags);
            }
            return CommonResult.buildSuccessResult(noNeedApprove?0:1);
        } else if (InterfaceSimpleTypeEnum.method.getCode().equals(type)) {
            //方法 关联云文档
            MethodManageDTO dto = methodManageService.getEntityById(id);
            Guard.notNull(dto, "当前id不存在方法信息");
            InterfaceManage interfaceManage = interfaceManageService.getOneById(dto.getInterfaceId());
            List<AppInfoMembers> info = new ArrayList<>();
            if (null != interfaceManage.getAppId()) {
                info = appInfoMembersService.getMemberByAppId(interfaceManage.getAppId());
            }
            List<String> erps=info.stream().map(AppInfoMembers::getErp).collect(Collectors.toList());
            Boolean noNeedApprove = erps.contains(UserSessionLocal.getUser().getUserId());
            String name = interfaceManage.getServiceCode()+"#"+dto.getMethodCode();
            if (dto.getType() == 1) {
                name = dto.getPath();
            }
            ApproveCreateDTO manage = approveService.converManage(Long.valueOf(dto.getId()),name, type, interfaceManage.getAppId(), dto.getAppCode(), path, noNeedApprove, tags);
            approveService.createApprove(manage);
            if (noNeedApprove) {
                methodManageService.updateCloudFile(id, path, tags);
            }
            return CommonResult.buildSuccessResult(noNeedApprove?0:1);
        }
        //3.service层
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }

    /**
     * 删除jsf接口的鉴权
     * @param interfaceId
     * @return
     */
    @GetMapping("/removeJsfAuthById/{interfaceId}")
    public CommonResult<Boolean> removeJsfAuthById(@PathVariable(name = "interfaceId") Long interfaceId) {
        LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
        luw.eq(InterfaceManage::getId, interfaceId)
                .eq(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode())
                .set(InterfaceManage::getCjgAppId, null);
        return CommonResult.buildSuccessResult(interfaceManageService.update(luw));
    }

}
