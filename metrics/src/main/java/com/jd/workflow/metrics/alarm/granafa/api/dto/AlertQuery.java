package com.jd.workflow.metrics.alarm.granafa.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AlertQuery {

    String dashboardId;
    String panelId;
    String query; // ALL、no_data、paused、alerting、ok、pending
    List<String> state = new ArrayList<>();; //ALL,no_data, paused, alerting, ok, pending
    Integer limit;
    List<Long> folderId = new ArrayList<>();
    String dashboardQuery;
    String dashboardTag;
}
