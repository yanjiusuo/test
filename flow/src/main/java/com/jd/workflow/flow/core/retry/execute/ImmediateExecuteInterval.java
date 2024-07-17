package com.jd.workflow.flow.core.retry.execute;

import com.jd.workflow.flow.core.retry.ExecContext;
import com.jd.workflow.flow.core.retry.ExecuteInterval;

public class ImmediateExecuteInterval extends ExecuteInterval {
    @Override
    public String getType() {
        return "immediate";
    }

    @Override
    public int getNextInterval(ExecContext context) {
        return 0;
    }
}
