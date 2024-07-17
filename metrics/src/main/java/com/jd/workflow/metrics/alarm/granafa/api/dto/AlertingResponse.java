package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

/**
 *  "id": 1,
 *     "dashboardId": 1,
 *     "dashboardUId": "ABcdEFghij"
 *     "dashboardSlug": "sensors",
 *     "panelId": 1,
 *     "name": "fire place sensor",
 *     "state": "alerting",
 *     "newStateDate": "2018-05-14T05:55:20+02:00",
 *     "evalDate": "0001-01-01T00:00:00Z",
 *     "evalData": null,
 *     "executionError": "",
 *     "url": "http://grafana.com/dashboard/db/sensors"
 */
@Data
public class AlertingResponse {
    Long id;
    String dashboardUId;
    String dashboardSlug;
    String panelId;
    String name;
    String state;
    String newStateDate;
    String evalDate;
    String evalData;
    String executionError;
    String url;
}
