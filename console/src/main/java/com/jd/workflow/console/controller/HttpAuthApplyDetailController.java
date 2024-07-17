package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.ApplySourceEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IHttpAuthApplyDetailService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.impl.HttpAuthApplyDetailServiceImpl;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 项目名称：鉴权标识明细服务
 *
 * @author wangwenguang
 * @date 2023-01-06 11:31
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/httpAuthApplyDetail")
@UmpMonitor
public class HttpAuthApplyDetailController {

    /**
     * 鉴权标识服务
     */
    @Resource
    private HttpAuthApplyDetailServiceImpl httpAuthApplyDetailService;

    /**
     * @date: 2022/5/13 11:18
     * @author wubaizhao1
     */
    @Resource
    private IInterfaceManageService interfaceManageService;

    @Resource
    private IInterfaceMethodGroupService methodGroupService;

    @Resource
    private IAppInfoService appInfoService;

    /**
     * 查询已申请鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryList")
    public CommonResult queryList(@RequestBody QueryHttpAuthApplyDetailReqDTO query) {
        log.info("#HttpAuthApplyDetailController queryList requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询已申请鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询已申请鉴权标识明细列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        List<HttpAuthApplyDetailDTO> httpAuthList = httpAuthApplyDetailService.queryAllList(query);
        log.info("#HttpAuthApplyDetailController queryList result={} ", JSON.toJSONString(httpAuthList));
        return CommonResult.buildSuccessResult(httpAuthList);
    }


    /**
     * 查询已申请鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListGroupByInterface")
    public CommonResult queryListGroupByInterface(@RequestBody QueryHttpAuthApplyDetailReqDTO query) {
        log.info("#HttpAuthApplyDetailController queryListGroupByInterface requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询已申请鉴权标识接口列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询已申请鉴权标识接口列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        List<HttpAuthApplyDetailDTO> httpAuthList = httpAuthApplyDetailService.queryListGroupByInterface(query);
        log.info("#HttpAuthApplyDetailController queryListGroupByInterface result={} ", JSON.toJSONString(httpAuthList));
        return CommonResult.buildSuccessResult(httpAuthList);
    }

    /**
     * 查询已申请鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListGroupByMethod")
    public CommonResult queryListGroupByMethod(@RequestBody QueryHttpAuthApplyDetailReqDTO query) {
        log.info("#HttpAuthApplyDetailController queryListGroupByMethod requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询已申请鉴权标识方法列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询已申请鉴权标识方法列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        List<HttpAuthApplyDetailDTO> httpAuthList = httpAuthApplyDetailService.queryListGroupByMethod(query);
        log.info("#HttpAuthApplyDetailController queryListGroupByMethod result={} ", JSON.toJSONString(httpAuthList));
        return CommonResult.buildSuccessResult(httpAuthList);
    }


    /**
     * 查询已申请鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListPageGroupByInterface")
    public CommonResult<Page<HttpAuthApplyDetailDTO>> queryListPageGroupByInterface(@RequestBody QueryHttpAuthApplyDetailReqDTO query) {
        log.info("#HttpAuthApplyDetailController queryListPageGroupByInterface requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询已申请鉴权标识接口列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询已申请鉴权标识接口列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        Page<HttpAuthApplyDetailDTO> page = httpAuthApplyDetailService.queryListPageGroupByInterface(query);
        log.info("#HttpAuthApplyDetailController queryListPageGroupByInterface result={} ", JSON.toJSONString(page));
        return CommonResult.buildSuccessResult(page);
        /*if (Objects.isNull(query.getType())) {
            Page<HttpAuthApplyDetailDTO> page = httpAuthApplyDetailService.queryListPageGroupByInterface(query);
            log.info("#HttpAuthApplyDetailController queryListPageGroupByInterface result={} ", JSON.toJSONString(page));
            return CommonResult.buildSuccessResult(page);
        } else if (Objects.equals(query.getType(), 1)) {
            InterfacePageQuery interfacePageQuery = new InterfacePageQuery();
            // 查询HTTP、JSF接口
            interfacePageQuery.setType("1,3");
            interfacePageQuery.setCurrent(query.getCurrent());
            interfacePageQuery.setSize(Objects.isNull(query.getPageSize()) ? 0 : Long.valueOf(query.getPageSize()));
            interfacePageQuery.setTenantId(UserSessionLocal.getUser().getTenantId());
            //3.service层
            Page<InterfaceManage> interfaceManagePage = interfaceManageService.pageList(interfacePageQuery);
            for (InterfaceManage record : interfaceManagePage.getRecords()) {
                record.init();
            }
            Page<HttpAuthApplyDetailDTO> page = new Page<HttpAuthApplyDetailDTO>();
            page.setCurrent(interfaceManagePage.getCurrent());
            page.setSize(interfaceManagePage.getSize());
            page.setTotal(interfaceManagePage.getTotal());
            List<HttpAuthApplyDetailDTO> collect = interfaceManagePage.getRecords().stream().map(e -> {
                HttpAuthApplyDetailDTO httpDto = new HttpAuthApplyDetailDTO();
                httpDto.setAppName(e.getAppName());
                httpDto.setAppCode(e.getAppId() + "");
                httpDto.setModifier(e.getModifier());
                httpDto.setCreator(e.getCreator());
                httpDto.setInterfaceId(e.getId());
                httpDto.setInterfaceName(e.getName());
                return httpDto;
            }).collect(Collectors.toList());
            page.setRecords(collect);
            //4.出参
            return CommonResult.buildSuccessResult(page);
        }
        return CommonResult.buildErrorCodeMsg(ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode(), "param type is error!!!");*/
    }

    /**
     * 按鉴权标签分组查询已申请鉴权标识列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListPageGroupByAuthCode")
    public CommonResult queryListPageGroupByAuthCode(@RequestBody QueryHttpAuthApplyDetailReqDTO query) {
        log.info("#HttpAuthApplyDetailController queryListPageGroupByAuthCode requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "按鉴权标签分组查询已申请鉴权标识列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "按鉴权标签分组查询已申请鉴权标识列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        Page<HttpAuthApplyDetailDTO> page = httpAuthApplyDetailService.queryListPageGroupByAuthCode(query);
        log.info("#HttpAuthApplyDetailController queryListPageGroupByAuthCode result={} ", JSON.toJSONString(page));
        return CommonResult.buildSuccessResult(page);
    }

    /**
     * 按鉴权标签分组查询已申请鉴权标识列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListPageGroupByAuthCodeAndMethod")
    public CommonResult queryListPageGroupByAuthCodeAndMethod(@RequestBody QueryHttpAuthApplyDetailReqDTO query) {
        log.info("#HttpAuthApplyDetailController queryListPageGroupByAuthCodeAndMethod requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "按鉴权标签分组查询已申请鉴权标识列表时，入参不能为空");
        Guard.notNull(query.getSource(), "source入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "按鉴权标签分组查询已申请鉴权标识列表时，site 参数不正确！");
        if (query.getSource().equals(ApplySourceEnum.CALLER.getCode())) {
            query.setCallPin(UserSessionLocal.getUser().getUserId());
        }
        query.setCallUserInfo(query.getUserInfo());
        query.setUserInfo(null);
        log.info("#HttpAuthApplyDetailController queryListPageGroupByAuthCodeAndMethod query={} ", JSON.toJSONString(query));
        Page<HttpAuthApplyDetailDTO> page = httpAuthApplyDetailService.queryListPageGroupByAuthCodeAndMethod(query);
        log.info("#HttpAuthApplyDetailController queryListPageGroupByAuthCodeAndMethod result={} ", JSON.toJSONString(page));
        return CommonResult.buildSuccessResult(page);
    }
    @RequestMapping("dumpAllApplyData")
    public CommonResult<List<HttpAuthApplyDetailServiceImpl.HttpAuthApplyDetailExt>> dumpData(){
        return CommonResult.buildSuccessResult(httpAuthApplyDetailService.dumpData());
    }

    @RequestMapping("getCsv")
    public void dumpExcel(HttpServletResponse response){
        List<HttpAuthApplyDetailServiceImpl.HttpAuthApplyDetailExt> exts = httpAuthApplyDetailService.dumpData();
        StringBuilder sb = new StringBuilder();
        for (HttpAuthApplyDetailServiceImpl.HttpAuthApplyDetailExt ext : exts) {
            String line = "";
            String methodName = ext.getMethodName();
            methodName = methodName.replace('\n',';').replace(',','，');
            line+=ext.getId()+","
            +ext.getAppCode()+","
            +ext.getAppName()+","
            +ext.getAppDept()+","
            +ext.getCallAppCode()+","
            +ext.getCallAppName()+","
            +ext.getCallerDept()+","
            +ext.getTicketId()+","
            +ext.getPath()+","
            +methodName+"\n"
            ;
            sb.append(line);
        }
        try {
            response.getOutputStream().write(sb.toString().getBytes("utf-8"));
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
