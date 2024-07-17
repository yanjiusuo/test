package com.jd.workflow.console.dto.jsf;

import lombok.Data;

/**
 * 快捷调用历史记录的内容
 */
@Data
public class JsfDebugData {
    String desc;
    NewJsfDebugDto input;
    Object output;
}
