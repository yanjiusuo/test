package com.jd.workflow.console.dto.debug;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class JsfMavenInfo {
    String groupId;
    String artifactId;
    String version;
    List<String> versions = new ArrayList<>();
}
