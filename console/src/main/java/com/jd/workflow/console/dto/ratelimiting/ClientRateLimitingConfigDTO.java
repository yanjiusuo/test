package com.jd.workflow.console.dto.ratelimiting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.util.List;

@Data
public class ClientRateLimitingConfigDTO {
    @JsonIgnore
    private String name;

    private List<String> limitList;
}