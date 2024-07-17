package com.jd.workflow.console.dao.mapper.usecase;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.entity.usecase.CaseSetExeLog;
import com.jd.workflow.console.entity.usecase.CaseSetExeLogDetail;
import com.jd.workflow.server.dto.requirement.QueryRequirementCodeParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description:
 * 用例集执行记录表 Mapper 接口
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
public interface CaseSetExeLogMapper extends BaseMapper<CaseSetExeLog> {

    List<String> queryRequirementCodes(@Param("query")QueryRequirementCodeParam queryParam);
}

