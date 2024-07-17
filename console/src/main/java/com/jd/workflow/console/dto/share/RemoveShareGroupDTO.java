package com.jd.workflow.console.dto.share;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/10 16:10
 * @Description:
 */
@Data
public class RemoveShareGroupDTO {
    /**
     * 分享分组id
     */
    private Long shareGroupId;

    /**
     * 删除类型 0：分享人删除分享  1：被分享人取消收藏
     */
    private int type;
}
