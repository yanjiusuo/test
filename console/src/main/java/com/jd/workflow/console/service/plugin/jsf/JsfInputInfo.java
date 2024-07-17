package com.jd.workflow.console.service.plugin.jsf;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;
@Data
public class JsfInputInfo {
     List<JsonType> inputParams;
     List demoValues;
     List<String> parameterTypes;
}
