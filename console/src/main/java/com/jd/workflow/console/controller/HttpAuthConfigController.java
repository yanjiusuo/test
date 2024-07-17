package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.dto.AuthEnableLogDto;
import com.jd.workflow.console.dto.HttpAuthConfigDTO;
import com.jd.workflow.console.dto.QueryHttpAuthConfigReqDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IHttpAuthConfigService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * 项目名称：鉴权标识配置
 * @author wangwenguang
 * @date 2023-01-06 11:31
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/httpAuthConfig")
@UmpMonitor
public class HttpAuthConfigController {

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthConfigService httpAuthConfigService;

    /**
     * 应用相关人员
     */
    @Resource
    private IAppInfoService appInfoService;


    /**
     * 查询鉴权配置
     * TODO 此处需要什么权限的人操作？？？
     * @param query
     * @return
     */
    @PostMapping("/queryOne")
    public CommonResult<HttpAuthConfigDTO> queryOne(@RequestBody QueryHttpAuthConfigReqDTO query) {
        log.info("#HttpAuthConfigController queryOne requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查看应用鉴权配置时，入参不能为空");
        Guard.notNull(query.getAppCode(), "查看应用鉴权配置时，appCode入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查看应用鉴权配置时，site 参数不正确！");

        //此处需校验是不是
        AppInfo appInfo = appInfoService.findApp(query.getAppCode());
        Guard.notNull(appInfo, "查看鉴权配置的应用（" + query.getAppCode() + "）不存在！");
        Guard.notNull(appInfo.getMembers(), "查看鉴权配置的应用，无权限操作！");

        //需要校验操作人是不是应用相关人员
        String members = appInfo.getMembers();
        if (members.indexOf(UserSessionLocal.getUser().getUserId() + ",") < 0) {
            Guard.notNull(false, "查看鉴权配置的应用，无权限操作，请联系应用相关人员！");
        }
        query.setPin(UserSessionLocal.getUser().getUserId());

        HttpAuthConfigDTO authConfigDTO = httpAuthConfigService.queryOne(query);
        log.info("#HttpAuthConfigController queryOne result={} ", JSON.toJSONString(authConfigDTO));

        return CommonResult.buildSuccessResult(authConfigDTO);
    }


    /**
     * 查询鉴权配置列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryList")
    public CommonResult queryList(@RequestBody QueryHttpAuthConfigReqDTO query) {
        log.info("#HttpAuthConfigController queryList requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询应用鉴权配置时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询应用鉴权配置时，site 参数不正确！");

        query.setPin(UserSessionLocal.getUser().getUserId());

        Page<HttpAuthConfigDTO> httpAuthPage = httpAuthConfigService.queryListPage(query);
        log.info("#HttpAuthConfigController queryList result={} ", JSON.toJSONString(httpAuthPage));

        return CommonResult.buildSuccessResult(httpAuthPage);
    }

    /**
     * 初始化鉴权配置
     * TODO 此处需要什么权限的人操作？？？
     * @param addDTO
     * @return
     */
    @PostMapping("/add")
    public CommonResult add(@RequestBody HttpAuthConfigDTO addDTO) {
        log.info("#HttpAuthConfigController add requestBody={} ", JSON.toJSONString(addDTO));

        //参数校验
        Guard.notNull(addDTO, "初始化应用鉴权配置时，入参不能为空");
        Guard.notNull(addDTO.getAppCode(), "初始化应用鉴权配置时，appCode入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(addDTO.getSite()), "初始化应用鉴权配置时，site 参数不正确！");

        //校验应用是否存在
        String appCode = addDTO.getAppCode();
        AppInfo appInfo = appInfoService.findApp(appCode);
        Guard.notNull(appInfo, "初始化应用（" + appCode + "）不存在！");
        Guard.notNull(appInfo.getMembers(), "初始化应用（" + appCode + "），无权限操作！");

        //需要校验操作人是不是应用相关人员
        String members = appInfo.getMembers();
        if (members.indexOf(UserSessionLocal.getUser().getUserId() + ",") < 0) {
            Guard.assertTrue(false, "初始化应用（" + appCode + "），无权限操作，请联系应用相关人员！");
        }
        if (StringUtils.isNotBlank(appInfo.getAppName())){
            addDTO.setAppName(appInfo.getAppName());
        }
        HttpAuthConfigDTO result = httpAuthConfigService.add(addDTO);
        log.info("#HttpAuthConfigController add result={} ", result);

        return CommonResult.buildSuccessResult(result);
    }


    /**
     * 一键降级鉴权配置
     * 需要校验操作人是不是应用相关人员
     *
     * @param validDTO
     * @return
     */
    @PostMapping("/valid")
    public CommonResult valid(@RequestBody HttpAuthConfigDTO validDTO) {
        log.info("#HttpAuthConfigController valid requestBody={} ", JSON.toJSONString(validDTO));
        Guard.notNull(validDTO, "一键降级鉴权配置时，入参不能为空");
        Guard.notNull(validDTO.getId(), "一键降级鉴权配置时，id 入参不能为空");
        Guard.notNull(validDTO.getAppCode(), "一键降级鉴权配置时，appCode 入参不能为空");
        Guard.notNull(validDTO.getValid(), "一键降级鉴权配置时，valid 入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(validDTO.getSite()), "一键降级鉴权配置时，site 参数不正确！");

        //校验应用是否存在
        String appCode = validDTO.getAppCode();
        AppInfo appInfo = appInfoService.findApp(appCode);
        Guard.notNull(appInfo, "一键降级的应用（" + appCode + "）不存在！");
        Guard.notNull(appInfo.getMembers(), "一键降级鉴权应用（" + appCode + "），无权限操作！");

        //需要校验操作人是不是应用相关人员
        String members = appInfo.getMembers();
        if (members.indexOf("1-" + UserSessionLocal.getUser().getUserId() + ",") < 0) {
            Guard.assertTrue(false, "一键降级鉴权应用（" + appCode + "），无权限操作，请联系应用负责人！");
        }

        HttpAuthConfigDTO result = httpAuthConfigService.update(validDTO);
        log.info("#HttpAuthConfigController valid result={} ", result);

        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 更新http日志开关
     * @param dto
     * @return 更新结果
     */
    @PostMapping("/updateEnableLog")
    public CommonResult<AuthEnableLogDto> updateEnableLog(@RequestBody @Valid AuthEnableLogDto dto) {
        log.info("#HttpAuthConfigController valid requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto, "入参不能为空");
        Guard.notNull(dto.getId(), "id 入参不能为空");
        Guard.notNull(dto.getEnableAuditLog(), "是否开启日志不允许为空");

        AppInfo appInfo = appInfoService.findApp(dto.getAppCode());
        Guard.notNull(appInfo, "应用（" + dto.getAppCode() + "）不存在！");
        Guard.notNull(appInfo.getMembers(), "应用（" + dto.getAppCode() + "），无权限操作！");

        //需要校验操作人是不是应用相关人员
        String members = appInfo.getMembers();
        if (members.indexOf("1-" + UserSessionLocal.getUser().getUserId() + ",") < 0) {
            Guard.assertTrue(false, "应用（" + dto.getAppCode() + "），无权限操作，请联系应用负责人！");
        }

        HttpAuthConfigDTO authConfigDTO = new HttpAuthConfigDTO();
        authConfigDTO.setId(dto.getId());
        authConfigDTO.setEnableAuditLog(dto.getEnableAuditLog());
        authConfigDTO.setAppCode(dto.getAppCode());
        HttpAuthConfigDTO result = httpAuthConfigService.update(authConfigDTO);
        log.info("#HttpAuthConfigController valid result={} ", result);

        return CommonResult.buildSuccessResult(dto);
    }
   /* @GetMapping("/getEnableLog")
    public CommonResult<Boolean> getEnableLog(String appCode,Long id) {
        log.info("#HttpAuthConfigController valid appCode={},id={} ", appCode,id);
        final HttpAuthConfigDTO dto = httpAuthConfigService.selectById(id);
        Map<String,Object> result = httpAuthConfigService.getAuthConfig(dto);
        log.info("#HttpAuthConfigController valid result={} ", result);

        return CommonResult.buildSuccessResult(true);
    }*/

    /**
     * 强制鉴权处理
     * 需要校验操作人是不是应用负责人
     *
     * @param forceValidDTO
     * @return
     */
    @PostMapping("/forceValid")
    public CommonResult forceValid(@RequestBody HttpAuthConfigDTO forceValidDTO) {
        log.info("#HttpAuthConfigController forceValid requestBody={} ", JSON.toJSONString(forceValidDTO));
        Guard.notNull(forceValidDTO, "强制鉴权处理时，入参不能为空");
        Guard.notNull(forceValidDTO.getId(), "强制鉴权处理时，id 入参不能为空");
        Guard.notNull(forceValidDTO.getAppCode(), "强制鉴权处理时，appCode 入参不能为空");
        Guard.notNull(forceValidDTO.getForceValid(), "强制鉴权处理时，forceValid 入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(forceValidDTO.getSite()), "强制鉴权处理时，site 参数不正确！");

        //校验应用是否存在
        String appCode = forceValidDTO.getAppCode();
        AppInfo appInfo = appInfoService.findApp(appCode);
        Guard.notNull(appInfo, "强制鉴权处理的应用（" + appCode + "）不存在！");
        Guard.notNull(appInfo.getMembers(), "强制鉴权处理的应用（" + appCode + "），您无权限操作！");
        //需要校验操作人是不是应用负责人
        String members = appInfo.getMembers();
        if (members.indexOf(AppUserTypeEnum.OWNER.getType()+"-"+UserSessionLocal.getUser().getUserId() + ",") < 0) {
            Guard.assertTrue(false, "强制鉴权处理的应用（" + appCode + "），您无权限操作，请联系应用负责人！");
        }

        HttpAuthConfigDTO result = httpAuthConfigService.update(forceValidDTO);
        log.info("#HttpAuthConfigController forceValid result={} ", result);

        return CommonResult.buildSuccessResult(result);
    }
}
