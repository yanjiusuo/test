package com.jd.workflow.console.dto.jsf;

import com.jd.workflow.console.dto.ParamDepDto;
import com.jd.workflow.console.dto.ParamOptDto;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class NewJsfDebugDto  {
    /**
     * 调用类型
     */
    JsfCallType callType;
    /**
     * callType为jar时，入参字段
     */
    String jarInputData;
    /**
     * 调用类型为jar时，maven坐标信息
     */
    MavenJarLocation mavenLocation;

    /**
     * jsf方法id
     */
    Long methodId;

    String url;
    /**
     * 输入数据
     */
    List<? extends JsonType> input;

    List<String> paramTypes;

    List<JsonType> colorHeaders;

    List<JsonType> colorInputParam;
    /**
     *
     */
    String reqType;
    /**
     * 入参数据，json格式
     */
    List inputData;

    String interfaceName;
    String methodName;
    String alias;
    /**
     * 站点信息
     */
    String site;
    /**
     * 环境 local 本地 test 测试 pre 预发 online 线上
     */
    String env;
    /**
     * 环境中文名
     */
    String envName;
    /**
     * 附件信息（公共参数）
     */
    List<? extends JsonType> attachments;
    String protocol;

    /**
     * 本地ip
     */
    private String ip;

    Boolean isColor;

    String methodType;


    /**
     * 前置操作
     */
    List<ParamOptDto> preOpt;

    /**
     * 后置操作
     */
    List<ParamOptDto> postOpt;

    /**
     * 参数依赖
     */
    List<ParamDepDto> paramDep;


}
