package com.jd.workflow.domain;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */

import com.jd.matrix.sdk.base.DomainModel;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23 
 */
@Data
@NoArgsConstructor
public class ModelContentParseModel implements DomainModel {

    private String content;
}
