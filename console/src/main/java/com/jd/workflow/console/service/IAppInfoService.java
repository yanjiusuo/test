package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.app.AppMembers;
import com.jd.workflow.console.dto.manage.AppSearchResult;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.AppInfoMembers;
import io.swagger.annotations.Api;

import java.util.List;
import java.util.Map;

/**
 * 项目名称：parent
 * 类 名 称：IAppInfoService
 * 类 描 述：应用service
 * 创建时间：2022-11-16 16:50
 * 创 建 人：wangxiaofei8
 */
@Api(value = "创建app应用")
public interface IAppInfoService extends IService<AppInfo> {

    /**
     * 新增app
     *
     * @param dto
     * @return
     */
    public Long addApp(AppInfoDTO dto);

    public AppInfo findByJdosAppCode(String jdosAppCode);

    public Long syncCjgAppToLocal(String cjgAppCode, boolean delete);
    public void syncJdosMembers();
    public void initJdosApp();

    void syncDeptt(AppInfo info);


//    List<Long> modifyInfoYn(AppInfo info,String ids, Integer yn);

    List<Long> modifyInfoYn(AppInfo info, List<Map<String, String>> ids, Integer yn);

    void removeById(List<Long> ids);


    public void syncJdosMembers(Long appId);

    void syncDeptInfo(AppInfo appInfo);

    public void syncMembersFromJdos(AppInfo appInfo);
    /**
     * 修改app
     *
     * @param dto
     * @return
     */
    public Boolean modifyApp(AppInfoDTO dto);

    /**
     * 内部使用，不校验权限
     * @param dto
     * @param skipValidate
     * @return
     */
    public Boolean modifyApp(AppInfoDTO dto,boolean skipValidate);

    /**
     * 删除应用
     *
     * @param id
     * @return
     */
    public Boolean removeApp(Long id,String cookieValue);

    public AppMembers getJdosAppMembers(String appCode);
    /**
     * 真实删除应用
     *
     * @param id
     * @return
     */
    public Boolean realRemoveApp(Long id);


    /**
     * 更新用应用租户信息
     *
     * @param appTenant
     */
    public void updateAppTenant(UpdateAppTenant appTenant);

    /**
     * 应用详情
     *
     * @param id
     * @return
     */
    public AppInfoDTO findApp(Long id);

    /**
     * 应用详情
     *
     * @param appCode
     * @return
     */
    public AppInfo findApp(String appCode);

    public AppInfoDTO findAppByCode(String appCode);
    public AppInfo findByJdosCode(String jdosAppCode);

    /**
     * 校验秘钥
     *
     * @param appCode
     * @param appSecret
     * @return
     */
    public Boolean checkSecret(String appCode, String appSecret);

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    public QueryAppResultDTO queryAppByCondition(QueryAppReqDTO query);

    Page<AppInfoDTO> querySpaceAppByCondition(QueryAppReqDTO query,Integer queryType);

    public QueryAppResultDTO queryHasNoApp(QueryAppReqDTO query);
    public List<AppSearchResult> searchApp(String app,Long id,int onlySelf,Integer includeNoApp);



    /**
     * 查询导入app
     *
     * @return
     */
    public QueryAppResultDTO queryImportSysApp(QueryAppReqDTO query);


    public List<AppInfo> queryDjAppByPrefix(String prefix);
    public List<AppInfo> queryBeanApps(String name);

    /**
     * 查询用用负责人的部门信息
     * @param app
     * @return
     */
    String getDeptNameFoyAppMember(AppInfo app);

    /**
     * 查询用用负责人的部门信息
     * @param appId
     * @return
     */
    String getDeptNameFoyAppMember(Long appId);
    /**
     * 初始化应用人员信息
     * @return
     */
    public void sysAppInfoMem(String appCode);

    /**
     * 更新jdos应用业务域
     */
    public void updateAllAppTrace(String cookie);
    public void updateAppTrace(AppInfo appInfo,String cookie);

    void updateProductTrace(AppInfo appInfo);

    public boolean isMember(Long id);

    List<AppInfo> InitByJdosCode(String jdosAppCode,String erp);

    public List<AppInfo> queryAppInfosByJdosCoeds(List<String> jdosCoedes);

    /**
     * 通过Jdos应用Code 初始化 JApi应用 。
     * @param jdosAppCode
     * @return
     */
    AppInfo InitByJdosCode(String jdosAppCode);

    /**
     * 获取要求下的应用列表
     */
    List<AppInfo> getAppByRequirementId(Long requirementId);

    List<AppInfo> queryAppInfoListByAppCoeds(List<String> appCodes);
}
