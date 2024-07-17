package com.jd.workflow.console.service.color;
import com.jd.fastjson.annotation.JSONType;
import com.jd.workflow.server.dto.color.ColorApiParamDto;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ColorApiParam {
    private String api;
    /**
     * 管理员
     */
    private String owner;
    /**
     * 域名
     */
    private String clusterName;
    /**
     * 描述信息
     */
    private String description;
    /**
     * 前缀 http
     */
    private String protocol;

    private String httpBackendAddress;
    /**
     * 请求path
     */
    private String httpPath;
    private int timeout;
    /**
     * 网关参数+自定义参数
     */
    private List<GateWayParam> params;
    /**
     * color接口成员
     */
    private List<String> members;
    /**
     * color接口成员
     */
    private String participant;

    //已经授权的appId集合
    private List<String> appIds;

@Data
    public class GateWayParam {
        private List<ColorApiParamDto> gatewayParam;
        private List<ColorApiParamDto> customParam;

    }
}
