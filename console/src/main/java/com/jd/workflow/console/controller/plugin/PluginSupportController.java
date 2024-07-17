package com.jd.workflow.console.controller.plugin;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.jd.common.web.LoginContext;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.*;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.dto.plugin.*;
import com.jd.workflow.console.dto.plugin.jdos.JdosApps;
import com.jd.workflow.console.dto.plugin.jdos.JdosGroup;
import com.jd.workflow.console.dto.plugin.jdos.JdosPod;
import com.jd.workflow.console.dto.plugin.jdos.JdosSystemApps;
import com.jd.workflow.console.dto.remote.CjgPage;
import com.jd.workflow.console.dto.remote.DemandInfo;
import com.jd.workflow.console.dto.requirement.DmMember;
import com.jd.workflow.console.dto.test.deeptest.TestResult;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.entity.plugin.dto.PluginStatisticDto;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.interceptor.ErpInfoInterceptor;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.console.service.ProxyDebuggerRegistryService;
import com.jd.workflow.console.service.ducc.entity.HotUpdateEnvironmentConf;
import com.jd.workflow.console.service.plugin.*;
import com.jd.workflow.console.service.plugin.jdos.EoneLaneResponse;
import com.jd.workflow.console.service.plugin.jdos.JdosAbstract;
import com.jd.workflow.console.service.plugin.jdos.JdosFactoryService;
import com.jd.workflow.console.service.plugin.log.LocalDeployLog;
import com.jd.workflow.console.service.plugin.log.SingleDeployInfo;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StdCalendar;
import com.jd.workflow.soap.common.util.StringHelper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * idea插件更新
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/plugin")
@UmpMonitor
public class PluginSupportController {
    @Autowired
    PluginUpdateService pluginUpdateService;

    @Autowired
    PluginLoginService pluginLoginService;
    @Autowired
    HotswapDeployInfoService hotswapDeployInfoService;

    @Autowired
    HotUpdateService hotUpdateService;

    @Resource
    JdosFactoryService jdosFactoryService;

    @Resource
    ErpInfoInterceptor erpInfoInterceptor;

    @Autowired
    ProxyDebuggerRegistryService proxyDebuggerRegistryService;
    @Autowired
    UserHelper userHelper;

    @Autowired
    ScheduledThreadPoolExecutor defaultScheduledExecutor;

    @Autowired
    IUserInfoService userInfoService;

    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    PluginStatisticService pluginStatisticService;

    @Autowired
    private PluginAgentService pluginAgentService;


    @RequestMapping("/getUserToken")
    public CommonResult<String> getUesrToken(String reqId) {
        log.info("login reqId:{}", reqId);
        String loginedUser = pluginLoginService.getUser(reqId);
        return CommonResult.buildSuccessResult(loginedUser);
    }

    @RequestMapping("/clearUserToken")
    public CommonResult<String> clearUserToken(String reqId) {
        log.info("login reqId:{}", reqId);
        pluginLoginService.clearUser(reqId);
        return CommonResult.buildSuccessResult(null);
    }

    @RequestMapping("getAppIdByAppCode")
    public CommonResult<Long> getAppIdByAppCode(String appCode) {
        AppInfo app = appInfoService.findApp(appCode);
        if (app != null) {
            return CommonResult.buildSuccessResult(app.getId());
        }
        return CommonResult.buildSuccessResult(null);
    }

    @RequestMapping("statistic")
    public CommonResult<Long> saveStatistic(@RequestBody PluginStatisticDto dto, HttpServletRequest request, HttpServletResponse response) {
        String remoteHost = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(remoteHost)) {
            remoteHost = request.getRemoteHost();
        }
        if (remoteHost.indexOf(",") != -1) {
            remoteHost = remoteHost.substring(0, remoteHost.indexOf(","));
        }
        dto.setRemoteIp(remoteHost);
        Long id = pluginStatisticService.saveStatistic(dto);
        return CommonResult.buildSuccessResult(id);
    }

    @RequestMapping("/getUserInfo")
    public CommonResult<PluginLoginService.UserBaseInfo> getUserInfo(String userToken) {
        PluginLoginService.UserBaseInfo loginedUser = PluginLoginService.getUserInfo(userToken);
        return CommonResult.buildSuccessResult(loginedUser);
    }

    @RequestMapping("/saveErrorLog")
    public CommonResult<Boolean> saveErrorLog(@RequestBody LocalDeployLog log) {
        Guard.notEmpty(log.getUserToken(), "无效的userToken");
        Guard.notEmpty(log.getLog(), "无效的log");
        PluginLoginService.UserBaseInfo loginedUser = PluginLoginService.getUserInfo(log.getUserToken());
        String fileName = loginedUser.getUserName().replace(".", "_") + StringHelper.formatDate(new Date(), "_HH_mm_ss");
        hotswapDeployInfoService.saveErrorLog(log.getLog(), fileName + ".txt");
        return CommonResult.buildSuccessResult(true);
    }

    @RequestMapping("/saveHtml")
    public CommonResult<Boolean> saveHtml() {
        String fileName = "xxx.html";
        String htmlData = "";
        hotswapDeployInfoService.saveErrorLog(htmlData, fileName);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @title 查询需求列表
     *
     * @param erp
     * @return
     */
    @RequestMapping("queryDemandList")
    public CommonResult<List<DemandInfo>> queryUserDemandList(String erp) {
        // http://cjg-dm.jd.com/api/jagile/aboutMyDemandNologin?erp=chenyufeng18&start=0&startTime=2023-12-29&endTime=2024-01-29&xyStatus=1&xyStatus=2&pageIndex=1&pageSize=10
        Guard.notEmpty(erp, "不可为空");

        Map<String,Object> headers = new HashMap<>();
        String token = new PluginLoginService().encrypt(erp);
        headers.put("CJG-PLUGIN-LOGIN",token);
        RequestClient requestClient = new RequestClient("http://cjg-api.jd.com", headers);

        Map<String, Object> params = new HashMap();
        Map<String, Object> body = new HashMap();
        /**
         *  DRAFT(1,"草稿"),
         *         WAIT_ACCEPTED(2,"等待受理"),
         *         ACCEPTED(3,"已受理"),
         *         REJECT(4,"已驳回"),
         *         DEV_PROCESS(5,"研发处理中"),
         *         VERIFICATION(6,"验证中"),
         *         VER_SUCCESS(7,"验证成功"),
         *         VER_FAILED(8,"验证失败"),
         *         WAIT_INSPECTED(9,"待验收"),
         *         INSPECTED_FAILED(10,"验收失败"),
         *         SPLIT(11,"拆分"),
         *         PROCESS(12,"处理中"),
         *         DEMAND_APPROVAL(18,"需求审批中"),
         *         INSPECTED_APPROVAL(19,"验收审批中"),
         *         FINISH(20,"完成"),
         *         CANCEL(21,"已取消"),
         *         DELETE(22,"删除");
         */

        Integer[] statusList = new Integer[]{
                1,2,3,4,5,6,7,8,9,10,11,12,18,19
        };

        body.put("statusList",statusList);
        body.put("pageSize",100);
        body.put("pageNum",1);

        StatusResult<CjgPage<DemandInfo>> result = requestClient.post("/api/idea/plugin/demandList", params,headers,body, new TypeReference<StatusResult<CjgPage<DemandInfo>>>() {
        });
        if (result.getData() != null && result.getData().getData() != null) {
            return CommonResult.buildSuccessResult(result.getData().getData());
        } else {
            DemandInfo demandInfo = new DemandInfo();
            demandInfo.setId(0L);
            demandInfo.setProjectName("6个月内没有卡片");
            return CommonResult.buildSuccessResult(Lists.newArrayList(demandInfo));
        }

    }

    @RequestMapping("/addDeployReq")
    public CommonResult<Long> addDeployReq(String userToken, @RequestBody HotDeployDto dto) {
        try {
            PluginLoginService.UserBaseInfo userInfo = PluginLoginService.getUserInfo(userToken);
        } catch (Exception e) {
            throw new BizException("统计失败", e);
        }


        Long result = hotswapDeployInfoService.addHotDeploy(dto);
        return CommonResult.buildSuccessResult(result);
    }

    @RequestMapping("queryEoneLaneList")
    public CommonResult<List<EoneLaneResponse>> queryEoneLaneList(String codeRepo, String codeBranch, String username, String jdosEnv) {
        PluginLoginService.UserBaseInfo loginedUser = PluginLoginService.getUserInfo(username);
        List<EoneLaneResponse> result = pluginUpdateService.queryEoneLaneList(codeRepo, codeBranch, loginedUser.getUserName(), jdosEnv);
        return CommonResult.buildSuccessResult(result);
    }

    @RequestMapping("getUserTokenByGptToken")
    public CommonResult<String> getUserTokenByGptToken(String gptToken, String userName, String reqId, String pluginVersion) {
        RequestClient requestClient = new RequestClient("http://jdhgpt.jd.com", null);
        Map<String, Object> params = new HashMap<>();
        params.put("uuid", reqId);
        params.put("pversion", pluginVersion);
        params.put("userName", userName);
        params.put("userToken", gptToken);
        params.put("sourceType", "cangjingge");
        String response = requestClient.post("/login/loginResultCheck", null, params);
        TestResult<JdhPluginLoginInfo> result = JsonUtils.parse(response, new TypeReference<TestResult<JdhPluginLoginInfo>>() {
        });

        UserInfo userInfo = new UserInfo();
        if (result.getData() == null) {
            return CommonResult.buildSuccessResult(null);
        }
        String erp = result.getData().getErp();


        return CommonResult.buildSuccessResult(pluginLoginService.encrypt(erp));
    }

    @RequestMapping("queryCreateUrl")
    public CommonResult<String> queryCreateUrl(String codeRepo, String codeBranch, String username, String jdosEnv) {
        PluginLoginService.UserBaseInfo loginedUser = PluginLoginService.getUserInfo(username);
        String result = pluginUpdateService.queryCreateUrl(codeRepo, codeBranch, loginedUser.getUserName(), jdosEnv);
        return CommonResult.buildSuccessResult(result);
    }

    @RequestMapping("queryUserInfos")
    public CommonResult<List<UserInfoDTO>> queryAppMembers(@RequestBody List<String> erps) {
        List<UserInfoDTO> userInfoDTOS = listUsers(erps);
        return CommonResult.buildSuccessResult(userInfoDTOS);
    }

    @RequestMapping("queryRequirementMembers")
    public CommonResult<List<UserInfoDTO>> queryRequirementMembers(String requirementCode) {
        // http://cjg-dm.jd.com/api/jagile/aboutMyDemandFlowFollowers?dmCode=R2024012566528

        List<DmMember> dmMembers = queryDemandMembers(requirementCode);
        if (dmMembers == null) {
            return CommonResult.buildSuccessResult(Collections.emptyList());
        }
        Set<String> members = dmMembers.stream().map(item -> item.getErp()).collect(Collectors.toSet());
        List<UserInfoDTO> userInfos = listUsers(members);
        return CommonResult.buildSuccessResult(userInfos);

    }

    private static List<DmMember> queryDemandMembers(String requirementCode) {
        RequestClient requestClient = new RequestClient("http://cjg-dm.jd.com", null);
        Map<String, Object> params = new HashMap<>();
        params.put("dmCode", requirementCode);
        StatusResult<List<DmMember>> result = requestClient.get("/api/jagile/aboutMyDemandFlowFollowers", params, new TypeReference<StatusResult<List<DmMember>>>() {
        });
        return result.getData();
    }

    public static void main(String[] args) {
        PluginSupportController controller = new PluginSupportController();
        long start = System.currentTimeMillis();
        CommonResult<List<DemandInfo>> result = controller.queryUserDemandList("chenyufeng18");
        System.out.printf("cost:%d", System.currentTimeMillis() - start);
    }

    @RequestMapping("queryAppMembers")
    public CommonResult<List<UserInfoDTO>> queryAppMembers(String appCode) {
        Guard.notEmpty(appCode, "应用成员不可为空");
        AppInfo appInfo = appInfoService.getOne(Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getAppCode, appCode).eq(AppInfo::getYn, DataYnEnum.VALID.getCode()));
        AppInfoDTO dto = new AppInfoDTO();
        if (appInfo != null) {
            List<String> erps = dto.splitMembers(appInfo.getMembers());
            List<UserInfoDTO> userInfoDTOS = listUsers(erps);
            return CommonResult.buildSuccessResult(userInfoDTOS);
        } else {
            return CommonResult.buildSuccessResult(Collections.emptyList());
        }
    }


    private List<UserInfoDTO> listUsers(Collection<String> erps) {
        List<UserInfoDTO> users = new ArrayList<>();
        Set<String> erpSet = erps.stream().collect(Collectors.toSet());
        List<Future> futures = new ArrayList<>();
        for (String s : erpSet) {
            Future<?> future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    UserVo userVo = userHelper.getUserBaseInfoByUserName(s);
                    if (userVo == null || userVo.getUserCode() == null) return;
                    UserInfoDTO dto = new UserInfoDTO();
                    dto.setUserCode(userVo.getUserName());
                    dto.setUserName(userVo.getRealName());
                    dto.setDept(userVo.getOrganizationFullName());
                    users.add(dto);
                }
            });
            futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("app.err_get_users:erps={}", erps, e);
            }
        }
        return users;
    }

    /**
     *
     */
    @RequestMapping("japiPluginXml")
    public void updateXmlForJapiPlugin(HttpServletRequest request, HttpServletResponse response) {
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setPlugins(pluginUpdateService.buildPluginConfig());
        String configXml = pluginConfig.toXml();
        response.setContentType("text/xml");
        try {
            IOUtils.write(configXml, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw StdException.adapt(e);
        }

    }

    @GetMapping("/listUserByCode")
    @ApiOperation(value = "List模糊搜索")
    public CommonResult<List<UserInfo>> listByCode(UserInfoDTO userInfoDTO) {
        log.info("UserInfoController listByCode query={}", JsonUtils.toJSONString(userInfoDTO));
        //1.判空
        //2.入参封装
        //		String operator="system";
        //3.service层
        List<UserInfo> userInfos = userInfoService.listByCode(userInfoDTO);
        //4.出参
        return CommonResult.buildSuccessResult(userInfos);
    }

    @RequestMapping("downloadApiPlugin/{id}")
    public void downloadPlugin(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
        String remoteHost = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(remoteHost)) {
            remoteHost = request.getRemoteHost();
        }
        try {
            LoginContext loginContext = UpLoginContextHelper.getLoginContext();
            if (loginContext != null) {
                UserInfoInSession userInfoInSession = new UserInfoInSession();
                userInfoInSession.setUserId(loginContext.getPin());
                userInfoInSession.setUserName(loginContext.getNick());
                userInfoInSession.setDept(EmptyUtil.isNotEmpty(loginContext.getOrgName()) ? loginContext.getOrgName() : "");

                UserSessionLocal.setUser(userInfoInSession);
                erpInfoInterceptor.checkAndAddUser(userInfoInSession);
            } else {
                UserSessionLocal.setUser(new UserInfoInSession());
            }


        } catch (Exception e) {
            UserSessionLocal.setUser(new UserInfoInSession());
        }

        pluginUpdateService.download(id, remoteHost, response);

    }

    @RequestMapping(value = "/deployRemote", method = RequestMethod.POST)
    public CommonResult<String> deployRemote(@RequestBody HotDeployDto hotDeployDto) {
        return hotswapDeployInfoService.remoteHotDeploy(hotDeployDto);

    }

    @RequestMapping(value = "/deployLocal", method = RequestMethod.POST)
    public CommonResult<String> deployLocal(@RequestBody HotDeployDto hotDeployDto) {
        return hotswapDeployInfoService.localHotDeploy(hotDeployDto);

    }

    @RequestMapping(value = "/queryLog", method = RequestMethod.GET)
    public CommonResult<SingleDeployInfo> queryLog(String envCode, String remoteIp, String reqId) {
        Guard.notEmpty(reqId, "reqId不能为空");
        return hotswapDeployInfoService.queryLog(envCode, remoteIp, reqId);
    }

    @RequestMapping(value = "/queryLocalLog", method = RequestMethod.GET)
    public CommonResult<SingleDeployInfo> queryLocalLog(String envCode, String remoteIp, String reqId) {
        Guard.notEmpty(reqId, "reqId不能为空");
        return CommonResult.buildSuccessResult(hotswapDeployInfoService.queryLocalLog(envCode, remoteIp, reqId));
    }

    @RequestMapping("getEnvironmentInfo")
    @ResponseBody
    public List<HotUpdateEnvironmentConf> getEnvironmentInfo() {
        return hotUpdateService.getEnvironmentList();
    }


    /**
     * @param erp
     * @return
     */
    @RequestMapping(value = "getSystemList", method = RequestMethod.GET)
    @ResponseBody
    public List<SystemDto> getSystemList(String erp, String env) {
        JdosAbstract service = jdosFactoryService.getService(env);
        List<SystemDto> systemList = Lists.newArrayList();
        List<JdosSystemApps> systemApps = service.getSystemApps(erp);
        for (JdosSystemApps systemApp : systemApps) {
            SystemDto systemDto = new SystemDto();
            systemDto.setSystemCode(systemApp.getSystemName());
            systemDto.setSystemName(String.format("%s(%s)", systemApp.getNickname(), systemApp.getSystemName()));
            systemDto.setErp(erp);
            systemDto.setTenant(systemApp.getTenant());
            systemDto.setEnv(env);
            if (!containsValue(systemList, "systemCode", systemDto.getSystemCode())) {
                systemList.add(systemDto);
            }
        }
        return systemList;
    }

    @RequestMapping("getAllApps")
    public CommonResult<List<JdosAndJapiApp>> getAllApps(String erp, String search) {
        List<JdosAndJapiApp> userApps = hotUpdateService.getUserApp(erp, search);
        return CommonResult.buildSuccessResult(userApps);
    }

    /**
     * 获取全部应用，共有1000条
     *
     * @return
     */
    @RequestMapping("getUserAllApp")
    public CommonResult<List<JdosAndJapiApp>> getUserAllApp(String userToken) {
        PluginLoginService.UserBaseInfo loginedUser = PluginLoginService.getUserInfo(userToken);
        List<JdosAndJapiApp> userApps = hotUpdateService.getUserAllApp(loginedUser.getUserName());
        return CommonResult.buildSuccessResult(userApps);
    }

    @RequestMapping("getJdosApps")
    public CommonResult<List<JdosAndJapiApp>> getJdosApps(String erp, String search) {
        List<JdosAndJapiApp> userApps = hotUpdateService.getJdosApps(erp, search);
        return CommonResult.buildSuccessResult(userApps);
    }

    @RequestMapping(value = "getAppList", method = RequestMethod.POST)
    @ResponseBody
    public List<AppDto> getAppList(@RequestBody SystemDto systemDto) {
        JdosAbstract service = jdosFactoryService.getService(systemDto.getEnv());
        List<AppDto> appDtoList = Lists.newArrayList();

        List<JdosSystemApps> systemApps = service.getSystemApps(systemDto.getErp());
        for (JdosSystemApps systemApp : systemApps) {
            if (systemDto.getSystemCode().equals(systemApp.getSystemName())) {
                List<JdosApps> apps = systemApp.getApps();
                for (JdosApps app : apps) {
                    AppDto appDto = new AppDto();
                    BeanUtils.copyProperties(systemDto, appDto);
                    appDto.setAppCode(app.getAppName());
                    appDto.setAppName(String.format("%s(%s)", app.getNickname(), app.getAppName()));
                    if (!containsValue(appDtoList, "appCode", appDto.getAppCode())) {
                        appDtoList.add(appDto);
                    }
                }
            }
        }
        return appDtoList;
    }

    @RequestMapping(value = "getGroupList", method = RequestMethod.POST)
    @ResponseBody
    public List<GroupDto> getGroupList(@RequestBody AppDto appDto) {
        JdosAbstract service = jdosFactoryService.getService(appDto.getEnv());
        List<GroupDto> groupDtoList = Lists.newArrayList();
        List<JdosGroup> groups = service.getGroups(appDto.getAppCode());
        for (JdosGroup group : groups) {
            GroupDto groupDto = new GroupDto();
            BeanUtils.copyProperties(appDto, groupDto);
            groupDto.setGroupCode(group.getGroupName());
            groupDto.setGroupName(group.getNickname());
            if (!containsValue(groupDtoList, "groupCode", groupDto.getGroupCode())) {
                groupDtoList.add(groupDto);
            }
        }
        return groupDtoList;
    }

    @RequestMapping(value = "getContainerList", method = RequestMethod.POST)
    @ResponseBody
    public List<ContainerDto> getContainerList(@RequestBody GroupDto groupDto) {
        JdosAbstract service = jdosFactoryService.getService(groupDto.getEnv());
        List<ContainerDto> containerDtoList = Lists.newArrayList();
        List<JdosPod> ips = service.getIps(groupDto.getAppCode(), groupDto.getGroupCode());
        for (JdosPod pod : ips) {
            ContainerDto containerDto = new ContainerDto();
            BeanUtils.copyProperties(groupDto, containerDto);
            containerDto.setIp(pod.getPodIp());
            containerDto.setImage(pod.getImage());
            containerDto.setHash(getHash(pod.getImage()));
            if (!containsValue(containerDtoList, "ip", containerDto.getIp())) {
                containerDtoList.add(containerDto);
            }
        }
        return containerDtoList;
    }

    @RequestMapping(value = "queryLatestAgentFileVersion")
    @ResponseBody
    public CommonResult<Long> queryLatestAgentFileVersion() {
        Long latestAgentFileVersion = pluginUpdateService.getLatestAgentFileVersion();
        return CommonResult.buildSuccessResult(latestAgentFileVersion);
    }

    @RequestMapping(value = "downloadLatestAgentFile")
    @ResponseBody
    public void downloadLatestAgentFile(HttpServletRequest request, HttpServletResponse response) {
        String remoteHost = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(remoteHost)) {
            remoteHost = request.getRemoteHost();
        }
        pluginUpdateService.downloadAgentFile(remoteHost, response);

    }

    /**
     * @param objs
     * @param fieldName
     * @return
     */
    boolean containsValue(List<? extends Object> objs, String fieldName, String value) {
        List<JSONObject> collect = objs.stream().map(item -> JSON.parseObject(JSON.toJSONString(item))).collect(Collectors.toList());
        List<String> result = collect.stream().map(item -> item.getString(fieldName)).collect(Collectors.toList());
        return result.contains(value);
    }


    @RequestMapping(value = "removeProxy", method = RequestMethod.GET)
    @ResponseBody
    public String removeProxy(Long id) {
        proxyDebuggerRegistryService.removeProxy(id);
        return "success";
    }

    private String getHash(String image) {
        if (StringUtils.isEmpty(image)) {
            return "";
        }
        try {
            String hash = image.split(":")[1].split("-")[1];
            return hash;
        } catch (Exception ex) {
            log.error("getHash:" + image, ex);
        }
        return "";
    }

    @RequestMapping(value = "getAgent", method = RequestMethod.GET)
    @ResponseBody
    public RedirectView getAgent(@RequestParam(required = false) String appCode, @RequestParam(required = false) String ip) {
        RedirectView redirectView = new RedirectView();
        String url = pluginAgentService.getAgentJarUrl(appCode, ip);
        redirectView.setUrl(url);
        return redirectView;
    }

    @RequestMapping(value = "clearAppVersion", method = RequestMethod.GET)
    @ResponseBody
    public boolean clearAppVersion(String appCode) {
        return pluginAgentService.clearAppVersion(appCode);
    }

    /**
     * 所有旧版本更新到指定版本
     *
     * @param oldVersion 旧版本
     * @param newVersion 新版本
     * @return
     */
    @RequestMapping(value = "updateOldVersion", method = RequestMethod.GET)
    @ResponseBody
    public Integer updateOldVersion(String oldVersion, String newVersion) {
        return pluginAgentService.updateOldVersion(oldVersion, newVersion);

    }

    @RequestMapping(value = "updateAppVersion", method = RequestMethod.GET)
    @ResponseBody
    public boolean updateAppVersion(String appCode, String newVersion) {
        return pluginAgentService.updateAppVersion(appCode, newVersion);
    }

    @RequestMapping(value = "getAppVersion", method = RequestMethod.GET)
    @ResponseBody
    public String getAppVersion(String appCode) {
        return pluginAgentService.getAppVersion(appCode);
    }

    @RequestMapping(value = "getVersionMap", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getVersionMap() {
        return pluginAgentService.getVersionMap();
    }

    @RequestMapping(value = "clearVersionCount", method = RequestMethod.GET)
    @ResponseBody
    public boolean clearVersionCount(String version) {
        return pluginAgentService.clearVersionCount(version);
    }

    @RequestMapping(value = "getAppMap", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getAppMap() {
        return pluginAgentService.getAppMap();
    }

    @RequestMapping(value = "getAppIps", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> getAppIps(String appCode) {
        return pluginAgentService.getAppIps(appCode);
    }

    /**
     * 获取当日的应用使用版本统计
     *
     * @return
     */
    @RequestMapping(value = "getTodayAppMap", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getTodayAppMap() {
        return pluginAgentService.getTodayAppMap();
    }

    /**
     * 所有发布到新版本
     *
     * @param newVersion
     * @return
     */
    @RequestMapping(value = "publishNewVersion", method = RequestMethod.GET)
    @ResponseBody
    public Integer publishNewVersion(String newVersion) {
        return pluginAgentService.publishNewVersion(newVersion);
    }


}
