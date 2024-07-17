package com.jd.workflow.soap.common.mapping;

import com.jd.workflow.soap.common.exception.StdException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommonParamMappingUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(CommonParamMappingUtils.class);

    // deep clone using json - POJO
    public Map<String, Object> clone(Map<String, Object> inputTemplate) {
        return (Map<String, Object>)cloneObj(inputTemplate);
    }
    private Object cloneObj(Object o){
        if(o == null) return o;
        if(o instanceof Map){
            Map map = new HashMap<>();
            Map m = (Map) o;
            for (Object o1 : m.entrySet()) {
                Map.Entry entry = (Map.Entry) o1;
                map.put(entry.getKey(),cloneObj(entry.getValue()));
            }
            return map;
        }else if(o instanceof List){
            List list = new ArrayList();
            for(Object item : (List)o){
                list.add(cloneObj(item));
            }
            return list;
        }else{
            return o;
        }

    }

   /* public Map<String, Object> replace(Map<String, Object> input, Object json) {
        Object doc;
        if (json instanceof String) {
            doc = JsonPath.parse(json.toString());
        } else {
            doc = json;
        }


        Configuration option =
                Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse(doc, option);
        return replace(input, documentContext);
    }*/

    public Object replace(String paramString) {
        Configuration option =
                Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse(Collections.emptyMap(), option);
        return replaceVariables(paramString, documentContext);
    }
    public Object replace(Object value,EvalContext context){
        if(value  == null) return null;
        if(value instanceof String){
            return replaceVariables((String)value,context.getDocumentContext());
        }else if(value instanceof Map){
            return replace((Map<String, Object>) value,context);
        }else if(value instanceof List){
            return replaceList((List<?>) value,context);
        }else{
            throw new StdException("utils.err_not_support");
        }
    }
    @SuppressWarnings("unchecked")
    public Map<String, Object> replace(
            Map<String, Object> input, EvalContext evalContext) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> e : input.entrySet()) {
            Object newValue;
            Object value = e.getValue();
            if (value instanceof String) {
                newValue = replaceVariables(value.toString(), evalContext.getDocumentContext());
            }else if(value instanceof ICustomParameterMapper){
                newValue = ((ICustomParameterMapper) value).evaluate(this,evalContext);
            }else if (value instanceof Map) {
                // recursive call
                newValue = replace((Map<String, Object>) value, evalContext);
            } else if (value instanceof List) {
                newValue = replaceList((List<?>) value, evalContext);
            } else {
                newValue = value;
            }
            result.put(e.getKey(), newValue);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Object replaceList(List<?> values, EvalContext evalContext) {
        List<Object> replacedList = new LinkedList<>();
        for (Object listVal : values) {
            if (listVal instanceof String) {
                Object replaced = replaceVariables(listVal.toString(), evalContext.getDocumentContext());
                replacedList.add(replaced);
            } else if (listVal instanceof Map) {
                Object replaced = replace((Map<String, Object>) listVal, evalContext);
                replacedList.add(replaced);
            } else if (listVal instanceof List) {
                Object replaced = replaceList((List<?>) listVal, evalContext);
                replacedList.add(replaced);
            } else {
                replacedList.add(listVal);
            }
        }
        return replacedList;
    }

    public Object replaceVariables(
            String paramString, DocumentContext documentContext) {
        String[] values = paramString.split("(?=(?<!\\$)\\$\\{)|(?<=})");
        Object[] convertedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            convertedValues[i] = values[i];
            if (values[i].startsWith("${") && values[i].endsWith("}")) {
                String paramPath = values[i].substring(2, values[i].length() - 1);
                // if the paramPath is blank, meaning no value in between ${ and }
                // like ${}, ${  } etc, set the value to empty string
                if (StringUtils.isBlank(paramPath)) {
                    convertedValues[i] = "";
                    continue;
                }
               /* if (EnvUtils.isEnvironmentVariable(paramPath)) {
                    String sysValue = EnvUtils.getSystemParametersValue(paramPath, taskId);
                    if (sysValue != null) {
                        convertedValues[i] = sysValue;
                    }

                } else {*/
                try {
                    convertedValues[i] = documentContext.read(paramPath);
                } catch (Exception e) {
                    LOGGER.warn(
                            "Error reading documentContext for paramPath: {}. Exception: {}",
                            paramPath,
                            e);
                    convertedValues[i] = null;
                }
                //}
            } else if (values[i].contains("$${")) {
                convertedValues[i] = values[i].replaceAll("\\$\\$\\{", "\\${");
            }
        }

        Object retObj = convertedValues[0];
        // If the parameter String was "v1 v2 v3" then make sure to stitch it back
        if (convertedValues.length > 1) {
            for (int i = 0; i < convertedValues.length; i++) {
                Object val = convertedValues[i];
                if (val == null) {
                    val = "";
                }
                if (i == 0) {
                    retObj = val;
                } else {
                    retObj = retObj + "" + val.toString();
                }
            }
        }
        return retObj;
    }
    @Data
   public static final class EvalContext{
        DocumentContext documentContext;
        Map<String,Object> args;
    }
}
