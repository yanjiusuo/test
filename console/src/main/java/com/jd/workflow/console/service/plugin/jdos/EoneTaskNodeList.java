package com.jd.workflow.console.service.plugin.jdos;

import lombok.Data;

import java.util.List;

@Data
public class EoneTaskNodeList {
    String name;
    List<EonePodIp> podVOList;

}
