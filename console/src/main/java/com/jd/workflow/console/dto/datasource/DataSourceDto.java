package com.jd.workflow.console.dto.datasource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class DataSourceDto {
    Long id;
    /**
     * 4-ducc 5-jimdb
     */
    @NotNull(message = "类型不可为空")
    Integer type;
    @NotNull(message = "服务名称不可为空")
    String name;
    @NotNull(message = "服务编码不可为空")
    String serviceCode;
    @NotNull(message = "负责人不可为空")
    String adminCode;
    @NotNull(message = "描述不可为空")
    String desc;
    /**
     * 针对jimdb，url、serviceEndpoint分别对应jimurl 、serviceEndpoint
     */
    @NotNull(message = "配置不可为空")
    Map<String,Object> config;



}
