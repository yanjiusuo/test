package com.jd.workflow.console.dto;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

import java.util.List;

@Data
public class UserPinDTO extends PageParam {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户唯一标识
     */
    private String userCode;

    /**
     * 资源id
     */
    private Long resourceId;

    /**
     * 根据枚举控制
     * {@link com.jd.workflow.console.base.enums.ResourceTypeEnum}
     */
    private Integer resourceType;

    /**
     * 资源角色 0-无 1-租户管理员 2负责人 3成员
     * {@link com.jd.workflow.console.base.enums.ResourceRoleEnum}
     */
    private Integer resourceRole;

    /**
     * 角色列表
     * @date: 2022/5/18 11:13
     * @author wubaizhao1
     */
    private List<Integer> resourceRoleList;

    /**
     * 用户表信息===========================
     */
    /**
     * 名称备注
     * @date: 2022/6/14 15:21
     * @author wubaizhao1
     */
    private String userName;
}
