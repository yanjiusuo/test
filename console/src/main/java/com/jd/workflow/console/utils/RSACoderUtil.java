package com.jd.workflow.console.utils;


import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/20
 */
@Slf4j
public class RSACoderUtil {
    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) {
        try {
            // base64编码的公钥
            byte[] decoded = Base64.decodeBase64(publicKey.getBytes());
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            //RSA加密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] bytes = Base64.encodeBase64(cipher.doFinal(str.getBytes("UTF-8")));

            String outStr = new String(bytes,"utf-8");
            return outStr;
        } catch (Throwable e) {
            throw new BizException("RSA公钥加密出现异常!", e);
        }
    }
}
