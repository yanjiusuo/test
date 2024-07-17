package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.PublishClusterDTO;
import com.jd.workflow.console.dto.PublishMethodDTO;
import com.jd.workflow.console.dto.PublishMethodQueryReqDTO;
import com.jd.workflow.console.dto.QueryClusterReqDTO;
import com.jd.workflow.console.dto.QueryClusterResultDTO;
import com.jd.workflow.console.entity.PublishCluster;

/**
 * 项目名称：parent
 * 类 名 称：IPublishClusterService
 * 类 描 述：TODO
 * 创建时间：2022-12-27 17:50
 * 创 建 人：wangxiaofei8
 */
public interface IPublishClusterService extends IService<PublishCluster> {

    /**
     * 新增集群
     * @param dto
     * @return
     */
    public Long addPublishCluster(PublishClusterDTO dto);


    /**
     * 修改集群
     * @param dto
     * @return
     */
    public Boolean modifyPublishCluster(PublishClusterDTO dto);


    /**
     * 删除集群
     * @param id
     * @return
     */
    public Boolean removePublishCluster(Long id);

    /**
     * 查看详情
     * @param id
     * @return
     */
    public PublishClusterDTO findPublishCluster(Long id);

    /**
     * 分页查询
     * @param query
     * @return
     */
    public QueryClusterResultDTO queryAppByCondition(QueryClusterReqDTO query);

    /**
     * 分页查询发布的方法
     * @param queryDTO
     * @return
     */
    public Page<PublishMethodDTO> queryPublishMethods(PublishMethodQueryReqDTO queryDTO);


}
