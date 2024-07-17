package com.jd.workflow.flow.core.retry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jd.workflow.flow.core.retry.execute.FixedDelayInterval;
import com.jd.workflow.flow.core.retry.execute.ImmediateExecuteInterval;
import com.jd.workflow.flow.core.retry.policy.FixedCountPolicy;
import com.jd.workflow.flow.core.retry.policy.FixedTimePolicy;

/**
 * 执行下一个任务的时候需要等待的间隔
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,property = "type",visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImmediateExecuteInterval.class, name = "immediate"),
        @JsonSubTypes.Type(value = FixedDelayInterval.class ,names = {
                "fixedDelay"
        }),
})
public abstract class ExecuteInterval {
    public abstract String getType();
    /**
     * 获取执行下一次任务的时间间隔
     * @param context
     * @return
     */
    public abstract int getNextInterval(ExecContext context);
}
