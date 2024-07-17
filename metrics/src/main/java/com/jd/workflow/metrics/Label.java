package com.jd.workflow.metrics;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Label {
    String key;
    String value;

}
