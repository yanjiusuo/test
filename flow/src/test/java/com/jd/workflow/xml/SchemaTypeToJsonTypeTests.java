package com.jd.workflow.xml;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.xml.SchemaTypeToJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.*;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class SchemaTypeToJsonTypeTests extends BaseTestCase {
    SchemaTypeLoader schemaTypeLoader;
    XmlObject loadXml(String fileName){
        File file = null;

        try {
            file = loadFile("classpath:xsd/"+fileName);
            String xml = IOUtils.toString(new FileInputStream(file));
            XmlObject xmlObject = XmlObject.Factory.parse(xml);
            return xmlObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    SchemaType loadSchema(String fileName){
        return loadXml(fileName).schemaType();

    }
    SchemaTypeLoader getSchemaTypeLoader(String fileName) {
        try{
            XmlOptions options = new XmlOptions();
            options.setCompileNoValidation();
            options.setCompileNoPvrRule();
            options.setCompileDownloadUrls();
            options.setCompileNoUpaRule();
            options.setValidateTreatLaxAsSkip();
            XmlObject xmlObject = loadXml(fileName);
            SchemaTypeSystem schemaTypes = XmlBeans.compileXsd(new XmlObject[]{xmlObject},
                    XmlBeans.getBuiltinTypeSystem(), options);
            List<XmlObject> objects = new ArrayList<>();
            objects.add(xmlObject);
            SchemaType schemaType = xmlObject.schemaType();
            schemaTypeLoader = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{schemaTypes,
                    XmlBeans.getBuiltinTypeSystem()});

            return schemaTypeLoader;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
     @Test
     public void testSchema2Xml(){


    }
    static final String targetNamespace = "http://schemas.eviware.com/TestService/v1/";
     @Test public void testListString(){
        SchemaType schemaType = getSchemaType("ListString");

        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
    private SchemaType getSchemaType(String name){

        return getSchemaType(targetNamespace, name);
    }
    private SchemaType getSchemaType(String uri,String name){
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        cursor.toNextToken();
        cursor.beginElement("root");
        cursor.toFirstChild();
        QName qName = new QName(uri, name);
        schemaTypeLoader =  getSchemaTypeLoader("testservice1.xsd");
        SchemaType schemaType = schemaTypeLoader.findType(qName);
        if(schemaType == null){
            return schemaTypeLoader.findDocumentType(qName);
            /*SchemaGlobalElement elm = schemaTypeLoader.findElement(qName);
            if (elm != null) {
                cursor.toFirstChild();
                 return elm.getType();
            }*/
        }
        return schemaType;
    }
     @Test public void testListEnum(){

        SchemaType schemaType = getSchemaType("ListString");
        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
     @Test public void testListMap(){
        SchemaType schemaType = getSchemaType("ArrayOfRawNameAndXml");

        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
     @Test public void testElemListMap(){
        SchemaType schemaType = getSchemaType(targetNamespace,"GetPage");


        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
     @Test public void testElementListMap(){
        SchemaType schemaType = getSchemaType("SignatureHeader");

        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
     @Test public void testNestMap(){
        SchemaType schemaType = getSchemaType("GetDefaultPageData");

        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
     @Test public void testArrayOfRaw(){
        SchemaType schemaType = getSchemaType("ArrayOfRawProperty");

        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
    }
}
