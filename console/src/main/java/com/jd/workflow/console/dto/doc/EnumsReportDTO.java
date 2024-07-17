package com.jd.workflow.console.dto.doc;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/14
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/14 
 */
@Data
public class EnumsReportDTO {
    /**
     * 应用编码
     */
    String appCode;
    /**
     * 应用密钥
     */
    String appSecret;
    /**
     * ip列表
     */
    String ip;

    /**
     * 枚举上报信息
     */
    List<EnumClassDTO> enums;
}
