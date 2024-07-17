package com.jd.workflow.console.utils;

import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserUtils {

    /**
     * 成员分解: 存成1-wangxf1,1-wangxf2,4-tesst1,4-teest2, 这种形式,方便like查询
     * @param members
     */
    public static List<String> splitMembers(String members){
        List<String> result = new ArrayList<>();
        if(StringUtils.isNotBlank(members)){
            List<String> owner = AppUserTypeEnum.OWNER.splitErps(members, "-", ",");
            List<String> member =  AppUserTypeEnum.MEMBER.splitErps(members, "-", ",");
            List<String> productor = AppUserTypeEnum.PRODUCTOR.splitErps(members, "-", ",");
            List<String> tester = AppUserTypeEnum.TESTER.splitErps(members, "-", ",");
            List<String> testMember = AppUserTypeEnum.TEST_MEMBER.splitErps(members, "-", ",");
            result.addAll(owner);
            result.addAll(member);
            result.addAll(productor);
            result.addAll(tester);
            result.addAll(testMember);
        }
        return  result;
    }


    /**
     * 成员分解: 存成1-wangxf1,1-wangxf2,4-tesst1,4-teest2, 这种形式,方便like查询
     * @param members
     */
    public static List<String> getOwners(String members){
        List<String> result = new ArrayList<>();
        if(StringUtils.isNotBlank(members)){
            List<String> owner = AppUserTypeEnum.OWNER.splitErps(members, "-", ",");
            result.addAll(owner);
        }
        return  result;
    }

    /**
     * 获取产品人员
     * @param members
     * @return
     */
    public static List<String> getProductMembers(String members){
        List<String> result = new ArrayList<>();
        if(StringUtils.isNotBlank(members)){
            List<String> productor = AppUserTypeEnum.PRODUCTOR.splitErps(members, "-", ",");
            result.addAll(productor);
        }
        return  result;
    }

    /**
     * 获取研发人员
     * @param members
     * @return
     */
    public static List<String> getDevMembers(String members){
        List<String> result = new ArrayList<>();
        if(StringUtils.isNotBlank(members)){
            List<String> owner = AppUserTypeEnum.OWNER.splitErps(members, "-", ",");
            result.addAll(owner);
        }
        return  result;
    }
}
