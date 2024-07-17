package com.jd.workflow.console.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Data
public class EasyDataPageResult<T> implements Serializable {
    @JsonProperty("status")
    private Integer status;
    @JsonProperty("message")
    private String message;
    @JsonProperty("result")
    private Object result;
    @JsonProperty("content")
    private List<Object> content;
    @JsonProperty("first")
    private Boolean first;
    @JsonProperty("last")
    private Boolean last;
    @JsonProperty("number")
    private Integer number;
    @JsonProperty("numberOfElements")
    private Integer numberOfElements;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("totalElements")
    private Integer totalElements;
    @JsonProperty("totalPages")
    private Integer totalPages;
    @JsonProperty("requestId")
    private String requestId;

    public EasyDataPageResult(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
