package com.jd.workflow.console.service.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/1
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.requirement.InterfaceSpaceLogParam;
import com.jd.workflow.console.entity.requirement.RequirementInfoLog;

import java.util.Date;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/1
 */
public interface RequirementInfoLogService extends IService<RequirementInfoLog> {

    void createLog(Long requirementId, String erp, Date date, String desc);

    Page<RequirementInfoLog> queryLog(InterfaceSpaceLogParam interfaceSpaceLogParam);
}
