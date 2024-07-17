package com.jd.workflow.console.dto.doc;

import lombok.Data;

@Data
public class MethodSnapshotItem {
    Long methodId;
    String path;
    String digest;
}
