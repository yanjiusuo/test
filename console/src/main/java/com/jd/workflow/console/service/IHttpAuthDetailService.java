package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.doc.InterfaceAppSortModel;
import com.jd.workflow.console.entity.HttpAuth;
import com.jd.workflow.console.entity.HttpAuthDetail;

import java.util.List;
import java.util.Set;

/**
 * 项目名称：parent
 * 类 名 称：IAppInfoService
 * 类 描 述：应用service
 * 创建时间：2022-11-16 16:50
 * 创 建 人：wangxiaofei8
 */
public interface IHttpAuthDetailService extends IService<HttpAuthDetail> {

    /**
     * 新增app
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthDetailDTO> queryList(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetailDTO> queryAllList(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetail> queryAllSourceList(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询列表数量
     * @param queryDTO
     * @return
     */
    public Long queryListCount(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 按接口分组展示
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthDetailDTO> queryListPageGroupByInterface(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 按接口分组展示
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetailDTO> queryListGroupByInterface(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 按接口分组展示
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetailDTO> queryListGroupByMethod(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 按方法分组展示
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthDetailDTO> queryListPageGroupByMethod(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 按鉴权分组展示
     * @param queryDTO
     * @return
     */
    public List<HttpAuthDetailDTO> queryListGroupByAuthCode(QueryHttpAuthDetailReqDTO queryDTO);

    /**
     * 查询列表
     * @param authDetailDTOList
     * @return
     */
    public boolean saveBatch( List<HttpAuthDetailDTO> authDetailDTOList);

    /**
     * 批量删除
     * @param authDetails
     * @return
     */
    public boolean removeBatch( List<HttpAuthDetail> authDetails);

    public void removeInterfaceAuthDetail(Long interfaceId);
    public void removeMethodAuthDetail(List<Long> methodIds);

    public Set<Long> queryExists(List<Long> interfaceIds);

    public List<HttpAuthDetailDTO>  queryAppInterfaceAuth(QueryHttpAuthDetailReqDTO query);

    public List<HasChildrenHttpAuthDetail> queryInterfaceMethod(QueryHttpAuthDetailReqDTO query);

    public List<HasChildrenHttpAuthDetail> queryAppInterfaceAndMethod(QueryHttpAuthDetailReqDTO query);
}
