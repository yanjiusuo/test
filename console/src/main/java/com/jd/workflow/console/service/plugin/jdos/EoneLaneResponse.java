package com.jd.workflow.console.service.plugin.jdos;

import lombok.Data;

import java.util.List;
@Data
public class EoneLaneResponse {
    Integer jdosEnv;
    String name;
    List<EoneTaskNodeList> taskNodeWithCodeVOList;
}
