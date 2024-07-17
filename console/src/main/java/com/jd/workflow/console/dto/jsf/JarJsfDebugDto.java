package com.jd.workflow.console.dto.jsf;

import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

@Data
public class JarJsfDebugDto {
    MavenJarLocation location;
    /**
     * jsf方法id
     */
    Long methodId;
    String url;

    /**
     * 入参数据，json格式
     */
    String inputData;

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
     * 附件信息
     */
    List<? extends JsonType> attachments;
    String protocol;

    /**
     * 本地ip
     */
    private String ip;

}
