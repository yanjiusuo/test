package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dao.mapper.FlowParamMapper;
import com.jd.workflow.console.dto.flow.param.FlowParamDTO;
import com.jd.workflow.console.dto.flow.param.FlowParamGroupDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamReqDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamResultDTO;
import com.jd.workflow.console.entity.FlowParam;
import com.jd.workflow.console.entity.FlowParamGroup;
import com.jd.workflow.console.entity.FlowParamQuote;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.service.IFlowParamService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:11
 * @Description: 公共参数管理service
 */
@Service
@Slf4j
public class FlowParamServiceImpl extends ServiceImpl<FlowParamMapper, FlowParam> implements IFlowParamService {

    @Resource
    private FlowParamGroupServiceImpl flowParamGroupService;

    @Resource
    private FlowParamMapper flowParamMapper;

    @Resource
    private FlowParamQuoteServiceImpl flowParamQuoteService;

    /**
     * 用户表
     *
     * @date: 2022/6/1 16:57
     * @author wubaizhao1
     */
    @Resource
    IUserInfoService userInfoService;

    @Override
    public Long addParam(FlowParamDTO dto) {
        if (Objects.nonNull(dto.getGroupName()) && Objects.isNull(dto.getGroupId())) {
            //自动创建分组
            FlowParamGroupDTO groupDTO = new FlowParamGroupDTO();
            groupDTO.setGroupName(dto.getGroupName());
            Long groupId = flowParamGroupService.addGroup(groupDTO);
            Guard.notEmpty(groupId, "分组名称重复", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
            dto.setGroupId(groupId);
        }
        //校验参数名称是否重复，同一分组下参数名称不可重复
        if (checkParamNameDuplicate(dto)) {
            throw new BizException("参数名称重复！");
        }
        FlowParam flowParam = new FlowParam();
        flowParam.setName(dto.getName());
        flowParam.setDescription(dto.getDescription());
        flowParam.setValue(dto.getValue());
        flowParam.setGroupId(dto.getGroupId());
        Date opTime = new Date();
        flowParam.setCreated(opTime);
        flowParam.setModified(opTime);
        flowParam.setCreator(UserSessionLocal.getUser().getUserId());
        flowParam.setModifier(UserSessionLocal.getUser().getUserId());
        flowParam.setYn(DataYnEnum.VALID.getCode());
        boolean save = save(flowParam);
        if (!save) {
            log.error("FlowParamServiceImpl addParam error!! name:{},value:{}", dto.getName(), dto.getValue());
        }
        return flowParam.getId();
    }


    @Override
    public Boolean editParam(FlowParamDTO dto) {
        checkAuth(getFlowParamById(dto.getId()));
        if (Objects.nonNull(dto.getGroupName()) && Objects.isNull(dto.getGroupId())) {
            //自动创建分组
            FlowParamGroupDTO groupDTO = new FlowParamGroupDTO();
            groupDTO.setGroupName(dto.getGroupName());
            Long groupId = flowParamGroupService.addGroup(groupDTO);
            Guard.notEmpty(groupId, "分组名称重复", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
            dto.setGroupId(groupId);
        }
        FlowParam flowParam = new FlowParam();
        flowParam.setId(dto.getId());
        flowParam.setDescription(dto.getDescription());
        flowParam.setValue(dto.getValue());
        flowParam.setGroupId(dto.getGroupId());
        Date opTime = new Date();
        flowParam.setModified(opTime);
        flowParam.setModifier(UserSessionLocal.getUser().getUserId());
        return updateById(flowParam);
    }

    @Override
    public Boolean removeParam(Long id) {
        FlowParam flowParam = getFlowParamById(id);
        checkAuth(flowParam);
        LambdaQueryWrapper<FlowParamQuote> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowParamQuote::getYn, DataYnEnum.VALID.getCode())
                .eq(FlowParamQuote::getFlowParamId, id);
        int count = flowParamQuoteService.count(lqw);
        if (count > 0) {
            throw new BizException("公共参数已被服务引用，不允许删除！");
        }
        return removeById(id);
    }

    @Override
    public QueryParamResultDTO queryParams(QueryParamReqDTO query) {
        QueryParamResultDTO resultDTO = new QueryParamResultDTO();
        query.initPageParam(10000);
        // 查询当前用户有权限的分组ids
        List<Long> userResourceList = flowParamGroupService.getUserResourceList();
        if (CollectionUtils.isEmpty(userResourceList)) {
            // 当前用户无可查询的分组信息
            return resultDTO;
        }
        if (Objects.nonNull(query.getGroupId()) && userResourceList.contains(query.getGroupId())) {
            userResourceList.clear();
            userResourceList.add(query.getGroupId());
        }
        List<FlowParam> flowParams = flowParamMapper.queryParamList(query, userResourceList);
        long count = flowParamMapper.queryParamCount(query, userResourceList);
        if (CollectionUtils.isNotEmpty(flowParams)) {
            List<String> userCodeList = flowParams.stream().map(FlowParam::getCreator).distinct().collect(Collectors.toList());
            List<UserInfo> userList = userInfoService.getUsers(userCodeList);
            Map<String, String> userMap = userList.stream().collect(Collectors.toMap(UserInfo::getUserCode, UserInfo::getUserName));
            flowParams = flowParams.stream().map(e -> {
                e.setUserCode(e.getCreator());
                e.setUserName(userMap.get(e.getCreator()));
                return e;
            }).collect(Collectors.toList());
        }
        resultDTO.setTotalCnt(count);
        resultDTO.setList(flowParams);
        resultDTO.setCurrentPage(query.getCurrentPage());
        resultDTO.setPageSize(query.getPageSize());
        return resultDTO;
    }

    @Override
    public FlowParam getParamById(Long paramId) {
        FlowParam lastObj = this.getOne(Wrappers.<FlowParam>lambdaQuery().eq(FlowParam::getId, paramId).eq(FlowParam::getYn, DataYnEnum.VALID.getCode()));
        return lastObj;
    }


    private void checkAuth(FlowParam flowParam) {
        if (UserSessionLocal.getUser().getUserId() == null) return;
        if (!Objects.equals(flowParam.getCreator(), UserSessionLocal.getUser().getUserId())) {
            throw new BizException("无参数操作权限!");
        }
    }

    /**
     * 根据id查已存在的对象数据
     *
     * @param id
     * @return
     */
    private FlowParam getFlowParamById(Long id) {
        FlowParam lastObj = this.getOne(Wrappers.<FlowParam>lambdaQuery().eq(FlowParam::getId, id).eq(FlowParam::getYn, DataYnEnum.VALID.getCode()));
        if (lastObj == null) {
            throw new BizException("参数不存在!");
        }
        return lastObj;
    }


    /**
     * 校验参数名称是否重复，同一分组下参数名称不可重复
     *
     * @param dto
     * @return
     */
    private boolean checkParamNameDuplicate(FlowParamDTO dto) {
        LambdaQueryWrapper<FlowParam> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowParam::getYn, DataYnEnum.VALID.getCode())
                .eq(FlowParam::getGroupId, dto.getGroupId())
                .eq(FlowParam::getName, dto.getName());
        int count = count(lqw);
        if (count > 0) {
            //参数名称重复
            return true;
        }
        // 参数名称不重复
        return false;
    }
}
