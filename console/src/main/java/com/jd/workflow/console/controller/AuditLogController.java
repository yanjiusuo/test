package com.jd.workflow.console.controller;

import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
 
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.EasyDataPageResult;
import com.jd.workflow.console.base.EasyDataResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.dashboard.DashBoardInfoManageMapper;
import com.jd.workflow.console.dto.EasyDataParamDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.dashboard.DashBoardInfoDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.dashboard.DashBoardInfo;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Author yuanshuaiming
 * @Date 2022/12/15 11:08 上午
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/console-log")
@Api(tags = "审计日志")
public class AuditLogController {
    private static Integer maxPageSize = 1000;
    @Resource
    IInterfaceManageService interfaceManageService;
    @Resource
    IMethodManageService methodManageService;
    @Resource
    DashBoardInfoManageMapper dashBoardInfoManageMapper;
    @Value("${EasyData.appToken}")
    private String appToken;
    @Value("${EasyData.apiGroupName}")
    private String apiGroupName;
    @Value("${EasyData.url}")
    private String url;
    @Value("${EasyData.pageUrl}")
    private String pageUrl;
    @Value("${cjgApiUrl:http://cjg-api.jd.com}")
    private String cjgApiUrl;

    private static Long getLong(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return ((Double) obj).longValue();
        }
    }

    private static Integer getInteger(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return ((Double) obj).intValue();
        }
    }

    /**
     * 通用EasyData数据请求参数
     *
     * @param apiName 接口名称，如 getInvokeCountByMinute
     * @return
     */
    public EasyDataParamDTO getEasyDataParamDTO(String apiName) {
        EasyDataParamDTO easyDataParamDTO = new EasyDataParamDTO();
        easyDataParamDTO.setParams(new HashMap<>());
        easyDataParamDTO.setStringSubs(new HashMap<>());
        easyDataParamDTO.setRequestId(UUID.randomUUID().toString());
        easyDataParamDTO.setAppToken(appToken);
        easyDataParamDTO.setApiGroupName(apiGroupName);
        easyDataParamDTO.setApiName(apiName);
        return easyDataParamDTO;
    }

    /**
     * 日志列表接口
     *
     * @param methodId   方法ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param cjgAppName 藏经阁客户端AppName
     * @param cjgAlias   客户端别名
     * @param isSuccess  执行结果是否成功，true、false
     * @param current    当前页，默认1
     * @param size       分页大小，默认10
     * @param type       0 jsf审计日志，1 http审计日志，可选
     * @return
     */
    @GetMapping("/getAuditLogList")
    public CommonResult<Page<Object>> getAuditLogList(Long methodId, String startTime, String endTime, String cjgAppName, String cjgAlias, String isSuccess, String type, String providerAlias,
                                                      @RequestParam(required = false, defaultValue = "1") int current,
                                                      @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("getAuditLogList methodId={}, startTime={}, endTime={}, cjgAppName={}, cjgAlias={}, isSuccess={}, type={}, current={}, size={}, providerAlias={}", methodId, startTime, endTime, cjgAppName, cjgAlias, isSuccess, type, current, size, providerAlias);
//        String operator = UserSessionLocal.getUser().getUserId();

        MethodManageDTO methodManageDTO = methodManageService.getEntityById(methodId);
        log.info(String.format("methodManageDTO:%s", JsonUtils.toJSONString(methodManageDTO)));

        EasyDataParamDTO easyDataParamDTO = getEasyDataParamDTO("getAuditLogList");
        if (methodManageDTO.getType() == 1) { //http
            String interfaceId = methodManageDTO.getPath();
            easyDataParamDTO.getStringSubs().put("interfaceId", interfaceId);
        } else if (methodManageDTO.getType() == 3) { //jsf
            Long intId = methodManageDTO.getInterfaceId();
            InterfaceManage interfaceManage = interfaceManageService.getOneById(intId);
            log.info(String.format("interfaceManage:%s", JsonUtils.toJSONString(interfaceManage)));
            String interfaceId = interfaceManage.getServiceCode();
            String methodName = methodManageDTO.getMethodCode();
            easyDataParamDTO.getStringSubs().put("interfaceId", interfaceId);
            easyDataParamDTO.getStringSubs().put("methodName", methodName);
        }

        easyDataParamDTO.setPageNumber(current - 1);
        easyDataParamDTO.setPageSize(size);

        if (StringUtils.isNotBlank(startTime)) {
            easyDataParamDTO.getStringSubs().put("startTime", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            easyDataParamDTO.getStringSubs().put("endTime", endTime);
        }

        if (StringUtils.isNotBlank(cjgAppName)) {
            easyDataParamDTO.getStringSubs().put("cjgAppName", cjgAppName);
        }
        if (StringUtils.isNotBlank(cjgAppName)) {
            easyDataParamDTO.getStringSubs().put("cjgAlias", cjgAlias);
        }
        if (StringUtils.isNotBlank(isSuccess)) {
            easyDataParamDTO.getStringSubs().put("isSuccess", isSuccess);
        }
        if (StringUtils.isNotBlank(type)) {
            easyDataParamDTO.getStringSubs().put("type", type);
        }
        if (StringUtils.isNotBlank(providerAlias)) {
            easyDataParamDTO.getStringSubs().put("providerAlias", providerAlias);
        }
        String result2 = HttpRequest.post(pageUrl).header("Content-Type", "application/json;charset=UTF-8")
                .body(JsonUtils.toJSONString(easyDataParamDTO))
                .execute().body();
        log.info("{} params:{}, result:{}", "getAuditLogList", easyDataParamDTO, result2);
        EasyDataPageResult easyDataResult;
        if (StringUtils.isBlank(result2) || result2.startsWith("<html>")) {
            easyDataResult = new EasyDataPageResult(1, result2);
        } else {
            easyDataResult = JsonUtils.parse(result2, EasyDataPageResult.class);
        }
        CommonResult commonResult;
        if (easyDataResult.getStatus() == 200) {
            Page<Object> result = new PageDTO<>(current, size, easyDataResult.getTotalElements());
            result.setRecords(easyDataResult.getContent());
            commonResult = CommonResult.buildSuccessResult(result);
        } else {
            commonResult = CommonResult.buildErrorCodeMsg(easyDataResult.getStatus(), easyDataResult.getMessage());
        }
        //4.出参
        return commonResult;
    }

    /**
     * 日志详情接口
     *
     * @param interfaceId 接口
     * @param methodName  方法
     * @param cjgAppName  客户端访问的cjgAppName
     * @param cjgAlias    客户端别名
     * @param dateStr     日期字符串，精确都毫秒 2022-12-09 16:22:37.867
     * @param clientIp    客户端ip
     * @param threadName  线程名
     * @return
     */
    @GetMapping("/getLogDetail")
    public CommonResult getLogDetail(String interfaceId, String methodName, String cjgAppName, String cjgAlias, String dateStr, String clientIp, String threadName, String type) {
        log.info("getLogDetail interfaceId={}, methodName={}, cjgAppName={}, cjgAlias={}, dateStr={}, clientIp={}, threadName={}, type={}", interfaceId, methodName, cjgAppName, cjgAlias, dateStr, clientIp, threadName, type);
        EasyDataParamDTO easyDataParamDTO = getEasyDataParamDTO("getLogDetail");
        easyDataParamDTO.getStringSubs().put("interfaceId", interfaceId);
        if (StringUtils.isNotBlank(methodName)) {
            easyDataParamDTO.getStringSubs().put("methodName", methodName);
        }
        easyDataParamDTO.getStringSubs().put("dateStr", dateStr);
        easyDataParamDTO.getStringSubs().put("clientIp", clientIp);
        easyDataParamDTO.getStringSubs().put("cjgAppName", cjgAppName);
        easyDataParamDTO.getStringSubs().put("cjgAlias", cjgAlias);
        easyDataParamDTO.getStringSubs().put("threadName", threadName);
        if (StringUtils.isNotBlank(type)) {
            easyDataParamDTO.getStringSubs().put("type", type);
        }
        String result2 = HttpRequest.post(url).header("Content-Type", "application/json;charset=UTF-8")
                .body(JsonUtils.toJSONString(easyDataParamDTO))
                .execute().body();
        log.info("{} params:{}, result:{}", "getLogDetail", easyDataParamDTO, result2);
        EasyDataResult easyDataResult;
        if (StringUtils.isBlank(result2) || result2.startsWith("<html>")) {
            easyDataResult = new EasyDataResult(1, result2);
        } else {
            easyDataResult = JsonUtils.parse(result2, EasyDataResult.class);
        }
        CommonResult commonResult;
        if (easyDataResult.getStatus() == 200) {
            commonResult = CommonResult.buildSuccessResult(easyDataResult.getResult());
        } else {
            commonResult = CommonResult.buildErrorCodeMsg(easyDataResult.getStatus(), easyDataResult.getMessage());
        }
        //4.出参
        return commonResult;
    }

    /**
     * 应用列表接口
     *
     * @param methodId 方法id
     * @param cjgAlias 客户端别名，可选
     * @param type     0 jsf审计日志，1 http审计日志，可选
     * @return
     */
    @GetMapping("/getClientAppNameList")
    public CommonResult getClientAppNameList(Long methodId, String cjgAlias, String type) {
        return getCommonInvokeCountSubs(methodId, null, null, null, cjgAlias, "getClientAppNameList", type);
    }

    /**
     * 分钟、小时、天粒度 客户端应用调用次数分布查询
     *
     * @param methodId     方法id
     * @param startTime    开始时间 2022-12-09 16:22:00
     * @param endTime      结束时间 2022-12-13 10:00:00
     * @param cjgAppName   客户端注册的藏经阁应用名称
     * @param cjgAlias     客户端别名
     * @param timeInterval 时间粒度
     * @return
     */
    @GetMapping("/getInvokeCounts")
    public CommonResult getInvokeCounts(Long methodId, String startTime, String endTime, String cjgAppName, String cjgAlias, String timeInterval, String type) {
        if (StringUtils.isBlank(startTime)) {
            return CommonResult.buildErrorCodeMsg(1, "startTime cannot be blank!");
        }
        if (StringUtils.isBlank(endTime)) {
            return CommonResult.buildErrorCodeMsg(1, "endTime cannot be blank!");
        }
        switch (timeInterval.toLowerCase()) {
            case "hour":
                return getCommonInvokeCountSubs(methodId, startTime, endTime, cjgAppName, cjgAlias, "getInvokeCountByHour", type);
            case "minute":
                return getCommonInvokeCountSubs(methodId, startTime, endTime, cjgAppName, cjgAlias, "getInvokeCountByMinute", type);
            case "day":
                return getCommonInvokeCountSubs(methodId, startTime, endTime, cjgAppName, cjgAlias, "getInvokeCountByDay", type);
            default:
                return new CommonResult(1, "Invalid timeInterVal!");
        }
    }

    //分钟接口
    @GetMapping("/getInvokeCountByMinute")
    public CommonResult getInvokeCountByMinute(Long methodId, String startTime, String endTime, String cjgAppName, String cjgAlias, String type) {
        if (StringUtils.isBlank(startTime)) {
            return CommonResult.buildErrorCodeMsg(1, "startTime cannot be blank!");
        }
        if (StringUtils.isBlank(endTime)) {
            return CommonResult.buildErrorCodeMsg(1, "endTime cannot be blank!");
        }
        return getCommonInvokeCountSubs(methodId, startTime, endTime, cjgAppName, cjgAlias, "getInvokeCountByMinute", type);
    }

    //小时接口
    @GetMapping("/getInvokeCountByHour")
    public CommonResult getInvokeCountByHour(Long methodId, String startTime, String endTime, String cjgAppName, String cjgAlias, String type) {
        if (StringUtils.isBlank(startTime)) {
            return CommonResult.buildErrorCodeMsg(1, "startTime cannot be blank!");
        }
        if (StringUtils.isBlank(endTime)) {
            return CommonResult.buildErrorCodeMsg(1, "endTime cannot be blank!");
        }
        return getCommonInvokeCountSubs(methodId, startTime, endTime, cjgAppName, cjgAlias, "getInvokeCountByHour", type);
    }

    //每天接口
    @GetMapping("/getInvokeCountByDay")
    public CommonResult getInvokeCountByDay(Long methodId, String startTime, String endTime, String cjgAppName, String cjgAlias, String type) {
        if (StringUtils.isBlank(startTime)) {
            return CommonResult.buildErrorCodeMsg(1, "startTime cannot be blank!");
        }
        if (StringUtils.isBlank(endTime)) {
            return CommonResult.buildErrorCodeMsg(1, "endTime cannot be blank!");
        }
        return getCommonInvokeCountSubs(methodId, startTime, endTime, cjgAppName, cjgAlias, "getInvokeCountByDay", type);
    }

    public CommonResult getCommonInvokeCountSubs(Long methodId, String startTime, String endTime, String cjgAppName, String cjgAlias, String apiName, String type) {
        log.info("{} methodId={}, startTime={}, endTime={}, cjgAppName={}, cjgAlias={}, type={}", apiName, methodId, startTime, endTime, cjgAppName, cjgAlias, type);
        MethodManageDTO methodManageDTO = methodManageService.getEntityById(methodId);
        Long intId = methodManageDTO.getInterfaceId();
        EasyDataParamDTO easyDataParamDTO = getEasyDataParamDTO(apiName);
        if (methodManageDTO.getType() == 1) { //http
            String interfaceId = methodManageDTO.getPath();
            easyDataParamDTO.getStringSubs().put("interfaceId", interfaceId);
        } else if (methodManageDTO.getType() == 3) { //jsf
            InterfaceManage interfaceManage = interfaceManageService.getOneById(intId);
            log.info(String.format("interfaceManage:%s", JsonUtils.toJSONString(interfaceManage)));
            String interfaceId = interfaceManage.getServiceCode();
            String methodName = methodManageDTO.getMethodCode();
            easyDataParamDTO.getStringSubs().put("interfaceId", interfaceId);
            easyDataParamDTO.getStringSubs().put("methodName", methodName);
        }

        if (StringUtils.isNotBlank(startTime)) {
            easyDataParamDTO.getStringSubs().put("startTime", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            easyDataParamDTO.getStringSubs().put("endTime", endTime);
        }

        if (StringUtils.isNotBlank(cjgAppName)) {
            easyDataParamDTO.getStringSubs().put("cjgAppName", cjgAppName);
        }
        if (StringUtils.isNotBlank(cjgAlias)) {
            easyDataParamDTO.getStringSubs().put("cjgAlias", cjgAlias);
        }
        if (StringUtils.isNotBlank(type)) {
            easyDataParamDTO.getStringSubs().put("type", type);
        }
        String result2 = HttpRequest.post(url).header("Content-Type", "application/json;charset=UTF-8")
                .body(JsonUtils.toJSONString(easyDataParamDTO))
                .execute().body();

        log.info("{} params:{}, result:{}", apiName, easyDataParamDTO, result2);
        EasyDataResult easyDataResult;
        if (StringUtils.isBlank(result2) || result2.startsWith("<html>")) {
            easyDataResult = new EasyDataResult(1, result2);
        } else {
            easyDataResult = JsonUtils.parse(result2, EasyDataResult.class);
        }
        CommonResult commonResult;
        if (easyDataResult.getStatus() == 200) {
            commonResult = CommonResult.buildSuccessResult(easyDataResult.getResult());
        } else {
            commonResult = CommonResult.buildErrorCodeMsg(easyDataResult.getStatus(), easyDataResult.getMessage());
        }
        //4.出参
        return commonResult;
    }

    /**
     * 看板接口
     *
     * @param appName appName模糊检索
     * @return
     */
    @GetMapping("/getDashBoardQueryCountData")
    public CommonResult getDashBoardQueryCountData(String appName, @CookieValue(name = "sso.jd.com") String cookie) {
        // 结果数据
        Map<String, DashBoardInfoDto> rstMap = new HashMap<>();
        String userPin = UserSessionLocal.getUser().getUserId(); //当前登陆用户
        log.info("appName={}, userPin={}", appName, userPin);
        if (StringUtils.isBlank(userPin)) {
            return CommonResult.buildErrorCodeMsg(9999, "Invalid login user!");
        }

        // 查询data-flow数据库，查有负责人或者相关研发权限的应用，将数据填充到结果中 // 若通过藏经阁接口获取所有应用列表，包含已授权的 cjgAppCodes = getCjgAppCode(cookie,pageSize);
        List<DashBoardInfo> rst = dashBoardInfoManageMapper.queryAppDashMethodCountInfoList(userPin, appName);
        log.info("getDashBoardQueryCountData appName={}, userPin={}, rst={}", appName, userPin, JsonUtils.toJSONString(rst));
        DashBoardInfoDto dto = null;
        for (DashBoardInfo dashBoardInfo : rst) {
            dto = new DashBoardInfoDto();
            dto.setAppCode(dashBoardInfo.getAppCode());
            dto.setAppName(dashBoardInfo.getAppName());
            dto.setInterfaceTotalCount(dashBoardInfo.getInterfaceTotalCount());
            dto.setAppId(dashBoardInfo.getAppId());
            rstMap.put(dashBoardInfo.getAppCode(), dto);
        }
        // 无应用权限直接返回
        if (rstMap.size() <= 0) {
            return CommonResult.buildSuccessResult(rstMap.values());
        }

        // 查询并组装cjgAppId
        Map<String, Integer> cjgAppMap = getCjgAppCode(cookie);
        for (String s : cjgAppMap.keySet()) {
            if (rstMap.containsKey(s)) {
                rstMap.get(s).setCjgAppId(cjgAppMap.get(s));
            }
        }

        // 查询近两日调用统计数据，组装到结果集
        EasyDataParamDTO easyDataParamDTO = getEasyDataParamDTO("getDashBoardQueryCountData");
        if (StringUtils.isNotBlank(appName)) {
            easyDataParamDTO.getStringSubs().put("appName", appName);
        }
        easyDataParamDTO.getStringSubs().put("appNameTest", "test"); //默认参数，easydata默认必须至少指定一个参数

        String result2 = HttpRequest.post(url).header("Content-Type", "application/json;charset=UTF-8")
                .body(JsonUtils.toJSONString(easyDataParamDTO))
                .execute().body();
        log.info("{} params:{}, result:{}", "getDashBoardQueryCountData", easyDataParamDTO, result2);
        EasyDataResult easyDataResult;
        if (StringUtils.isBlank(result2) || result2.startsWith("<html>")) {
            easyDataResult = new EasyDataResult(1, result2);
        } else {
            easyDataResult = JsonUtils.parse(result2, EasyDataResult.class);
        }
        CommonResult commonResult;
        if (easyDataResult.getStatus() == 200) {
            // 转换拼装到rstMap中
            if (easyDataResult.getResult() != null) {
                List<Map<String, Object>> tmpMapLst = (List<Map<String, Object>>) easyDataResult.getResult();
                for (Map<String, Object> stringObjectMap : tmpMapLst) {
                    DashBoardInfoDto tmp = rstMap.get(stringObjectMap.get("appCode"));
                    if (tmp == null) {
                        continue;
                    }
                    if (stringObjectMap.get("logDay").equals("today")) {
                        tmp.getToday().setSuccessRequest(getLong(stringObjectMap.get("successRequest")));
                        tmp.getToday().setTotalRequest(getLong(stringObjectMap.get("totalRequest")));
                        tmp.getToday().setAvailableRate(getInteger(stringObjectMap.get("availableRate")));
                        tmp.getToday().setTimeDay(String.valueOf(stringObjectMap.get("timeDay")));
                    } else if (stringObjectMap.get("logDay").equals("yesterday")) {
                        tmp.getYesterday().setSuccessRequest(getLong(stringObjectMap.get("successRequest")));
                        tmp.getYesterday().setTotalRequest(getLong(stringObjectMap.get("totalRequest")));
                        tmp.getYesterday().setAvailableRate(getInteger(stringObjectMap.get("availableRate")));
                        tmp.getYesterday().setTimeDay(String.valueOf(stringObjectMap.get("timeDay")));
                    }
                }
            }
            commonResult = CommonResult.buildSuccessResult(rstMap.values());
        } else {
            commonResult = CommonResult.buildErrorCodeMsg(easyDataResult.getStatus(), easyDataResult.getMessage());
        }
        return commonResult;
    }

    private Map<String, Integer> getCjgAppCode(String ssoCookie) {
//        String cjgAppUrl = "http://test.cjg-api.jd.com";
//        String cjgAppUrl = "http://cjg-api.jd.com";
        Map<String, Object> headers = new HashMap<>();
        headers.put("Cookie", "sso.jd.com=" + ssoCookie);
//         headers.put("Cookie","sso.jd.com=BJ.A15270A5AAD40AF93C379912419C14322420230714095728");
        RequestClient client = new RequestClient(cjgApiUrl, headers);
        final String result = client.get("/api/component/searchCurrentApp?start=0&pageSize=" + maxPageSize, null); //&source=
        log.info("app.fetch_cjg_app_result={}", result);
        return fetchCjgAppCodeNamesByQueryResult(result);
    }

    private Map<String, Integer> fetchCjgAppCodeNamesByQueryResult(String result) {
        final Map map = JsonUtils.parse(result, Map.class);
        final Map data = (Map) map.get("data");
        Map<String, Integer> ret = new HashMap<>();
        List<Map<String, Object>> datas = (List<Map<String, Object>>) data.get("data");
        for (Map<String, Object> obj : datas) {
            String name = (String) obj.get("name");
            Integer id = (Integer) obj.get("id");
            ret.put(name, id);
        }
        return ret;
    }
}
