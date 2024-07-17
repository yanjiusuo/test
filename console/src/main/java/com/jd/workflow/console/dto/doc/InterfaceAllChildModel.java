package com.jd.workflow.console.dto.doc;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/10
 */

import com.jd.workflow.console.dto.MethodGroupTreeDTO;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/10
 */
@Data
public class InterfaceAllChildModel extends InterfaceTopCountModel {
    private MethodGroupTreeDTO methodGroupTreeDTO;
}
