package com.jd.workflow.console.controller.auth;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.QueryAppReqDTO;
import com.jd.workflow.console.dto.auth.*;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.manage.InterfaceMarketSearchResult;
import com.jd.workflow.console.dto.manage.MethodSearchResult;
import com.jd.workflow.console.elastic.service.EsInterfaceService;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.Menu;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.auth.InterfaceAuthService;
import com.jd.workflow.console.service.ducc.DuccConfigServiceAdapter;
import com.jd.workflow.console.service.ducc.entity.DuccItem;
import com.jd.workflow.console.service.manage.GraphListServiceImpl;
import com.jd.workflow.console.service.manage.HistorySearchKeywordService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.StringHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 接口鉴权控制器
 */
@Slf4j
@RestController
@RequestMapping("/interfaceAuth")
@UmpMonitor
@Profile("erpLogin")
@Api(value = "接口鉴权管理",tags="接口鉴权管理")
public class InterfaceAuthController {
    @Autowired
    InterfaceAuthService authService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    EsInterfaceService esInterfaceService;

    @Autowired
    HistorySearchKeywordService historySearchKeywordService;
    @Autowired
    GraphListServiceImpl graphListService;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    IMethodManageService methodManageService;

    @Autowired
    DuccConfigServiceAdapter duccConfigServiceAdapter;
    /**
     * 获取接口市场检索
     * @param filter
     * @return
     */
    @PostMapping(value = "/interfaceList")
    @ResponseBody
    public CommonResult<Page<InterfaceManage>> interfaceList(@RequestBody InterfaceAuthFilter filter) {
        if(StringUtils.isNotBlank(filter.getName())){
            historySearchKeywordService.saveSearchStr(filter.getName());
        }
        return CommonResult.buildSuccessResult(interfaceManageService.pageMarketInterface(filter));
    }

    /**
     * 接口市场列表 接口视图
     * @param filter
     * @return
     */
    @PostMapping(value = "/methodList")
    @ResponseBody
    public CommonResult<Page<MethodManageDTO>> methodList(@RequestBody InterfaceAuthFilter filter) {
        Page<MethodManageDTO> result = methodManageService.marketMethod(filter);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 获取接口市场列表  应用视图
     * @param filter
     * @return
     */
    @PostMapping(value = "/queryAppInfo")
    @ResponseBody
    public CommonResult<Page<AppInfoDTO>> queryAppInfo(@RequestBody AppFilter filter) {
        if(filter.getQueryType()==null){
            filter.setQueryType(1);
        }
        //根据产品查询应用信息
        QueryAppReqDTO query = new QueryAppReqDTO();
        query.setCjgProductTrace(filter.getCjgProductTrace());
        query.setCjgBusinessDomainTrace(filter.getCjgBusinessDomainTrace());
        query.setDept(filter.getDeptName());
        query.setAdmin(filter.getAdminCode());
        query.setName(filter.getNameOrCode());
        query.setCurrentPage(filter.getCurrent().intValue());
        query.setPageSize(filter.getSize().intValue());

        Page<AppInfoDTO> dto = appInfoService.querySpaceAppByCondition(query,filter.getQueryType());

        if (CollectionUtils.isNotEmpty(dto.getRecords())) {
            Map<Long, List<String>> app2Type = interfaceManageService.getAppInterfaceTypes(dto.getRecords());
            log.info("test11-{}",JSONObject.toJSONString(app2Type));
            for (AppInfoDTO item : dto.getRecords()) {
                log.info("test222-{}",item.getJdosAppCode());
                item.setInterfaceType(app2Type.get(item.getId()));
            }
        }
        return CommonResult.buildSuccessResult(dto);
    }

    @GetMapping(value = "/querySolutionConfig")
    @ResponseBody
    public CommonResult<List<Menu>> querySolutionConfig() {
        List<Menu> config = duccConfigServiceAdapter.getItemsByConfigCode("solutionMenu");
        return  CommonResult.buildSuccessResult(config);
    }


    /**
     * 用户搜索历史记录
     * @return 最新的历史搜索记录
     */
    @GetMapping(value = "/historySearchLog")
    @ResponseBody
    public CommonResult<List<String>> historySearchLog() {
        return CommonResult.buildSuccessResult(historySearchKeywordService.getKeyWords());
    }
    /**
     * 接口市场高级搜索
     * @param search 搜索的字符串
     * @param type 类型，搜索翻页的时候使用 1-http 3-jsf
     * @param current 当前页数
     * @param size 分页大小
     * @return 搜索结果
     */
    @GetMapping(value = "/searchInterfaceOrMethod")
    @ResponseBody
    public CommonResult<List<InterfaceMarketSearchResult>> searchInterfaceOrMethod(String search, @RequestParam(required = false) Integer type, int current, int size) {
        List<InterfaceMarketSearchResult> result = new ArrayList<>();
        if (StringHelper.isBlank(search)) {
            throw new BizException("搜索条件不可为空!");
        }
        historySearchKeywordService.saveSearchStr(search);
        if (type == null) {
            result.add(esInterfaceService.searchMethodOrInterface(search, InterfaceTypeEnum.HTTP.getCode(), current, size));
            result.add(esInterfaceService.searchMethodOrInterface(search, InterfaceTypeEnum.JSF.getCode(), current, size));
            result.add(esInterfaceService.searchOnlyInterface(search, current, size));
        } else {
            if (Objects.equals(InterfaceTypeEnum.JSF_INTERFACE.getCode(), type)) {
                result.add(esInterfaceService.searchOnlyInterface(search, current, size));
            } else {
                result.add(esInterfaceService.searchMethodOrInterface(search, type, current, size));
            }
        }
        return CommonResult.buildSuccessResult(result);
    }
    /**
     * 搜索接口信息
     * @description 高级搜索时接口分页使用
     * @param search 搜索的字符串
     * @param type 接口类型 {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     * @param current 当前页数
     * @param size 分页大小
     * @return 搜索结果
     */
    @GetMapping(value = "/searchInterface")
    @ResponseBody
    public CommonResult<Page<InterfaceManage>> searchInterface(String search,int type,int current,int size) {
        return CommonResult.buildSuccessResult(interfaceManageService.searchInterface(type,search,current,size));
    }
    /**
     * 搜索方法信息
     * @description 高级搜索时方法分页使用
     * @param search 搜索的字符串
     * @param type 接口类型 {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     * @param current 当前页数
     * @param size 分页大小
     * @return 搜索结果
     */
    @GetMapping(value = "/searchMethod")
    @ResponseBody
    public CommonResult<Page<MethodSearchResult>> searchMethod(String search, int type, int current, int size) {
        return CommonResult.buildSuccessResult(interfaceManageService.searchMethod(type,search,current,size));
    }


    @GetMapping(value = "/checkHasAuth")
    @ResponseBody
    @ApiOperation(value="校验接口是否开启鉴权")
    public CommonResult<Boolean> checkHasAuth(Long id) {
        return CommonResult.buildSuccessResult(authService.checkHasAuth(id));
    }
    @PostMapping(value = "/saveAuthConfig")
    @ResponseBody
    @ApiOperation(value="保存鉴权配置")
    public CommonResult<Boolean> saveAuthConfig(@RequestBody AuthDto authDto) {
        authService.saveAuthConfig(authDto);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping(value = "/getAuthConfig")
    @ResponseBody
    @ApiOperation(value="获取鉴权配置")
    public CommonResult<AuthDto> getAuthConfig( Long id) {

        return CommonResult.buildSuccessResult(authService.getAuthConfig(id));
    }
    @GetMapping(value = "/checkAppCanRelated")
    @ResponseBody
    @ApiOperation(value="校验接口是否可以关联应用")
    public CommonResult<Boolean> checkAppCanRelated(Long id,String appId) {

        return CommonResult.buildSuccessResult(authService.checkAppCanRelated(id,appId));
    }
    @GetMapping(value = "/queryInterfaceByCjgAppCode")
    @ResponseBody
    @ApiOperation(value="根据藏经阁应用编码查询应用")
    public CommonResult<InterfaceManage> queryInterfaceByCjgAppCode(String interfaceName,String cjgAppCode) {

        return CommonResult.buildSuccessResult(authService.queryJsfInterface(interfaceName,cjgAppCode));
    }

    /**
     * 获取未鉴权接口列表
     * @param appId
     * @return
     */
    @GetMapping(value = "/unAuthInterface")
    public CommonResult<List<InterfaceSortModel>> unAuthInterface(Long appId){
        Guard.notEmpty(appId,"appId无效");
        List<InterfaceSortModel> sortModels = authService.listUnAuthInterface(appId);
        return CommonResult.buildSuccessResult(sortModels);
    }

    /**
     * 添加jsf鉴权
     * @param appAuthDto
     * @return true为成功，false为失败
     */
    @PostMapping(value = "/addJsfHttpAuth")
    public CommonResult<Boolean> addJsfHttpAuth(@RequestBody AppAuthDto appAuthDto, @CookieValue("sso.jd.com")String cookie){
        boolean result = authService.addJsfInterface(appAuthDto,cookie);
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 更新jsf鉴权配置
     * @param dto
     * @return true表示成功，false为失败
     */
    @PostMapping(value="updateAuthConfig")
    public CommonResult<Boolean> updateAuthConfig(@RequestBody UpdateInterfaceAuthDto dto){
        authService.updateJsfReq(dto);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 获取jsf鉴权详情
     * @param id
     * @return
     */
    @GetMapping(value="getJsfAuthDetail")
    public CommonResult<JsfInterfaceAuthDto> getJsfAuthDetail(Long id){
        JsfInterfaceAuthDto detail = authService.getJsfAuthDetail(id);
        return CommonResult.buildSuccessResult(detail);
    }

    /**
     * 获取jsf鉴权列表
     * @param appId 应用id
     * @param current 当前页
     * @param size 分页大小
     * @return
     */
    @GetMapping(value="getJsfAuthList")
    public CommonResult<Page<JsfInterfaceAuthDto>> jsfInterfaceAuthDtos(Long appId,Long current,Long size){
        Page<JsfInterfaceAuthDto> detail = authService.jsfInterfaceAuthDtos(appId,current,size);
        return CommonResult.buildSuccessResult(detail);
    }

    @GetMapping(value="enableCjgDucc")
    public CommonResult<Boolean> enableCjgDucc(Long appId,@RequestHeader("Cookie")String cookie){
        AppInfo app = appInfoService.getById(appId);
        authService.saveDuccAuth(app,cookie);
        return CommonResult.buildSuccessResult(true);
    }
}
