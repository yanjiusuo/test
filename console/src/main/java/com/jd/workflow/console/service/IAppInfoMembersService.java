package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.QueryAppReqDTO;
import com.jd.workflow.console.dto.QueryAppResultDTO;
import com.jd.workflow.console.dto.UpdateAppTenant;
import com.jd.workflow.console.dto.manage.AppSearchResult;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.AppInfoMembers;

import java.util.List;
import java.util.Set;

/**
 * 项目名称：parent
 * 类 名 称：IAppInfoService
 * 类 描 述：应用service
 * 创建时间：2022-11-16 16:50
 * 创 建 人：wangxiaofei8
 */
public interface IAppInfoMembersService extends IService<AppInfoMembers> {

   //根据应用id或者应用code删除成员信息
    void delMembers(String appCode, Integer appId);
    /**
     * 
     * @author wufagang
     * @date 2023/5/31 16:59 
     */
    void saveMembersByStr(AppInfo app,String appCode);

    List<AppInfoMembers> listAppCodeByErp(String erp);

    List<AppInfoMembers> listErpByAppCode(String appCode);

    public AppInfoMembers getMemberByErp(String erp, Long appId);

 List<AppInfoMembers> getMemberByAppId(Long appId);
}
