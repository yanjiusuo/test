package com.jd.workflow.flow.core.definition;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;

public class ListAggregationStrategy extends AbstractListAggregationStrategy {

    @Override
    public Object getValue(Exchange exchange) {
        return exchange.getIn().getBody();
    }
}