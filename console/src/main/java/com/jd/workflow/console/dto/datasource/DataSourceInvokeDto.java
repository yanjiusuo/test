package com.jd.workflow.console.dto.datasource;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DataSourceInvokeDto {
    @NotNull(message = "接口id不可为空")
    Long interfaceId;
    @NotNull(message = "输入数据不可为空")
    List<JsonType> input;
    /**
     * beanType_methodName_methodCount
     */
    @NotNull(message = "方法id不可为空")
    String methodId;
}
