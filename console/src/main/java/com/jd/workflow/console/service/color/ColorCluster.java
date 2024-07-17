package com.jd.workflow.console.service.color;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ColorCluster {

    private String name;
    private String domain;
    /**
     * prod/test
     */
    private String env;
    /**
     * "外网"/"内网"
     */
    private String netType;
}
