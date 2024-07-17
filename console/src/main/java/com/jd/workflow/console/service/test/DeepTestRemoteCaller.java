package com.jd.workflow.console.service.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dto.share.CaseBatchExecuteDto;
import com.jd.workflow.console.dto.share.CaseBatchExecuteResult;
import com.jd.workflow.console.dto.test.deeptest.*;
import com.jd.workflow.console.dto.test.deeptest.step.StepCaseDetail;
import com.jd.workflow.metrics.client.RequestClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用deeptest服务
 */

public class DeepTestRemoteCaller implements IDeeptestRemoteCaller {
    String url = "http://test.intest-client.jd.com";
    RequestClient client = null;
    String token = "65d04a406985f5cf57372c5df38168f16f6e21347435ce0b";
    String appId = "";

    public DeepTestRemoteCaller() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> headers = new HashMap<>();
        if (!StringUtils.isEmpty(appId)) {
            headers.put("appid", appId);
        }

        headers.put("token", token);
        client = new RequestClient(url, headers);
    }

    /**
     * 创建模块
     *
     * @param createModule
     * @return
     */
    public TestResult<ModuleCreateDto> createModule(ModuleCreateDto createModule) {
        return client.post("/online/addModule", null, createModule, new TypeReference<TestResult<ModuleCreateDto>>() {
        });
    }

    /**
     * 执行功能用例集合
     *
     * @param caseBatchExecuteDto
     * @return
     */
    public TestResult<Long> executeCaseSuite(CaseBatchExecuteDto caseBatchExecuteDto) {
        return client.post("/online/executeCaseSuite", null, caseBatchExecuteDto, new TypeReference<TestResult<Long>>() {
        });
    }

    /**
     * 新增用例集
     *
     * @param createDto
     * @return
     */
    public TestResult<CaseGroupCreateDto> addCatalogTree(CaseGroupCreateDto createDto) {
        return client.post("/online/addCatalogTree", null, createDto, new TypeReference<TestResult<CaseGroupCreateDto>>() {
        });
    }

    /**
     * 新增成员
     *
     * @param dto
     * @return
     */
    public TestResult<MemberAddDto> addMember(MemberAddDto dto) {
        return client.post("/online/addMember", null, dto, new TypeReference<TestResult<MemberAddDto>>() {
        });
    }

    public TestResult<List<MemberAddDto>> addMembers(List<MemberAddDto> dtos) {
        return client.post("/online/addMembers", null, dtos, new TypeReference<TestResult<List<MemberAddDto>>>() {
        });
    }

    /**
     * 查询角色权限
     *
     * @param moduleId
     * @return
     */
    public TestResult<List<ModuleUserInfo>> getRoleManageList(Long moduleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("moduleId", moduleId);
        return client.get("/online/getRoleManageList", params, new TypeReference<TestResult<List<ModuleUserInfo>>>() {
        });
    }

    /**
     * 删除角色
     *
     * @param id
     * @return
     */
    public TestResult<String> deleteRoleById(Long id) {
        return client.delete("/online/" + id, null, new TypeReference<TestResult<String>>() {

        });
    }

    /**
     * 获取所有用例步骤的执行结果
     *
     * @param id
     * @return
     */
    public TestResult<List<CaseBatchExecuteResult>> getCaseExecuteResult(Long id) {
        return client.get("/online/" + id + "/details", null, new TypeReference<TestResult<List<CaseBatchExecuteResult>>>() {
        });
    }

    /**
     * 过滤冒烟用例
     *
     * @param suiteId
     * @param type
     * @return
     */
    public TestResult<List<SuiteDetail>> getCaseListByType(Long suiteId, Integer type) {
        Map<String, Object> params = new HashMap<>();
        params.put("suiteId", suiteId);
        params.put("type", type);
        return client.get("/online/getCaseListByType", params, new TypeReference<TestResult<List<SuiteDetail>>>() {

        });
    }

    @Override
    public TestResult<CaseGroupCreateDto> addTestCase(CaseInfo caseInfo) {
        return client.post("/online/addCaseInfo", null, caseInfo, new TypeReference<TestResult<CaseGroupCreateDto>>() {
        });
    }

    @Override
    public TestResult<StepCaseDetail> addTestCaseStep(StepCaseDetail stepCaseDetail) {
        return client.post("/online/addCaseStep", null, stepCaseDetail, new TypeReference<TestResult<StepCaseDetail>>() {
        });
    }
}
