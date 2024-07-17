package com.jd.workflow.console.utils;

import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板处理工具类
 * @author liangchengshuo
 * @date 2021-08-03
 */
public class TemplateUtils {
    /**
     *
     */
    public static void main(String[] sdd) throws Exception {
        String templateStr = "hello,$<{name}>,我今年,$<{age}>岁.";
        List<String> keyList = getKeyList(templateStr);
        for (String key : keyList) {
            System.out.println(key);
        }
    }

    /**
     *
     */
    public static String renderTpl(String templateStr, Map<String, String> context){
        return replace(templateStr, context, "$<{", "}>", false);
    }

    /**
     * 替换
     * @param source 源内容
     * @param parameter 占位符参数
     * @param prefix 占位符前缀 例如:${
     * @param suffix 占位符后缀 例如:}
     * @param enableSubstitutionInVariables 是否在变量名称中进行替换 例如:${system-${版本}}
     *
     * 转义符默认为'$'。如果这个字符放在一个变量引用之前，这个引用将被忽略，不会被替换 如$${a}将直接输出${a}
     * @return
     */
    public static String replace(String source,Map<String, String> parameter,String prefix, String suffix,boolean enableSubstitutionInVariables){
        StringSubstitutor strSubstitutor = new StringSubstitutor(parameter,prefix, suffix);
        strSubstitutor.setEnableSubstitutionInVariables(enableSubstitutionInVariables);
        return strSubstitutor.replace(source);
    }

    /**
     *
     * @param text
     * @return
     */
    public static List<String> getKeyList(String text) {
        String pattern = "\\$\\<\\{.*?\\}\\>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        List<String> matchList = new ArrayList<>();
        while (m.find()) {
            String group = m.group();
            matchList.add(group);
        }
        return matchList;
    }

    /**
     *
     * @param text
     * @return
     */
    public static String genKey(Serializable text) {
        return "$<{"+text+"}>";
    }

}
