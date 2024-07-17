package com.jd.workflow.console.service.share;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.InterfaceShareTreeDTO;
import com.jd.workflow.console.dto.share.*;
import com.jd.workflow.console.entity.share.InterfaceShareGroup;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 14:30
 * @Description:
 */
public interface IInterfaceShareGroupService extends IService<InterfaceShareGroup> {
    /**
     * 新建分享
     *
     * @param interfaceShareDTO
     * @return
     */
    Long addInterfaceShare(InterfaceShareDTO interfaceShareDTO);
    Long appendInterfaceShare(AppendShareDTO dto);


    /**
     * 校验分享名称是否重复
     *
     * @param shareGroupName
     * @return
     */
    Boolean validShareName(String shareGroupName);


    /**
     * 分享分组填加被分享人
     *
     * @param shareGroupId
     * @return
     */
    Boolean addShareUser(Long shareGroupId);

    /**
     * 删除分享分组信息
     *
     * @param dto
     * @return
     */
    Boolean removeInterfaceShare(RemoveShareGroupDTO dto);
    InterfaceShareGroup getEntity(Long id);

    /**
     * 查询分享分组信息
     *
     * @param query
     * @return
     */
    QueryShareGroupResultDTO queryInterfaceShareGroup(QueryShareGroupReqDTO query);


    /**
     * 获取分享分组下的方法树
     *
     * @param shareGroupId
     * @return
     */
    InterfaceShareTreeDTO findInterfaceShareTree(Long shareGroupId);

    /**
     * 修改分享树
     *
     * @param dto
     * @return
     */
    Boolean modifyInterfaceShareTree(InterfaceShareTreeDTO dto,boolean validate);

    /**
     * 新增分组
     * @param name
     * @param shareGroupId
     * @param parentId
     * @return
     */
    Long addGroup(String name, Long shareGroupId, Long parentId);

    /**
     * 修改分组名称
     * @param shareGroupId
     * @param id
     * @param name
     * @param parentId
     * @return
     */
    boolean modifyGroupName(Long shareGroupId, Long id, String name, Long parentId);

    /**
     * 删除分组
     * @param shareGroupId
     * @param id
     * @param parentId
     * @return
     */
    boolean removeGroup(Long shareGroupId, Long id, Long parentId);
    boolean removeShareMethod(Long shareGroupId, Long id, Long parentId);
}
