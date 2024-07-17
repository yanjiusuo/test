package com.jd.workflow.console.base.enums;

import com.jd.common.util.StringUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 * 类 名 称：AppUserTypeEnum
 * 类 描 述：app用户类型
 * 创建时间：2022-11-16 14:55
 * 创 建 人：wangxiaofei8
 * @description 这是类介绍
 */
@AllArgsConstructor
public enum AppUserTypeEnum {

    OWNER(1,"应用负责人"),
    MEMBER(2,"应用成员"),
    PRODUCTOR(3,"产品负责人"),
    TESTER(4,"测试负责人"),
    TEST_MEMBER(5,"测试相关人"),
    JDOS_OWNER(9,"jdos应用负责人"),
    JDOS_MEMBER(10,"jdos应用成员"),
    PQT_MEMBER(11,"pqt成员");


    /**
     * @code
     */
    @Getter
    @Setter
    private Integer type;


    @Getter
    @Setter
    private String desc;

    /**
     * 拼接pin
     * @param list
     * @param builder
     * @param link
     * @param split
     */
    public void buildErps(List<String> list, StringBuilder builder,String link,String split){
        list.stream().forEach(pin->builder.append(getType()).append(link).append(pin).append(split));
    }

    /**
     * 分割成员
     * @param members
     * @param link
     * @param split
     * @return
     */
    public  List<String> splitErps(String members, String link, String split){
        return Arrays.stream(members.split(split)).filter(str->!StringUtils.isBlank(str)).filter(str->str.startsWith(getType()+link))
                .map(str->str.substring((getType()+link).length())).collect(Collectors.toList());
    }

    public  String splitOneErp(String members, String link, String split){
        List<String> result = splitErps(members, link, split);
        if(!ObjectHelper.isEmpty(result)){
            return result.get(0);
        }
        return null;
    }


    public static AppUserTypeEnum getByType(Integer type){
        for (AppUserTypeEnum value : AppUserTypeEnum.values()) {
            if(value.getType().equals(type)){
                return value;
            }
        }
        return null;
    }
}
