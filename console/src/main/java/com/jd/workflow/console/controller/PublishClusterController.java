package com.jd.workflow.console.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.PublishClusterDTO;
import com.jd.workflow.console.dto.PublishMethodDTO;
import com.jd.workflow.console.dto.PublishMethodQueryReqDTO;
import com.jd.workflow.console.dto.QueryClusterReqDTO;
import com.jd.workflow.console.dto.QueryClusterResultDTO;
import com.jd.workflow.console.service.IPublishClusterService;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 项目名称：parent
 * 类 名 称：PublishClusterController
 * 类 描 述：集群管理
 * 创建时间：2022-12-28 10:32
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/publishCluster")
@UmpMonitor
@Api(value = "集群管理",tags="集群管理")
public class PublishClusterController {

    @Resource
    private IPublishClusterService publishClusterService;

    /**
     * 新增集群
     * @param dto
     * @return
     */
    @PostMapping("/addCluster")
    @ApiOperation("新增集群")
    public CommonResult<Long> addCluster(@RequestBody PublishClusterDTO dto){
        log.info("PublishClusterController addCluster requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto,"新增集群时入参不能为空");
        return CommonResult.buildSuccessResult(publishClusterService.addPublishCluster(dto));
    }

    /**
     * 修改集群
     * @param dto
     * @return
     */
    @PostMapping("/modifyCluster")
    @ApiOperation(value="修改集群")
    public CommonResult<Boolean> modifyCluster(@RequestBody PublishClusterDTO dto){
        log.info("PublishClusterController modifyCluster requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto,"修改集群时入参不能为空");
        return CommonResult.buildSuccessResult(publishClusterService.modifyPublishCluster(dto));
    }

    /**
     * 删除集群
     * @param id
     * @return
     */
    @GetMapping("/removeCluster")
    @ApiOperation(value="删除集群")
    public CommonResult<Boolean> removeCluster(Long id){
        log.info("PublishClusterController removeCluster id={} ",id);
        Guard.notNull(id,"删除集群时id不能为空");
        return CommonResult.buildSuccessResult(publishClusterService.removePublishCluster(id));
    }

    /**
     * 集群详情
     * @param id
     * @return
     */
    @GetMapping("/findCluster")
    @ApiOperation(value="根据id获取集群信息")
    public CommonResult<PublishClusterDTO> findCluster(Long id){
        log.info("PublishClusterController findCluster id={} ",id);
        Guard.notNull(id,"查询集群时id不能为空");
        return CommonResult.buildSuccessResult(publishClusterService.findPublishCluster(id));
    }

    /**
     * 查询集群信息
     * @param query
     * @return
     */
    @PostMapping("/queryCluster")
    @ApiOperation(value="查询查询集群列表")
    public CommonResult<QueryClusterResultDTO> queryCluster(@RequestBody QueryClusterReqDTO query){
        log.info("PublishClusterController queryCluster requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query,"查询集群列表时入参不能为空");
        query.setPin(UserSessionLocal.getUser().getUserId());
        return CommonResult.buildSuccessResult(publishClusterService.queryAppByCondition(query));
    }

    /**
     * 查询集群发布方法
     * @param query
     * @return
     */
    @PostMapping("/queryPublishMethods")
    @ApiOperation(value="查询集群发布的方法列表")
    public CommonResult<Page<PublishMethodDTO>> queryPublishMethods(@RequestBody PublishMethodQueryReqDTO query){
        log.info("PublishClusterController queryPublishMethods requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query,"查询集群发布的方法列表时入参不能为空");
        Guard.notNull(query.getClusterId(),"查询集群发布的方法列表时入参集群ID不能为空");
        return CommonResult.buildSuccessResult(publishClusterService.queryPublishMethods(query));
    }


}
