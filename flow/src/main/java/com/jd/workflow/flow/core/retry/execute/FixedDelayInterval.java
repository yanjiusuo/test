package com.jd.workflow.flow.core.retry.execute;

import com.jd.workflow.flow.core.retry.ExecContext;
import com.jd.workflow.flow.core.retry.ExecuteInterval;

public class FixedDelayInterval extends ExecuteInterval {
    int delayTime;
    @Override
    public String getType() {
        return "fixedDelay";
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    @Override
    public int getNextInterval(ExecContext context) {
        return delayTime;
    }
}
