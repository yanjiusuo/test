package com.jd.workflow.console.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.dto.InterfaceCountQueryDto;
import com.jd.workflow.console.dto.InterfaceQueryDto;
import com.jd.workflow.console.dto.usecase.CaseInterfaceManageDTO;
import com.jd.workflow.console.entity.InterfaceManage;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 接口管理 Mapper 接口
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
public interface InterfaceManageMapper extends BaseMapper<InterfaceManage> {
    public List<InterfaceManage> selectAdminList(InterfaceQueryDto dto);

    public Long selectAdminListCount(InterfaceQueryDto dto);

    public Long queryListCount(InterfaceQueryDto dto);

    public List<InterfaceManage> queryList(InterfaceQueryDto dto);

    List<Map<String, Object>> queryUserInterfaceCount(InterfaceCountQueryDto dto);

    List<String> queryDeptNameList(InterfaceQueryDto dto);

    Long queryDeptNameCount(InterfaceQueryDto dto);

    /**
     * 根据AppId查询所有的接口列表
     *
     * @param appId
     * @return
     */
    List<InterfaceManage> queryListByAppId(Long appId);

    /**
     * 根据接口类型查询对应接口的接口数量
     *
     * @param type
     * @return
     */
    Long queryNumsByType(Integer type);



    /**
     * 根据AppId查询所有的接口列表
     *
     * @param interfaceManages
     * @return
     */
    int batchUpdateInterfaceDeptName(List<InterfaceManage> interfaceManages);

    int clearInvalidRelatedId();

    List<CaseInterfaceManageDTO> selectByAppId(@Param("appId") Long appId, @Param("type") Integer type);
}
