package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.HttpAuthApplyDetail;

import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：IAppInfoService
 * 类 描 述：应用service
 * 创建时间：2022-11-16 16:50
 * 创 建 人：wangxiaofei8
 */
public interface IHttpAuthApplyDetailService extends IService<HttpAuthApplyDetail> {

    /**
     * 新增app
     *
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthApplyDetailDTO> queryList(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询列表
     *
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetailDTO> queryAllList(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 查询列表数量
     *
     * @param queryDTO
     * @return
     */
    public Long queryListCount(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 按接口分组展示
     *
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthApplyDetailDTO> queryListPageGroupByInterface(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 按接口分组展示
     *
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetailDTO> queryListGroupByInterface(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 按接口分组展示
     *
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDetailDTO> queryListGroupByMethod(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 按鉴权标识分组展示
     *
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthApplyDetailDTO> queryListPageGroupByAuthCode(QueryHttpAuthApplyDetailReqDTO queryDTO);

    /**
     * 按鉴权标识分组展示
     *
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthApplyDetailDTO> queryListPageGroupByAuthCodeAndMethod(QueryHttpAuthApplyDetailReqDTO queryDTO);


    /**
     * 批量添加数据
     *
     * @param applyDetailList
     * @return
     */
    public boolean batchSaveApplyDetailDTO(List<HttpAuthApplyDetailDTO> applyDetailList);

    /**
     * 批量添加数据
     *
     * @param applyDetailDTO
     * @return
     */
    public boolean saveApplyDetailDTO(HttpAuthApplyDetailDTO applyDetailDTO);
}