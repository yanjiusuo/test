package com.jd.workflow.console.dto.tag;

import lombok.Data;

import java.util.Set;

@Data
public class TagParam {
    /**
     * appId
     */
    private Long appId;
    /**
     * 方法id集合
     */
    private Set<Long> methodIds;
    /**
     * 标签名称集合
     */
    private Set<String> tagNames;
}
