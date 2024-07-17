package com.jd.workflow.console.dto.doc;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/17
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/17
 */
@Data
public class ExportZipDTO {
    /**
     * 类型
     */
    private String sdkType;
    /**
     * 导出接口id
     */
    private String ids;
}
