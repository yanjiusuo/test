package com.jd.workflow.console.entity.doc;

import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import lombok.Data;

/**
 * 创建接口分组的dto对象
 */
@Data
public class MethodDocDto {
    /**
     * 文档名称
     */
    String name;
    /**
     * 文档内容
     */
    String docInfo;

    MethodDocConfig docConfig;
    /**
     * @hidden
     */
    Long interfaceId;
}
