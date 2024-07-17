package com.jd.workflow.console.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.entity.AppInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：AppInfoMapper
 * 类 描 述：appInfoDao
 * 创建时间：2022-11-16 16:48
 * 创 建 人：wangxiaofei8
 */
public interface AppInfoMapper extends BaseMapper<AppInfo> {
    List<AppInfo> selectByRequirementId(@Param("requirementId") Long requirementId);
}
