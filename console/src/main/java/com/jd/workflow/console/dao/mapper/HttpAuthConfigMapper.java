package com.jd.workflow.console.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.QueryHttpAuthConfigReqDTO;
import com.jd.workflow.console.dto.QueryHttpAuthReqDTO;
import com.jd.workflow.console.entity.HttpAuth;
import com.jd.workflow.console.entity.HttpAuthConfig;

import java.util.List;

/**
 * <p>
 * 鉴权标签管理 Mapper 接口
 * </p>
 *
 * @author wubaizhao1
 * @date 2023-01-06 10:55
 */

public interface HttpAuthConfigMapper extends BaseMapper<HttpAuthConfig> {

    /**
     * 查询鉴权数量
     * @param queryDTO
     * @return
     */
    public Long queryListCount(QueryHttpAuthConfigReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthConfig> queryList(QueryHttpAuthConfigReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthConfig> queryAllList(QueryHttpAuthConfigReqDTO queryDTO);
}
