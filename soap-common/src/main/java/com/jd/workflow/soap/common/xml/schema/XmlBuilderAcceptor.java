package com.jd.workflow.soap.common.xml.schema;

import com.jd.workflow.soap.common.xml.XNode;

public interface XmlBuilderAcceptor {
    public void beforeBuildNode(XNode node, JsonType jsonType);
    public void afterBuildNode(XNode node, JsonType jsonType);
}
