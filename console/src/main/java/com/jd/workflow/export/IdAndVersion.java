package com.jd.workflow.export;

import lombok.Data;

@Data
public class IdAndVersion {
    Long id;
    String version;

    public IdAndVersion(Long id, String version) {
        this.id = id;
        this.version = version;
    }
    public IdAndVersion(){}
}
