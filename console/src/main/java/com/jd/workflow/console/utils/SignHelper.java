package com.jd.workflow.console.utils;

import com.jd.workflow.soap.common.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SignHelper {
    static  final Logger logger = LoggerFactory.getLogger(SignHelper.class);
    static final Long SECOND = 1000L;
    static final String  KEY = "e2cc7256ab6d4ce3ba048fd1412b175d";
    public static boolean validateSign(String sign,int deltaRange){
        try{
            String query = AESCoderHelper.decrypt(sign,KEY);
            logger.info("sign.receive_sign:query={}",query);
            Map<String, Object> queryMap = StringHelper.parseQuery(query, "utf-8");
            String value = (String) queryMap.get("t");
            Long t = Long.parseLong(value);
            long delta = Math.abs(t - System.currentTimeMillis());
            if( delta >= deltaRange*SECOND){
                logger.info("sign.receive_invlid_sign:query={},t={},delta={}",query,t,delta);
                return false;
            }
            return true;
        }catch (Exception e){
            logger.error("sign.err_validate",e);
            return false;
        }
    }

    /**
     * 将参数转为查询字符串后进行排序操作
     * @param map
     */
    public static String signParams(Map<String,Object> map) throws Exception {
        Map<String,Object> params = new HashMap<>(map);
        params.remove("sign");
        params.put("t",System.currentTimeMillis());
        String[] arr = params.keySet().toArray(new String[0]);
        Arrays.sort(arr);
        Map<String,Object> newMap = new LinkedHashMap<>();

        for (String s : arr) {
            newMap.put(s,params.get(s));
        }
        String query = StringHelper.encodeQuery(newMap, "utf-8");
        return  AESCoderHelper.encrypt(query,KEY);
    }


}
