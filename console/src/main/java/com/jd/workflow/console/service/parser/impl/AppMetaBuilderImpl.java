package com.jd.workflow.console.service.parser.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jd.common.util.StringUtils;
import com.jd.fastjson.JSON;
import com.jd.fastjson.JSONObject;
import com.jd.jsf.open.api.ProviderService;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.SubscribeRequest;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.model.sync.BuildReportContext;
import com.jd.workflow.console.service.depend.InterfaceServiceWrap;
import com.jd.workflow.console.service.depend.ProviderServiceWrap;
import com.jd.workflow.console.service.parser.DocReportDtoBuilderService;
import com.jd.workflow.console.service.parser.ParserUtils;
import com.jd.workflow.console.service.remote.api.JDosAppOpenService;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Service(value = "appMetaBuilder")
@Slf4j
public class AppMetaBuilderImpl implements DocReportDtoBuilderService<DocReportDto> {

    @Autowired
    private ProviderServiceWrap providerServiceWrap;
    @Autowired
    JDosAppOpenService jDosAppOpenService;

    @Override
    public DocReportDto build(DocReportDto docReportDto, BuildReportContext context) {
        String jdosAppCode = providerServiceWrap.getProviderJdosAppCode(context.getInterfaceInfo().getInterfaceName());
        if (StringUtils.isEmpty(jdosAppCode)) {
            log.error("未取到appCode数据{}", context.getInterfaceInfo().getInterfaceName());
            throw new RuntimeException("未取到appCode数据");
        }
        checkJdosAppCode(jdosAppCode);
        docReportDto.setAppCode("J-dos-"+jdosAppCode);
        docReportDto.setAutoReport(2);
        docReportDto.setSync(true);
        return docReportDto;
    }

    /**
     * 校验应用code是否为jdos appCode
     *
     * @param jdosAppCode
     */
    private void checkJdosAppCode(String jdosAppCode) {
        JDosAppInfo info = null;
        info = jDosAppOpenService.queryJdosAppInfo(jdosAppCode, "cn");
        if (Objects.isNull(info)) {
            String errorMessage = "应用code【" + jdosAppCode + "】不是jdos应用code";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
}
