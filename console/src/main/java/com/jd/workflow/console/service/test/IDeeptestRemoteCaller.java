package com.jd.workflow.console.service.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.console.dto.share.CaseBatchExecuteDto;
import com.jd.workflow.console.dto.share.CaseBatchExecuteResult;
import com.jd.workflow.console.dto.test.deeptest.*;
import com.jd.workflow.console.dto.test.deeptest.step.StepCaseDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IDeeptestRemoteCaller {
    /**
     * 创建模块
     * @param createModule
     * @return
     */
    public TestResult<ModuleCreateDto> createModule(ModuleCreateDto createModule);

    /**
     * 执行功能用例集合
     * @param caseBatchExecuteDto
     * @return
     */
    public TestResult<Long> executeCaseSuite(CaseBatchExecuteDto caseBatchExecuteDto);

    /**
     * 新增用例集
     * @param createDto
     * @return
     */
    public TestResult<CaseGroupCreateDto> addCatalogTree(CaseGroupCreateDto createDto);

    /**
     * 新增成员
     * @param dto
     * @return
     */
    public TestResult<MemberAddDto> addMember(MemberAddDto dto);

    public TestResult<List<MemberAddDto>> addMembers(List<MemberAddDto> dtos);

    /**
     * 查询角色权限
     * @param moduleId
     * @return
     */
    public TestResult<List<ModuleUserInfo>> getRoleManageList(Long moduleId);

    /**
     * 删除角色
     * @param id
     * @return
     */
    public  TestResult<String> deleteRoleById(Long id);

    /**
     * 获取所有用例步骤的执行结果
     * @param id deeptest任务id
     * @return
     */
    public TestResult<List<CaseBatchExecuteResult>> getCaseExecuteResult(Long id);

    /**
     * 过滤冒烟用例
     * @param suiteId
     * @param type
     * @return
     */
    public TestResult<List<SuiteDetail>> getCaseListByType(Long suiteId, Integer type);


    /**
     * 创建用例
     * @param caseInfo
     * @return
     */
    TestResult<CaseGroupCreateDto> addTestCase(CaseInfo caseInfo);

    /**
     * 添加用例步骤
     * @param stepCaseDetail
     * @return
     */
    TestResult<StepCaseDetail> addTestCaseStep(StepCaseDetail stepCaseDetail);
}
