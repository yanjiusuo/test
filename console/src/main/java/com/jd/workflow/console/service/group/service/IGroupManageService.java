package com.jd.workflow.console.service.group.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import com.jd.workflow.console.dto.doc.InterfaceCountModel;
import com.jd.workflow.console.dto.doc.InterfaceTypeCount;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.TreeSortModel;
import com.jd.workflow.console.dto.group.GroupAddDto;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.manage.InterfaceOrMethod;
import com.jd.workflow.console.dto.manage.InterfaceOrMethodSearchDto;
import com.jd.workflow.console.dto.manage.MethodRelatedDto;
import com.jd.workflow.console.dto.requirement.MethodRemoveDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.MethodDocDto;

import java.util.List;

/**
 * 分组管理服务
 */
public interface IGroupManageService {
    /**
     * 获取根节点信息
     *
     * @param dto
     * @param type
     */
    public List<InterfaceCountModel> getRootGroups(GroupResolveDto dto, Integer type);

    public Long addGroup(GroupResolveDto dto, GroupAddDto groupDto);

    public Boolean modifyGroupName(GroupResolveDto dto, Long id, String name, String enName);


    public boolean  removeNode(GroupResolveDto dto,Long currentNodeId,Long currentNodeType,Long rootId,Long parentId);

    Long addDoc(GroupResolveDto dto, Long interfaceId, Long groupId, MethodDocDto methodDocDto);

    public boolean removeMethodByIds(GroupResolveDto dto, List<MethodRemoveDto> ids);
    public boolean removeInterfaceByIds(GroupResolveDto dto,List<Long> ids);

    public MethodGroupTreeDTO findMethodGroupTree(GroupResolveDto dto, Long interfaceId);

    public Boolean modifyMethodGroupTree(GroupResolveDto dto, MethodGroupTreeDTO groupTreeDto);

    /**
     * 分组树检索
     *
     * @param dto
     * @param search
     */
    public List<TreeSortModel> searchTree(GroupResolveDto dto, int interfaceType, String search);

    /**
     * 接口搜索
     *
     * @param dto
     * @param search
     */
    public IPage<MethodManage> findGroupMethods(GroupResolveDto dto, Long interfaceId,Integer status, Long groupId, String search, Long pageNo, Long pageSize);
    public IPage<InterfaceOrMethod> findGroupInterface(InterfaceOrMethodSearchDto dto);

    public List<InterfaceTypeCount> getInterfaceCount(GroupResolveDto dto);


    public boolean isMember(GroupResolveDto dto);
}
