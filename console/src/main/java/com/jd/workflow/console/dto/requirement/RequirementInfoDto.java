package com.jd.workflow.console.dto.requirement;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class RequirementInfoDto {
    private Long id;
    /**
     * 需求类型：1-工作流 2-迁移
     */
    private Integer type;
    /**
     * 流程id,为空时表示非需求流程
     */
    private Long flowId;
    private Long relatedId;
    private String spaceName;
    private String name;
    private String templateCode;
    private String code;
    private Set<String> nodeTypeCodeSet;
    private Date createAt;
    private Date modified;
    private Date finishAt;
    private String createBy;
    private Integer status;
    private Integer yn;
    private Integer source;
    private Long dwellDuration;
    private Set<String> nodeListAtMoment;
    private String templateName;
    Creator creator;
    @Data
    public static class Creator{
     private String headImg;
     String realName;
     String userName;
    }

    /**
     * 空间成员
     */
    private List<UserInfoDTO> members;
    /**
     * 空间负责人
     */
    private UserInfoDTO owner;
    /**
     * 当前访问人是否是接口空间负责人
     */
    private Boolean isOwner;
    /**
     * 当前访问人是否是空间成员；
     */
    private Boolean isMember;
    /**
     * 方案名称
     */
    private String openSolutionName;
    /**
     * 所属类别
     */
    private String openType;
    /**
     * 方案描述
     */
    private String openDesc;
    /**
     * 接口方法类型
     */
    private List<Integer> methodTypes;

    private String gitBranch;

}
