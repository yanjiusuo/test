package com.jd.workflow.console.dto.doc;

import com.jd.businessworks.domain.FlowBeanInfo;
import com.jd.workflow.console.dto.HttpMethodModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JavaBeanReportDto {
    /**
     * 应用编码
     */
    String appCode;
    /**
     * 应用密钥
     */
    String appSecret;
    /**
     * ip列表
     */
    String ip;
    /**
     * java bean配置信息
     */
    List<FlowBeanInfo> beanInfos;
}
