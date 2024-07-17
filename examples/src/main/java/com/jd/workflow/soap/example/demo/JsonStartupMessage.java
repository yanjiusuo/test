package com.jd.workflow.soap.example.demo;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.StartupListener;
import com.jd.workflow.soap.example.custom.OperationCallInfo;

public class JsonStartupMessage implements StartupListener {
    String body = "{\"details\":1,\"email\":\"wangjingfang3@jd.com\"}";
    @Override
    public void onCamelContextStarted(CamelContext context, boolean alreadyStarted) throws Exception {
        String reqBody = "{\"arg0\":{\"details\":1,\"email\":\"wangjingfang3@jd.com\"},\"arg1\":{\"id\":\"ava\"}}";
        OperationCallInfo callInfo = new OperationCallInfo();
        callInfo.setBindingName("IncidentServiceSoapBinding");
        callInfo.setInput(reqBody);
        callInfo.setOperationName("reportIncident");

        try (ProducerTemplate template = context.createProducerTemplate()) {

            template.sendBody("direct:start", callInfo);
        }
    }
}
