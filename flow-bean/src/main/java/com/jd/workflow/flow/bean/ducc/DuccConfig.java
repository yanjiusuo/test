package com.jd.workflow.flow.bean.ducc;

import com.jd.workflow.flow.core.bean.annotation.FlowConfigParam;
import lombok.Data;

@Data
public class DuccConfig {
    @FlowConfigParam(label="ducc配置链接",required = true,validator = DuccUrlValidator.class)
    String url;

}
