package com.jd.workflow.console.service;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.dto.doc.GroupHttpData;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yza
 * @description
 * @date 2024/1/17
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class MeasureDataServiceTest extends BaseTestCase {

    @Autowired
    private IMeasureDataService measureDataService;

    @Test
    public void testSaveReportDataLog() {
        List<GroupHttpData<MethodManage>> group2MethodManage = new ArrayList<>();
        GroupHttpData<MethodManage> httpData = new GroupHttpData<>();
        httpData.setGroupName("asd");
        httpData.setGroupDesc("asdas");
        List<MethodManage> manageList = new ArrayList<>();
        MethodManage manage1 = new MethodManage();
        manage1.setAppId(1L);
        manageList.add(manage1);
        MethodManage manage2 = new MethodManage();
        manage1.setAppId(2L);
        manageList.add(manage2);
        httpData.setHttpData(manageList);
        group2MethodManage.add(httpData);
        measureDataService.saveReportDataLog(1, group2MethodManage, "zhangsan");
    }
}
