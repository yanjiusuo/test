package com.jd.workflow.console.dao.mapper.share;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.share.QueryShareGroupReqDTO;
import com.jd.workflow.console.entity.share.InterfaceShareGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 16:23
 * @Description:
 */
public interface InterfaceShareGroupMapper extends BaseMapper<InterfaceShareGroup> {

    List<InterfaceShareGroup> queryShareGroupList(@Param("query") QueryShareGroupReqDTO query);

    Long queryShareGroupCount(@Param("query") QueryShareGroupReqDTO query);

}
