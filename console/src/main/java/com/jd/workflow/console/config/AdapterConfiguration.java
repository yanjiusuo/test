package com.jd.workflow.console.config;
import com.jd.workflow.console.service.ducc.DuccBizConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * WorkerConfiguration
 *
 * @author wangxianghui6
 * @date 2022/3/23 10:28 AM
 */
@Configuration
@ComponentScan(basePackages = {
        "com.jd.workflow.console.config"
})
public class AdapterConfiguration {
    @Resource
    private AdapterProperties adapterProperties;

    @Bean
    public DuccBizConfigProperties duccBizConfigProperties() {
        return adapterProperties.getDuccBizConfig();
    }

}
