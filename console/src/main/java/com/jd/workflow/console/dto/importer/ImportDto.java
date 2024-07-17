package com.jd.workflow.console.dto.importer;

import lombok.Data;

@Data
public class ImportDto {
    String djAppCode;
    String djEnv;
    String djApiGroup;
    String targetAppCode;
    String targetAppSecret;
}
