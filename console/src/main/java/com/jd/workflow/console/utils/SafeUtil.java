package com.jd.workflow.console.utils;

import org.apache.commons.lang3.StringUtils;

public class SafeUtil {
    public static boolean sqlValidate(String str) {
        str = str.toLowerCase();//统一转为小写
        //危险字符
        String badStr = "'|\"|and|exec|execute|insert|create|drop|table|from|grant|use|group_concat|column_name|" +
                "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
                "chr|mid|master|case|truncate|char|declare|or|xor|&|;|-|--|+|,|like|//|/|%|#";//过滤掉的sql关键字，可以手动添加
        String[] badStrs = badStr.split("\\|");
        for (int i = 0; i < badStrs.length; i++) {
            if (str.indexOf(badStrs[i]) !=-1) {
                return true;
            }
        }
        return false;
    }
    public static boolean sqlValidate(String... stringParam) {
        for (String s : stringParam) {
            if(StringUtils.isBlank(s)){
                continue;

            }
            s = s.toLowerCase();//统一转为小写
            //危险字符
            String badStr = "'|\"|and|exec|execute|insert|create|drop|table|from|grant|use|group_concat|column_name|" +
                    "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
                    "chr|mid|master|case|truncate|char|declare|or|xor|&|;|-|--|+|,|like|//|/|%|#";//过滤掉的sql关键字，可以手动添加
            String[] badStrs = badStr.split("\\|");
            for (int i = 0; i < badStrs.length; i++) {
                if (s.indexOf(badStrs[i]) !=-1) {
                    return true;
                }
            }
        }
        return false;
    }
}
