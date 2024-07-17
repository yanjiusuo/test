package com.jd.workflow.console.service.errorcode;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.dto.errorcode.BindPropParam;
import com.jd.workflow.console.entity.errorcode.REnumMethodProp;
import junit.framework.TestCase;
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
 * @Date: 2023/8/24
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class REnumMethodPropServiceImplTest extends BaseTestCase {

    @Autowired
    private REnumMethodPropServiceImpl rEnumMethodPropService;

    @Test
    public void testBindEnum() {
    }

    @Test
    public void testBindEnumList() {
        BindPropParam bindPropParam = new BindPropParam();
        bindPropParam.setEnumType(0);
        bindPropParam.setAppId(7292L);
        bindPropParam.setId(null);
        bindPropParam.setProp("");
        List<REnumMethodProp> rEnumMethodPropList = rEnumMethodPropService.bindEnumList(bindPropParam);
        System.out.println("result:" + JSON.toJSONString(rEnumMethodPropList));
    }

    @Test
    public void testDeleteBindEnum() {
    }
}