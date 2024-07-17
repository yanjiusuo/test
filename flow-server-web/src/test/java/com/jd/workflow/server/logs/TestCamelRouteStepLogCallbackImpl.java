package com.jd.workflow.server.logs;

import com.jd.businessworks.log.StepLogMessage;
import com.jd.workflow.server.ServerApplication;
import com.jd.workflow.server.dao.CamelStepLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ServerApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class TestCamelRouteStepLogCallbackImpl {

    @Autowired
    @InjectMocks
    private CamelRouteStepLogCallbackImpl camelRouteStepLogCallback;

    @Spy
    private CamelStepLogMapper camelStepLogMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(camelStepLogMapper.insert(ArgumentMatchers.any())).thenReturn(10);
    }

    @Test
    public void testLog() {
        String routeXml = "A";
        StepLogMessage stepLogMessage = StepLogMessage.builder()
                .businessName("businessName")
                .methodName("methodName")
                .data(routeXml).build();
        camelRouteStepLogCallback.log(stepLogMessage);
    }
}
