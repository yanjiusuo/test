package com.jd.workflow.console.dao.mapper.share;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.share.QueryShareGroupReqDTO;
import com.jd.workflow.console.entity.share.InterfaceShareGroup;
import com.jd.workflow.console.entity.share.InterfaceShareUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 16:23
 * @Description:
 */
public interface InterfaceShareUsersMapper extends BaseMapper<InterfaceShareUser> {

    List<String> getSharedUserCodeList(Long groupId);

}
