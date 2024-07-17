package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dao.mapper.FlowParamMapper;
import com.jd.workflow.console.dao.mapper.FlowParamQuoteMapper;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.flow.param.*;
import com.jd.workflow.console.entity.FlowParam;
import com.jd.workflow.console.entity.FlowParamGroup;
import com.jd.workflow.console.entity.FlowParamQuote;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.service.IFlowParamQuoteService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 17:14
 * @Description:
 */
@Service
@Slf4j
public class FlowParamQuoteServiceImpl extends ServiceImpl<FlowParamQuoteMapper, FlowParamQuote> implements IFlowParamQuoteService {

    @Resource
    private IMemberRelationService memberRelationService;


    @Resource
    private FlowParamQuoteMapper flowParamQuoteMapper;


    /**
     * 用户表
     *
     * @date: 2022/6/1 16:57
     * @author wubaizhao1
     */
    @Resource
    IUserInfoService userInfoService;


    @Override
    public Boolean quoteParam(FlowParamQuoteDTO flowParamQuoteDTO) {
        boolean authFlag = checkAuth(ResourceTypeEnum.PARAM_GROUP, UserSessionLocal.getUser().getUserId(), flowParamQuoteDTO.getInterfaceId());
        Guard.assertTrue(authFlag, "无权限", ServiceErrorEnum.NO_OPERATION_PERMISSION.getCode());
        List<FlowParamQuote> list = new ArrayList<>();
        for (Long paramId : flowParamQuoteDTO.getParamIds()) {
            FlowParamQuote flowParamQuote = new FlowParamQuote();
            flowParamQuote.setInterfaceId(flowParamQuoteDTO.getInterfaceId());
            flowParamQuote.setFlowParamId(paramId);
            flowParamQuote.setYn(DataYnEnum.VALID.getCode());
            list.add(flowParamQuote);
        }
        boolean saveFlag = saveBatch(list, list.size());
        if (BooleanUtils.isFalse(saveFlag)) {
            log.error("FlowParamQuoteServiceImpl quoteParam error!!");
        }
        return saveFlag;
    }

    @Override
    public Boolean cancelQuoteParam(FlowParamQuoteDTO flowParamQuoteDTO) {
        boolean authFlag = checkAuth(ResourceTypeEnum.PARAM_GROUP, UserSessionLocal.getUser().getUserId(), flowParamQuoteDTO.getInterfaceId());
        Guard.assertTrue(authFlag, "无权限", ServiceErrorEnum.NO_OPERATION_PERMISSION.getCode());
        List<FlowParamQuote> list = new ArrayList<>();
        for (Long paramId : flowParamQuoteDTO.getParamIds()) {
            FlowParamQuote flowParamQuote = new FlowParamQuote();
            flowParamQuote.setInterfaceId(flowParamQuoteDTO.getInterfaceId());
            flowParamQuote.setId(paramId);
            flowParamQuote.setYn(DataYnEnum.INVALID.getCode());
            list.add(flowParamQuote);
        }
        boolean saveFlag = updateBatchById(list, list.size());
        if (BooleanUtils.isFalse(saveFlag)) {
            log.error("FlowParamQuoteServiceImpl unquoteParam error!!");
        }
        return saveFlag;
    }

    @Override
    public QueryParamQuoteResultDTO queryQuoteParam(QueryParamQuoteReqDTO queryDTO) {
        boolean authFlag = checkAuth(ResourceTypeEnum.PARAM_GROUP, UserSessionLocal.getUser().getUserId(), queryDTO.getInterfaceId());
        Guard.assertTrue(authFlag, "无权限", ServiceErrorEnum.NO_OPERATION_PERMISSION.getCode());
        QueryParamQuoteResultDTO resultDTO = new QueryParamQuoteResultDTO();
        queryDTO.initPageParam(10000);
        resultDTO.setCurrentPage(queryDTO.getCurrentPage());
        resultDTO.setPageSize(queryDTO.getPageSize());
        List<FlowParamQuote> flowParamQuotes = flowParamQuoteMapper.queryQuoteParamList(queryDTO);
        if (CollectionUtils.isNotEmpty(flowParamQuotes)) {
            List<String> userCodeList = flowParamQuotes.stream().map(FlowParamQuote::getCreator).distinct().collect(Collectors.toList());
            List<UserInfo> userList = userInfoService.getUsers(userCodeList);
            Map<String, String> userMap = userList.stream().collect(Collectors.toMap(UserInfo::getUserCode, UserInfo::getUserName));
            flowParamQuotes = flowParamQuotes.stream().map(e -> {
                e.setUserCode(e.getCreator());
                e.setUserName(userMap.get(e.getCreator()));
                return e;
            }).collect(Collectors.toList());
        }
        resultDTO.setList(flowParamQuotes);
        resultDTO.setTotalCnt(flowParamQuoteMapper.queryQuoteParamCount(queryDTO));
        return resultDTO;
    }


    @Override
    public QueryParamResultDTO queryUnQuoteParam(QueryParamQuoteReqDTO queryDTO) {
        boolean authFlag = checkAuth(ResourceTypeEnum.PARAM_GROUP, UserSessionLocal.getUser().getUserId(), queryDTO.getInterfaceId());
        Guard.assertTrue(authFlag, "无权限", ServiceErrorEnum.NO_OPERATION_PERMISSION.getCode());
        QueryParamResultDTO resultDTO = new QueryParamResultDTO();
        queryDTO.initPageParam(10000);
        resultDTO.setCurrentPage(queryDTO.getCurrentPage());
        resultDTO.setPageSize(queryDTO.getPageSize());
        List<FlowParam> flowParamQuotes = flowParamQuoteMapper.queryUnQuoteParamList(queryDTO);
        if (CollectionUtils.isNotEmpty(flowParamQuotes)) {
            List<String> userCodeList = flowParamQuotes.stream().map(FlowParam::getCreator).distinct().collect(Collectors.toList());
            List<UserInfo> userList = userInfoService.getUsers(userCodeList);
            Map<String, String> userMap = userList.stream().collect(Collectors.toMap(UserInfo::getUserCode, UserInfo::getUserName));
            flowParamQuotes = flowParamQuotes.stream().map(e -> {
                e.setUserCode(e.getCreator());
                e.setUserName(userMap.get(e.getCreator()));
                return e;
            }).collect(Collectors.toList());
        }
        resultDTO.setList(flowParamQuotes);
        resultDTO.setTotalCnt(flowParamQuoteMapper.queryUnQuoteParamCount(queryDTO));
        return resultDTO;
    }

    @Override
    public QueryParamQuoteForGroupResultDTO queryQuoteParamForGroup(QueryParamQuoteReqDTO queryDTO) {
        boolean authFlag = checkAuth(ResourceTypeEnum.PARAM_GROUP, UserSessionLocal.getUser().getUserId(), queryDTO.getInterfaceId());
        Guard.assertTrue(authFlag, "无权限", ServiceErrorEnum.NO_OPERATION_PERMISSION.getCode());
        QueryParamQuoteForGroupResultDTO resultDTO = new QueryParamQuoteForGroupResultDTO();
        queryDTO.initPageParam(10000);
        resultDTO.setCurrentPage(queryDTO.getCurrentPage());
        resultDTO.setPageSize(queryDTO.getPageSize());
        List<FlowParamGroup> flowParamGroups = flowParamQuoteMapper.queryQuoteParamForGroup(queryDTO);
        if(CollectionUtils.isNotEmpty(flowParamGroups)){
            flowParamGroups.forEach(e -> e.getChildren().forEach(f -> {
                f.setExp_name("${workflow.params."+f.getName()+"}");
            }));
        }
        resultDTO.setList(flowParamGroups);
        resultDTO.setTotalCnt(flowParamQuoteMapper.queryQuoteParamForGroupCount(queryDTO));
        return resultDTO;
    }

    private boolean checkAuth(ResourceTypeEnum resourceType, String userCode, Long resourceId) {
        return true;

    }
}
