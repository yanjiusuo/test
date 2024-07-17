package com.jd.workflow.console.service.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/1
 */

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.requirement.*;
import com.jd.workflow.console.entity.requirement.RequirementInfo;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/1
 */
public interface InterfaceSpaceService extends IService<RequirementInfo> {

    Long createSpace(InterfaceSpaceDTO interfaceSpaceDTO);

    void addOrgIds(Long id);

    void updateTemplate(Long id);

    void updateMember(Long id);

    IPage<RequirementInfoDto> queryOpenSpaceList(InterfaceSpaceParam interfaceSpaceParam);

    Long editSpace(InterfaceSpaceDTO interfaceSpaceDTO);
    
    
    Boolean openSpace(InterfaceSpaceDTO space);



    boolean removeMembers(Long id);

    Long deleteSpace(InterfaceSpaceDTO interfaceSpaceDTO);

    InterfaceSpaceDetailDTO getSpaceInfo(Long spaceId);

     List<RequirementInfo> getRequirementByDemandCode(String demandCode);

    InterfaceSpaceStaticDTO getSpaceInfoStatic(Long spaceId);

    InterfaceSpaceUser getSpaceUser(Long spaceId);
    InterfaceSpaceUser pageSpaceUser(Long spaceId,String search,Long current,Long size);

    Boolean removeUser(RemoveSpaceUserDTO removeSpaceUser);

    Boolean addUser(AddSpaceUserDTO addSpaceUserDTO);

    InterfaceSpaceUser checkUser(Long spaceId,String erp);

    /**
     *
     * @param interfaceSpaceParam
     * @return
     */
    Page<InterfaceSpaceDetailDTO> querySpaceList(InterfaceSpaceParam interfaceSpaceParam);

    Boolean updateOwner(AddSpaceUserDTO addSpaceUserDTO);


}
