package com.jd.workflow.console.helper;

import com.jd.workflow.console.base.enums.LoginTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * pin密文
 * @date: 2022/6/21 16:43
 * @author wubaizhao1
 */
@Service
public class MaskPinHelper {
    /**
     * 部署启动
     */
    @Value("${interceptor.loginType}")
    private Integer loginType;
    /**
     * @date: 2022/6/21 16:23
     * @author wubaizhao1
     */
    private static final String maskStr="******";

    private String maskString(String str){
        if(StringUtils.isBlank(str)){
            return null;
        }
        str=str.trim();
        if(str.length()==1){
            return str+maskStr;
        }
        return str.charAt(0)+maskStr+str.charAt(str.length()-1);
    }
    public String maskUserCode(String userCode){
        if (LoginTypeEnum.PIN.getCode().equals(loginType)) {
            return maskString(userCode);
        }else {
            return userCode;
        }
    }
}
