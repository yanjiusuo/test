package com.jd.workflow.console.config;

import com.jd.workflow.console.service.ducc.DuccBizConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AdapterProperties
 *
 * @author wangxianghui6
 * @date 2022/3/2 8:48 PM
 */
@ConfigurationProperties(prefix = "up.adapter")
@Component
public class AdapterProperties {

    private DuccBizConfigProperties duccBizConfig;

    public DuccBizConfigProperties getDuccBizConfig() {
        return duccBizConfig;
    }

    public void setDuccBizConfig(DuccBizConfigProperties duccBizConfig) {
        this.duccBizConfig = duccBizConfig;
    }
}
