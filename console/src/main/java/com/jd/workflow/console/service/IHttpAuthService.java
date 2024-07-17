package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.*;

import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：IAppInfoService
 * 类 描 述：应用service
 * 创建时间：2022-11-16 16:50
 * 创 建 人：wangxiaofei8
 */
public interface IHttpAuthService extends IService<HttpAuth> {

    /**
     * 新增app
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthDTO> queryListPage(QueryHttpAuthReqDTO queryDTO);

    /**
     * 查询列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDTO> queryListGroupByAppAndSite(QueryHttpAuthReqDTO queryDTO);


    /**
     * 上报鉴权标识列表
     * @param authList
     */
    public void reportHttpAuth(List<HttpAuthDTO> authList, List<HttpAuthDetailDTO> authDetailList, AppInfo appInfo, Long interfaceId);

    /**
     *  批量添加鉴权标识列表
     * @param authList
     */
    public boolean batchSaveAuthOrUpdate(List<HttpAuthDTO> authList);

    /**
     * 批量删除
     * @param authList
     * @return
     */
    public boolean removeBatch( List<HttpAuth> authList);
    /**
     * 发送接口变更通知
     */
    public void sendInterfaceManageChangeNotice(InterfaceManage manage);
}
