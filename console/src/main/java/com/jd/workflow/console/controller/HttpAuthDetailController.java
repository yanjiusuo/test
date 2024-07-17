package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IHttpAuthDetailService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
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
@RequestMapping("/httpAuthDetail")
@UmpMonitor
public class HttpAuthDetailController {

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthDetailService httpAuthDetailService;

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
     * 查询鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryList")
    public CommonResult queryList(@RequestBody QueryHttpAuthDetailReqDTO query) {
        log.info("#HttpAuthDetailController queryList requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询鉴权标识明细列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        List<HttpAuthDetailDTO> httpAuthList = httpAuthDetailService.queryAllList(query);
        log.info("#HttpAuthDetailController queryList result={} ", JSON.toJSONString(httpAuthList));
        return CommonResult.buildSuccessResult(httpAuthList);
    }

    /**
     * 查询鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListGroupByInterface")
    public CommonResult queryListGroupByInterface(@RequestBody QueryHttpAuthDetailReqDTO query) {
        log.info("#HttpAuthDetailController queryListGroupByInterface requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询接口维度鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询接口维度鉴权标识明细列表时，site 参数不正确！");
        return CommonResult.buildSuccessResult(httpAuthDetailService.queryAppInterfaceAuth(query));
    }
    /**
     * 查询接口以及方法信息
     *
     * @param query
     * @return
     */
    @PostMapping("/queryAppInterfaceAndMethod")
    public CommonResult queryAppInterfaceAndMethod(@RequestBody QueryHttpAuthDetailReqDTO query) {
        log.info("#HttpAuthDetailController queryListGroupByInterface requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询接口维度鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询接口维度鉴权标识明细列表时，site 参数不正确！");
        return CommonResult.buildSuccessResult(httpAuthDetailService.queryAppInterfaceAndMethod(query));
    }
    /**
     * 查询接口的分组子节点列表
     * @param id 接口id
     * @return 当前项目的children节点,数组类型
     */
    @RequestMapping("/queryInterfaceChildren")
    public CommonResult<List<TreeSortModel>> queryInterfaceChildren(Long id) {
        MethodGroupTreeDTO methodGroupTree = methodGroupService.findMethodGroupTree(id);
        methodGroupTree.clearChildrenTreeModel(methodGroupTree.getTreeModel().getTreeItems());
        List<TreeSortModel> children = Objects.isNull(methodGroupTree.getTreeModel()) ? null : methodGroupTree.getTreeModel().getTreeItems();
        return CommonResult.buildSuccessResult(children);

    }

    /**
     * 查询鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListGroupByMethod")
    public CommonResult queryListGroupByMethod(@RequestBody QueryHttpAuthDetailReqDTO query) {
        log.info("#HttpAuthDetailController queryListGroupByMethod requestBody={} ", JSON.toJSONString(query));
        List<HasChildrenHttpAuthDetail> result = httpAuthDetailService.queryInterfaceMethod(query);
        return CommonResult.buildSuccessResult(result);

    }




    /**
     * 查询鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListPageGroupByMethod")
    public CommonResult queryListPageGroupByMethod(@RequestBody QueryHttpAuthDetailReqDTO query) {
        log.info("#HttpAuthDetailController queryListPageGroupByMethod requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询方法维度鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询方法维度鉴权标识明细列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        Page<HttpAuthDetailDTO> authDetailDTOPage = httpAuthDetailService.queryListPageGroupByMethod(query);
        log.info("#HttpAuthDetailController queryListPageGroupByMethod result={} ", JSON.toJSONString(authDetailDTOPage));
        return CommonResult.buildSuccessResult(authDetailDTOPage);
    }

    /**
     * 查询鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListPageGroupByInterface")
    public CommonResult queryListPageGroupByInterface(@RequestBody QueryHttpAuthDetailReqDTO query) {
        log.info("#HttpAuthDetailController queryListPageGroupByInterface requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询鉴权标识明细列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        if (Objects.isNull(query.getType())) {
            Page<HttpAuthDetailDTO> page = httpAuthDetailService.queryListPageGroupByInterface(query);
            log.info("#HttpAuthDetailController queryListPageGroupByInterface result={} ", JSON.toJSONString(page));
            return CommonResult.buildSuccessResult(page);
        } else if (Objects.equals(query.getType(), 1)) {
            InterfacePageQuery interfacePageQuery = new InterfacePageQuery();
            // 查询HTTP、JSF接口 按照接口筛选不包含 扩展点类型接口
            interfacePageQuery.setType("1,3");
            interfacePageQuery.setCurrent(query.getCurrent());
            interfacePageQuery.setName(query.getInterfaceInfo());
            interfacePageQuery.setSize(Objects.isNull(query.getPageSize()) ? 0 : Long.valueOf(query.getPageSize()));
            interfacePageQuery.setTenantId(UserSessionLocal.getUser().getTenantId());
            Page<InterfaceManage> interfaceManagePage = null;
            if(query.getInterfaceId() != null){
                interfaceManagePage = new Page<>(interfacePageQuery.getCurrent(),interfacePageQuery.getSize());
                interfaceManagePage.setRecords(Collections.singletonList(interfaceManageService.getById(query.getInterfaceId())));
                interfaceManagePage.setTotal(1);
            }else{
                //3.service层
                interfaceManagePage = interfaceManageService.pageList(interfacePageQuery);
            }

            for (InterfaceManage record : interfaceManagePage.getRecords()) {
                record.init();
            }
            Page<HttpAuthDetailDTO> page = new Page<HttpAuthDetailDTO>();
            page.setCurrent(interfaceManagePage.getCurrent());
            page.setSize(interfaceManagePage.getSize());
            page.setTotal(interfaceManagePage.getTotal());
            List<HttpAuthDetailDTO> collect = interfaceManagePage.getRecords().stream().map(e -> {
               // MethodGroupTreeDTO methodGroupTree = methodGroupService.findMethodGroupTree(e.getId());
                HttpAuthDetailDTO httpDto = new HttpAuthDetailDTO();
                httpDto.setInterfaceType(e.getType());
                httpDto.setId(e.getId());
                httpDto.setAppName(e.getAppName());
                httpDto.setAppCode(e.getAppId() + "");
                httpDto.setModifier(e.getModifier());
                httpDto.setCreator(e.getUserName());
                httpDto.setInterfaceId(e.getId());
                httpDto.setInterfaceName(e.getName());
                httpDto.setInterfaceCode(e.getServiceCode());
                if(InterfaceTypeEnum.JSF.getCode().equals(e.getType())){

                }else if(InterfaceTypeEnum.HTTP.getCode().equals(e.getType())){
                    httpDto.setInterfaceCode(e.getServiceCode());
                }

                // 接口/应用文件夹类型
                httpDto.setType(Integer.valueOf(TreeSortModel.TYPE_INTERFACE));
                //httpDto.setChildren(Objects.isNull(methodGroupTree.getTreeModel()) ? null : methodGroupTree.getTreeModel().getTreeItems());
                return httpDto;
            }).collect(Collectors.toList());
            page.setRecords(collect);
            //4.出参
            return CommonResult.buildSuccessResult(page);
        }
        return CommonResult.buildErrorCodeMsg(ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode(), "param type is error!!!");

    }

    /**
     * 查询鉴权标识明细列表
     *
     * @param query
     * @return
     */
    @PostMapping("/queryListGroupByAuthCode")
    public CommonResult queryListGroupByAuthCode(@RequestBody QueryHttpAuthDetailReqDTO query) {
        log.info("#HttpAuthDetailController queryListGroupByAuthCode requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询鉴权标识维度鉴权标识明细列表时，入参不能为空");
        Guard.notNull(SiteEnum.getEnumByCode(query.getSite()), "查询鉴权标识维度鉴权标识明细列表时，site 参数不正确！");
        query.setPin(UserSessionLocal.getUser().getUserId());
        List<HttpAuthDetailDTO> httpAuthList = httpAuthDetailService.queryListGroupByAuthCode(query);
        log.info("#HttpAuthDetailController queryListGroupByAuthCode result={} ", JSON.toJSONString(httpAuthList));
        return CommonResult.buildSuccessResult(httpAuthList);
    }


}
