package com.jd.workflow.console.service.impl;

import com.jd.workflow.console.dto.QueryHttpAuthDetailReqDTO;
import com.jd.workflow.console.service.IHttpAuthService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * TODO
 *
 * @author xiaobei
 * @date 2022-08-03 15:10
 */
@ContextConfiguration(classes = HttpAuthServiceImplTest.Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class HttpAuthServiceImplTest {

    @Autowired
    private IHttpAuthService initHttpAuthService;

    /**
     * 项目详情索引
     */
    @Configuration
    @ImportResource(locations = "classpath:spring/spring.xml")
    @TestPropertySource(properties = {
    })
    public static class Config {

        @Bean
        public IHttpAuthService initHttpAuthService() {
            HttpAuthServiceImpl httpAuthService = new HttpAuthServiceImpl();
//            httpAuthService.setHttpAuthMapper(httpAuthMapper);
            return httpAuthService;
        }

    }

    @Test
    public void test() {
        QueryHttpAuthDetailReqDTO queryDTO = new QueryHttpAuthDetailReqDTO();
        queryDTO.setAppCode("qqqq");
//        List<HttpAuth>  httpAuthList = initHttpAuthService.queryList(queryDTO);
//        System.out.println(JSON.toJSONString(httpAuthList));
    }



}
