package com.jd.workflow.console.dto;

import com.jd.workflow.console.dto.jingme.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/18
 */
@Data
public class SimpleAppInfo {

    /**
     * jdos应用负责人
     */
    private UserDTO jdosOwner;

    /**
     * jdos应用成员
     *
     * @required
     */
    private List<UserDTO> jdosMembers;

    /**
     * 产品负责人
     */
    private List<UserDTO> productor;

    /**
     * 负责人部门信息
     */
    private String department;


}
