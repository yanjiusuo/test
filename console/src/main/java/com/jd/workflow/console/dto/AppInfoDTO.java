package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.google.common.collect.Lists;
import com.jd.cjg.bus.request.AppTypeReq;
import com.jd.cjg.bus.request.ComponentCreateReq;
import com.alibaba.fastjson.JSON;
import static com.jd.workflow.console.base.DataUtil.*;

import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import com.jd.workflow.console.base.enums.AuthLevel;
import com.jd.workflow.console.dto.app.CjgAppType;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;

/**
 * 应用信息新增、展示
 */
@Data
public class AppInfoDTO {

    /**
     * 应用code
     */
    private String appCode;

    /**s
     * 应用名称
     */
    private String appName;
    /**
     * 应用类型 "pqt"藏经阁应用 jdos应用默认为空
     */
    private String appType;

    /**
     * 调用级别 0 接口 1方法
     */
    private String authLevel;

    /**
     * 秘钥
     */
    private String appSecret;
    /**
     * 站点信息
     */
    private String site;

    /**
     * 应用描述
     */
    private String desc;

    /**
     * 负责人
     */
    private List<String> owner;
    /**
     * jdos负责人
     */
    private String jdosOwner;

    /**
     * 应用成员
     */
    private List<String> member;
    /**
     *
     */
    private List<String> jdosMembers;

    /**
     * 产品负责人
     */
    private List<String> productor;

    /**
     * 测试负责人
     */
    private List<String> tester;

    /**
     * 测试成员
     */
    private List<String> testMember;

    /**
     * pqtMember成员
     */
    private List<String> pqtMember;

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联cjgAppCode
     */
    private String cjgAppId;
    /**
     * 关联藏经阁appId
     */
    private String cjgAppKey;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 操作人
     */
    private String modifier;

    /**
     * 部门查询
     */
    private String department;

    private String devLanguage;
    private List<CjgAppType> appTypeReqList;
    private ComponentCreateReq.JdosAppSysInfo jdosAppSysInfo;

    private String dept;

    private String cjgBusinessDomainTrace;

    private String cjgProductTrace;

    /**
     * [jsf,http]
     */
    private List<String> interfaceType;


    private String jdosAppCode;



    /**
     * 封装members
     * @return
     */
    public String buildMembers(){
        StringBuilder builder = new StringBuilder();
        Optional.ofNullable(owner).ifPresent(list->AppUserTypeEnum.OWNER.buildErps(list,builder,"-",","));
        Optional.ofNullable(member).ifPresent(list->AppUserTypeEnum.MEMBER.buildErps(list,builder,"-",","));
        Optional.ofNullable(productor).ifPresent(list->AppUserTypeEnum.PRODUCTOR.buildErps(list,builder,"-",","));
        Optional.ofNullable(jdosMembers).ifPresent(list->AppUserTypeEnum.JDOS_MEMBER.buildErps(list,builder,"-",","));
        if (StringUtils.isNotBlank(jdosOwner)) {
            AppUserTypeEnum.JDOS_OWNER.buildErps(Collections.singletonList(jdosOwner),builder,"-",",");
        }
        Optional.ofNullable(pqtMember).ifPresent(list->AppUserTypeEnum.PQT_MEMBER.buildErps(list,builder,"-",","));
        Optional.ofNullable(tester).ifPresent(list->AppUserTypeEnum.TESTER.buildErps(list,builder,"-",","));
        Optional.ofNullable(testMember).ifPresent(list->AppUserTypeEnum.TEST_MEMBER.buildErps(list,builder,"-",","));
        return builder.toString();//builder.length()>0?builder.substring(0,builder.length()-1):
    }

    /**
     * 成员分解: 存成1-wangxf1,1-wangxf2,4-tesst1,4-teest2, 这种形式,方便like查询
     * @param members
     */
    public List<String> splitMembers(String members){
        List<String> result = new ArrayList<>();
        if(StringUtils.isNotBlank(members)){
            this.owner = AppUserTypeEnum.OWNER.splitErps(members, "-", ",");
            this.member =  AppUserTypeEnum.MEMBER.splitErps(members, "-", ",");
            this.productor = AppUserTypeEnum.PRODUCTOR.splitErps(members, "-", ",");
            this.jdosMembers = AppUserTypeEnum.JDOS_MEMBER.splitErps(members, "-", ",");
            this.tester = AppUserTypeEnum.TESTER.splitErps(members, "-", ",");
            this.testMember = AppUserTypeEnum.TEST_MEMBER.splitErps(members, "-", ",");

            this.jdosOwner =  AppUserTypeEnum.JDOS_OWNER.splitOneErp(members, "-", ",");

            this.pqtMember =  AppUserTypeEnum.PQT_MEMBER.splitErps(members, "-", ",");

            result.addAll(owner);
            result.addAll(member);
            result.addAll(productor);
            result.addAll(jdosMembers);
            result.addAll(tester);
            result.addAll(testMember);
            result.addAll(pqtMember);
            result.add(jdosOwner);
        }
        return  result;
    }

    /**
     * 新增信息校验
     */
    public void checkAddInfo(){
        if(StringUtils.isBlank(appCode)){
            throw new BizException("应用code不能为空!");
        }
        if(StringUtils.isBlank(appName)){
            throw new BizException("应用名称不能为空!");
        }
        /*if(AuthLevel.getByCode(authLevel)==null){
            throw new BizException("调用级别参数非法!");
        }*/
        if(strElementIsInvalid(owner)){
            throw new BizException("研发负责人参数非法!");
        }else if(owner.size()>1){
            throw new BizException("研发负责人目前仅允许一个!");
        }
        if(strElementIsInvalidWhenNotEmpty(member)){
            throw new BizException("研发相关人员参数非法!");
        }
        if(strElementIsInvalid(productor)){
            throw new BizException("产品负责人参数非法!");
        }
        if(strElementIsInvalid(tester)){
            throw new BizException("测试负责人参数非法!");
        }
        if(strElementIsInvalidWhenNotEmpty(testMember)){
            throw new BizException("测试相关人员参数非法!");
        }
    }

    /**
     * 修改信息校验
     */
    public void checkModifyAppInfo(){
        if(id==null){
            throw new BizException("应用id不能为空!");
        }
        if(StringUtils.isBlank(appName)){
            throw new BizException("应用名称不能为空!");
        }

        if(strElementIsInvalid(owner)){
            throw new BizException("研发负责人参数非法!");
        }else if(owner.size()>1){
            throw new BizException("研发负责人目前仅允许一个!");
        }
        if(strElementIsInvalidWhenNotEmpty(member)){
            throw new BizException("研发相关人员参数非法!");
        }
        if(strElementIsInvalid(productor)){
            throw new BizException("产品负责人参数非法!");
        }
        if(strElementIsInvalid(tester)){
            throw new BizException("测试负责人参数非法!");
        }
        if(strElementIsInvalidWhenNotEmpty(testMember)){
            throw new BizException("测试相关人员参数非法!");
        }
    }

    public boolean isCanDelete(){
        if(owner == null) return false;
        return owner.contains(UserSessionLocal.getUser().getUserId());
    }

    public static AppInfoDTO from(AppInfo app){
        AppInfoDTO dto = new AppInfoDTO();
        BeanUtils.copyProperties(app, dto);
        dto.splitMembers(app.getMembers());
        return dto;
    }

    public static void main(String[] args) {
        AppInfoDTO  dto = new AppInfoDTO();
        dto.setOwner(Lists.newArrayList("wangxf1","wangxf2"));
        dto.setTester(Lists.newArrayList("tesst1","teest2"));
        String members = dto.buildMembers();
        System.out.println(members);
        List<String> strs = AppUserTypeEnum.OWNER.splitErps(members, "-", ",");
        System.out.println("Owner->"+ JSON.toJSONString(strs));
        strs = AppUserTypeEnum.MEMBER.splitErps(members, "-", ",");
        System.out.println("MEMBER->"+ JSON.toJSONString(strs));
        strs = AppUserTypeEnum.TESTER.splitErps(members, "-", ",");
        System.out.println("TESTER->"+ JSON.toJSONString(strs));
    }

}
