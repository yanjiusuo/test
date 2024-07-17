package com.jd.workflow.flow.bean.jimdb;

import com.jd.workflow.flow.bean.ducc.DuccUrlValidator;
import com.jd.workflow.flow.core.bean.annotation.FlowConfigParam;
import lombok.Data;

@Data
public class JimdbConfig {
    @FlowConfigParam(label="jimdb配置链接",required = true,validator = JImdbUrlValidator.class)
    String url;
    String serviceEndpoint;
}
