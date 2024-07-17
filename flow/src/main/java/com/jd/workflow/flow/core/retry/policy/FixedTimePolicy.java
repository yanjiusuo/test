package com.jd.workflow.flow.core.retry.policy;

import com.jd.workflow.flow.core.retry.ExecContext;
import com.jd.workflow.flow.core.retry.RetryPolicy;

public class FixedTimePolicy extends RetryPolicy {
    int waitTime;
    @Override
    public String getType() {
        return "fixedTime";
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    public boolean toBeContinue(ExecContext execContext) {
        if(execContext.getInvokedTime() > waitTime) return false;
        return true;
    }
}
