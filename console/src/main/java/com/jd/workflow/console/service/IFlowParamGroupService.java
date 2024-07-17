package com.jd.workflow.console.service;
/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:48
 * @Description:
 */

import com.jd.workflow.console.dto.InterfaceManageDTO;
import com.jd.workflow.console.dto.MemberRelationWithUser;
import com.jd.workflow.console.dto.UserForAddDTO;
import com.jd.workflow.console.dto.flow.param.FlowParamGroupDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamGroupReqDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamGroupResultDTO;
import com.jd.workflow.console.entity.FlowParamGroup;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:11
 * @Description: 公共参数分组管理service
 */
public interface IFlowParamGroupService {
    /**
     * 新增分组
     *
     * @param dto
     * @return
     */
    Long addGroup(FlowParamGroupDTO dto);

    /**
     * 修改分组
     *
     * @param dto
     * @return
     */
    Boolean editGroup(FlowParamGroupDTO dto);

    /**
     * 删除分组
     *
     * @param id
     * @return
     */
    Boolean removeGroup(Long id);


    QueryParamGroupResultDTO queryGroup(QueryParamGroupReqDTO dto);


    /**
     * 根据分组id查询分组信息
     *
     * @param groupId
     * @return
     */
    FlowParamGroup getGroupByGroupId(Long groupId);


    /**
     * 成员列表
     *
     * @param groupId
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    List<MemberRelationWithUser> listMember(Long groupId);


    /**
     * 成员列表 会区分是否是该接口的用户
     *
     * @param groupId
     * @param userCode
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    List<UserForAddDTO> listMemberForAdd(Long groupId, String userCode);

    /**
     * 添加成员
     *
     * @param groupId
     * @param userCode
     * @return
     * @date: 2022/5/16 14:56
     * @author wubaizhao1
     */
    Boolean addMember(Long groupId, String userCode);
}
