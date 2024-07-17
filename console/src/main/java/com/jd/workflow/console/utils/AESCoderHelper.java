package com.jd.workflow.console.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加解密辅助
 * AES加解密辅助
 * @author liulihua9
 * @date 2018/11/01
 */
@Slf4j
public class AESCoderHelper {
    /**
     * ecode方式
     */
    private static final String ENCODING = "UTF-8";
    /**
     * key长度
     */
    private static final int KEY_LENGTH = 16;

    /**
     * AES 128解密
     *
     * @param str 明文
     * @param key 密码，向量同密码
     * @return 密文
     * @throws Exception
     */
    public static String decrypt(String str, String key)
            throws Exception {
        try {
            // 判断Key是否正确
            if (key == null) {
                log.error("Key为空");
                return null;
            }
            key = align(key);
            byte[] raw = key.getBytes(ENCODING);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(key.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = hex2byte(str);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception ex) {
            log.error(ex.toString());
            return null;
        }
    }

    /**
     * @param str
     * @param key
     * @return
     * @throws Exception
     */
    public static String encrypt(String str, String key)
            throws Exception {
        if (key == null) {
            log.error("Key为空");
            return null;
        }
        key = align(key);
        byte[] raw = key.getBytes(ENCODING);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(str.getBytes());
        return byte2hex(encrypted);

    }

    /**
     * byte转十六进制
     * @param input
     * @return
     */
    public static String byte2hex(byte[] input) {

        char[] chars = Hex.encodeHex(input);
        return String.valueOf(chars);
    }

    /**
     * 十六进制转byte
     * @param input
     * @return
     * @throws DecoderException
     */
    public static byte[] hex2byte(String input) throws DecoderException {
        return Hex.decodeHex(input.toCharArray());
    }

    /**
     * 密码长度小于16个字节时补充空字符，大于16个字节时只取前16个字节
     *
     * @param key
     * @return
     */
    private static String align(String key) throws Exception {
        if (key == null) {
            return null;
        }
        int length = key.getBytes(ENCODING).length;
        if (length > KEY_LENGTH) {
            key = key.substring(0, KEY_LENGTH);
        } else {
            int zeroSize = KEY_LENGTH - length;
            for (int i = 0; i < zeroSize; i++) {
                key += '\0';
            }
        }
        return key;
    }
}
