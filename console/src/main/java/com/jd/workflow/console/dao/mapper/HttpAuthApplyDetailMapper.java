package com.jd.workflow.console.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.QueryHttpAuthApplyDetailReqDTO;
import com.jd.workflow.console.entity.HttpAuthApplyDetail;

import java.util.List;

/**
 * <p>
 * 鉴权标签管理 Mapper 接口
 * </p>
 *
 * @author wubaizhao1
 * @date 2023-01-06 10:55
 */

public interface HttpAuthApplyDetailMapper extends BaseMapper<HttpAuthApplyDetail> {

    /**
     * 查询鉴权数量
     * @param queryDTO
     * @return
     */
    public Long queryListCount(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetail> queryList(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetail> queryAllList(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetail> queryListGroupByInterface(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetail> queryListPageGroupByInterface(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public Long queryCountPageGroupByInterface(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetail> queryListGroupByMethod(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetail> queryListPageGroupByAuthCode(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetail> queryListPageGroupByAuthCodeAndMethod(QueryHttpAuthApplyDetailReqDTO queryDTO);


    /**
     * 查询鉴权标识数量
     * @param queryDTO
     * @return
     */
    public Long queryCountPageGroupByAuthCode(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询鉴权标识数量
     * @param queryDTO
     * @return
     */
    public Long queryCountPageGroupByAuthCodeAndMethod(QueryHttpAuthApplyDetailReqDTO queryDTO);

}
