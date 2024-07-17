package com.jd.workflow.console.service.depend.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jd.common.util.StringUtils;
import com.jd.jsf.open.api.ProviderService;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.QueryProviderRequest;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.service.depend.ProviderServiceWrap;
import com.jd.workflow.console.service.parser.ParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/28
 */
@Service
@Slf4j
public class ProviderServiceWrapImpl implements ProviderServiceWrap {

    @Autowired
    private ProviderService providerService;

    /**
     * @param interfaceFullName
     * @return
     */
    @Override
    public List<Server> query(String interfaceFullName) {
        List<Server> ret = null;
        try {
            QueryProviderRequest queryProviderRequest = JSFArgBuilder.buildSetRequestInfo(new QueryProviderRequest());
            queryProviderRequest.setInterfaceName(interfaceFullName);
            Result<List<Server>> query = providerService.query(queryProviderRequest);
            ret = query.getData();
        } catch (Exception e) {
            log.error("ProviderServiceWrapImpl.lookup Exception ", e);
        }
        return ret;
    }

    /**
     * 获取出现次数最多的 AppName
     *
     * @param interfaceFullName
     * @return
     */
    public String getProviderJdosAppCode(String interfaceFullName) {
        String appCode = "";
        List<Server> lookup = query(interfaceFullName);
        if (CollectionUtils.isNotEmpty(lookup)) {
            Optional<String> optional = lookup.stream().map(server -> {
                //正则匹配出应用code
                return getJdosAppCode(server.getAttrUrl());
            }).filter(data -> {
                return StringUtils.isNotBlank(data);
            }).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
            appCode = optional.orElse("");
            appCode = appCode.replace("jdos_", ""); //得到真正的jdos应用code
        }
        return appCode;
    }

    /**
     * 获取jdosAppCode
     *
     * @param dataStr
     * @return
     */
    public static String getJdosAppCode(String dataStr) {
        return ParserUtils.doPattern(dataStr, "appName=(.*?),");
    }

}
