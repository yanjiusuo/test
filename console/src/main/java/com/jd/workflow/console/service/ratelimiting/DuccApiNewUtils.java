package com.jd.workflow.console.service.ratelimiting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.cjg.config.model.ducc.DuccResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 * 在类{@link com.jd.cjg.config.utils.DuccApiUtils}中，
 */
@Component
@Slf4j
public class DuccApiNewUtils {

    private static String BATCH_UPDATE_ITEMS = "http://%s/v1/namespace/%s/config/%s/profile/%s/items";
    private static RestTemplate restTemplate = new RestTemplate();


    @Value("${ducc.domain}")
    private String duccDomain;

    @Value("${ducc.appName}")
    private String duccAppName;

    @Value("${ducc.appToken}")
    private String duccAppToken;

    public DuccResponse modifyDuccItems(String site, String namespace, String configCode, String profileCode, Map<String, String> map) {
        try {
            String toUrl = String.format(BATCH_UPDATE_ITEMS, duccDomain, namespace, configCode, profileCode);
            HttpHeaders headers = new HttpHeaders();
            headers.add("application", duccAppName);
            headers.add("token", duccAppToken);
            HttpEntity<String> httpEntity = new HttpEntity<>(buildParam(map), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(toUrl, HttpMethod.PUT, httpEntity, String.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                log.info("推送ducc返回结果："+responseEntity.getBody());
                return JSON.parseObject(responseEntity.getBody(), new TypeReference<DuccResponse>() {});
            }
        } catch (RestClientException|IOException exception) {
            log.error("modify ducc items error", exception);
            exception.printStackTrace();
        }

        return new DuccResponse().setFailure();
    }

    private String buildParam(Map<String, String> configItems) throws IOException {
        StringWriter stringWriter = new StringWriter();
        Properties p = new Properties();
        for (Map.Entry<String, String> entry : configItems.entrySet()) {
            p.put(entry.getKey(), entry.getValue());
        }
        p.store(stringWriter, "批量添加或者修改");
        return stringWriter.toString();
    }




}
