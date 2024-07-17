package com.jd.workflow.metrics.client;

import lombok.Data;

import java.util.List;

@Data
public class DemandUserResponse {
    /**
     * 受理人
     */
    private UserResponse recipient;
    /**
     * 关注人
     */
    private List<UserResponse> conserners;



}
