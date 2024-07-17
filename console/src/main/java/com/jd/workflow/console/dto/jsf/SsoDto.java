package com.jd.workflow.console.dto.jsf;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/20
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/20 
 */
@Data
public class SsoDto {

    /**
     * 用户名
     */
    private String user;
    /**
     * 密码明文
     */
    private String pwd;
}
