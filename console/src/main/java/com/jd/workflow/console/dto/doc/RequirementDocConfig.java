package com.jd.workflow.console.dto.doc;

import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY,property = "type",visible = true)
@JsonSubTypes({

        @JsonSubTypes.Type(value = JsfDocConfig.class, name = "jsf")})*/
@Data
public  class RequirementDocConfig {

    String docType;



}
