package com.jd.workflow.console.service.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/21
 */

import com.alibaba.fastjson.JSON;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.entity.JsfAlias;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class JsfAliasServiceImplTest extends BaseTestCase {

    @Autowired
    private JsfAliasServiceImpl jsfAliasService;


    @Test
    public void queryAliasFromJsf() {
        List<JsfAlias> jsfAliasList = jsfAliasService.queryAliasFromJsf(13528L);
        System.out.println("result: " + JSON.toJSONString(jsfAliasList));

    }

    @Test
    public void initAliasAllById() {
        Boolean result = jsfAliasService.initAliasAllById(13528L);
        System.out.println("result: " + result);
    }

}
