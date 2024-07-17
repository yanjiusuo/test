package com.jd.workflow.console.entity.plugin.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
public class DeployFileStatisticInfo {
    Map<String,Integer> extensions2Count = new HashMap<>();
    Long remoteStart;
    Integer filedownloadTime;
    Long remoteEnd;
    boolean onlyUpdateClass;
}
