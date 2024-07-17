package com.jd.workflow.console.dto.doc;

import lombok.Data;

@Data
public class InterfaceTypeCount {
    /**
     * 接口类型 {@link com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    int type;
    int count;

}
