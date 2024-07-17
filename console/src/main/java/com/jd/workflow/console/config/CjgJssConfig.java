package com.jd.workflow.console.config;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/22
 */

import com.jd.jss.Credential;
import com.jd.jss.JingdongStorageService;
import com.jd.jss.client.ClientConfig;
import com.jd.jss.http.Scheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/22 
 */
@Configuration
public class CjgJssConfig {
    /**
     *
     */
    @Value("${cjg.jss.accessKey}")
    private String accessKey;
    /**
     *
     */
    @Value("${cjg.jss.secretKey}")
    private String secretKey;
    /**
     *
     */
    @Value("${cjg.jss.hostName}")
    private String hostName;
    /**
     *
     */
    @Value("${cjg.jss.connectionTimeout}")
    private int connectionTimeout;

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     *
     */
    @Bean
    public JingdongStorageService getCjgJssClient() {
        JingdongStorageService jss = null;
        try {
            Credential credential = new Credential(accessKey, secretKey);
            ClientConfig config = new ClientConfig();
            //指定Protocol；不设置默认是http
            config.setProtocol(Scheme.HTTP);
            config.setEndpoint(hostName);
            config.setSocketTimeout(60000);
            config.setConnectionTimeout(connectionTimeout);
            jss = new JingdongStorageService(credential, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jss;
    }


}
