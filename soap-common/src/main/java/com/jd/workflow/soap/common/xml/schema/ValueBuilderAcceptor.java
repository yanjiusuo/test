package com.jd.workflow.soap.common.xml.schema;

import com.jd.workflow.soap.common.xml.XNode;

public interface ValueBuilderAcceptor {
    public Object afterSetValue(Object value, JsonType jsonType);
}
