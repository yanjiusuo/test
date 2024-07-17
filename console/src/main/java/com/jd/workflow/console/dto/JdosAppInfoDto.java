package com.jd.workflow.console.dto;

import com.google.common.collect.Lists;
import com.jd.cjg.bus.request.ComponentCreateReq;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import com.jd.workflow.console.base.enums.AuthLevel;
import com.jd.workflow.console.dto.app.CjgAppType;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.Data;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;

import static com.jd.workflow.console.base.DataUtil.strElementIsInvalid;
import static com.jd.workflow.console.base.DataUtil.strElementIsInvalidWhenNotEmpty;

/**
 * 应用信息新增、展示
 */
@Data
public class JdosAppInfoDto {

    /**
     * 应用code
     * @required
     */
    private String appCode;
    /**s
     * 应用名称
     * @required
     */
    private String appName;

    /**
     * 应用类型 pqt=藏经阁应用-通过接口创建
     */
    private String appType;
    /**s
     * 应用站点
     * @required
     */
    private String site;

    /**
     * 调用级别 0 接口 1方法
     * @required
     */
    private String authLevel;

    /**
     * 秘钥
     */
    private String appSecret;

    /**
     * 应用描述
     * @required
     */
    private String desc;

    /**
     * 研发负责人
     * @required
     */
    private String owner;
    /**
     * jdos应用负责人
     */
    private String jdosOwner;
    /**
     * 产品负责人
     */
    private List<String> productor;

    /**
     * 其他成员
     * @required
     */
    private List<String> members;
    /**
     * jdos应用成员
     * @required
     */
    private List<String> jdosMembers;

    /**
     * pqt成员
     * @required
     */
    private List<String> pqtMembers;


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
     * 封装members
     * @return
     */
    public String buildMembers(){
        StringBuilder builder = new StringBuilder();
        Optional.ofNullable(Collections.singletonList(owner)).ifPresent(list->AppUserTypeEnum.OWNER.buildErps(list,builder,"-",","));
        Optional.ofNullable(members).ifPresent(list->AppUserTypeEnum.MEMBER.buildErps(list,builder,"-",","));
        Optional.ofNullable(jdosMembers).ifPresent(list->AppUserTypeEnum.JDOS_MEMBER.buildErps(list,builder,"-",","));
        Optional.ofNullable(productor).ifPresent(list->AppUserTypeEnum.PRODUCTOR.buildErps(list,builder,"-",","));
        Optional.ofNullable(pqtMembers).ifPresent(list->AppUserTypeEnum.PQT_MEMBER.buildErps(list,builder,"-",","));
        if(StringUtils.isNotBlank(jdosOwner)){
            AppUserTypeEnum.JDOS_OWNER.buildErps(Collections.singletonList(jdosOwner),builder,"-",",");
        }


        return builder.toString();//builder.length()>0?builder.substring(0,builder.length()-1):
    }

    /**
     * 成员分解: 存成1-wangxf1,1-wangxf2,4-tesst1,4-teest2, 这种形式,方便like查询
     * @param memberStr
     */
    public List<String> splitMembers(String memberStr){
        List<String> result = new ArrayList<>();
        List<String> otherMembers = new ArrayList<>();
        if(StringUtils.isNotBlank(memberStr)){
            List<String> owners = AppUserTypeEnum.OWNER.splitErps(memberStr, "-", ",");
            this.owner = null;
            if(owners !=null && owners.size()>0){
                owner = owners.get(0);
            }
            //this.members =  AppUserTypeEnum.MEMBER.splitErps(memberStr, "-", ",");
            this.jdosMembers = AppUserTypeEnum.JDOS_MEMBER.splitErps(memberStr, "-", ",");
            this.pqtMembers = AppUserTypeEnum.PQT_MEMBER.splitErps(memberStr, "-", ",");
            otherMembers.addAll( AppUserTypeEnum.MEMBER.splitErps(memberStr, "-", ","));
            this.productor = AppUserTypeEnum.PRODUCTOR.splitErps(memberStr, "-", ",");
            List<String> jdosOwners = AppUserTypeEnum.JDOS_OWNER.splitErps(memberStr, "-", ",");
            if(ObjectHelper.isNotEmpty(jdosOwners)){
                this.jdosOwner = jdosOwners.get(0);
                result.addAll(jdosOwners);
            }
            //otherMembers.addAll(AppUserTypeEnum.PRODUCTOR.splitErps(memberStr, "-", ","));
            otherMembers.addAll(AppUserTypeEnum.TESTER.splitErps(memberStr, "-", ","));
            otherMembers.addAll(AppUserTypeEnum.TEST_MEMBER.splitErps(memberStr, "-", ","));
            this.members = otherMembers;


            result.add(owner);
            result.addAll(this.productor);
            result.addAll(otherMembers);
            result.addAll(jdosMembers);
            result.addAll(pqtMembers);

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
        if(AuthLevel.getByCode(authLevel)==null){
            throw new BizException("调用级别参数非法!");
        }
        if(strElementIsInvalid(Collections.singletonList(owner))){
            throw new BizException("研发负责人参数非法!");
        }
      /*  if(strElementIsInvalidWhenNotEmpty(jdosMembers)){
            throw new BizException("研发相关人员参数非法!");
        }*/

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



    }

    public boolean isCanDelete(){
        if(owner == null) return false;
        return owner.contains(UserSessionLocal.getUser().getUserId());
    }
    public AppInfoDTO toAppInfoDto(boolean validate){
        Guard.notEmpty(appCode,"应用编码不可为空");
        if(validate && !appCode.startsWith("J-dos-")){
            throw new BizException("应用编码必须以J-dos-开头");
        }
        AppInfoDTO dto = new AppInfoDTO();
        BeanUtils.copyProperties(this,dto);
        dto.setOwner(Collections.singletonList(owner)); // 研发负责人
        Set<String> otherMembers = new HashSet<>();
        if(members !=null){
            otherMembers.addAll(members);
        }

        if(jdosMembers != null){
            otherMembers.addAll(jdosMembers);
        }
        dto.setMember(new ArrayList<>(otherMembers));
        dto.setJdosMembers(jdosMembers);
        dto.setJdosOwner(jdosOwner);// jdos负责人需要同步一下
        dto.setTester(Collections.singletonList(owner)); // 测试负责人
        dto.setProductor(productor);// 产品负责人
        dto.setPqtMember(pqtMembers);
        return dto;
    }
    private static String first(List<String> list){
        if(list == null || list.size()==0){
            return null;
        }
        return list.get(0);
    }
    public static JdosAppInfoDto from(AppInfoDTO appInfoDTO){
        JdosAppInfoDto jdosAppInfoDto = new JdosAppInfoDto();
        BeanUtils.copyProperties(appInfoDTO,jdosAppInfoDto);
        jdosAppInfoDto.setOwner(first(appInfoDTO.getOwner()));
        jdosAppInfoDto.setJdosMembers(appInfoDTO.getJdosMembers());
        Set<String> otherMembers = new HashSet<>();
        otherMembers.addAll(appInfoDTO.getMember());
        otherMembers.addAll(appInfoDTO.getTester());
        otherMembers.addAll(appInfoDTO.getTestMember());
        //otherMembers.addAll(appInfoDTO.getProductor());
        if(appInfoDTO.getOwner() != null){
            otherMembers.removeAll(appInfoDTO.getOwner());
        }
        if(appInfoDTO.getJdosMembers() != null){
            otherMembers.removeAll(appInfoDTO.getJdosMembers());
        }
        jdosAppInfoDto.setMembers(new ArrayList<>(otherMembers));
        jdosAppInfoDto.setJdosOwner(appInfoDTO.getJdosOwner());
        jdosAppInfoDto.setProductor(appInfoDTO.getProductor());
        jdosAppInfoDto.setPqtMembers(appInfoDTO.getPqtMember());
        return jdosAppInfoDto;
    }

    public static void main(String[] args) {
        JdosAppInfoDto dto = new JdosAppInfoDto();
        //dto.setOwner(Lists.newArrayList("wangxf1","wangxf2"));
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
