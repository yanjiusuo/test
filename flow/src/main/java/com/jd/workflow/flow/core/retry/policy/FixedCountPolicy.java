package com.jd.workflow.flow.core.retry.policy;

import com.jd.workflow.flow.core.retry.ExecContext;
import com.jd.workflow.flow.core.retry.RetryPolicy;

public class FixedCountPolicy extends RetryPolicy {
    int count;
    @Override
    public String getType() {
        return "fixedCount";
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean toBeContinue(ExecContext execContext) {
        if(execContext.getRetryCount() >= count) return false;// retryCount从0开始
        return true;
    }
}
