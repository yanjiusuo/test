package com.jd.workflow.console.dao.mapper.dashboard;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.entity.dashboard.DashBoardInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author yuanshuaiming
 * @Date 2023/7/6 3:50 下午
 * @Version 1.0
 */
public interface DashBoardInfoManageMapper extends BaseMapper<DashBoardInfo> {
    /**
     * 统计有权限应用下的jsf + http接口数量总数
     *
     * @param currentUser
     * @param appName
     * @return
     */
    List<DashBoardInfo> queryAppDashInfoList(@Param("currentUser") String currentUser, @Param("appName") String appName);

    /**
     * 统计有权限应用下的jsf + http方法数量总数
     *
     * @param currentUser
     * @param appName
     * @return
     */
    List<DashBoardInfo> queryAppDashMethodCountInfoList(@Param("currentUser") String currentUser, @Param("appName") String appName);
}
