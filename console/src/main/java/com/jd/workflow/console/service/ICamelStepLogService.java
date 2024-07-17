package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.CamelLogConditionDTO;
import com.jd.workflow.console.dto.CamelLogListDTO;
import com.jd.workflow.console.dto.CamelLogQueryDTO;
import com.jd.workflow.console.dto.CamelLogReqDTO;
import com.jd.workflow.console.entity.CamelStepLog;

/**
 * 项目名称：example
 * 类 名 称：ICamelStepLogService
 * 类 描 述：日志service接口
 * 创建时间：2022-06-07 16:05
 * 创 建 人：wangxiaofei8
 */
public interface ICamelStepLogService extends IService<CamelStepLog> {

    /**
     * 查询日志
     * @param reqDTO
     * @return
     */
    public CamelLogListDTO queryCamleStepLog(CamelLogReqDTO reqDTO);


    /**
     * 查询接口
     * @param reqDTO
     * @return
     */
    public CamelLogConditionDTO queryLogInterfaceCondition(CamelLogReqDTO reqDTO);



    /**
     * 查询方法
     * @param reqDTO
     * @return
     */
    public CamelLogConditionDTO queryLogMethodCondition(CamelLogQueryDTO reqDTO);



    /**
     * 查询版本
     * @param reqDTO
     * @return
     */
    public CamelLogConditionDTO queryLogVersionCondition(CamelLogReqDTO reqDTO);
}
