package com.jd.workflow.console.base;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 项目名称：联调平台控制台
 * 类 名 称：MD5Util
 * 类 描 述：加密
 * 创建时间：2022-11-08 15:13
 * 创 建 人：wangxiaofei8
 */
public class MD5Util {

    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f" };

    private static MessageDigest md5Digest = null;

    static {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }

    /**
     * 转换字节数组为16进制字串
     * @param bytes 字节数组
     * @return 16进制字串
     */
    public static String byteArrayToString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(byteToHexString(b));
        }
        return builder.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String MD5Encode(String origin) {
        if (origin == null || origin.trim().length() == 0) {
            return origin;
        }
        return byteArrayToString(computeMd5(origin));
    }

    public static String MD5EncodeWithUtf8(String origin) {
        if (origin == null || origin.trim().length() == 0) {
            return origin;
        }
        return byteArrayToString(computeMd5WithUtf8(origin));
    }

    /**
     * Get the md5 of the given key.
     */
    public static byte[] computeMd5(String k) {
        MessageDigest md5;
        try {
            md5 = (MessageDigest) md5Digest.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("clone of MD5 not supported", e);
        }
        md5.update(k.getBytes());
        return md5.digest();
    }

    public static byte[] computeMd5WithUtf8(String k) {
        MessageDigest md5;
        try {
            md5 = (MessageDigest) md5Digest.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("clone of MD5 not supported", e);
        }
        try {
            md5.update(k.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encding utf8 fail", e);
        }
        return md5.digest();
    }
}
