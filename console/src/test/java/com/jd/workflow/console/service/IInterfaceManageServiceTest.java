package com.jd.workflow.console.service;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.ConsoleApplication;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.doc.AppInterfaceCount;
import com.jd.workflow.console.dto.doc.InterfaceCountModel;
import com.jd.workflow.console.dto.doc.InterfaceTopCountModel;
import com.jd.workflow.console.dto.test.deeptest.CaseGroupCreateDto;
import com.jd.workflow.console.dto.test.deeptest.CaseInfo;
import com.jd.workflow.console.dto.test.deeptest.MemberAddDto;
import com.jd.workflow.console.dto.test.deeptest.TestResult;
import com.jd.workflow.console.dto.test.deeptest.step.CompareRuleInfo;
import com.jd.workflow.console.dto.test.deeptest.step.CompareScript;
import com.jd.workflow.console.dto.test.deeptest.step.DataGroup;
import com.jd.workflow.console.dto.test.deeptest.step.StepCaseDetail;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.service.group.GroupHelper;
import com.jd.workflow.console.service.impl.InterfaceManageServiceImpl;
import com.jd.workflow.console.service.test.IDeeptestRemoteCaller;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/22
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ConsoleApplication.class},                                          // spring boot 的启动类
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT      // 启动容器使用随机端口号, 可以不用
)
@Slf4j
public class IInterfaceManageServiceTest extends BaseTestCase {

    @Autowired
    private InterfaceManageServiceImpl interfaceManageService;

    @Autowired
    IDeeptestRemoteCaller testRemoteCaller;

    @Test
    public void testListInterfaceByIds() {
        List<Long> idLists = Lists.newArrayList();
        List<AppInterfaceCount> list = new ArrayList<>();
        List<InterfaceTopCountModel> topCountModels = new ArrayList<>();
        idLists.add(13464L);
        List<InterfaceManage> interfaceManageList = interfaceManageService.listInterfaceByIds(idLists);
        System.out.println("result:" + JSON.toJSONString(interfaceManageList));
        List<InterfaceCountModel> interfaceCountModelList = GroupHelper.toInterfaceSortModels(interfaceManageList, 1, list);
        System.out.println("result2:" + JSON.toJSONString(interfaceCountModelList));
        interfaceCountModelList.stream().map(countModel -> {
            InterfaceTopCountModel topCountModel = new InterfaceTopCountModel();
            BeanUtils.copyProperties(countModel, topCountModel);
            topCountModel.initKey();
            topCountModels.add(topCountModel);
            return topCountModel;
        }).collect(Collectors.toList());
        System.out.println("result3:" + JSON.toJSONString(topCountModels));
    }


    @Test
    public void addTestCaseTest() {
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setLineId(8724L);
        caseInfo.setSuiteId(98497L);
        caseInfo.setName("自动生成冒烟用例2");
        caseInfo.setEditWay(0);
        caseInfo.setCaseType("1");
        caseInfo.setOwner("chenyufeng18");
        caseInfo.setPriority(0);
        TestResult<CaseGroupCreateDto> result = testRemoteCaller.addTestCase(caseInfo);
        log.info("result:{}", JSON.toJSONString(result));
    }

    @Test
    public void addHttpStep() {
        StepCaseDetail stepCaseDetail = new StepCaseDetail();
        stepCaseDetail.setOwnerErp("chenyufeng18");
        stepCaseDetail.setApiErp("chenyufeng18");
        stepCaseDetail.setInterfaceType("HTTP");
        stepCaseDetail.setType(1);
        stepCaseDetail.setCaseId(597051L);
        stepCaseDetail.setName("addHttpStep2");
        stepCaseDetail.setLineId(8724L);
        stepCaseDetail.setInterfaceId(-1L);
        stepCaseDetail.setInterfaceName("http://11.158.77.112/testRelated/queryMethodGroup");
        stepCaseDetail.setRequestType("POST");
        stepCaseDetail.setJdTls(0);
        stepCaseDetail.setExpectResult("{\n    \"code\":200,\n    \"data\":{},\n    \"msg\":\"ok\"\n}");
        stepCaseDetail.setMatchType(1);
        stepCaseDetail.setColorConfig(0);
        stepCaseDetail.setUserAccount(0);
        stepCaseDetail.setSleeptime(0L);
        stepCaseDetail.setParallelNum(0);

        stepCaseDetail.setUrlParam("[{\"key\":\"param1\",\"value\":\"123\"}]");
        stepCaseDetail.setInputParam("{\n    \"body\":{\"item\":1}\n}");
        stepCaseDetail.setBodyType("json");
        CompareRuleInfo compareRuleInfo = new CompareRuleInfo();
        CompareScript compareScript = new CompareScript();
        compareScript.setScriptType("Groovy");
        compareScript.setCustomScript("");
        //compareRuleInfo.setCompareScript(compareScript);
        compareRuleInfo.setIgnoreOrder(0);
        compareRuleInfo.setIgnoreNull(0);
        compareRuleInfo.setIgnorePaths("");
        stepCaseDetail.setCompareRuleInfo(compareRuleInfo);

        List<DataGroup> dataGroupList = Lists.newArrayList();
        DataGroup dataGroup = new DataGroup();
        dataGroup.setSequence(1);
        dataGroup.setName("第1组");
        dataGroup.setUrlParam("[{\"key\":\"param1\",\"value\":\"123\"}]");
        dataGroup.setInputParam("{\n    \"body\":{\"item\":1}\n}");
        dataGroup.setMatchType(1);
        dataGroup.setExpectResult("{\n    \"code\":200,\n    \"data\":{},\n    \"msg\":\"ok\"\n}");
        dataGroup.setCompareRuleInfo(compareRuleInfo);

        dataGroupList.add(dataGroup);
        stepCaseDetail.setDataGroups(dataGroupList);


        TestResult<StepCaseDetail> result = testRemoteCaller.addTestCaseStep(stepCaseDetail);
        log.info("result:{}", JSON.toJSONString(result));
    }

    @Test
    public void addJsfStep() {
        StepCaseDetail stepCaseDetail = new StepCaseDetail();
        stepCaseDetail.setOwnerErp("chenyufeng18");
        stepCaseDetail.setApiErp("chenyufeng18");

        stepCaseDetail.setType(1);
        stepCaseDetail.setInterfaceId(-1L);
        stepCaseDetail.setJdTls(0);

        stepCaseDetail.setCaseId(597051L);
        stepCaseDetail.setLineId(8724L);


        stepCaseDetail.setName("addJSFStep1");
        stepCaseDetail.setInterfaceType("JSF");
        stepCaseDetail.setInterfaceName("com.jd.workflow.soap.example.jsf.entity.IPersonService");
        stepCaseDetail.setRequestType("");
        stepCaseDetail.setAlias("jsf-demo-auth");
        stepCaseDetail.setMethodName("save");

        stepCaseDetail.setExpectResult("{\n    \"data\":[],\n    \"code\":200\n}");
        stepCaseDetail.setInputParam("[\n    {\n        \"name\": \"java.lang.String\",\n        \"id\": \"java.lang.Long\",\n        \"age\": \"java.lang.Integer\"\n    }\n]");


//        stepCaseDetail.setUrlParam("[{\"key\":\"param1\",\"value\":\"123\"}]");

//        stepCaseDetail.setBodyType("json");

        stepCaseDetail.setMatchType(1);
        stepCaseDetail.setColorConfig(0);
        stepCaseDetail.setUserAccount(0);
        stepCaseDetail.setSleeptime(0L);
        stepCaseDetail.setParallelNum(0);


        CompareRuleInfo compareRuleInfo = new CompareRuleInfo();
        CompareScript compareScript = new CompareScript();
        compareScript.setScriptType("Groovy");
        compareScript.setCustomScript("");
        //compareRuleInfo.setCompareScript(compareScript);
        compareRuleInfo.setIgnoreOrder(0);
        compareRuleInfo.setIgnoreNull(0);
        compareRuleInfo.setIgnorePaths("");
        stepCaseDetail.setCompareRuleInfo(compareRuleInfo);

        List<DataGroup> dataGroupList = Lists.newArrayList();
        DataGroup dataGroup = new DataGroup();
        dataGroup.setSequence(1);
        dataGroup.setName("第1组");
//        dataGroup.setUrlParam("[{\"key\":\"param1\",\"value\":\"123\"}]");
        dataGroup.setInputParam("[\n    {\n        \"name\": \"java.lang.String\",\n        \"id\": \"java.lang.Long\",\n        \"age\": \"java.lang.Integer\"\n    }\n]");
        dataGroup.setMatchType(1);
        dataGroup.setExpectResult("{\n    \"data\":[],\n    \"code\":200\n}");
        dataGroup.setCompareRuleInfo(compareRuleInfo);

        dataGroupList.add(dataGroup);
        stepCaseDetail.setDataGroups(dataGroupList);


        TestResult<StepCaseDetail> result = testRemoteCaller.addTestCaseStep(stepCaseDetail);
        log.info("result:{}", JSON.toJSONString(result));
    }

    @Test
    public void addMember(){
        MemberAddDto memberAddDto = new MemberAddDto();
        memberAddDto.setModuleId(8724L);
        memberAddDto.setMemberErp("yanzengan");
        memberAddDto.setRoleId(2);
        memberAddDto.setScope(2);
        TestResult<MemberAddDto> result = testRemoteCaller.addMember(memberAddDto);
        log.info("deeptestRemoteCaller.addMember param:{},response:{}", JSON.toJSONString(memberAddDto), JSON.toJSONString(result));
    }
}