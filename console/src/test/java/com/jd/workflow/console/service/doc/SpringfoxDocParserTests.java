package com.jd.workflow.console.service.doc;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.service.doc.app.TestApplication;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.*;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {TestApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class SpringfoxDocParserTests extends BaseTestCase {
    public String getSwagger() throws URISyntaxException, IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet();
        get.setURI(new URI("http://localhost:8080/v2/api-docs"));
        final CloseableHttpResponse response = client.execute(get);
        final String swagger = EntityUtils.toString(response.getEntity());
        log.info("swagger_docs={}",swagger);
        return swagger;
    }
    @Test
    public void testParseSwagger2() throws URISyntaxException, IOException {

        String swaggerJson = getSwagger();
        Swagger swagger = new SwaggerParser().parse(swaggerJson);
        for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            for (Operation operation : pathEntry.getValue().getOperations()) {
                HttpMethodModel.HttpMethodInput methodInput = new HttpMethodModel.HttpMethodInput();
                List<String> groupTags = operation.getTags();
                for (Parameter parameter : operation.getParameters()) {
                    if("query".equals(parameter.getIn())){

                    }
                }
            }
        }
    }

}
