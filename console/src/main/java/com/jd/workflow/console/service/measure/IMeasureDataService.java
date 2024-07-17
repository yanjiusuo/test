package com.jd.workflow.console.service.measure;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.doc.GroupHttpData;
import com.jd.workflow.console.dto.measure.RequirementMeasureDataDTO;
import com.jd.workflow.console.dto.measure.UserMeasureDataDTO;
import com.jd.workflow.console.entity.MeasureData;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.debug.FlowDebugLog;

import java.util.List;

/**
 * @author yza
 * @description
 * @date 2024/1/12
 */
public interface IMeasureDataService extends IService<MeasureData> {

    /**
     * 【指标度量】接口上报
     *
     * @param interfaceType
     * @param group2MethodManage
     * @return
     */
    void saveReportDataLog(Integer interfaceType, List<GroupHttpData<MethodManage>> group2MethodManage, String erp);

    /**
     * 【指标度量】快捷调用
     *
     * @param type
     * @param log
     */
    void saveQuickCallLog(Integer type, FlowDebugLog log);

    /**
     * 【指标度量】接口详情
     */
    void saveInterfaceDetailLog(String methodId);

    /**
     * 【指标度量】mock模版
     *
     * @param methodId
     */
    void saveMockTemplateLog(Integer type, String methodId);

    /**
     * 【指标度量】快捷调用一键mock
     *
     * @param type
     * @param methodId
     */
    void saveQuickCallMockTempLog(Integer type, String methodId);

    /**
     * 【指标度量】用户明细
     *
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    List<UserMeasureDataDTO> queryUserMeasureData(String department, String timeStart, String timeEnd, String erp);

    /**
     * 【指标度量】空间明细
     *
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    List<RequirementMeasureDataDTO> queryRequirementMeasureData(String department, String timeStart, String timeEnd,
                                                                String requirementName, String requirementCode, String creator);

    /**
     * 查询所属部门最近几条动态
     *
     * @param dept
     * @return
     */
    List<MeasureData> queryDeptInfo(String dept, Integer count);

    /**
     * 查询用户最近浏览接口文档记录
     *
     * @param erp
     * @param count
     * @return
     */
    List<MeasureData> queryUserView(String erp, Integer count);

    /**
     * 刷新部门
     * @return
     */
    Boolean refreshMeasureDataDept();
}
