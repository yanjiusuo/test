package com.jd.workflow.soap.common;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.StringHelper;

import java.security.MessageDigest;

public class Md5Utils {
    public static String md5(String str) {
        // 加密后的16进制字符串
        String hexStr = "";
        try {
            // 此 MessageDigest 类为应用程序提供信息摘要算法的功能
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // 转换为MD5码
            byte[] digest = md5.digest(str.getBytes("utf-8"));
            hexStr = StringHelper.bytesToHex(digest);
        } catch (Exception e) {
            throw StdException.adapt(e);
        }
        return hexStr;
    }
}
