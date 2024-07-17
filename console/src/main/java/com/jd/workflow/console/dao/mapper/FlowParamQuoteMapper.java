package com.jd.workflow.console.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.flow.param.QueryParamQuoteReqDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamReqDTO;
import com.jd.workflow.console.entity.FlowParam;
import com.jd.workflow.console.entity.FlowParamGroup;
import com.jd.workflow.console.entity.FlowParamQuote;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 17:25
 * @Description:
 */
public interface FlowParamQuoteMapper extends BaseMapper<FlowParamQuote> {

    /**
     * @param query
     * @return
     */
    List<FlowParamQuote> queryQuoteParamList(@Param("query") QueryParamQuoteReqDTO query);

    long queryQuoteParamCount(@Param("query") QueryParamQuoteReqDTO query);


    /**
     * @param query
     * @return
     */
    List<FlowParam> queryUnQuoteParamList(@Param("query") QueryParamQuoteReqDTO query);

    long queryUnQuoteParamCount(@Param("query") QueryParamQuoteReqDTO query);


    /**
     * @param query
     * @return
     */
    List<FlowParamGroup> queryQuoteParamForGroup(@Param("query") QueryParamQuoteReqDTO query);

    long queryQuoteParamForGroupCount(@Param("query") QueryParamQuoteReqDTO query);
}
