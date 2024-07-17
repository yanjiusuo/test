package com.jd.workflow.console.dto.plugin;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/2
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/2 
 */
@Data
public class HotFiles {
    /**
     * files
     */
    private List<HotFile> files;
    /**
     * classFilePaths
     */
    private List<String> classFilePaths;
    /**
     * localDeployFlag
     */
    private Integer localDeployFlag;

    /**
     * deployId 部署id，部署结果回调
     */
    private String reqId;

    private boolean onlyUpdateClass;
}
