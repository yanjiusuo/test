package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.cjg.config.http.CjgHttpAuth;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IHttpAuthService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 项目名称：鉴权标识服务
 *
 * @author wangwenguang
 * @date 2023-01-06 11:31
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/httpAuth")
@UmpMonitor
public class HttpAuthController {

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthService cjgHttpAuthService;

    @Resource
    private IAppInfoService appInfoService;


    /**
     * 查询鉴权标识列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryList")
    public CommonResult queryList(@RequestBody QueryHttpAuthReqDTO query) {
        log.info("#HttpAuthController queryList requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询鉴权标识列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询鉴权标识列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        Page<HttpAuthDTO> httpAuthPage = cjgHttpAuthService.queryListPage(query);
        log.info("#HttpAuthController queryList result={} ", JSON.toJSONString(httpAuthPage));
        return CommonResult.buildSuccessResult(httpAuthPage);
    }


    /**
     * 查询鉴权应用列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListGroupByAppAndSite")
    public CommonResult<List<HttpAuthDTO>> queryListGroupByAppAndSite(@RequestBody QueryHttpAuthReqDTO query) {
        log.info("#HttpAuthController queryListGroupByAppAndSite requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询需鉴权应用列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询需鉴权应用列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        if (Objects.isNull(query.getType()) || Objects.equals(query.getType(), 0)) {
            List<HttpAuthDTO> authDTOList = cjgHttpAuthService.queryListGroupByAppAndSite(query);
            /*for (HttpAuthDTO httpAuthDTO : authDTOList) {
                httpAuthDTO.setAppName(httpAuthDTO.getAppName()+"（"+httpAuthDTO.getAppCode()+"）");
            }*/
            log.info("#HttpAuthController queryListGroupByAppAndSite result={} ", JSON.toJSONString(authDTOList));
            return CommonResult.buildSuccessResult(authDTOList);
        } else if (Objects.equals(query.getType(), 1)) {
            QueryAppReqDTO queryAppReqDTO = new QueryAppReqDTO();
            log.info("AppInfoController queryListPageGroupByAuthCode requestBody={} ", JSON.toJSONString(queryAppReqDTO));
            queryAppReqDTO.setPageSize(query.getPageSize());
            queryAppReqDTO.setCurrentPage(query.getCurrent().intValue());
            queryAppReqDTO.setTenantId(UserSessionLocal.getUser().getTenantId());
            queryAppReqDTO.setPin(UserSessionLocal.getUser().getUserId());
            queryAppReqDTO.setName(query.getAppInfo());
            queryAppReqDTO.setAppCode(query.getAppCode());
            queryAppReqDTO.setAppName(query.getAppName());
            QueryAppResultDTO resultDTO = appInfoService.queryAppByCondition(queryAppReqDTO);
            List<HttpAuthDTO> httpAuthList = new ArrayList<>();
            if (Objects.nonNull(resultDTO) && CollectionUtils.isNotEmpty(resultDTO.getList())) {
                httpAuthList = resultDTO.getList().stream().map(e -> {
                    HttpAuthDTO httpAuthDTO = new HttpAuthDTO();
                    httpAuthDTO.setAppId(e.getId()+"");
                    httpAuthDTO.setAppCode(e.getAppCode()+"");
                    httpAuthDTO.setAppName(e.getAppName());
                    // 接口/应用文件夹类型
                    httpAuthDTO.setType(Integer.valueOf(TreeSortModel.TYPE_INTERFACE));
                    return httpAuthDTO;
                }).collect(Collectors.toList());
            }
            return CommonResult.buildSuccessResult(httpAuthList);
        }
        return CommonResult.buildErrorCodeMsg(ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode(), "param type is error!!!");

    }

}
