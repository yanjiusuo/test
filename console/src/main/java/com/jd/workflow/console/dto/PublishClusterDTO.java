package com.jd.workflow.console.dto;

import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import com.jd.workflow.console.base.enums.AuthLevel;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static com.jd.workflow.console.base.DataUtil.strElementIsInvalid;
import static com.jd.workflow.console.base.DataUtil.strElementIsInvalidWhenNotEmpty;

/**
 * 项目名称：parent
 * 类 名 称：PublishClusterDTO
 * 类 描 述：TODO
 * 创建时间：2022-12-27 15:49
 * 创 建 人：wangxiaofei8
 */
@Data
public class PublishClusterDTO {

    /**
     * 集群code
     */
    private String clusterCode;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 集群域名
     */
    private String clusterDomain;

    /**
     * 应用描述
     */
    private String desc;

    /**
     * 负责人
     */
    private List<String> owner;
    /**
     *  负责人姓名
     */
    private List<String> ownerNames = new ArrayList<>();

    /**
     * 应用成员
     */
    private List<String> member;

    /**
     * 应用成员名称
     */
    private List<String> memberNames = new ArrayList<>();

    /**
     * 主键
     */
    private Long id;

    /**
     * 状态 0 未启用 1 启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 修改时间
     */
    private Date modified;

    /**
     * 创建人
     */
    private String creator;

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
        Optional.ofNullable(owner).ifPresent(list-> AppUserTypeEnum.OWNER.buildErps(list,builder,"-",","));
        Optional.ofNullable(member).ifPresent(list->AppUserTypeEnum.MEMBER.buildErps(list,builder,"-",","));
        return builder.toString();
    }

    /**
     * @param members
     */
    public List<String> splitMembers(String members){
        List<String> result = new ArrayList<>();
        if(StringUtils.isNotBlank(members)){
            this.owner = AppUserTypeEnum.OWNER.splitErps(members, "-", ",");
            this.member =  AppUserTypeEnum.MEMBER.splitErps(members, "-", ",");
            result.addAll(owner);
            result.addAll(member);
        }
        return  result;
    }

    /**
     * 新增集群校验
     */
    public void checkAddCluster(){
        if(StringUtils.isBlank(clusterCode)||!clusterCode.matches("[a-zA-Z]{1}[a-zA-Z0-9-_.]{0,99}")){
            throw new BizException("集群编码非法!");
        }
        if(StringUtils.isBlank(clusterName)||!clusterName.matches("[a-zA-Z0-9\u4e00-\u9fa5]{1,30}")){
            throw new BizException("集群名称非法!");
        }
        if(StringUtils.isBlank(clusterDomain)){
            throw new BizException("集群域名非法!");
        }
        if(strElementIsInvalid(owner)){
            throw new BizException("负责人参数非法!");
        }else if(owner.size()>1){
            throw new BizException("负责人目前仅允许一个!");
        }
        if(strElementIsInvalidWhenNotEmpty(member)){
            throw new BizException("成员参数非法!");
        }
        if(StringUtils.isNotBlank(desc)&&desc.length()>200){
            throw new BizException("集群描述非法长度不能大于200个字符!");
        }
    }

    /**
     * 修改集群校验
     */
    public void checkModifyCluster(){
        if(id==null){
            throw new BizException("集群id不能为空!");
        }
        checkAddCluster();
    }
}
