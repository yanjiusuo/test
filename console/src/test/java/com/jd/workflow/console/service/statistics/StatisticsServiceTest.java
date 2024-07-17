package com.jd.workflow.console.service.statistics;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import com.jd.rcenter.businessworks.plugins.PluginsClient;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.dto.dashboard.InterfaceHealthDTO;
import com.jd.workflow.console.dto.dashboard.UserDashboardDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/12
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class StatisticsServiceTest extends BaseTestCase {

    @Autowired
    private StatisticsService statisticsService;

    @Test
    public void testGetInterfaceStatistics() {
        UserDashboardDTO userDashboardDTO = statisticsService.getPersonInterfaceStatistics("wangjingfang3");
        System.out.println("result:" + JSON.toJSONString(userDashboardDTO));
    }

    @Test
    public void testGetHeathStatistics() {
        InterfaceHealthDTO interfaceHealthDTO = statisticsService.getHeathStatistics("wangjingfang3");
        System.out.println("result:" + JSON.toJSONString(interfaceHealthDTO));
    }

    @Test
    public void getMethodStatusStatistics() {

        InterfaceHealthDTO interfaceHealthDTO = statisticsService.getMethodStatusStatistics("wangjingfang3");
        System.out.println("result:" + JSON.toJSONString(interfaceHealthDTO));
    }

    @Test
    public void getTableHeadInfoTest(){
        String result = getTableHeadInfo("japiconfig", "module", "guide");
    }

    private String getTableHeadInfo(String headRuleKey, String headKey, String headValue) {
        log.info("getTableHeadInfo 入参：ruleKey：{},key:{},value:{}", headRuleKey, headKey, headValue);
        String data = null;

        try {
            PluginsClient.PluginParam pluginParam = new PluginsClient.PluginParam(headRuleKey);
            pluginParam.add(headKey, headValue);
            log.info("getTableHeadInfo 入参：pluginParam：{}", JSONObject.toJSONString(pluginParam));
            data = (String) PluginsClient.send(pluginParam);
        } catch (Exception e) {

            log.error("getTableHeadInfo 异常：", e);
        }
        log.info("getTableHeadInfo 出参：data：{}", data);
        return data;
    }
}