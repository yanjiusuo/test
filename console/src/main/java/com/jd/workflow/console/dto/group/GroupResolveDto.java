package com.jd.workflow.console.dto.group;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class GroupResolveDto {
    /**
     * 分组类型：{@link GroupTypeEnum}
     */
    @NotNull(message = "类型不可为空")
    private Integer type;
    /**
     * 关联id,当type为1时为应用id
     * type为2时为需求id
     * type为3时为工作流步骤id
     *
     */
    @NotNull(message = "id不可为空")
    private Long id;
    /**
     * requirementId，type为3的时候必填，别的情况下不用传
     */
    private Long requirementId;
    /**
     * 标签名称
     */
    private List<String> tagName;

    /**
     * 移动的原接口分组id
     */
    private Long fromInterfaceId;

    /**
     * 被调整的方法id
     */
    private Long methodId;

    /**
     * 被调整的文件夹id
     */
    private Long groupId;
}
