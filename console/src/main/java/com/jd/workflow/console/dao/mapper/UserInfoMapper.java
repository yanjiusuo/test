package com.jd.workflow.console.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.entity.UserInfo;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    public int insertUniqueUser(UserInfo userInfo);
}
