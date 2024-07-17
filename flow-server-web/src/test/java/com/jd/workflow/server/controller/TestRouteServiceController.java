package com.jd.workflow.server.controller;

import com.jd.workflow.server.ServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ServerApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT      // 启动容器使用随机端口号, 可以不用
)
@AutoConfigureMockMvc
@Slf4j
public class TestRouteServiceController {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mvc;

    // 获取上面的随机端口号
    @LocalServerPort
    private int port;

    @Test
    public void execute() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/api/routeService/{id}", 12).param("par01", "参数01")).andReturn();
        MockHttpServletResponse response = result.getResponse();
        String body = response.getContentAsString();
        System.out.println(body);
    }

}
