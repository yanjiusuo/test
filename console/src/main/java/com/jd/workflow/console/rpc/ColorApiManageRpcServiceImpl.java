package com.jd.workflow.console.rpc;

import com.jd.workflow.console.entity.ColorGatewayParam;
import com.jd.workflow.console.service.IColorGatewayServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.color.ColorApiParamDto;
import com.jd.workflow.server.service.ColorApiManageRpcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ColorApiManageRpcServiceImpl implements ColorApiManageRpcService {

    @Resource
    IColorGatewayServiceImpl colorGatewayService;

    @Resource
    MethodManageServiceImpl methodManageService;

    public QueryResult<Integer> updateGateWayParam(List<ColorApiParamDto> params, String methodId) {
        if (CollectionUtils.isEmpty(params) || StringUtils.isBlank(methodId)||StringUtils.isBlank(params.get(0).getName())) {
            return QueryResult.error("入参为空");
        }
        List<ColorGatewayParam> colorParams = params.stream().map(it -> {
            ColorGatewayParam colorParam = new ColorGatewayParam();
            BeanUtils.copyProperties(it, colorParam);
            return colorParam;
        }).collect(Collectors.toList());
        Integer num = null;
        try {
            num = colorGatewayService.updateColorParam(colorParams, methodId);
        } catch (Exception e) {
            return QueryResult.error("更新异常");
        }
        return QueryResult.buildSuccessResult(num);
    }




}
