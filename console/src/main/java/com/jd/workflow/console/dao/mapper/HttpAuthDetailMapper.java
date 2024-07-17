package com.jd.workflow.console.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.QueryHttpAuthDetailReqDTO;
import com.jd.workflow.console.dto.QueryHttpAuthReqDTO;
import com.jd.workflow.console.entity.HttpAuth;
import com.jd.workflow.console.entity.HttpAuthDetail;

import java.util.List;

/**
 * <p>
 * 鉴权标签管理 Mapper 接口
 * </p>
 *
 * @author wubaizhao1
 * @date 2023-01-06 10:55
 */

public interface HttpAuthDetailMapper extends BaseMapper<HttpAuthDetail> {

    /**
     * 查询鉴权数量
     * @param queryDTO
     * @return
     */
    public Long queryListCount(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryList(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryAllList(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryListGroupByInterface(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryListPageGroupByInterface(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public Long queryCountPageGroupByInterface(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询方法列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryListGroupByMethod(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询方法列表,支持分页
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryListPageGroupByMethod(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询方法数量
     * @param queryDTO
     * @return
     */
    public Long queryCountPageGroupByMethod(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryListGroupByAuthCode(QueryHttpAuthDetailReqDTO queryDTO);
}
