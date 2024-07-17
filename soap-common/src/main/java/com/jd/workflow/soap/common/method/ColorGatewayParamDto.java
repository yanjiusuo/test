package com.jd.workflow.soap.common.method;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColorGatewayParamDto {

    /**
     * 表头
     */
    List<JsonType> headers;
    /**
     *
     */
    List<JsonType> body;

    List<JsonType> params;
}
