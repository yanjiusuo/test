package com.jd.workflow.console.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/7/15
 */
public class CodingUtils {
    /**
     * 获取coding地址的唯一标识
     * @param codeAddress
     * @return
     */
    public static String getOnlyCodingAddress(String codeAddress){
        String onlyCodeAddress = "";
        if(StringUtils.isNotBlank(codeAddress)){
            onlyCodeAddress = codeAddress.replace("git@coding.jd.com:", "")
                    .replace("https://coding.jd.com/", "");
        }
        return onlyCodeAddress;
    }
}
