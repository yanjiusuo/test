package com.jd.workflow.flow.core.retry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jd.workflow.flow.core.retry.policy.FixedCountPolicy;
import com.jd.workflow.flow.core.retry.policy.FixedTimePolicy;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,property = "type",visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FixedTimePolicy.class, name = "fixedTime"),
        @JsonSubTypes.Type(value = FixedCountPolicy.class ,names = {
                "fixedCount"
        }),
})
public abstract class RetryPolicy {
    public abstract String getType();
    public abstract  boolean toBeContinue(ExecContext execContext);
}
