package com.jd.workflow.console.model.sync;

import com.jd.fastjson.JSONObject;
import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Getter
@Setter
@Accessors(chain = true)
public class BuildReportContext {
    private JSONObject methodJsonObject;
    private ClassMetadata classMetadata;
    private InterfaceInfo interfaceInfo;
    private List<BuilderJsonType> allBuilderJsonTypes = new ArrayList<>();
}
