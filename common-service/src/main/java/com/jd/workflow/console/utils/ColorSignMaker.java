package com.jd.workflow.console.utils;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * color接口sign生成工具
 */
public class ColorSignMaker {


    /**
     *
     * @param map   请求全部参数（request的所有Parameter键值对）
     * @param secretKey  签名key
     * @return
     */
    public static String generateSign(Map<String, String> map, String secretKey) {


        map.remove("sign");
        List<String> paramNameList = new ArrayList<>();
        map.forEach((key, value) -> {
            // ----加密开关部分不参与签名，如果没有ep、ef、bef这3个参数，可以忽略此判断----
            if (StringUtils.equals(key, "ep") || StringUtils.equals(key, "ef") || StringUtils.equals(key, "bef")) {
                return;
            }
            //-----end-------

            if (StringUtils.isNotBlank(value)) {
                paramNameList.add(key);
            }
        });
        Collections.sort(paramNameList);


        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String paramName : paramNameList) {
            if (first) {
                builder.append(map.get(paramName));
                first = false;
            } else {
                builder.append("&").append(map.get(paramName));
            }
        }

        String freshSign = HMACSHA256(builder.toString(), secretKey);
        return freshSign;


    }


    public static String HMACSHA256(String message, String key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("utf-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] digest = mac.doFinal(message.getBytes("utf-8"));
            return bytesToHex(digest);
        } catch (Exception e) {
            return null;
        }
    }


    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                builder.append("0");
            }
            builder.append(Integer.toHexString(digital));
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Map<String, String> maps=new HashMap<>();
        maps.put("menuId",1+"");
        String pp=ColorSignMaker.generateSign(maps,"sst");
        Long ss=System.currentTimeMillis();
        System.out.println(ss);
    }


}


