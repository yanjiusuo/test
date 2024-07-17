package com.jd.workflow.console.entity.method;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class DeltaInfo {
    /*// 用户修改的文档信息，若与上报的不一致则记录下来
    String docInfo;
    String name;*/
    // 存储delta信息
    Map<String,Object> deltaAttrs = new HashMap<>();

    public abstract boolean isEmpty();
}
