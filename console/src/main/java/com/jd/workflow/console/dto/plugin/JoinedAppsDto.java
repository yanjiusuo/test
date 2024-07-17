package com.jd.workflow.console.dto.plugin;

import lombok.Data;

@Data
public class JoinedAppsDto {
    /**
     * jdos应用编码
     */
    String jdosAppCode;

    /**
     * 代码仓库
     */
    String codeRepository;

    String site;
}
