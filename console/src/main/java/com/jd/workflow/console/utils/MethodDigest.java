package com.jd.workflow.console.utils;

import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 方法的摘要分为2个部分，结构摘要以及内容摘要，已_分割
 * 结构摘要放到第一个，内容摘要放到第二个
 */
@Data
public class MethodDigest {
    private String contentDigest;
    private String structDigest;
    public static MethodDigest parse(String str){
        if(StringHelper.isEmpty(str)) return null;
         List<String> split = StringHelper.split(str, "_");
        MethodDigest digest = new MethodDigest();
        digest.structDigest = split.get(0);
        if(split.size() > 1){
            digest.contentDigest = split.get(1);
        }

        return digest;
    }
    public String toString(){
        return structDigest+"_"+contentDigest;
    }
    public boolean structHasUpdate(String digest){
        if(StringHelper.isEmpty(digest)) return true;
        final MethodDigest old = parse(digest);
        return !ObjectHelper.equals(old.getStructDigest(),getStructDigest());
    }
}
