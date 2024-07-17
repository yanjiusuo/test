package com.jd.workflow.console.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.HttpAuthApplyDetailMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.HttpAuthApplyDetailDTO;
import com.jd.workflow.console.dto.QueryHttpAuthApplyDetailReqDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.HttpAuthApplyDetail;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.service.IHttpAuthApplyDetailService;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 *
 * @author wangwenguang
 * @date 2023-01-06 11:07
 */
@Service
@Slf4j
public class HttpAuthApplyDetailServiceImpl extends ServiceImpl<HttpAuthApplyDetailMapper, HttpAuthApplyDetail> implements IHttpAuthApplyDetailService {


    /**
     * 鉴权明细管理
     */
    @Resource
    private HttpAuthApplyDetailMapper httpAuthApplyDetailMapper;
    @Autowired
    UserInfoServiceImpl userInfoService;
    @Autowired
    AppInfoServiceImpl appInfoService;
    /**
     * 导入时默认用户
     */
    private static String DEFAULT_IMPORT_USER = "systemImport";

    /**
     * 新增app
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthApplyDetailDTO> queryList(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        //分页处理
        Page<HttpAuthApplyDetailDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        queryDTO.setOffset((queryDTO.getCurrent()-1) * queryDTO.getPageSize());
        log.info("#HttpAuthApplyDetailServiceImpl.queryList={}", JSON.toJSONString(queryDTO));
        long total = NumberUtils.toLong(httpAuthApplyDetailMapper.queryListCount(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthApplyDetail> records = httpAuthApplyDetailMapper.queryList(queryDTO);
            //转化为DTO
            List<HttpAuthApplyDetailDTO> recordDTOs = toDTOList(records);
            page.setRecords(recordDTOs);
        }

        return page;
    }

    /**
     * 查询列表
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthApplyDetailDTO> queryAllList(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        List<HttpAuthApplyDetail> records = httpAuthApplyDetailMapper.queryList(queryDTO);
        List<HttpAuthApplyDetailDTO> recordDTOs = toDTOList(records);
        return recordDTOs;

    }


    /**
     * 查询列表数量
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Long queryListCount(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        return httpAuthApplyDetailMapper.queryListCount(queryDTO);
    }

    /**
     * 按接口分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthApplyDetailDTO> queryListPageGroupByInterface(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        Page<HttpAuthApplyDetailDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        queryDTO.setOffset((queryDTO.getCurrent()-1) * queryDTO.getPageSize());
        log.info("#HttpAuthApplyDetailServiceImpl.queryListPageGroupByInterface={}", JSON.toJSONString(queryDTO));
        long total = NumberUtils.toLong(httpAuthApplyDetailMapper.queryCountPageGroupByInterface(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthApplyDetail> records = httpAuthApplyDetailMapper.queryListPageGroupByInterface(queryDTO);
            List<HttpAuthApplyDetailDTO> recordDTOs = toDTOList(records); //转化为DTO
            page.setRecords(recordDTOs);
        }
        return page;
    }

    /**
     * 按接口分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthApplyDetailDTO> queryListGroupByInterface(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        List<HttpAuthApplyDetail> records = httpAuthApplyDetailMapper.queryListGroupByInterface(queryDTO);
        return toDTOList(records);
    }



    /**
     * 按方法分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthApplyDetailDTO> queryListGroupByMethod(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        List<HttpAuthApplyDetail> records = httpAuthApplyDetailMapper.queryListGroupByMethod(queryDTO);
        return toDTOList(records);
    }

    /**
     * 按鉴权标识分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthApplyDetailDTO> queryListPageGroupByAuthCode(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        Page<HttpAuthApplyDetailDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        queryDTO.setOffset((queryDTO.getCurrent()-1) * queryDTO.getPageSize());
        log.info("#HttpAuthApplyDetailServiceImpl.queryListPageGroupByAuthCode={}", JSON.toJSONString(queryDTO));
        long total = NumberUtils.toLong(httpAuthApplyDetailMapper.queryCountPageGroupByAuthCode(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthApplyDetail> records = httpAuthApplyDetailMapper.queryListPageGroupByAuthCode(queryDTO);
            List<HttpAuthApplyDetailDTO> recordDTOs = toDTOList(records); //转化为DTO
            page.setRecords(recordDTOs);
        }
        return page;
    }

    /**
     * 按鉴权标识分组展示
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthApplyDetailDTO> queryListPageGroupByAuthCodeAndMethod(QueryHttpAuthApplyDetailReqDTO queryDTO) {
        Page<HttpAuthApplyDetailDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        queryDTO.setOffset((queryDTO.getCurrent()-1) * queryDTO.getPageSize());
        log.info("#HttpAuthApplyDetailServiceImpl.queryListPageGroupByAuthCodeAndMethod={}", JSON.toJSONString(queryDTO));
        long total = NumberUtils.toLong(httpAuthApplyDetailMapper.queryCountPageGroupByAuthCodeAndMethod(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthApplyDetail> records = httpAuthApplyDetailMapper.queryListPageGroupByAuthCodeAndMethod(queryDTO);
            List<HttpAuthApplyDetailDTO> recordDTOs = toDTOList(records); //转化为DTO
            page.setRecords(recordDTOs);
        }
        return page;
    }

    /**
     * 批量添加数据
     *
     * @param applyDetailList
     * @return
     */
    @Override
    public boolean batchSaveApplyDetailDTO(List<HttpAuthApplyDetailDTO> applyDetailList) {
        for (HttpAuthApplyDetailDTO authApplyDetailDTO : applyDetailList){
            authApplyDetailDTO.setCreated(new Date());
            authApplyDetailDTO.setModified(new Date());
            authApplyDetailDTO.setModifier(authApplyDetailDTO.getCreator());
        }
        List<HttpAuthApplyDetail> authApplyDetails = toEntityList(applyDetailList);
        boolean success = saveBatch(authApplyDetails,50);
        log.info("#batchSaveApplyDetailDTO.success= "+success);
        return success;
    }

    /**
     * 批量添加数据
     *
     * @param applyDetailDTO
     * @return
     */
    @Override
    public boolean saveApplyDetailDTO(HttpAuthApplyDetailDTO applyDetailDTO) {
        HttpAuthApplyDetail applyDetail = toEntity(applyDetailDTO);
        log.info("#saveApplyDetailDTO.applyDetail= {}"+ JsonUtils.toJSONString(applyDetail));
        boolean success = save(applyDetail);
        log.info("#saveApplyDetailDTO.success= "+success);
        return success;
    }


    /**
     * 转化为DTO
     *
     * @param entity
     * @return
     */
    private HttpAuthApplyDetailDTO toDTO(HttpAuthApplyDetail entity) {
        if (entity == null) {
            return null;
        }
        HttpAuthApplyDetailDTO dto = new HttpAuthApplyDetailDTO();
        BeanUtils.copyProperties(entity, dto);
        String modifier = entity.getModifier();
        //如果是导入的历史数据，则清空
        if (StringUtils.isNotBlank(modifier) && DEFAULT_IMPORT_USER.equals(modifier)){
            dto.setTicketId(null);
        }
        return dto;
    }

    /**
     * 转化为entity
     *
     * @param dto
     * @return
     */
    private HttpAuthApplyDetail toEntity(HttpAuthApplyDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        HttpAuthApplyDetail entity = new HttpAuthApplyDetail();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * 转化为DTO列表
     *
     * @param entityList
     * @return
     */
    private List<HttpAuthApplyDetailDTO> toDTOList(List<HttpAuthApplyDetail> entityList) {
        //转化为DTO
        return Optional.ofNullable(entityList).orElse(new ArrayList<>()).stream().map(v -> {
            return toDTO(v);
        }).collect(Collectors.toList());
    }

    /**
     * 转化为entity列表
     *
     * @param dtoList
     * @return
     */
    private List<HttpAuthApplyDetail> toEntityList(List<HttpAuthApplyDetailDTO> dtoList) {
        //转化为DTO
        return Optional.ofNullable(dtoList).orElse(new ArrayList<>()).stream().map(v -> {
            return toEntity(v);
        }).collect(Collectors.toList());
    }
    Map<String, AppInfoDTO> appCode2Apps = new ConcurrentHashMap<>();
    Map<String, String> appCode2Dept = new ConcurrentHashMap<>();
    public String getAppDept(String appCode){

        return appCode2Dept.computeIfAbsent(appCode,vs->{
             AppInfoDTO appDto = appCode2Apps.computeIfAbsent(appCode,appVs->{

                 return appInfoService.findAppByCode(appCode);
             });
             String owner  = null;
             if(!ObjectHelper.isEmpty(appDto.getOwner())){
                 owner = appDto.getOwner().get(0);
             }else if(!ObjectHelper.isEmpty(appDto.getJdosOwner())){
                 owner = appDto.getJdosOwner();
             }
             if(owner == null) return null;
            UserInfo userInfo = userInfoService.getOne(owner);
            if(userInfo != null){
                return userInfo.getDept();
            }
            return null;
        });
    }
    private List<HttpAuthApplyDetailExt> queryAuthApply(){
        List<HttpAuthApplyDetailExt> exts = new ArrayList<>();
        LambdaQueryWrapper<HttpAuthApplyDetail> lqw = new LambdaQueryWrapper();
        lqw.orderByDesc(HttpAuthApplyDetail::getAppCode);
        List<HttpAuthApplyDetail> details = list(lqw);
        for (HttpAuthApplyDetail detail : details) {
            HttpAuthApplyDetailExt ext = new HttpAuthApplyDetailExt();
            BeanUtils.copyProperties(detail,ext);
            ext.setAppDept(getAppDept(ext.getAppCode()));
            ext.setCallerDept(getAppDept(ext.getCallAppCode()));
            exts.add(ext);
        }
        return exts;
    }
    public List<HttpAuthApplyDetailExt> dumpData(){
        return queryAuthApply();
    }
    @Data
    public static class HttpAuthApplyDetailExt extends HttpAuthApplyDetail{
        String appDept;

        String callerDept;
    }
}
