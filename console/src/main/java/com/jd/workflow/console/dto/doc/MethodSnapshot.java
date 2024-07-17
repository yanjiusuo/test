package com.jd.workflow.console.dto.doc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MethodSnapshot {
    List<MethodSnapshotItem> methods = new ArrayList<>();
}
