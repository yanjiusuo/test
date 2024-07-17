package com.jd.workflow.console.service;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.dto.flow.param.HttpOutputExt;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class DebugServiceTest extends BaseTestCase {

    @Autowired
    private DebugService debugService;

    @Test
    public void testDebugHttp() {
        String inputStr = "{\"type\":\"http\",\"methodId\":307189,\"site\":\"test\",\"envName\":\"测试环境\",\"input\":{\"method\":\"POST\",\"fullUrl\":\"http://11.158.77.112/debug/uploadFile\",\"reqType\":\"json\",\"path\":[],\"params\":[{\"name\":\"multipartFile\",\"type\":\"file\",\"className\":null,\"required\":true,\"exprType\":\"expr\",\"value\":\"http://storage.jd.local/lht/data2.zip?Expires=1703872720&AccessKey=ZDcl7q0ygh8Asm9a&Signature=%2Fmn32VZD2MxGzx3DZWM5U2ud07s%3D\"}],\"headers\":[{\"name\":\"Cookie\",\"value\":\"jdd69fo72b8lfeoe=SHRSDVJ4XKSVDJMZFV4BBMW3HTESCERHOK57CKI6BYVCABBOIBPTF3EILNQL5AYXXYQZ7PQHAYVBHDUQJZA3DCIGDU; __jda=137720036.1649750385746770236405.1649750386.1692179141.1692275138.42; __jdu=1649750385746770236405; jd.erp.lang=zh_CN; __jdc=137720036; ssa.global.ticket=59094F51922D7231D332ADBD05436D03; __jdv=162094517|direct|-|none|-|1692179140596; 3AB9D23F7A4B3C9B=554O2V3IMCC6AX5IH72EXNSFYUWIEQDQVRTGU3TFIN7A3DXHK55N5CGZGPSN6ILVF6BBRU2PW4NYJY3TZQLYQLPY3E; sso.jd.com=BJ.A6AFD6D28302F1C7D9E5EB1D3E94987D4120230823112403; ssa.test=59173b6c2e72c23e686dbdbf8889d06b958572f7ad47c5429a10892abe2ab652a45571e67ba7edc60bf58b49a5ea54e434e166b279c942b1c34f2c77b09f9cb7da0a1f47672552ed0bf31be4ab7d1a91e982df21440ec5232ec10d7e29fce9585a2d3c6d33b6870a2f5fe394c5bea6d0265f11f97bf54129c3029ef394093a15\",\"enabled\":false,\"key\":\"1689579639561-0\",\"type\":\"string\",\"exprType\":\"expr\"}],\"body\":[],\"bodyData\":{}}}";
        HttpDebugDto dto = JSON.parseObject(inputStr, HttpDebugDto.class);
        JsonType jsonType = new SimpleJsonType(SimpleParamType.FILE);
        jsonType.setValue("http://storage.jd.local/lht/data2.zip?Expires=2803883007&AccessKey=ZDcl7q0ygh8Asm9a&Signature=XJyasAhguHjZDIJtxjqI8oCcUXQ%3D");
        jsonType.setName("multipartFile");
        List<JsonType> jsonTypeList = Lists.newArrayList();
        jsonTypeList.add(jsonType);
        dto.getInput().setParams(jsonTypeList);
        List<JsonType> headerList = Lists.newArrayList();
        JsonType header = new SimpleJsonType(SimpleParamType.STRING);
        header.setName("Cookie");
        header.setValue("jdd69fo72b8lfeoe=SHRSDVJ4XKSVDJMZFV4BBMW3HTESCERHOK57CKI6BYVCABBOIBPTF3EILNQL5AYXXYQZ7PQHAYVBHDUQJZA3DCIGDU; __jda=137720036.1649750385746770236405.1649750386.1692179141.1692275138.42; __jdu=1649750385746770236405; jd.erp.lang=zh_CN; __jdc=137720036; ssa.global.ticket=59094F51922D7231D332ADBD05436D03; __jdv=162094517|direct|-|none|-|1692179140596; 3AB9D23F7A4B3C9B=554O2V3IMCC6AX5IH72EXNSFYUWIEQDQVRTGU3TFIN7A3DXHK55N5CGZGPSN6ILVF6BBRU2PW4NYJY3TZQLYQLPY3E; sso.jd.com=BJ.A6AFD6D28302F1C7D9E5EB1D3E94987D4120230823112403; ssa.test=59173b6c2e72c23e686dbdbf8889d06b958572f7ad47c5429a10892abe2ab652a45571e67ba7edc60bf58b49a5ea54e434e166b279c942b1c34f2c77b09f9cb7da0a1f47672552ed0bf31be4ab7d1a91e982df21440ec5232ec10d7e29fce9585a2d3c6d33b6870a2f5fe394c5bea6d0265f11f97bf54129c3029ef394093a15");
        headerList.add(header);
        dto.getInput().setHeaders(headerList);

        HttpOutputExt httpOutputExt = debugService.debugHttp(dto,null);
        System.out.println("result:" + JSON.toJSONString(httpOutputExt));
    }
}