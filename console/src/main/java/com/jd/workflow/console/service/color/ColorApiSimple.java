package com.jd.workflow.console.service.color;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ColorApiSimple {
    /**
     * "/hi/hello",
     */
    private String path;

    private String functionId;
    /**
     * "wardentest-testapi.apigatewaycn.svc.hc04.n.jd.local（不建议）",
     */
    private String zone;
    /**
     * "bq-gatewaytestservice-bqgatewaytest.bq-color.svc.sh01.n.jd.local"
     */
    private String backendAddress;
}
