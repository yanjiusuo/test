package com.jd.workflow.flow.core.exception;

import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;

import javax.xml.soap.Detail;
import javax.xml.soap.SOAPFault;
import java.util.Map;

/**
 <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
     <soap:Body>
         <soap:Fault>
             <faultcode>soap:Server</faultcode>
             <faultstring>Fault occurred while processing.</faultstring>
             <detail>
                 <ns1:UserDefineException xmlns:ns1="http://service.workflow.jd.com/">
                     <message xsi:nil="true" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns2="http://service.workflow.jd.com/"/>
                     <data xsi:nil="true" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns2="http://service.workflow.jd.com/"/>
                     <code xsi:type="xs:int" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:ns2="http://service.workflow.jd.com/">0</code>
                 </ns1:UserDefineException>
         </detail>
         </soap:Fault>
     </soap:Body>
 </soap:Envelope>

 */
@Data
public class WebServiceError {

    String faultCode;
    String faultActor;
    String faultString;
    FaultDetail detail;
    @Data
    public static class FaultDetail{
        String name;
        Object desc;
    }
    public Map toMap(){
        return JsonUtils.cast(this,Map.class);
    }

}
