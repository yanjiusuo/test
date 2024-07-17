package com.jd.workflow.console.dto.usecase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CaseParamBuilderDTO implements Serializable {

    private Long paramBuilderId;

    @ApiModelProperty(value = "场景名称")
    private String sceneName;

}
