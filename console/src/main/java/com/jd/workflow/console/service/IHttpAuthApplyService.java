package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.HttpAuth;
import com.jd.workflow.console.entity.HttpAuthApply;
import com.jd.workflow.console.entity.HttpAuthApplyXbpParam;

import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：IAppInfoService
 * 类 描 述：应用service
 * 创建时间：2022-11-16 16:50
 * 创 建 人：wangxiaofei8
 */
public interface IHttpAuthApplyService extends IService<HttpAuthApply> {

    /**
     * 新增app
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthApplyDTO> queryListPage(QueryHttpAuthApplyReqDTO queryDTO);

    /**
     * 查询列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthApplyDTO> queryAllList(QueryHttpAuthApplyReqDTO queryDTO);

    /**
     * 提交鉴权申请
     * @param applyParamDTO
     * @return
     */
    public boolean submit(HttpAuthApplyParamDTO applyParamDTO);


    /**
     * xbp申请回调处理
     * @param applyParamDTO
     * @return
     */
    public void callBackXbpFlow(HttpAuthApplyXbpParam applyParamDTO);

    /**
     * 批量添加数据
     * @param appCode
     * @param appName
     * @param authApplyList
     * @return
     */
    public HttpAuthApplyResultDTO importApplyData(String appCode,String appName, List<HttpAuthApplyDTO> authApplyList);

}
