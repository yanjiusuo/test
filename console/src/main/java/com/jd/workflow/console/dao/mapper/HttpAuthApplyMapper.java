package com.jd.workflow.console.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.QueryHttpAuthApplyReqDTO;
import com.jd.workflow.console.dto.QueryHttpAuthReqDTO;
import com.jd.workflow.console.entity.HttpAuth;
import com.jd.workflow.console.entity.HttpAuthApply;

import java.util.List;

/**
 * <p>
 * 鉴权标签管理 Mapper 接口
 * </p>
 *
 * @author wubaizhao1
 * @date 2023-01-06 10:55
 */

public interface HttpAuthApplyMapper extends BaseMapper<HttpAuthApply> {

    /**
     * 查询鉴权数量
     * @param queryDTO
     * @return
     */
    public Long queryListCount(QueryHttpAuthApplyReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApply> queryList(QueryHttpAuthApplyReqDTO queryDTO);

    /**
     * 查询鉴权列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApply> queryAllList(QueryHttpAuthApplyReqDTO queryDTO);

}
