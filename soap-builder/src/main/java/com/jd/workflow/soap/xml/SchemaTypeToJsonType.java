package com.jd.workflow.soap.xml;

import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.exception.XmlTransformException;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import org.apache.xmlbeans.*;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * 将schematype转为json desc类型，方便webservice录入
 * xml schema有点过于灵活，而json schema则更方便加工
 * 根据json schema比较方便生成xml信息
 *  不生成anyOf、oneOf约束
 * json描述类型如下：
 JsonDesc总共有这几种描述：
 {
 type:array,
 items:JsonDesc
 }
 {
 type:object,
 properties:{
 name:JsonDesc,
 value:JsonDesc
 }
 },
 {
 type:string,
 required:true
 }
 XmCursor :https://xmlbeans.apache.org/guide/CursorNavigation.html
 * 用来将java类转成schema，可以参考：https://docs.oracle.com/javase/9/tools/schemagen.htm#JSWOR738
 *
 *  java生成schema:https://docs.oracle.com/javase/tutorial/jaxb/intro/j2schema.html
 *  schemaType与java类型映射：https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 */
public class SchemaTypeToJsonType {

    private boolean soapEnc;
    private boolean exampleContent = false;
    private boolean typeComment = false;

    private boolean skipComments = false;
    private boolean ignoreOptional = false;
    private Set<QName> excludedTypes = new HashSet<QName>();


    private ArrayList<SchemaType> typeStack = new ArrayList<SchemaType>();

    public SchemaTypeToJsonType(boolean soapEnc) {
       this(soapEnc,SoapContext.DEFAULT);
    }
    public SchemaTypeToJsonType(boolean soapEnc, SoapContext context) {
        this.soapEnc = soapEnc;
        excludedTypes.addAll(context.getExcludedTypes());
    }

    public boolean isSoapEnc() {
        return soapEnc;
    }

    public JsonType createJsonType(SchemaType stype) {

        BuilderJsonType jsonType = new BuilderJsonType();
       if(stype.getName() != null){
            //jsonType.setXmlns(stype.getName().getNamespaceURI());
            jsonType.setName(stype.getName());
        }
        /*if(soapEnc){
            // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            jsonType.addXmlAttr("xmlns:xsi",XSI_TYPE.getNamespaceURI());
            jsonType.insertAttributeWithValue(XSI_TYPE,);
        }*/

        // Using the type and the cursor, call the utility method to get a
        // sample XML payload for that Schema element
         createJsonType(stype, jsonType);
        return jsonType.toJsonType();
        // Cursor now contains the sample payload
        // Pretty print the result. Note that the cursor is positioned at the
        // end of the doc so we use the original xml object that the cursor was
        // created upon to do the xmlText() against.


    }
    /**
     * Cursor position Before: <theElement>^</theElement> After:
     * <theElement><lots of stuff/>^</theElement>
     */
    public void createJsonType(SchemaType stype, BuilderJsonType parentJsonType) {
        QName nm = stype.getName();
        if (nm == null && stype.getContainerField() != null)
            nm = stype.getContainerField().getName();

        if (nm != null && excludedTypes.contains(nm)) {

            return;
        }

        if (typeStack.contains(stype))
            return ;

        typeStack.add(stype);

        try {
            if (stype.isSimpleType() || stype.isURType()) {
                 processSimpleType(stype, parentJsonType);
            }

            processAttributes(stype, parentJsonType);
            // <theElement attri1="string">^</theElement>
            switch (stype.getContentType()) {
                case SchemaType.NOT_COMPLEX_TYPE:
                case SchemaType.EMPTY_CONTENT: //1
                   //parentJsonType.setType(SimpleParamType.STRING);
                    return ;
                case SchemaType.SIMPLE_CONTENT: {
                     processSimpleType(stype, parentJsonType);
                }
                case SchemaType.MIXED_CONTENT:
                    /*ObjectJsonType objectJsonType = new ObjectJsonType();
                    objectJsonType.addChild(new SimpleJsonType(SimpleParamType._TEXT));
                    //xmlc.insertChars(pick(WORDS) + " ");
                    if (stype.getContentModel() != null) {
                        objectJsonType.addChild(processParticle(stype.getContentModel(), xmlc, true));
                    }
                    objectJsonType.addChild(new SimpleJsonType(SimpleParamType._TEXT));
                    //xmlc.insertChars(pick(WORDS));
                    return  objectJsonType;*/
                    /*
                     暂时不支持混合内容
                    * */
                    throw new XmlTransformException("xml.err_mixed_content_not_support");
                case SchemaType.ELEMENT_CONTENT:
                    if (stype.getContentModel() != null) {
                        processParticle(stype.getContentModel(), parentJsonType, false);
                    }
                    break;
            }
            // complex Type
            // <theElement>^</theElement>

        } finally {
            typeStack.remove(typeStack.size() - 1);
        }
    }

    private void processSimpleType(SchemaType stype, BuilderJsonType parentType) {

        if (soapEnc) {
            QName typeName = stype.getName();
            if (typeName != null) {
               // parentType.setXsiType( formatQName(parentType, typeName));
                parentType.insertAttributeWithValue(XSI_TYPE,formatQName(parentType, typeName));
            }
        }

        SimpleParamType dataType = sampleDataForSimpleType(stype);
        parentType.setName(parentType.getName());
        parentType.setType(dataType);

    }
    private SimpleParamType sampleDataForSimpleType(SchemaType sType) {


        SchemaType primitiveType = sType.getPrimitiveType();


        if (sType.getSimpleVariety() == SchemaType.LIST) {
            return SimpleParamType.STRING;
        }

        if (sType.getSimpleVariety() == SchemaType.UNION) {
            return SimpleParamType.STRING;
        }

        XmlAnySimpleType[] enumValues = sType.getEnumerationValues();
        if (enumValues != null && enumValues.length > 0) {
            return SimpleParamType.STRING;
        }
        if (XmlObject.type.equals(sType))
            throw new XmlTransformException("xml.err_anyType_not_support");

      /*  if (XmlAnySimpleType.type.equals(sType))
            throw new XmlTransformException("xml.err_anyType_not_support");
            <xs:element minOccurs="0" name="dateTimeVar" type="xs:anySimpleType"/>
            */

        switch (primitiveType.getBuiltinTypeCode()) {
            case SchemaType.BTC_BOOLEAN:
                return SimpleParamType.BOOLEAN;
            case SchemaType.BTC_NOT_BUILTIN:
            case SchemaType.BTC_ANY_TYPE:
            case SchemaType.BTC_ANY_SIMPLE:
            case SchemaType.BTC_BASE_64_BINARY:
            case SchemaType.BTC_HEX_BINARY:
            case SchemaType.BTC_ANY_URI:
            case SchemaType.BTC_QNAME:
            case SchemaType.BTC_NOTATION:
                return SimpleParamType.STRING;
            case SchemaType.BTC_FLOAT:
                return SimpleParamType.FLOAT;
            case SchemaType.BTC_DOUBLE:
                return SimpleParamType.DOUBLE;
            case SchemaType.BTC_DECIMAL:
                switch (closestBuiltin(sType).getBuiltinTypeCode()) {
                    case SchemaType.BTC_SHORT:
                    case SchemaType.BTC_INTEGER:
                    case SchemaType.BTC_NON_POSITIVE_INTEGER:
                    case SchemaType.BTC_NEGATIVE_INTEGER:
                    case SchemaType.BTC_NON_NEGATIVE_INTEGER:
                    case SchemaType.BTC_POSITIVE_INTEGER:
                    case SchemaType.BTC_UNSIGNED_SHORT:
                    case SchemaType.BTC_UNSIGNED_BYTE:
                    case SchemaType.BTC_INT:
                    case SchemaType.BTC_UNSIGNED_INT:
                    case SchemaType.BTC_BYTE:
                        return SimpleParamType.INTEGER;
                    case SchemaType.BTC_LONG:
                    case SchemaType.BTC_UNSIGNED_LONG:
                        return SimpleParamType.LONG;

                    default:
                    case SchemaType.BTC_DECIMAL:
                        return SimpleParamType.STRING;
                }
            case SchemaType.BTC_STRING:
            case SchemaType.BTC_DURATION:
            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                return SimpleParamType.STRING;
            default:
                return SimpleParamType.STRING;
        }
    }





    private SchemaType closestBuiltin(SchemaType sType) {
        while (!sType.isBuiltinType())
            sType = sType.getBaseType();
        return sType;
    }



    /**
     * Cursor position: Before this call: <outer><foo/>^</outer> (cursor at the
     * ^) After this call: <<outer><foo/><bar/>som text<etc/>^</outer>
     */
    private void processParticle(SchemaParticle sp, BuilderJsonType parentJsonType, boolean mixed) {
        boolean isArrayType = isArrayType(sp);

        /*if(isArrayType){
            ArrayJsonType arrayJsonType = new ArrayJsonType();
            arrayJsonType.setName(parentJsonType.getName());
            arrayJsonType.addChild(internalProcessParticle(sp,parentJsonType,mixed));
            return arrayJsonType;
        }else{
            return internalProcessParticle(sp,parentJsonType,mixed);
        }*/
        int beforeSize = parentJsonType.getChildren().size();
        internalProcessParticle(sp,parentJsonType,mixed);
        List<BuilderJsonType> added = parentJsonType.getChildren().subList(beforeSize,parentJsonType.getChildren().size());

        if(isArrayType){ // 本次添加的对象需要作为array类型
            for (BuilderJsonType child : added) {
                if("object".equalsIgnoreCase(child.getType())){ // 子节点可能为数组或者对象
                    BuilderJsonType childChild = child.getChildren().get(0);
                    if(child.getChildren().size() == 1
                     && "array".equalsIgnoreCase(child.getChildren().get(0).getType())
                    ){
                        child.setArrayItemType("array");
                    }else{
                        child.setArrayItemType("object");
                    }
                }else{
                    child.setArrayItemType(child.getType());
                }

                child.setType("array");
            }
        }

    }
    boolean isRequired(SchemaParticle sp){
        int minOccurs = sp.getIntMinOccurs();
        //int maxOccurs = sp.getIntMaxOccurs();
        if(minOccurs <= 0){
            return false;
        }
        return true;
    }
    private void internalProcessParticle(SchemaParticle sp, BuilderJsonType parentJsonType, boolean mixed) {
        switch (sp.getParticleType()) {
            case (SchemaParticle.ELEMENT):
                 processElement(sp, parentJsonType, mixed);
                 break;
            case (SchemaParticle.SEQUENCE):
            case (SchemaParticle.CHOICE):
            case (SchemaParticle.ALL):
                 processSequence(sp, parentJsonType, mixed);
                 break;
                /*return processChoice(sp, parentJsonType, mixed);
                return processAll(sp, parentJsonType, mixed);*/
            case (SchemaParticle.WILDCARD):
                //processWildCard(sp, xmlc, mixed);
                throw new XmlTransformException("xml.err_wildcard_type_is_not_support");
            default:
                throw new XmlTransformException("No Match on Schema Particle Type: " +
                        String.valueOf(sp.getParticleType()));
        }

    }
    public boolean isArrayType(SchemaParticle sp){
        if (sp.getMaxOccurs() == null || sp.getIntMaxOccurs() > 1) {
            return true;
        }
        return false;
    }



    private void processElement(SchemaParticle sp, BuilderJsonType xmlc, boolean mixed) {
        // cast as schema local element
        SchemaLocalElement element = (SchemaLocalElement) sp;

        // Add comment about type
        //addElementTypeAndRestricionsComment(element, xmlc);

        // / ^ -> <elemenname></elem>^
        BuilderJsonType jsonType = xmlc.createChild();

        QName tagName = null;
        if (soapEnc)
            //xmlc.insertElement(element.getName().getLocalPart()); // test
            tagName = new QName(null,element.getName().getLocalPart());

            // encoded?
            // drop
            // namespaces.
        else
            tagName =  new QName(element.getName().getNamespaceURI(),element.getName().getLocalPart());
        jsonType.setName(tagName);
        if(isRequired(sp)){
                jsonType.setRequired(true);
        }
        xmlc.setType("object");
        // / -> <elem>^</elem>
        // processAttributes( sp.getType(), xmlc );


        // -> <elem>stuff^</elem>

        if (sp.isDefault()){ // 若parent是array类型，则为List<String>,否则为简单元素类型
            jsonType.setType(SimpleParamType.STRING);
        }
        else {
            createJsonType(element.getType(), jsonType);
        }

        // -> <elem>stuff</elem>^
    }



   static String prefixForNamespace(BuilderJsonType jsonType,String namespaceURI){

        return jsonType.prefixForNamespace(namespaceURI,null,true);
    }

    private static final String formatQName(BuilderJsonType xmlc, QName qName) {

        String prefix = prefixForNamespace(xmlc,qName.getNamespaceURI());
        String name;
        if (prefix == null || prefix.length() == 0)
            name = qName.getLocalPart();
        else
            name = prefix + ":" + qName.getLocalPart();
        return name;
    }
    private static final String formatQName(XmlCursor xmlc, QName qName) {
        XmlCursor parent = xmlc.newCursor();
        parent.toParent();
        String prefix = parent.prefixForNamespace(qName.getNamespaceURI());
        parent.dispose();
        String name;
        if (prefix == null || prefix.length() == 0)
            name = qName.getLocalPart();
        else
            name = prefix + ":" + qName.getLocalPart();
        return name;
    }

    private static final QName HREF = new QName("href");
    private static final QName ID = new QName("id");
    public static final QName XSI_TYPE = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");
    public static final QName ENC_ARRAYTYPE = new QName("http://schemas.xmlsoap.org/soap/encoding/", "arrayType");
    private static final QName ENC_OFFSET = new QName("http://schemas.xmlsoap.org/s/encoding/", "offset");

    public static final Set<QName> SKIPPED_SOAP_ATTRS = new HashSet<QName>(Arrays.asList(new QName[]{HREF, ID,
            ENC_OFFSET}));

    private void processAttributes(SchemaType stype, BuilderJsonType xmlc) {
        if (soapEnc) {
            QName typeName = stype.getName();
            if (typeName != null) {
               // xmlc.setXsiType(formatQName(xmlc, typeName));
                xmlc.insertAttributeWithValue(XSI_TYPE,formatQName(xmlc,typeName));
            }
        }

        SchemaProperty[] attrProps = stype.getAttributeProperties();
        for (int i = 0; i < attrProps.length; i++) {
            SchemaProperty attr = attrProps[i];
            if (attr.getMinOccurs().intValue() == 0 && ignoreOptional)
                continue;

            if (attr.getName().equals(new QName("http://www.w3.org/2005/05/xmlmime", "contentType"))) {
                xmlc.addXmlAttr(attr.getName(), SimpleParamType.STRING.typeName());
                continue;
            }

            if (soapEnc) {
                if (SKIPPED_SOAP_ATTRS.contains(attr.getName()))
                    continue;
                if (ENC_ARRAYTYPE.equals(attr.getName())) {
                 /*   SOAPArrayType arrayType = ((SchemaWSDLArrayType) stype.getAttributeModel().getAttribute(
                            attr.getName())).getWSDLArrayType();
                    if (arrayType != null)
                        xmlc.insertAttributeWithValue(attr.getName(),
                                formatQName(xmlc, arrayType.getQName()) + arrayType.soap11DimensionString());*/
                    continue;
                }
            }

            String value = null;

            if (value == null)
                value = attr.getDefaultText();
            if (value == null)
                value = sampleDataForSimpleType(attr.getType()).typeName();

            xmlc.addXmlAttr(attr.getName(), value);
        }

        /*if (soapEnc) {

            QName typeName = stype.getName();
            if (typeName != null) { // 看下父元素里对应的namespace是啥，

                formatQName(xmlc, typeName,pare);
            }
        }
        if(!(jsonType instanceof ObjectJsonType)){
            return;
        }
        SchemaProperty[] attrProps = stype.getAttributeProperties();
        for (int i = 0; i < attrProps.length; i++) {
            SchemaProperty attr = attrProps[i];

            if (soapEnc) {
                if (SKIPPED_SOAP_ATTRS.contains(attr.getName())){
                    continue;
                }
            }
            SimpleParamType paramType = SimpleParamType._ATTR;
            SimpleJsonType simpleJsonType = new SimpleJsonType(paramType);
            simpleJsonType.setName(attr.getName());
            ((ObjectJsonType) jsonType).addChild(simpleJsonType);
            //xmlc.insertAttributeWithValue(attr.getName(), value);
        }*/
    }

    private void processSequence(SchemaParticle sp, BuilderJsonType xmlc, boolean mixed) {
        SchemaParticle[] spc = sp.getParticleChildren();
        xmlc.setType("object");
        for (int i = 0; i < spc.length; i++) {
            // / <parent>maybestuff^</parent>
            processParticle(spc[i], xmlc, mixed);
            // <parent>maybestuff...morestuff^</parent>
            if (mixed && i < spc.length - 1){
                 //jsonType.addChild();
                xmlc.addChild(BuilderJsonType.from(SimpleParamType._TEXT));
            }

        }

    }




    /**
     * This method will get the base type for the schema type
     */

    @SuppressWarnings("unused")
    private static QName getClosestName(SchemaType sType) {
        while (sType.getName() == null)
            sType = sType.getBaseType();

        return sType.getName();
    }



    public void setTypeComment(boolean b) {
        typeComment = b;
    }

    public void setIgnoreOptional(boolean b) {
        ignoreOptional = b;
    }
}
