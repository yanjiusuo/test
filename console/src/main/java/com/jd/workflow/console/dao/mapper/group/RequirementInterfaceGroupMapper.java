package com.jd.workflow.console.dao.mapper.group;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequirementInterfaceGroupMapper extends BaseMapper<RequirementInterfaceGroup> {

    List<AppInfoDTO> getAppCodeByRequirementId(@Param("requirementId") Long requirementId);

    /**
     * 空间聚合接口数
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    List<String> getRequirementInterfaceCount(@Param("department") String department, @Param("timeStart") String timeStart,
                                              @Param("timeEnd") String timeEnd);

}

