package com.jd.workflow.console.dto.test.deeptest;

import lombok.Data;

@Data
public class ModuleCreateDto extends BaseTestEntityInfo{
    Long id;
    String name;
    String note;

}
