package com.jd.workflow.console.service.doc;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.model.ApiModelGroup;
import com.jd.workflow.console.service.doc.app.jsf.JsfTestInterface;
import com.jd.workflow.console.service.doc.impl.DocReportServiceImpl;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.parser.ClassParser;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class DocReportServiceImplTests  extends BaseTestCase {
    @Autowired
    DocReportServiceImpl docReportService;
    AppInfo appInfo;
    @Before
    public void before(){
        appInfo = new AppInfo();
        appInfo.setId(1L);
        appInfo.setAppCode("wjfReportCode");
        appInfo.setAppName("应用测试");
        appInfo.setAppSecret("d49743d20b0528d31fd0aa886db44655");
    }
    @Test
    public void testMergeHttpDoc(){
        DocReportDto dto = new DocReportDto();
        dto.setAppCode(appInfo.getAppCode());
        dto.setAppSecret(appInfo.getAppSecret());
        dto.setIp(null);
        String swaggerJson = getResourceContent("classpath:swagger/swagger2-openapi.json");

        dto.setSwagger(swaggerJson);
        docReportService.mergeHttpDoc(appInfo,dto);

    }
    @Test
    public void testMergeAuthKey(){
        DocReportDto dto = new DocReportDto();
        dto.setAppCode(appInfo.getAppCode());
        dto.setAppSecret(appInfo.getAppSecret());
        dto.setIp(null);
        String swaggerJson = getResourceContent("classpath:swagger/http-auth-key.json");

        dto.setSwagger(swaggerJson);
        final List<MethodManage> methodManages = docReportService.mergeHttpDoc(appInfo, dto);
        processAuthKey(methodManages);

    }
    @Test
    public void insertGroupTests(){

        docReportService = new DocReportServiceImpl();
        MethodGroupTreeModel treeModel = new MethodGroupTreeModel();
        List<ApiModelGroup> groups = new ArrayList<>();
        docReportService.collectAllGroups(1L,"com.workflow",groups);
        Map<String, ApiModelGroup> map = groups.stream().collect(Collectors.toMap(ApiModelGroup::getFullName, item->{
            return item;
        }));
        docReportService.insertGroup(treeModel,map,groups.get(0));
    }
    @Test
    public void testMergeHttpDataAuthKey(){
        DocReportDto dto = new DocReportDto();
        dto.setAppCode(appInfo.getAppCode());
        dto.setAppSecret(appInfo.getAppSecret());
        dto.setIp(null);
        String swaggerJson = getResourceContent("classpath:swagger/http-data-auth-key.json");
        dto.setHttpAppCode("wjfTest");
        dto.setSwagger(swaggerJson);
        final List<MethodManage> methodManages = docReportService.mergeHttpDoc(appInfo, dto);
        processAuthKey(methodManages);

    }

    private void processAuthKey(List<MethodManage> methodManages) {

    }

    @Test
    public void testMergeLowcodeDoc(){
        DocReportDto dto = new DocReportDto();
        dto.setAppCode(appInfo.getAppCode());
        dto.setHttpAppCode("lowcode");
        dto.setAppSecret(appInfo.getAppSecret());
        dto.setIp(null);
        String swaggerJson = getResourceContent("classpath:swagger/lowcode-swagger.json");

        dto.setSwagger(swaggerJson);
        docReportService.mergeHttpDoc(appInfo,dto);

    }
    @Test
    public void testMergeJsfDoc(){
        DocReportDto dto = new DocReportDto();
        dto.setAppCode(appInfo.getAppCode());
        dto.setAppSecret(appInfo.getAppSecret());
        dto.setIp(null);

        ClassMetadata metadata = new ClassMetadata();
        metadata.setClassName(JsfTestInterface.class.getName());
        for (Method method : JsfTestInterface.class.getMethods()) {
            final MethodMetadata methodMetadata = ClassParser.buildMethodInfo(method);
            metadata.getMethods().add(methodMetadata);
        }
        dto.setJsfDocs(JsonUtils.toJSONString(Collections.singletonList(metadata)));
        log.info("jsonStr={}",JsonUtils.toJSONString(Collections.singletonList(metadata)));
        docReportService.mergeJsfDoc(appInfo,dto);

    }

}
