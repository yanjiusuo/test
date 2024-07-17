package com.jd.workflow.metrics.alarm.granafa.api;

import com.jd.workflow.metrics.client.GrafanaClient;

public class BaseApi {
    protected GrafanaClient client;

    public void setClient(GrafanaClient client) {
        this.client = client;
    }
}
