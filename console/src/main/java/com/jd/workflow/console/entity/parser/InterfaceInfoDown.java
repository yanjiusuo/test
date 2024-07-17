package com.jd.workflow.console.entity.parser;

import com.jd.jsf.open.api.vo.InterfaceInfo;
import lombok.Data;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/6
 */
@Data
public class InterfaceInfoDown extends InterfaceInfo {
    private String cjgDepartment;
    private String jdosAppCode;
    private String consumerApCodes;
    private String errorMsg;
}
