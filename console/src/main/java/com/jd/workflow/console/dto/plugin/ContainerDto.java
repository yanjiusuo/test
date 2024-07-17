package com.jd.workflow.console.dto.plugin;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/6
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/6
 */
@Data
public class ContainerDto extends GroupDto {
    private String ip;
    private String image;
    private String hash;
}
