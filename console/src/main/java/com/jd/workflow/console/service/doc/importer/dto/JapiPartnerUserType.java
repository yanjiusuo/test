package com.jd.workflow.console.service.doc.importer.dto;

import com.jd.workflow.console.base.enums.ResourceRoleEnum;

/**
 * 用户类型枚举值
 * 在项目成员里管理的用户类型
 * @author liulihua9
 * @date 2018/07/01
 */
public enum JapiPartnerUserType {  //warning ***** 修改这里需要同时修改  valueOf 这个函数
    /**
     * 创建者:项目负责人
     */
    PARTNER_CREATER(0), //创建者
    /**
     * 管理员
     */
    PARTNER_MANAGER(1),   //管理员
    /**
     * 普通成员（读写）
     */
    PARTNER_RW(2), //普通成员（读写）
    /**
     * 普通成员（只读）
     */
    PARTNER_R(3),  //普通成员（只读）
    /**
     * 申请中
     */
    PARTNER_APPLYING(4), //申请中

    /**
     * 无任何权限
     */
    PARTNER_NONE(99999); //没有任何权限


    private int value = 0;

    /**
     * 枚举构造函数，有了这个函数才能自定义枚举值
     * @param value
     */
    private JapiPartnerUserType(Integer value) {    //必须是private的，否则编译错误
        this.value = value;
    }

    /**
     * 获取枚举值的int值
     * @return
     */
    public int value() {
        return this.value;
    }

    public ResourceRoleEnum toResourceRole(){
        if(this.equals(PARTNER_CREATER)  ){
            return ResourceRoleEnum.ADMIN;
        }else if(this.equals(PARTNER_RW) || this.equals(PARTNER_MANAGER)) {
            return ResourceRoleEnum.MEMBER;
        }else if(this.equals(PARTNER_R)){
            return ResourceRoleEnum.READONLY_MEMBER;
        }
        return null;
    }

    /**
     * 给定值返回权限类型
     * @param value
     * @return
     */
    public static JapiPartnerUserType valueOf(Integer value) {
        if (value == null){
            return PARTNER_NONE;
        }
        switch (value) {
            case 0:
                return PARTNER_CREATER;
            case 1:
                return PARTNER_MANAGER;
            case 2:
                return PARTNER_RW;
            case 3:
                return PARTNER_R;
            case 4:
                return PARTNER_APPLYING;
            case 99999:
                return PARTNER_NONE;
            default:
                return PARTNER_NONE;
        }
    }

    /**
     * 根据userType获取typeString
     * @param userType
     * @return
     */
    public static String userTypeString(JapiPartnerUserType userType){
        String typeStr = "未知类型";
        switch (userType)
        {
            case PARTNER_CREATER:
                typeStr = "创建者";
                break;
            case PARTNER_MANAGER:
                typeStr = "管理员";
                break;
            case PARTNER_RW:
                typeStr = "普通成员（读写）";
                break;
            case PARTNER_R:
                typeStr = "普通成员（只读）";
                break;
            case PARTNER_APPLYING:
                typeStr = "申请中";
                break;
            default:
                break;
        }
        return typeStr;
    }

    /**
     * 判断是否是一个合法的用户类型
     * @param value
     * @return
     */
    public static boolean isValidUserType(Integer value) {
        if (value == null || value < 0 || value > PARTNER_APPLYING.value()){
            return false;
        }
        return true;
    }

    /**
     * 是否有可读权限
     * @return
     */
    public boolean isXXCanRead(){
        return this.value <= PARTNER_R.value();
    }

    /**
     * 是否有可写权限
     * @return
     */
    public boolean isCanWrite(){
        return this.value <= PARTNER_RW.value();
    }

    /**
     * 是否是管理员
     * @return
     */
    public boolean isManager(){
        return this.value <= PARTNER_MANAGER.value();
    }

    /**
     * 是否是创建者
     * @return
     */
    public boolean isCreator(){
        return this.value <= PARTNER_CREATER.value();
    }

    /**
     * 是否是项目的一个成员，申请加入中的人也算是成员
     * @return
     */
    public boolean isMember(){
        return this.value <= PARTNER_APPLYING.value();
    }


}