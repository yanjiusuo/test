package com.jd.workflow.console.service.ducc;

/**
 * @author by lishihao4
 * @date 2022/4/25
 * DESC
 */
public class DuccUtils {

    public static String getProfile(Integer site){
        return "cjg_profile_"+site;
    }

    public static String getConfig(String templateId){
        return "cjg_"+templateId;
    }

    public static String getConfig(Long templateId){
        return "cjg_"+templateId;
    }
}
