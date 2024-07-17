package com.jd.workflow.console.service.platform.jsf.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jd.cjg.unapp.vo.AppInfoVo;
import com.jd.common.util.StringUtils;
import com.jd.jsf.gd.util.DateUtils;
import com.jd.jsf.open.api.InterfaceService;
import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.QueryInterfaceRequest;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.entity.parser.InterfaceInfoDown;
import com.jd.workflow.console.entity.sync.SynJsfInfo;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.app.UnAppProviderWarp;
import com.jd.workflow.console.service.depend.ProviderServiceWrap;
import com.jd.workflow.console.service.platform.jsf.ExportService;
import com.jd.workflow.console.service.remote.api.JDosAppOpenService;
import com.jd.workflow.console.service.remote.api.dto.jdos.JdosAppMembers;
import com.jd.workflow.console.service.sync.SynJsfInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/7/15
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {
    @Autowired
    private InterfaceService interfaceService;
    @Autowired
    private ProviderServiceWrap providerServiceWrap;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    JDosAppOpenService jDosAppOpenService;
    @Autowired
    private SynJsfInfoService synJsfInfoService;
    @Autowired
    private UnAppProviderWarp unAppProviderWarp;

    @Override
    public void exportInterfaces(String startStr) {
        try {
            log.error("ExportServiceImpl.exportInterfaces ");
            int pageSize = 40, startNo = 0, totalNo = 0;
            Boolean flag = true;
            QueryInterfaceRequest queryInterfaceRequest = JSFArgBuilder.buildQueryInterfaceRequest();
            queryInterfaceRequest.setPageSize(pageSize);
            queryInterfaceRequest.setInterfaceName(startStr);
            int count = 0, endCount = 10000, totalCount = 0;
            do {
                //最多循环1万次
                totalCount = totalCount + 1;
                if (totalCount > 10000) {
                    break;
                }
                queryInterfaceRequest.setOffset(startNo);
                try {
                    Result<List<InterfaceInfo>> interfaceListResult = interfaceService.searchInterface(queryInterfaceRequest);
                    totalNo = interfaceListResult.getTotal();
                    startNo = startNo + pageSize;
                    for (InterfaceInfo data : interfaceListResult.getData()) {
                        SynJsfInfo synJsfInfo = buildSetSynJsfInfo(data);
                        //设置应用code
                        String appCode = buildSetAppCode(data, synJsfInfo);
                        if (StringUtils.isNotBlank(appCode)) {
                            JdosAppMembers jdosAppMembers = jDosAppOpenService.queryJdosAppMembersAppCode(appCode, null);
                            if (Objects.nonNull(jdosAppMembers) && CollectionUtils.isNotEmpty(jdosAppMembers.getAppOwner())) {
                                synJsfInfo.setAppOwner(jdosAppMembers.getAppOwner().get(0));
                                UserVo userVo = userHelper.getUserBaseInfoByUserName(jdosAppMembers.getAppOwner().get(0));
                                if (Objects.nonNull(userVo)) {
                                    synJsfInfo.setCjgDepartment(userVo.getOrganizationFullName());
                                }
                            }
                        }
                        AppInfoVo appInfoVo = unAppProviderWarp.obtainAppInfoVo(appCode);
                        if (Objects.nonNull(appInfoVo) && StringUtils.isNotBlank(appInfoVo.getCodeAddress())) {
                            String codingAddress = appInfoVo.getCodeAddress().replace("git@coding.jd.com:", "")
                                    .replace("https://coding.jd.com/", "");
                            synJsfInfo.setCodeAddress(codingAddress);
                        }
                        LambdaQueryWrapper<SynJsfInfo> lqw = new LambdaQueryWrapper<>();
                        lqw.eq(SynJsfInfo::getInterfaceName, synJsfInfo.getInterfaceName());
                        synJsfInfoService.saveOrUpdate(synJsfInfo, lqw);
                    }
                    count = count + 1;
                } catch (Throwable e) {
                    log.error("exportInterface.while in exception ", e);
                }
                if (flag) {
                    break;
                }
            } while (startNo < totalNo && count <= endCount);

        } catch (Throwable e) {
            log.error("exportInterface.queryJsfMethodInfo exception ", e);
        }
    }

    /**
     * 构建设置SynJsfInfo
     *
     * @param data
     * @return
     */
    private SynJsfInfo buildSetSynJsfInfo(InterfaceInfo data) {
        SynJsfInfo synJsfInfo = new SynJsfInfo();
        synJsfInfo.setInterfaceName(data.getInterfaceName());
        synJsfInfo.setProviderLive(data.getProviderLive());
        synJsfInfo.setConsumerLive(data.getConsumerLive());
        synJsfInfo.setOwnerUser(data.getOwnerUser());
        synJsfInfo.setDepartment(data.getDepartment());
        synJsfInfo.setDepartmentCode(data.getDepartmentCode());
        synJsfInfo.setRemark(data.getRemark());
        if (Objects.nonNull(data.getCreatedTime())) {
            synJsfInfo.setCreatedTime(DateUtils.dateToStr(data.getCreatedTime()));
        }
        if (Objects.nonNull(data.getModifiedTime())) {
            synJsfInfo.setModifiedTime(DateUtils.dateToStr(data.getModifiedTime()));
        }
        return synJsfInfo;
    }

    private String buildSetAppCode(InterfaceInfo data, SynJsfInfo synJsfInfo) {
        String jdosAppCode = "";
        try {
            if (synJsfInfo.getProviderLive() > 0) {
                jdosAppCode = providerServiceWrap.getProviderJdosAppCode(data.getInterfaceName());
                jdosAppCode = jdosAppCode.replace("jdos_", "");
                synJsfInfo.setAppCode(jdosAppCode);
            }
        } catch (Exception e) {
            log.error("JsfTestController.buildSetAppCode synJsfInfo exception ", e);
        }
        return jdosAppCode;
    }
}
