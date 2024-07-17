package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.PublishClusterMapper;
import com.jd.workflow.console.dto.PublishClusterDTO;
import com.jd.workflow.console.dto.PublishMethodDTO;
import com.jd.workflow.console.dto.PublishMethodQueryDTO;
import com.jd.workflow.console.dto.PublishMethodQueryReqDTO;
import com.jd.workflow.console.dto.QueryClusterReqDTO;
import com.jd.workflow.console.dto.QueryClusterResultDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.PublishCluster;
import com.jd.workflow.console.entity.PublishManage;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.helper.ProjectHelper;
import com.jd.workflow.console.service.IPublishClusterService;
import com.jd.workflow.console.service.IPublishManageService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 * 类 名 称：PublishClusterServiceImpl
 * 类 描 述：TODO
 * 创建时间：2022-12-27 18:09
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Service
public class PublishClusterServiceImpl extends ServiceImpl<PublishClusterMapper, PublishCluster> implements IPublishClusterService {

    @Autowired
    private IPublishManageService publishManageService;

    @Resource
    private ProjectHelper projectHelper;
    @Autowired
    IUserInfoService userInfoService;

    @Override
    public Long addPublishCluster(PublishClusterDTO dto) {
        dto.checkAddCluster();
        dto.setId(null);
        //校验code重复
        PublishCluster lastObj = this.getOne(Wrappers.<PublishCluster>lambdaQuery().eq(PublishCluster::getClusterCode, dto.getClusterCode()).eq(PublishCluster::getYn, DataYnEnum.VALID.getCode()));
        if(lastObj!=null){
            BizException e =  new BizException("集群编码已经存在!");
            e.data(lastObj.getId());
            throw e;
        }
        PublishCluster entity  = new PublishCluster();
        BeanUtils.copyProperties(dto,entity);
        //预留状态
        entity.setStatus(DataYnEnum.VALID.getCode());
        Date opTime = new Date();
        entity.setCreated(opTime);
        entity.setModified(opTime);
        entity.setCreator(UserSessionLocal.getUser().getUserId());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        entity.setYn(DataYnEnum.VALID.getCode());
        entity.setMembers(dto.buildMembers());
        boolean save = save(entity);
        if(!save){
            log.error("IPublishClusterService.addPublishCluster execute save but return false , param={}>>>>>>>", JSON.toJSONString(entity));
        }
        return entity.getId();
    }


    @Override
    public Boolean modifyPublishCluster(PublishClusterDTO dto) {
        dto.checkModifyCluster();
        dto.setClusterCode(null);
        PublishCluster cluster = getClusterById(dto.getId());
        checkAuth(cluster);
        PublishCluster entity  = new PublishCluster();
        entity.setId(dto.getId());
        entity.setClusterName(dto.getClusterName());
        entity.setDesc(dto.getDesc());
        entity.setClusterDomain(dto.getClusterDomain());
        entity.setMembers(dto.buildMembers());
        Date opTime = new Date();
        entity.setModified(opTime);
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        return updateById(entity);
    }

    @Override
    public Boolean removePublishCluster(Long id) {
        //校验权限
        checkAuth(getClusterById(id));
        //校验是否被发布过
        LambdaQueryWrapper<PublishManage> queryWrapper = Wrappers.<PublishManage>lambdaQuery();
        queryWrapper.eq(PublishManage::getClusterId,id);
        List<PublishManage> list = publishManageService.list(queryWrapper.setEntityClass(PublishManage.class).select(new Predicate<TableFieldInfo>() {
            @Override
            public boolean test(TableFieldInfo tableFieldInfo) {
                return !tableFieldInfo.getColumn().equalsIgnoreCase("content");
            }
        }));
        if(CollectionUtils.isNotEmpty(list)){
            throw new BizException("集群已经被发布接口使用,不允许删除!");
        }
        PublishCluster entity  = new PublishCluster();
        entity.setId(id);
        entity.setYn(DataYnEnum.INVALID.getCode());
        entity.setModified(new Date());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        return updateById(entity);
    }

    @Override
    public PublishClusterDTO findPublishCluster(Long id) {
        PublishCluster cluster = getClusterById(id);
        PublishClusterDTO dto = new PublishClusterDTO();
        BeanUtils.copyProperties(cluster,dto);
        dto.splitMembers(cluster.getMembers());
        return dto;
    }

    @Override
    public QueryClusterResultDTO queryAppByCondition(QueryClusterReqDTO query) {
        query.initPageParam(20);
        QueryClusterResultDTO result = new QueryClusterResultDTO();
        result.setCurrentPage(query.getCurrentPage());
        result.setPageSize(query.getPageSize());
        LambdaQueryWrapper<PublishCluster> qw = Wrappers.<PublishCluster>lambdaQuery().orderByDesc(PublishCluster::getModified);
        qw.eq(query.getId()!=null,PublishCluster::getId,query.getId());
        qw.eq(PublishCluster::getYn,DataYnEnum.VALID.getCode());
        if(StringUtils.isNotBlank(query.getClusterName())){
            qw.and(wq->wq.like(PublishCluster::getClusterName,query.getClusterName()).or().like(PublishCluster::getClusterCode,query.getClusterName()));
        }
        qw.like(StringUtils.isNotBlank(query.getPin()),PublishCluster::getMembers,(query.getPin()+","));
        Page<PublishCluster> pageResult = this.page(new Page(query.getCurrentPage(),query.getPageSize()), qw);
        if(pageResult!=null&& CollectionUtils.isNotEmpty(pageResult.getRecords())){
            result.setTotalCnt(Long.valueOf(pageResult.getTotal()));
            result.setList(pageResult.getRecords().stream().map(o->{
                PublishClusterDTO dto = new PublishClusterDTO();
                BeanUtils.copyProperties(o,dto);
                dto.splitMembers(o.getMembers());
                return dto;
            }).collect(Collectors.toList()));
        }
        if(result.getList() == null){
            result.setList(Collections.emptyList());
        }
        fixUserInfo(result);
        return result;
    }
    private void fixUserInfo(QueryClusterResultDTO dto){
        if(dto.getList() == null  || dto.getList().isEmpty()) return;
        Set<String> names = new HashSet<>();
        for (PublishClusterDTO publishClusterDTO : dto.getList()) {
            names.addAll(publishClusterDTO.getOwner());
            if(publishClusterDTO.getMember() != null){
                names.addAll(publishClusterDTO.getMember());
            }
        }
        if(names.isEmpty()) return;
        List<UserInfo> users = userInfoService.getUsers(names.stream().collect(Collectors.toList()));
        Map<String, List<UserInfo>> userCode2User = users.stream().collect(Collectors.groupingBy(UserInfo::getUserCode));
        for (PublishClusterDTO item : dto.getList()) {
            for (String s : item.getOwner()) {
                final List<UserInfo> userInfos = userCode2User.get(s);
                if(userInfos!=null){
                    item.getOwnerNames().add(userInfos.get(0).getUserName());
                }else{
                    item.getOwnerNames().add(s);
                }

            }
            for (String s : item.getMember()) {
                final List<UserInfo> userInfos = userCode2User.get(s);
                if(userInfos!=null){
                    item.getMemberNames().add(userInfos.get(0).getUserName());
                }else{
                    item.getOwnerNames().add(s);
                }

            }
        }
    }
    @Override
    public Page<PublishMethodDTO> queryPublishMethods(PublishMethodQueryReqDTO queryDTO) {
        PublishCluster cluster = getClusterById(queryDTO.getClusterId());
        PublishMethodQueryDTO query = new PublishMethodQueryDTO(queryDTO);
        Page<PublishMethodDTO> result = publishManageService.queryPublishMethods(query);
        if(CollectionUtils.isNotEmpty(result.getRecords())){
            result.getRecords().forEach(o->{
                o.setClusterDomain(cluster.getClusterDomain());
                o.setAddress(projectHelper.getPublishUrl(cluster.getClusterDomain(),getPublishMethodId(o)));
            });
        }
        return result;
    }



    /**
     * 根据id查已存在的对象数据
     * @param id
     * @return
     */
    private PublishCluster getClusterById(Long id){
        PublishCluster lastObj = this.getOne(Wrappers.<PublishCluster>lambdaQuery().eq(PublishCluster::getId, id).eq(PublishCluster::getYn, DataYnEnum.VALID.getCode()));
        if(lastObj==null){
            throw new BizException("集群不存在!");
        }
        return lastObj;
    }

    private void checkAuth(PublishCluster info){
        if(UserSessionLocal.getUser().getUserId()==null)return;
        if(UserSessionLocal.getUser().getUserId()!=null&&info.getMembers().indexOf((UserSessionLocal.getUser().getUserId()+","))==-1){
            throw new BizException("无集群操作权限!");
        }
    }

    public String getPublishMethodId(PublishMethodDTO dto) {
        if (!StringUtils.isBlank(dto.getServiceCode())
                && !StringUtils.isBlank(dto.getMethodCode())
        ) {
            return "/" + dto.getServiceCode() + "/" + dto.getMethodCode();
        }
        return dto.getRelatedMethodId() + "";
    }
}
