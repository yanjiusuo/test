package com.jd.workflow.common;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@Data
public class ObjectJsonType extends JsonType {
    List<JsonType> children = new LinkedList<>();


}
