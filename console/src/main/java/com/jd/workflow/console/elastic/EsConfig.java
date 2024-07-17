package com.jd.workflow.console.elastic;

import com.jd.workflow.soap.common.util.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class EsConfig extends AbstractElasticsearchConfiguration {
    @Value("${es.userName}")
    private String userName;
    @Value("${es.password}")
    private String password;
    @Value("${es.ipAndPorts}")
    private String ipAndPorts;
    @Bean(name = "esClient")
    public RestHighLevelClient apmEsClient() {
        return getEsClient();
    }

    private RestHighLevelClient getEsClient() {
        List<HttpHost> httpHosts = new ArrayList<>();
        List<String> ipAndPortList = StringHelper.split(ipAndPorts, ",");
        for (String s : ipAndPortList) {
            List<String> result = StringHelper.split(s, ":");
            httpHosts.add(new HttpHost(result.get(0), Integer.parseInt(result.get(1)), "http"));
        }
        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));
        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
            // 有密码
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        return new RestHighLevelClient(builder);
    }

    @Override
    public RestHighLevelClient elasticsearchClient() {
        return getEsClient();
    }
}
