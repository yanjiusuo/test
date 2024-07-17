package com.jd.workflow.console.service.test;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;

import java.util.Map;

@Getter
@Setter
public class JagileApiConfig {


    @Value("${jagile.api.gw.host}")
    private String host;
    @Value("${jagile.api.security.appid}")
    private String appId;
    @Value("${jagile.api.security.token}")
    private String token;

    @Value("${jagile.acceptance.url}")
    private String acceptanceUrl;

    public String signature(long time) {
        Preconditions.checkArgument(this.appId != null && this.token != null, "行云网关appId/token 配置错误!");
        String signatureText = this.appId + time + token;
        return DigestUtils.md5DigestAsHex(signatureText.getBytes());
    }



    /**
     * 请求头中增加三个Header信息：appId、timestamp、signature
     *
     * @return Header Map
     */
    public Map<String, Object> prepareHeader(String optErp) {
        Map<String, Object> headersMap = Maps.newHashMapWithExpectedSize(4);
        headersMap.put("appId", this.appId);
        long time = System.currentTimeMillis();
        headersMap.put("signature", this.signature(time));
        headersMap.put("timestamp",time);
        headersMap.put("optErp",optErp);
        return headersMap;
    }

}
