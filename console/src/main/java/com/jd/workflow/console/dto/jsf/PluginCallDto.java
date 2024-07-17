package com.jd.workflow.console.dto.jsf;

import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

@Data
public class PluginCallDto {
    /**
     * 调用类型
     */
    JsfCallType callType = JsfCallType.generic;






    List<String> paramTypes;

    /**
     * json
     */
    String reqType = "json";
    /**
     * 入参数据，json格式
     */
    String inputData;

    String interfaceName;
    String methodName;
    String alias;

    /**
     * 环境 local 本地 test 测试 pre 预发 online 线上
     */
    String env;

    /**
     * 附件信息（公共参数）
     */
    List<? extends JsonType> attachments;


    /**
     * ip以及端口号
     */
    private String ip;

    private String userToken;

}
