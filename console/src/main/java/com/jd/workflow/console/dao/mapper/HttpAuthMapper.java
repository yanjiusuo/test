package com.jd.workflow.console.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.QueryHttpAuthReqDTO;
import com.jd.workflow.console.entity.HttpAuth;

import java.util.List;

/**
 * <p>
 * 鉴权标签管理 Mapper 接口
 * </p>
 *
 * @author wubaizhao1
 * @date 2023-01-06 10:55
 */

public interface HttpAuthMapper extends BaseMapper<HttpAuth> {

    /**
     * 查询鉴权数量
     * @param queryDTO
     * @return
     */
    public Long queryListCount(QueryHttpAuthReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuth> queryList(QueryHttpAuthReqDTO queryDTO);

    /**
     * 查询鉴权列表，按分组处理
     * @param queryDTO
     * @return
     */
    public List<HttpAuth> queryListGroupByAppAndSite(QueryHttpAuthReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuth> queryAllList(QueryHttpAuthReqDTO queryDTO);

}
