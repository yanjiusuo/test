package com.jd.workflow.flow.core.camel;

import com.jd.workflow.soap.common.xml.XNode;

public interface RouteStepBuilder {
    public static final String SUB_FLOW_PREFIX = "subFlow";
    public void build(XNode parent,XNode root,String nodeIdPrefix);
}
