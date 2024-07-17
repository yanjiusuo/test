package com.jd.workflow.console.dto.requirement;

import com.jd.workflow.console.entity.MemberRelation;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29 
 */
@Data
public class InterfaceSpaceUser {

    /**
     * 负责人
     */
    private UserInfoDTO  owner;

    private Long total;

    /**
     * 成员
     */
    private List<UserInfoDTO> members;

    /**
     * 是否本空间成员
     */
    private boolean include;

}
