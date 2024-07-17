package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/28
 */

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/28
 */
@Data
public class InterfaceSpaceDTO {


    /**
     * 空间id
     */
    private Long id;
    /**
     * 需求名称
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
    private String owner;

    /**
     * 空间成员
     */
    private List<String> members;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 需求code
     *
     * @required true
     */
    @Length(max=500,message = "需求code字符长度超过500字符")
    private String code;

    /**
     * 开放名称
     */
    private String openSolutionName;
    /**
     * 开放类型
     */
    private String openType;
    /**
     * 开放方案描述
     */
    private String openDesc;
    /**
     * 是否上架，开放到接口空间 0 下架 1 上架
     */
    private Integer shelve;


}
