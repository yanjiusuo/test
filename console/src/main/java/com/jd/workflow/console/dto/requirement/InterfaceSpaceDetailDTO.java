package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/31
 */

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/31 
 */
@Data
public class InterfaceSpaceDetailDTO {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String name;

    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间详情
     */
    private String desc;


    /**
     * 空间负责人
     */
    private UserInfoDTO owner;

    /**
     * 空间成员
     */
    private List<UserInfoDTO> members;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 当前访问人是否是接口空间负责人
     */
    private Boolean isOwner;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 修改人
     */
    private String modifier;
    /**
     * 需求code
     */
    private String code;

    /**
     * 空间类型
     */
    private Integer type;
}
