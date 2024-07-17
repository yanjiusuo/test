package com.jd.workflow.flow.core.step;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
public class Environment {
    /**
     * 执行路径
     */
    Map<String/* tag */, List<EndpointUrl>> endpointUrls = new HashMap<>();
}
