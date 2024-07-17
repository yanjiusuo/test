package com.jd.workflow.soap.common.xml.schema;/**
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
public class JsonEnumDTO {

    private String name;

    private Integer type;

    private List<JsonEnumPropDTO> props;
}
