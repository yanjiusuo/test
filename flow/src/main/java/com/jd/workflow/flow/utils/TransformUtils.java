package com.jd.workflow.flow.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.XmlString;
import com.jd.workflow.soap.common.lang.IVariant;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.XmlUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用来在mvel里做数据转换
 */
public class TransformUtils {
    public static String formatDate(String str,String sourcePattern,String targetPattern) throws ParseException {
        if(StringUtils.isBlank(str)) return str;
        SimpleDateFormat sourceFormat = new SimpleDateFormat(sourcePattern);
        SimpleDateFormat targetFormat = new SimpleDateFormat(targetPattern);
        Date date = sourceFormat.parse(str);
        return targetFormat.format(date);
    }

    public static Map<String,Object> parseXml(String xml, String... arrayPaths){

        return XmlUtils.xmlToMap(xml,arrayPaths);
    }

    public static Object xpathSelectString(String xml,String expression) throws XPathExpressionException {
        return XmlUtils.select(xml,expression, XPathConstants.STRING);
    }
    public static Object xpathSelectNode(String xml,String expression,String ...arrayPaths) throws XPathExpressionException {
        return XmlUtils.select(xml,expression, XPathConstants.NODE,arrayPaths);
    }
    public static Object xpathSelectNodeList(String xml,String expression,String ...arrayPaths) throws XPathExpressionException {
        return XmlUtils.select(xml,expression, XPathConstants.NODESET,arrayPaths);
    }

    public static IVariant valueOf(Object o){
        return Variant.valueOf(o);
    }
    public static Date currentDate(){
        return new Date();
    }
    public static String currentDate(String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(currentDate());
    }
    public static Object parseJson(String pattern){
        return JsonUtils.parse(pattern);
    }
    public static Object parseJson(String pattern,Class clazz){
        return JsonUtils.parse(pattern,clazz);
    }
    public static String toJson(Object o){
        return JsonUtils.toJSONString(o);
    }
    public static Object jsonGet(Object o,String jsonPath){
        if(o == null) return null;
        Configuration option =
                Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse(o, option);

        return documentContext.read(jsonPath);
    }

    public static String render(String xmlTemplate){//, StepContext stepContext

        Map<String, Object> vars = EvalContextVars.getVars();
        if(vars == null){
            vars = new HashMap<>();
        }
        return render(xmlTemplate,vars);
    }
    public static String render(String xmlTemplate, Map<String,Object> vars){
        ParametersUtils utils = new ParametersUtils();

        String result = utils.evalJsonExpr(xmlTemplate, vars);
        return result;
    }

    /**
     * 将json转为xml, tagname为key
     * 比如：{
     *     a:1,
     *     "#attrs":{a:1}
     *     b:{c:1}
     * }
     * 被转换为<a a:1>1</a><b><c>1</c></b>
     * @param json
     * @return
     */
    public static String jsonToXml(Map<String,Object> json){
        if(ObjectHelper.isEmpty(json)) return "";
        XNode root = XNode.make("root");
        objToXml(json,null,root);
        String result = root.toXml(false,false);
        if (result.startsWith("<?xml ")) {

            result = result.substring(result.indexOf("?>") + 2);

        }
        return result.substring("<root>".length(),result.length()-"</root>".length()).trim();
    }
    static final String ATTR_NAME = "#attr";
    static final String TEXT_NAME = "#text";
    private static void objToXml(Object json,String arrayTagName,XNode parent){
        if(ObjectHelper.isEmpty(json)) return ;
        if(json instanceof List){
            List list = (List) json;
            for (Object o : list) {
                XNode child = XNode.make(arrayTagName);
                parent.appendChild(child);
                objToXml(o,null,child);
            }

        }else if(json instanceof Map){
            Map map = (Map) json;

            Object attrsObj = map.get(ATTR_NAME);
            if(attrsObj != null){
                Map<String,Object> attrs = (Map) attrsObj;
                for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                    parent.attr(entry.getKey(),Variant.valueOf(entry.getValue()).toString());
                }
            }
            for (Object entrySetObj : map.entrySet()) {
                Map.Entry<String,Object> entry = (Map.Entry) entrySetObj;
                if("#text".equals(entry.getKey())){
                    parent.content(Variant.valueOf(entry.getValue()).toString());
                    continue;
                }
                if(entry.getKey().startsWith("#")){
                    continue;
                }
                if(entry.getValue() instanceof List){
                    objToXml(entry.getValue(),entry.getKey(),parent);
                    continue;
                }

                XNode child = XNode.make(entry.getKey());
                parent.appendChild(child);
                objToXml(entry.getValue(),entry.getKey(),child);
            }
        }else{
            parent.content(Variant.valueOf(json).toString());
        }

    }
}
