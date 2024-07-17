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
public class DemandDetailDTO {


    /**
     * 需求名称
     */
    private String name;

    /**
     * 需求code
     */
    private String code;

    /**
     * 项目
     */
    private String project;

    private String projectCode;


    /**
     * 级别
     */
    private String level;

    /**
     * 受理人
     */
    private List<UserInfoDTO> members;

    /**
     * 需求创建时间
     */
    private Date created;



}
