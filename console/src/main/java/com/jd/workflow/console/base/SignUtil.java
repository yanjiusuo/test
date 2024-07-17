package com.jd.workflow.console.base;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @date: 2022/6/1 15:48
 * @author wubaizhao1
 */
public class SignUtil {
    private static final String CHARSET_NAME = "UTF-8";
    private static final String INSTANCE_NAME = "AES/CBC/PKCS5Padding";
    private static final String ENCRYPT_METHOD = "AES";

    /**
     * 解密
     * @param str
     * @param key
     * @param ivStr
     * @return
     * @throws Exception
     */
    public static String decrypt(String str, String key, String ivStr)throws Exception {
        if (str != null && str.trim().length() >= 1) {
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), ENCRYPT_METHOD);
            IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes());
            Cipher cipher = Cipher.getInstance(INSTANCE_NAME);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted = Hex.decodeHex(str.toCharArray());;
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, CHARSET_NAME);
        } else {
            return null;
        }
    }

    /**
     * 加密
     * @param str
     * @param key
     * @param ivStr
     * @return
     * @throws Exception
     */
    public static String encrypt(String str, String key, String ivStr) throws Exception {
        if (str != null && str.trim().length() >= 1) {
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), ENCRYPT_METHOD);
            IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes());
            Cipher cipher = Cipher.getInstance(INSTANCE_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = str.getBytes(CHARSET_NAME);
            byte[] original = cipher.doFinal(encrypted);
//            String originalString = Hex.encodeHexString(original);
            String originalString = Hex.encodeHex(original).toString();
            return originalString;
        } else {
            return null;
        }
    }
    /**
     *
     * @param params
     * @return
     */
    public static String createMD5(String... params) {
        StringBuilder input = new StringBuilder();
        String[] arr = params;
        int len = params.length;

        //这里需要一个排序算法
        for (int i = 0; i < len; ++i) {
            String param = arr[i];
            input.append(param);
        }
        return encodeMD5Hex(input.toString()).toUpperCase();
    }

    /**
     *
     * @param input
     * @return
     */
    public static String encodeMD5Hex(String input) {
        return DigestUtils.md5Hex(input);
    }
}
