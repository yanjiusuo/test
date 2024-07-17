package com.jd.workflow.console.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 获取MD5的数据
 */
@Slf4j
public class EncodeUtils {

    /**
     * 获取md5的16位长度
     * @param sourceStr
     * @return
     */
    public static String getMD5_16(String sourceStr){
            String result = "";
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(sourceStr.getBytes());
                byte b[] = md.digest();
                int i;
                StringBuffer buf = new StringBuffer("");
                for (int offset = 0; offset < b.length; offset++) {
                    i = b[offset];
                    if (i < 0) {
                        i += 256;
                    }
                    if (i < 16) {
                        buf.append("0");
                    }
                    buf.append(Integer.toHexString(i));
                }
                result = buf.toString().substring(8, 24);
                log.info("MD5(" + sourceStr + ",16) = " + buf.toString().substring(8, 24));
            } catch (Exception e) {
                log.error("#getMD5_16.error=",e);
            }
            return result;
    }

    /**
     * 获取md5的32位长度
     * @param sourceStr
     * @return
     */
    public static String getMD5_32(String sourceStr){
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
            log.info("MD5(" + sourceStr + ",32) = " + result);
        } catch (Exception e) {
            log.error("#getMD5_32.error=",e);
        }
        return result;
    }

    public static void main(String[] args) {
        getMD5_16("jsm_wjfTest");
        getMD5_32("jsm_wjfTest");
    }
}
