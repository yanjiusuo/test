package com.jd.workflow.console.service;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.MethodPropDTO;
import com.jd.workflow.console.dto.errorcode.MethodPropParam;
import com.jd.workflow.console.dto.errorcode.SaveEnumDTO;
import com.jd.workflow.console.service.errorcode.IEnumPropService;
import com.jd.workflow.soap.common.util.JsonUtils;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/17
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class IMethodManageServiceTest extends BaseTestCase {


    @Autowired
    private IMethodManageService methodManageService;
    @Autowired
    private IEnumPropService enumPropService;


    @Test
    public void testGetTopProp() {
        MethodPropParam methodPropParam = new MethodPropParam();
        methodPropParam.setSize(20L);
        methodPropParam.setCurrent(1L);
        methodPropParam.setAppId(7291L);
        methodPropParam.setProp("createAt");
        List<MethodPropDTO> methodPropDTOList = methodManageService.getTopProp(methodPropParam);
        System.out.println("result:" + JSON.toJSONString(methodPropDTOList));
    }

    @Test
    public void saveEnumProps() {

        SaveEnumDTO saveEnumDTO = JSON.parseObject("{\"appId\":7246,\"enumPropDTOList\":[{\"propCode\":\"200\",\"propName\":\"请求成功\",\"propDesc\":\"请求成功\",\"propSolution\":\"sdsds\"}]}", SaveEnumDTO.class);
        Boolean result = enumPropService.saveEnumProps(saveEnumDTO);
        System.out.println("result:" + JSON.toJSONString(result));
    }

    @Test
    public void getEntity() {
        UserInfoInSession user = new UserInfoInSession();
        user.setUserId("wangjingfang3");
        UserSessionLocal.setUser(user);
        MethodManageDTO ref = methodManageService.getEntity("384764");
        System.out.println("result:" + JSON.toJSONString(ref));
    }

    @Test
    public void edit() {
        String content = "{\"type\":\"http\",\"input\":{\"reqType\":\"json\",\"url\":\"/mockInfo/mockExample\",\"method\":\"GET\",\"body\":[],\"headers\":[],\"params\":[],\"path\":[]},\"output\":{\"body\":[{\"name\":\"root\",\"type\":\"ref\",\"genericTypes\":[{\"name\":\"root\",\"type\":\"array\",\"typeVariableName\":\"T\",\"genericTypes\":[{\"name\":\"root\",\"type\":\"ref\",\"typeVariableName\":\"E\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto\",\"children\":[{\"name\":\"type\",\"type\":\"string\",\"desc\":\"mock名称\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"mock描述\",\"className\":\"java.lang.String\"},{\"name\":\"function\",\"type\":\"string\",\"desc\":\"mock函数\",\"className\":\"java.lang.String\"},{\"name\":\"examples\",\"type\":\"array\",\"genericTypes\":[{\"name\":\"root\",\"type\":\"ref\",\"typeVariableName\":\"E\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\"}],\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"ref\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\"}]}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto\"}],\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"ref\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto\",\"children\":[{\"name\":\"type\",\"type\":\"string\",\"desc\":\"mock名称\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"mock描述\",\"className\":\"java.lang.String\"},{\"name\":\"function\",\"type\":\"string\",\"desc\":\"mock函数\",\"className\":\"java.lang.String\"},{\"name\":\"examples\",\"type\":\"array\",\"genericTypes\":[{\"name\":\"root\",\"type\":\"ref\",\"typeVariableName\":\"E\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\"}],\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"ref\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\"}]}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto\"}]}],\"className\":\"com.jd.workflow.console.base.CommonResult\",\"children\":[{\"name\":\"code\",\"type\":\"integer\",\"desc\":\"返回值:0为成功，非0为失败\",\"className\":\"java.lang.Integer\",\"exprType\":\"expr\",\"value\":\"\"},{\"name\":\"message\",\"type\":\"string\",\"desc\":\"错误信息\",\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\"},{\"name\":\"data\",\"type\":\"array\",\"desc\":\"返回数据\",\"typeVariableName\":\"T\",\"genericTypes\":[{\"name\":\"root\",\"type\":\"ref\",\"typeVariableName\":\"E\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto\",\"children\":[{\"name\":\"type\",\"type\":\"string\",\"desc\":\"mock名称\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"mock描述\",\"className\":\"java.lang.String\"},{\"name\":\"function\",\"type\":\"string\",\"desc\":\"mock函数\",\"className\":\"java.lang.String\"},{\"name\":\"examples\",\"type\":\"array\",\"genericTypes\":[{\"name\":\"root\",\"type\":\"ref\",\"typeVariableName\":\"E\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\"}],\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"ref\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\"}]}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto\"}],\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"ref\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto\",\"children\":[{\"name\":\"type\",\"type\":\"string\",\"desc\":\"mock名称\",\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"mock描述\",\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\"},{\"name\":\"function\",\"type\":\"string\",\"desc\":\"mock函数啊啊啊@AppUserTypeEnum\",\"_delta\":{\"desc\":\"mock函数啊啊啊@错误码\"},\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\",\"enumId\":44},{\"name\":\"examples\",\"type\":\"array\",\"genericTypes\":[{\"name\":\"root\",\"type\":\"ref\",\"typeVariableName\":\"E\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\"}],\"className\":\"java.util.List\",\"children\":[{\"name\":\"$$0\",\"type\":\"ref\",\"className\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"children\":[{\"name\":\"example\",\"type\":\"string\",\"desc\":\"mock示例\",\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\"},{\"name\":\"desc\",\"type\":\"string\",\"desc\":\"描述\",\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\"},{\"name\":\"result\",\"type\":\"string\",\"desc\":\"示例结果\",\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto.MockExample\",\"exprType\":\"expr\",\"value\":\"\"}],\"exprType\":\"expr\",\"value\":\"\"}],\"refName\":\"com.jd.workflow.console.dto.mock.MockDocDto\",\"exprType\":\"expr\",\"value\":\"\"}],\"exprType\":\"expr\",\"value\":\"\"},{\"name\":\"traceId\",\"type\":\"string\",\"desc\":\"日志跟踪uuid\",\"className\":\"java.lang.String\",\"exprType\":\"expr\",\"value\":\"\"}],\"refName\":\"com.jd.workflow.console.base.CommonResult\",\"exprType\":\"expr\",\"value\":\"\"}],\"headers\":[]}}";
        HttpMethodModel httpMethodModel = JsonUtils.parse(content, HttpMethodModel.class);

        String model = "{\"interfaceId\":\"16177\",\"type\":1,\"name\":\"获取mock示例\",\"methodCode\":\"mockDto\",\"httpMethod\":\"GET\",\"path\":\"/mockInfo/mockExample\",\"content\":\"\",\"docConfig\":{\"inputExample\":\"{}\",\"outputExample\":\"{\\n  \\\"code\\\": 0,\\n  \\\"message\\\": \\\"HULQYpxW\\\",\\n  \\\"data\\\": [\\n    {\\n      \\\"type\\\": \\\"s\\\",\\n      \\\"desc\\\": \\\"tIwzhDRLdxfQxkWdDE\\\",\\n      \\\"function\\\": \\\"YOPSyMph\\\",\\n      \\\"examples\\\": [\\n        {\\n          \\\"example\\\": \\\"QWeWQVDeerfbKvQaFYG\\\",\\n          \\\"desc\\\": \\\"ptezrl\\\",\\n          \\\"result\\\": \\\"bUKyicUHMB\\\"\\n        }\\n      ]\\n    }\\n  ],\\n  \\\"traceId\\\": \\\"BCmCkASCzFyxeK\\\"\\n}\",\"docType\":\"md\"},\"docInfo\":\"获取mock示例\",\"id\":\"384764\"}";
        MethodManageDTO methodManageDTO = JsonUtils.parse(model, MethodManageDTO.class);
        methodManageDTO.setContent(content);
        methodManageService.edit(methodManageDTO);
        System.out.println("result:");

    }

}