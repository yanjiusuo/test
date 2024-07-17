package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.PublishManageDTO;
import com.jd.workflow.console.dto.PublishMethodDTO;
import com.jd.workflow.console.dto.PublishMethodQueryDTO;
import com.jd.workflow.console.dto.WorkFlowPublishReqDTO;
import com.jd.workflow.console.entity.PublishManage;

import java.util.List;

/**
 * 项目名称：example
 * 类 名 称：IPublishManageService
 * 类 描 述：发布服务接口定义
 * 创建时间：2022-06-01 10:07
 * 创 建 人：wangxiaofei8
 */
public interface IPublishManageService extends IService<PublishManage> {


    /**
     * 发布http转换后的webservice
     * @param methodId
     * @param interfaceId
     * @return
     */
    public Boolean publishConvertWebService(Long methodId, Long interfaceId,Long clusterId);


    /**
     * 重新发布服务
     * @param id
     * @param methodId
     * @param interfaceId
     * @return
     */
    public Boolean republishService(Long id , Long methodId, Long interfaceId);


    /**
     * 发布流程编排

     * @return
     */
    public Boolean publishWorkflow(WorkFlowPublishReqDTO dto);


    /**
     * 查询发布列表
     * @param methodId
     * @param interfaceId
     * @return
     */
    public List<PublishManageDTO> findPublicVersionList(Long methodId, Long interfaceId);


    /**
     * 查询详情
     * @param id
     * @param methodId
     * @param interfaceId
     * @return
     */
    public PublishManageDTO findPublishVersionDetail(Long id , Long methodId, Long interfaceId);


    /**
     *
     * @param queryDTO
     * @return
     */
    public Page<PublishMethodDTO> queryPublishMethods(PublishMethodQueryDTO queryDTO);

}
