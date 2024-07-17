package com.jd.workflow.metrics.summary;

public interface ISummary {
    public Watcher newWatcher(String ...labelValues);
}
