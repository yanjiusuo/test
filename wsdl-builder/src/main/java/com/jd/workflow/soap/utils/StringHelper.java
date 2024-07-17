package com.jd.workflow.soap.utils;

import com.jd.workflow.soap.common.exception.StdException;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringHelper {

    public static String getPkgNameByNamespace(String targetNamespace){
        if(StringUtils.isEmpty(targetNamespace)){
            throw new StdException("wsdl.err_target_namespace_not_allow_empty");
        }
        String namespace = targetNamespace;
        try {
            URL url = new URL(targetNamespace);
            namespace = url.getHost();
        } catch (MalformedURLException e) {

        }
        String[] strs = StringUtils.split(namespace, ".");
        List<String> list = new ArrayList<>();
        for (String str : strs) {
            list.add(str);
        }
        Collections.reverse(list);
        String pkgName = list.stream().collect(Collectors.joining("."));
        return pkgName;
    }
    public static String getPkgNameByClassName(String className){
       if(className.indexOf(".") == -1){
           return "";
       }
       return className.substring(0,className.lastIndexOf("."));
    }
    public static String simpleClassName(String className){
        if(className.indexOf(".") == -1){
            return className;
        }
        return className.substring(className.lastIndexOf(".")+1);
    }
}
