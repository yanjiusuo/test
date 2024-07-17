package com.jd.workflow.soap.common.xml;

import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
public class JsonTypeUtilsTests extends TestCase {
    String simpleXml = "<a>1</a>";
    String objXml = "<a><b>c</b></a>";
    String arrayXml = "<a><b>c</b><b>c</b></a>";
    String arrayObjXml = "<a><b><d>1</d></b><b><d>1</d></b></a>";
    public void testToXml(){
        Object simple = JsonTypeUtils.xmlNodeToJson(XmlUtils.parseXml(simpleXml).getDocumentElement());
        Object obj =  JsonTypeUtils.xmlNodeToJson(XmlUtils.parseXml(objXml).getDocumentElement());
        Object array = JsonTypeUtils.xmlNodeToJson(XmlUtils.parseXml(arrayXml).getDocumentElement());
        Object arrayObjString =  JsonTypeUtils.xmlNodeToJson(XmlUtils.parseXml(arrayObjXml).getDocumentElement());
        assertEquals("{\"a\":\"1\"}",JsonUtils.toJSONString(simple));
        assertEquals("{\"a\":{\"b\":\"c\"}}",JsonUtils.toJSONString(obj));
        assertEquals("{\"a\":{\"b\":[\"c\",\"c\"]}}",JsonUtils.toJSONString(array));
        assertEquals("{\"a\":{\"b\":[{\"d\":\"1\"},{\"d\":\"1\"}]}}",JsonUtils.toJSONString(arrayObjString));

        log.info("simple={}",simple);
        log.info("obj={}",obj);
        log.info("array={}",array);
        log.info("arrayObjString={}",arrayObjString);

    }
    protected String getResourceContent(String path){

        try {
            File file = ResourceUtils.getFile(path);
            return IOUtils.toString(new FileInputStream(file),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }
    /*
      转换：将 json转换
     */
    public void testParseJsonString(){
        String jsonTypeStr = getResourceContent("classpath:json_type/json-string-or-array.json");
        JsonType jsonType = JsonUtils.parse(jsonTypeStr,JsonType.class);
        String data = "{\"id\":1,\"jsonMap\":\"{\\\"id\\\":1,\\\"name\\\":\\\"name\\\"}\",\"jsonArray\":\"[{\\\"id\\\":1,\\\"name\\\":\\\"name\\\"}]\"}";
        Object obj = JsonTypeUtils.parseJson(data, jsonType);
        System.out.println(obj);
    }

}
