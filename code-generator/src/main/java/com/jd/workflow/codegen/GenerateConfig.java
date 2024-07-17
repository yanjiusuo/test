package com.jd.workflow.codegen;

import lombok.Data;

@Data
public class GenerateConfig {
    String commonModelPath = "/common";
    String groupModelPath = "/type";
    String apiPath = "/api";

    boolean generateImport = false;
}
