package com.jd.workflow.console.service.plugin;

import com.jd.jim.cli.Cluster;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.utils.AESCoderHelper;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PluginLoginService {
    public static String USER_LOGINED_KEY = "user:req2erp:";
    private static String SECRET_SALT = "_034L}eten&^#Htsffs*&%#";
    /**
     * 登录有效期：90天
     */
    static long LOGIN_VALID_TIME = 90*24*60*60*1000L;
    @Autowired
    Cluster jimClient;


    public void setUser(String reqId,String erp){
        Long time = System.currentTimeMillis();
        jimClient.set(USER_LOGINED_KEY+reqId,erp+":"+time,5*60, TimeUnit.SECONDS,false);
    }
    public void clearUser(String reqId){
          Long id = jimClient.del(USER_LOGINED_KEY + reqId);

    }
    public String getUser(String reqId){
        String erpAndTime = jimClient.get(USER_LOGINED_KEY + reqId);
        if(erpAndTime == null) return null;

        try {
            return AESCoderHelper.encrypt(erpAndTime,SECRET_SALT);
        } catch (Exception e) {
            throw new BizException("加密失败",e);
        }
    }

       public String encrypt(String erp){
           Long time = System.currentTimeMillis();

           try {
               return AESCoderHelper.encrypt(erp+":"+time,SECRET_SALT);
           } catch (Exception e) {
               throw new BizException("加密失败",e);
           }

       }


    public static UserBaseInfo getUserInfo(String userToken){
        try {
            String decrypt = AESCoderHelper.decrypt(userToken, SECRET_SALT);
            String[] strs = decrypt.split(":");
            String erp = strs[0];
            Long loginTime = Long.valueOf(strs[1]);
            /*if(System.currentTimeMillis()  - loginTime > LOGIN_VALID_TIME){
                throw new BizException("登录超时");
            }*/
            UserBaseInfo userBaseInfo = new UserBaseInfo();
            userBaseInfo.setUserName(erp);
            return userBaseInfo;
        } catch (Exception e) {
            throw new BizException("解密失败",e);
        }

    }
    @Data
    public static class UserBaseInfo{
        String userName;
        String userNick;
    }

    public static void main(String[] args) {
        PluginLoginService pluginLoginService = new PluginLoginService();
        String encrypt = pluginLoginService.encrypt("ext.sss");
        System.out.println(encrypt);
        UserBaseInfo userInfo = getUserInfo(encrypt);
        System.out.println(userInfo);
    }
}
