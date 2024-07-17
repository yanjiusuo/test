package com.jd.workflow.console.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.flow.param.QueryParamReqDTO;
import com.jd.workflow.console.entity.FlowParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:13
 * @Description:
 */
public interface FlowParamMapper extends BaseMapper<FlowParam> {

    /**
     * @param query
     * @param groupList
     * @return
     */
    public List<FlowParam> queryParamList(@Param("query") QueryParamReqDTO query, @Param("groupList") List<Long> groupList);

    public long queryParamCount(@Param("query") QueryParamReqDTO query, @Param("groupList") List<Long> groupList);
}
