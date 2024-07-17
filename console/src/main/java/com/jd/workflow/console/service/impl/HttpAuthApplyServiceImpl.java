package com.jd.workflow.console.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.flow.xbp.service.XbpService;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.base.enums.XbpStatusEnum;
import com.jd.workflow.console.dao.mapper.HttpAuthApplyDetailMapper;
import com.jd.workflow.console.dao.mapper.HttpAuthApplyMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.utils.EncodeUtils;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.console.utils.UserUtils;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 *
 * @author wangwenguang
 * @date 2023-01-06 11:07
 */
@Service
@Slf4j
public class HttpAuthApplyServiceImpl extends ServiceImpl<HttpAuthApplyMapper, HttpAuthApply> implements IHttpAuthApplyService {

    /**
     * 鉴权管理
     */
    @Resource
    private HttpAuthApplyMapper httpAuthApplyMapper;

    /**
     * 鉴权明细管理
     */
    @Resource
    private HttpAuthApplyDetailMapper httpAuthApplyDetailMapper;

    /**
     * xbp 服务
     */
    @Autowired(required = false)
    private XbpService xbpService;

    /**
     * 应用服务
     */
    @Resource
    private IAppInfoService appInfoService;

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthConfigService httpAuthConfigService;

    /**
     * 鉴权标识服务
     */
    @Resource
    private IHttpAuthApplyDetailService httpAuthApplyDetailService;

    /**
     * 项目管理
     */
    @Resource
    private IInterfaceManageService interfaceManageService;

    /**
     * 接口服务
     */
    @Resource
    private IMethodManageService methodManageService;


    /**
     * 鉴权明细管理
     */
    @Resource
    private IHttpAuthDetailService httpAuthDetailService;

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
    public Page<HttpAuthApplyDTO> queryListPage(QueryHttpAuthApplyReqDTO queryDTO) {
        //分页处理
        Page<HttpAuthApplyDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        long total = NumberUtils.toLong(httpAuthApplyMapper.queryListCount(queryDTO));
        page.setTotal(total);
        if (total > 0) {
            List<HttpAuthApply> records = httpAuthApplyMapper.queryList(queryDTO);
            List<HttpAuthApplyDTO> recordDTOs = toDTOList(records);
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
    public List<HttpAuthApplyDTO> queryAllList(QueryHttpAuthApplyReqDTO queryDTO) {
        List<HttpAuthApply> records = httpAuthApplyMapper.queryList(queryDTO);
        return toDTOList(records);
    }

    /**
     * 提交鉴权申请
     *
     * @param applyParamDTO
     * @return
     */
    @Override
    public boolean submit(HttpAuthApplyParamDTO applyParamDTO) {
        //参数校验
        checkSubmitParam(applyParamDTO);

        //按应用进行分堆处理
        Map<String, List<HttpAuthApplyDTO>> applyMap = getApplyMap(applyParamDTO);

        //按分堆后应用进行xbp拆单
        for (Map.Entry<String, List<HttpAuthApplyDTO>> entry : applyMap.entrySet()) {
            List<HttpAuthApplyDTO> appCodeApplyList = entry.getValue();

            //获取xbp申请单
            HttpAuthApplyDTO xbpApplyDTO = getXbpApplyDTO(appCodeApplyList);

            //提交xbp申请
            Integer ticketId = submitXbp(xbpApplyDTO);

            //新增鉴权申请单以及申请明细
            if (NumberUtils.toInt(ticketId) > 0) {
                List<HttpAuthApplyDTO> addApplyDTOs = new ArrayList<>();
                Map<String, HttpAuthApplyDTO> addApplyMap = new HashMap<>();
                for (HttpAuthApplyDTO applyDTO : appCodeApplyList) {
                    applyDTO.setTicketId(String.valueOf(ticketId));
                    for (HttpAuthApplyDetailDTO interfaceDTO : applyDTO.getInterfaceList()) {
                        for (HttpAuthApplyDetailDTO methodDTO : interfaceDTO.getMethodList()) {
                            String key = applyDTO.getAppCode() + "_" + applyDTO.getCallAppCode() + "_" + methodDTO.getAuthCode();
                            HttpAuthApplyDTO authApplyDTO = addApplyMap.get(key);
                            if (authApplyDTO == null) {
                                HttpAuthApplyDTO addApplyDTO = new HttpAuthApplyDTO();
                                BeanUtils.copyProperties(applyDTO, addApplyDTO);
                                addApplyDTO.setAuthCode(methodDTO.getAuthCode());
                                addApplyMap.put(key, addApplyDTO);
                                addApplyDTOs.add(addApplyDTO);
                            }
                        }
                    }
                }
                boolean addAuthApply = addAuthApplyList(addApplyDTOs);

                //添加申请单明细
                if (addAuthApply) {
                    for (HttpAuthApplyDTO applyDTO : appCodeApplyList) {
                        for (HttpAuthApplyDetailDTO interfaceDTO : applyDTO.getInterfaceList()) {
                            for (HttpAuthApplyDetailDTO methodDTO : interfaceDTO.getMethodList()) {
                                methodDTO.setTicketId(String.valueOf(ticketId));
                                addAuthApplyDetail(methodDTO);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 按应用进行分堆处理
     *
     * @param applyParamDTO
     * @return
     */
    @NotNull
    private Map<String, List<HttpAuthApplyDTO>> getApplyMap(HttpAuthApplyParamDTO applyParamDTO) {
        Map<String, List<HttpAuthApplyDTO>> applyMap = new HashMap<>();
        for (HttpAuthApplyDTO applyDTO : applyParamDTO.getAuthApplyList()) {
            List<HttpAuthApplyDTO> appCodeApplyList = applyMap.get(applyDTO.getAppCode());
            if (CollectionUtils.isEmpty(appCodeApplyList)) {
                appCodeApplyList = new ArrayList<>();
            }
            appCodeApplyList.add(applyDTO);
            applyMap.put(applyDTO.getAppCode(), appCodeApplyList);
        }
        return applyMap;
    }

    /**
     * 获取xbp申请单
     *
     * @param appCodeApplyList
     * @return
     */
    @NotNull
    private HttpAuthApplyDTO getXbpApplyDTO(List<HttpAuthApplyDTO> appCodeApplyList) {
        HttpAuthApplyDTO authApplyDTO = appCodeApplyList.get(0);
        List<HttpAuthApplyDetailDTO> interfaceList = new ArrayList<>();
        HttpAuthApplyDTO xbpApplyDTO = new HttpAuthApplyDTO();
        xbpApplyDTO.setAppCode(authApplyDTO.getAppCode());
        xbpApplyDTO.setAppName(authApplyDTO.getAppName());
        xbpApplyDTO.setCallAppCode(authApplyDTO.getCallAppCode());
        xbpApplyDTO.setCallAppName(authApplyDTO.getCallAppName());
        xbpApplyDTO.setApplyDesc(authApplyDTO.getApplyDesc());
        xbpApplyDTO.setProductApprovers(authApplyDTO.getProductApprovers());
        xbpApplyDTO.setDevApprovers(authApplyDTO.getDevApprovers());
        xbpApplyDTO.setSite(authApplyDTO.getSite());

        for (HttpAuthApplyDTO applyDTO : appCodeApplyList) {
            interfaceList.addAll(applyDTO.getInterfaceList());
        }
        xbpApplyDTO.setInterfaceList(interfaceList);
        return xbpApplyDTO;
    }

    /**
     * xbp申请回调处理
     *
     * @param applyParamDTO
     * @return
     */
    @Override
    public void callBackXbpFlow(HttpAuthApplyXbpParam applyParamDTO) {
        log.info("#callBackXbpFlow.applyParamDTO={}", JSON.toJSONString(applyParamDTO));
        Guard.notNull(applyParamDTO, "#callBackXbpFlow 监听xbp审批节点，applyParamDTO 为空！");
        Guard.notNull(applyParamDTO.getTicketId(), "#callBackXbpFlow 监听xbp审批节点，申请单 ticketId为空！");

        Map<String, String> tokenMap = new HashMap<>();
        QueryHttpAuthApplyReqDTO queryDTO = new QueryHttpAuthApplyReqDTO();
        queryDTO.setTicketId(NumberUtils.toString(applyParamDTO.getTicketId()));
        List<HttpAuthApply> authApplyList = httpAuthApplyMapper.queryAllList(queryDTO);
        log.info("#callBackXbpFlow.authApplyList={}", JSON.toJSONString(authApplyList));
        if (CollectionUtils.isNotEmpty(authApplyList)) {
            for (HttpAuthApply authApply : authApplyList) {
                authApply.setTicketStatus(XbpStatusEnum.FINISHED.getCode());
                //调用方应用 + 提供方应用
                String tokenKey = authApply.getAppCode() + "_" + authApply.getCallAppCode();
                //生成16位的token
                String tokenValue = EncodeUtils.getMD5_16(tokenKey);
                //如果已经存在申请接口的记录，则使用原来的token
                QueryHttpAuthApplyReqDTO applyReqDTO = new QueryHttpAuthApplyReqDTO();
                applyReqDTO.setCallAppCode(authApply.getCallAppCode());
                applyReqDTO.setAppCode(authApply.getAppCode());
                applyReqDTO.setAuthCode(authApply.getAuthCode());
                applyReqDTO.setSite(authApply.getSite());
                applyReqDTO.setHasToken(true);
                applyReqDTO.setPageSize(1);
                List<HttpAuthApply> oldApplyList = httpAuthApplyMapper.queryList(applyReqDTO);
                if (CollectionUtils.isNotEmpty(oldApplyList)) {
                    HttpAuthApply oldAuthApply = oldApplyList.get(0);
                    if (oldAuthApply != null && StringUtils.isNotBlank(oldAuthApply.getToken())) {
                        tokenValue = oldAuthApply.getToken();
                    }
                }

                log.info("#callBackXbpFlow.HttpAuthApply.tokenKey= {} ,tokenValue= {}", tokenKey, tokenValue);
                tokenMap.put(tokenKey, tokenValue);
                authApply.setToken(tokenValue);
                //推送ducc并更新duccStatus状态
                httpAuthConfigService.pushAuthCodeToDucc(authApply);

                boolean success = saveOrUpdate(authApply);
                log.info("#callBackXbpFlow.saveOrUpdate.authApply[" + authApply.getId() + "].success={}", success);
            }
        }

        QueryHttpAuthApplyDetailReqDTO detailReqDTO = new QueryHttpAuthApplyDetailReqDTO();
        detailReqDTO.setTicketId(NumberUtils.toString(applyParamDTO.getTicketId()));
        List<HttpAuthApplyDetail> authApplyDetails = httpAuthApplyDetailMapper.queryAllList(detailReqDTO);
        log.info("#callBackXbpFlow.authApplyDetails={}", JSON.toJSONString(authApplyDetails));
        if (CollectionUtils.isNotEmpty(authApplyDetails)) {
            for (HttpAuthApplyDetail authApplyDetail : authApplyDetails) {
                //生成16位的token
                String tokenKey = authApplyDetail.getAppCode() + "_" + authApplyDetail.getCallAppCode();
                String tokenValue = tokenMap.get(tokenKey);
                //如果已经存在申请接口的记录，则使用原来的token
                QueryHttpAuthApplyDetailReqDTO applyReqDTO = new QueryHttpAuthApplyDetailReqDTO();
                applyReqDTO.setCallAppCode(authApplyDetail.getCallAppCode());
                applyReqDTO.setAppCode(authApplyDetail.getAppCode());
                applyReqDTO.setAuthCode(authApplyDetail.getAuthCode());
                applyReqDTO.setSite(authApplyDetail.getSite());
                applyReqDTO.setHasToken(true);
                applyReqDTO.setPageSize(1);
                List<HttpAuthApplyDetail> oldApplyDetailList = httpAuthApplyDetailMapper.queryList(applyReqDTO);
                if (CollectionUtils.isNotEmpty(oldApplyDetailList)) {
                    HttpAuthApplyDetail oldAuthApplyDetail = oldApplyDetailList.get(0);
                    if (oldAuthApplyDetail != null && StringUtils.isNotBlank(oldAuthApplyDetail.getToken())) {
                        tokenValue = oldAuthApplyDetail.getToken();
                    }
                }

                if (StringUtils.isBlank(tokenValue)) {
                    tokenValue = EncodeUtils.getMD5_16(tokenKey);
                }
                log.info("#callBackXbpFlow.HttpAuthApplyDetail.tokenKey= {} ,tokenValue= {}", tokenKey, tokenValue);
                authApplyDetail.setToken(tokenValue);
                boolean success = httpAuthApplyDetailService.saveOrUpdate(authApplyDetail);
                log.info("#callBackXbpFlow.saveOrUpdateBatch.authApplyDetail[" + authApplyDetail.getId() + "].success={}", success);
            }
        }
    }

    /**
     * 批量添加数据
     *
     * @param appCode
     * @param appName
     * @param authApplyList
     * @return
     */
    @Override
    public HttpAuthApplyResultDTO importApplyData(String appCode, String appName, List<HttpAuthApplyDTO> authApplyList) {
        HttpAuthApplyResultDTO applyResultDTO = new HttpAuthApplyResultDTO();
        if (CollectionUtils.isEmpty(authApplyList)) {
            return applyResultDTO;
        }
        List<HttpAuthApplyDTO> applyFailList = new ArrayList<>();
        List<HttpAuthApplyDTO> applySuccessList = new ArrayList<>();
        Map<String, String> authCodeUserMap = new HashMap<>();
        Map<String, AppInfo> appInfoMap = new HashMap<>();
        //补充提供方应用名称
        AppInfo appInfo = appInfoService.findApp(appCode);
        if (appInfo != null) {
            appInfoMap.put(appCode, appInfo);
        }
        for (HttpAuthApplyDTO applyDTO : authApplyList) {
            String userKey = applyDTO.getCallAppCode()+"_"+applyDTO.getAppCode()+"_"+applyDTO.getAuthCode();
            authCodeUserMap.put(userKey, applyDTO.getCreator());
            //校验提供方应用是否一致
            if (!appCode.equals(applyDTO.getAppCode())) {
                applyDTO.setFailMsg("提供方应用不一致！");
                applyFailList.add(applyDTO);
                continue;
            }

            //校验数据必填项
            if (StringUtils.isBlank(applyDTO.getCallAppCode())
                    || StringUtils.isBlank(applyDTO.getAppCode())
                    || StringUtils.isBlank(applyDTO.getAuthCode())
                    || StringUtils.isBlank(applyDTO.getCreator())
                    || StringUtils.isBlank(applyDTO.getToken())) {
                applyDTO.setFailMsg("必填项为空！");
                applyFailList.add(applyDTO);
                continue;
            }

            //补充提供方应用名称
            if (appInfo != null && StringUtils.isNotBlank(appInfo.getAppName())) {
                applyDTO.setAppName(appInfo.getAppName());
            }
            //补充调用方应用名称
            AppInfo callAppInfo = appInfoMap.get(applyDTO.getCallAppCode());
            if (callAppInfo == null) {
                callAppInfo = appInfoService.findApp(applyDTO.getCallAppCode());
                if (callAppInfo == null) {
                    applyDTO.setFailMsg("调用方应用不存在！");
                    applyFailList.add(applyDTO);
                    continue;
                }
                appInfoMap.put(applyDTO.getCallAppCode(), callAppInfo);
            }
            if (callAppInfo != null && StringUtils.isNotBlank(callAppInfo.getAppName())) {
                applyDTO.setCallAppName(callAppInfo.getAppName());
            }

            //校验是否已存在鉴权申请
            QueryHttpAuthApplyReqDTO queryDTO = new QueryHttpAuthApplyReqDTO();
            queryDTO.setAppCode(applyDTO.getAppCode());
            queryDTO.setCallAppCode(applyDTO.getCallAppCode());
            queryDTO.setSite(SiteEnum.China.getCode());
            queryDTO.setAuthCode(applyDTO.getAuthCode());
            log.info("#httpAuthApplyMapper.queryListCount.request={}", JsonUtils.toJSONString(queryDTO));
            long count = NumberUtils.toLong(httpAuthApplyMapper.queryListCount(queryDTO));
            log.info("#httpAuthApplyMapper.queryListCount.result={}", count);
            if (count > 0) {
                applyDTO.setFailMsg("数据已存在！");
                applyFailList.add(applyDTO);
                continue;
            }

            applyDTO.setCreated(new Date());
            applyDTO.setModified(new Date());
            applyDTO.setModifier(DEFAULT_IMPORT_USER);
            applyDTO.setYn(DataYnEnum.VALID.getCode());
            applyDTO.setSite(SiteEnum.China.getCode());
            String authCode = applyDTO.getAuthCode();
            String callAppCode = applyDTO.getCallAppCode();
            String key = appCode + "_" + callAppCode + "_" + authCode;
            String ticketId = EncodeUtils.getMD5_16(key);
            applyDTO.setTicketId(ticketId);
            applyDTO.setTicketStatus(XbpStatusEnum.FINISHED.getCode());
            HttpAuthApply authApply = toEntity(applyDTO);

            //推送ducc并更新duccStatus状态
            boolean duccSuccess = httpAuthConfigService.pushAuthCodeToDucc(authApply);
            boolean dbSuccess = false;
            if (duccSuccess) {
                dbSuccess = save(authApply);
                if (!dbSuccess) {
                    applyDTO.setFailMsg("保存db失败，请稍后再试！");
                }
            } else {
                applyDTO.setFailMsg("推送ducc失败，请稍后再试！");
            }

            if (dbSuccess) {
                applySuccessList.add(applyDTO);
            } else {
                applyFailList.add(applyDTO);
            }
        }

        List<HttpAuthApplyDetailDTO> authApplyDetailList = new ArrayList<>();

        for (HttpAuthApplyDTO applyDTO : authApplyList) {
            QueryHttpAuthDetailReqDTO detailReqDTO = new QueryHttpAuthDetailReqDTO();
            detailReqDTO.setAppCode(applyDTO.getAppCode());
            detailReqDTO.setAuthCode(applyDTO.getAuthCode());
            detailReqDTO.setSite(SiteEnum.China.getCode());
            List<HttpAuthDetail> authDetails = httpAuthDetailService.queryAllSourceList(detailReqDTO);
            if (CollectionUtils.isEmpty(authDetails)){
                continue;
            }

            for (HttpAuthDetail authDetail : authDetails){
                HttpAuthApplyDetailDTO applyDetailDTO = new HttpAuthApplyDetailDTO();
                applyDetailDTO.setInterfaceId(authDetail.getInterfaceId());
                applyDetailDTO.setInterfaceCode(authDetail.getInterfaceCode());
                applyDetailDTO.setInterfaceName(authDetail.getInterfaceName());
                applyDetailDTO.setMethodId(authDetail.getMethodId());
                applyDetailDTO.setMethodCode(authDetail.getMethodCode());
                applyDetailDTO.setMethodName(authDetail.getMethodName());
                applyDetailDTO.setPath(authDetail.getPath());
                applyDetailDTO.setAppCode(authDetail.getAppCode());
                applyDetailDTO.setAppName(applyDTO.getAppName());
                applyDetailDTO.setAuthCode(applyDTO.getAuthCode());
                applyDetailDTO.setCallAppCode(applyDTO.getCallAppCode());
                applyDetailDTO.setCallAppName(applyDTO.getCallAppName());
                applyDetailDTO.setSite(authDetail.getSite());
                applyDetailDTO.setToken(applyDTO.getToken());
                applyDetailDTO.setYn(DataYnEnum.VALID.getCode());
                applyDetailDTO.setCreator(applyDTO.getCreator());
                applyDetailDTO.setCreated(new Date());
                applyDetailDTO.setModified(new Date());
                applyDetailDTO.setModifier(DEFAULT_IMPORT_USER);
                String authCode = applyDTO.getAuthCode();
                String callAppCode = applyDTO.getCallAppCode();
                String key = appCode + "_" + callAppCode + "_" + authCode;
                String ticketId = EncodeUtils.getMD5_16(key);
                applyDetailDTO.setTicketId(ticketId);
                authApplyDetailList.add(applyDetailDTO);
            }
        }

        List<HttpAuthApplyDetailDTO> applyDetailFailList = new ArrayList<>();
        List<HttpAuthApplyDetailDTO> applyDetailSuccessList = new ArrayList<>();
        for (HttpAuthApplyDetailDTO applyDetailDTO : authApplyDetailList) {
            //校验提供方应用是否一致
            if (!appCode.equals(applyDetailDTO.getAppCode())) {
                applyDetailDTO.setFailMsg("提供方应用不一致！");
                applyDetailFailList.add(applyDetailDTO);
                continue;
            }

            //校验数据必填项
            if (StringUtils.isBlank(applyDetailDTO.getCallAppCode())
                    || StringUtils.isBlank(applyDetailDTO.getAppCode())
                    || StringUtils.isBlank(applyDetailDTO.getAuthCode())
                    || applyDetailDTO.getInterfaceId() == null
                    || StringUtils.isBlank(applyDetailDTO.getPath())
                    || StringUtils.isBlank(applyDetailDTO.getMethodName())) {
                applyDetailDTO.setFailMsg("必填项为空！");
                applyDetailFailList.add(applyDetailDTO);
                continue;
            }

            //补充提供方应用名称
            if (appInfo != null && StringUtils.isNotBlank(appInfo.getAppName())) {
                applyDetailDTO.setAppName(appInfo.getAppName());
            }
            //补充调用方应用名称
            AppInfo callAppInfo = appInfoMap.get(applyDetailDTO.getCallAppCode());
            if (callAppInfo == null) {
                callAppInfo = appInfoService.findApp(applyDetailDTO.getCallAppCode());
                if (callAppInfo == null) {
                    applyDetailDTO.setFailMsg("调用方应用不存在！");
                    applyDetailFailList.add(applyDetailDTO);
                    continue;
                }
                appInfoMap.put(applyDetailDTO.getCallAppCode(), callAppInfo);
            }
            if (callAppInfo != null && StringUtils.isNotBlank(callAppInfo.getAppName())) {
                applyDetailDTO.setCallAppName(callAppInfo.getAppName());
            }

            //校验申请项目是否存在
            InterfaceManage interfaceManage = interfaceManageService.getById(applyDetailDTO.getInterfaceId());
            if (interfaceManage == null) {
                applyDetailDTO.setFailMsg("申请项目ID不存在！");
                applyDetailFailList.add(applyDetailDTO);
                continue;
            }
            applyDetailDTO.setInterfaceCode(interfaceManage.getServiceCode());
            applyDetailDTO.setInterfaceName(interfaceManage.getName());

            //校验接口是否存在
            LambdaQueryWrapper<MethodManage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MethodManage::getInterfaceId, applyDetailDTO.getInterfaceId())
                    .eq(MethodManage::getPath, applyDetailDTO.getPath())
                    .eq(MethodManage::getName, applyDetailDTO.getMethodName())
                    .eq(MethodManage::getYn, DataYnEnum.VALID.getCode());
            log.info("#methodManageService.getOne.request={}", JsonUtils.toJSONString(queryWrapper));
            MethodManage methodManage = methodManageService.getOne(queryWrapper);
            log.info("#methodManageService.getOne.result={}", JsonUtils.toJSONString(methodManage));
            if (methodManage == null) {
                applyDetailDTO.setFailMsg("申请接口不存在！");
                applyDetailFailList.add(applyDetailDTO);
                continue;
            }
            applyDetailDTO.setMethodId(methodManage.getId());
            applyDetailDTO.setMethodCode(methodManage.getMethodCode());

            //校验是否已存在鉴权申请
            QueryHttpAuthApplyDetailReqDTO queryDTO = new QueryHttpAuthApplyDetailReqDTO();
            queryDTO.setAppCode(applyDetailDTO.getAppCode());
            queryDTO.setCallAppCode(applyDetailDTO.getCallAppCode());
            queryDTO.setSite(SiteEnum.China.getCode());
            queryDTO.setAuthCode(applyDetailDTO.getAuthCode());
            queryDTO.setInterfaceId(applyDetailDTO.getInterfaceId());
            queryDTO.setMethodName(applyDetailDTO.getMethodName());
            queryDTO.setPath(applyDetailDTO.getPath());
            log.info("#httpAuthApplyDetailMapper.queryListCount.request={}", JsonUtils.toJSONString(queryDTO));
            long count = NumberUtils.toLong(httpAuthApplyDetailMapper.queryListCount(queryDTO));
            log.info("#httpAuthApplyDetailMapper.queryListCount.result={}", count);
            if (count > 0) {
                LambdaQueryWrapper<HttpAuthApplyDetail> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(HttpAuthApplyDetail::getAppCode, applyDetailDTO.getAppCode());
                deleteWrapper.eq(HttpAuthApplyDetail::getCallAppCode, applyDetailDTO.getCallAppCode());
                deleteWrapper.eq(HttpAuthApplyDetail::getYn, DataYnEnum.VALID.getCode());
                deleteWrapper.eq(HttpAuthApplyDetail::getSite,applyDetailDTO.getSite());
                deleteWrapper.eq(HttpAuthApplyDetail::getAuthCode,applyDetailDTO.getAuthCode());
                deleteWrapper.eq(HttpAuthApplyDetail::getMethodId,applyDetailDTO.getMethodId());
                deleteWrapper.eq(HttpAuthApplyDetail::getInterfaceId,applyDetailDTO.getInterfaceId());
                httpAuthApplyDetailMapper.delete(deleteWrapper);
//                applyDetailDTO.setFailMsg("数据已存在！");
//                applyDetailFailList.add(applyDetailDTO);
//                continue;
            }

            String userKey = applyDetailDTO.getCallAppCode()+"_"+applyDetailDTO.getAppCode()+"_"+applyDetailDTO.getAuthCode();
            String creator = authCodeUserMap.get(userKey);
            applyDetailDTO.setCreator(creator);
            applyDetailDTO.setCreated(new Date());
            applyDetailDTO.setModified(new Date());
            applyDetailDTO.setModifier(DEFAULT_IMPORT_USER);
            applyDetailDTO.setYn(DataYnEnum.VALID.getCode());
            applyDetailDTO.setSite(SiteEnum.China.getCode());
            String authCode = applyDetailDTO.getAuthCode();
            String callAppCode = applyDetailDTO.getCallAppCode();
            String key = appCode + "_" + callAppCode + "_" + authCode;
            String ticketId = EncodeUtils.getMD5_16(key);
            applyDetailDTO.setTicketId(ticketId);
            boolean success = httpAuthApplyDetailService.saveApplyDetailDTO(applyDetailDTO);
            if (success) {
                applyDetailSuccessList.add(applyDetailDTO);
            } else {
                applyDetailDTO.setFailMsg("保存db失败，请稍后再试！");
                applyDetailFailList.add(applyDetailDTO);
            }
        }
        applyResultDTO.setApplyFailList(applyFailList);
        applyResultDTO.setApplySuccessList(applySuccessList);
        applyResultDTO.setApplyDetailFailList(applyDetailFailList);
        applyResultDTO.setApplyDetailSuccessList(applyDetailSuccessList);

        log.info("#saveApplyDetailDTO.result= {}", JsonUtils.toJSONString(applyResultDTO));
        return applyResultDTO;
    }


    /**
     * 提交xbp
     *
     * @param applyDTO
     * @return
     */
    private Integer submitXbp(HttpAuthApplyDTO applyDTO) {
        HttpAuthApplyXbpParam xbpParam = new HttpAuthApplyXbpParam();
        //设置审批人
        xbpParam.setProductApprovers(applyDTO.getProductApprovers());
        xbpParam.setDevApprovers(applyDTO.getDevApprovers());
        //设置申请人
        xbpParam.setApplyUser(UserSessionLocal.getUser().getUserId());
        //设置接口所属应用
        String appInfo = NumberUtils.toString(applyDTO.getAppName()) + "(" + applyDTO.getAppCode() + ")";
        xbpParam.setAppInfo(appInfo);
        //设置调用应用
        String callAppInfo = NumberUtils.toString(applyDTO.getCallAppName()) + "(" + applyDTO.getCallAppCode() + ")";
        xbpParam.setCallAppInfo(callAppInfo);
        //申请原因
        xbpParam.setApplyDesc(applyDTO.getApplyDesc());

        List<HttpAuthApplyDetailXbpParam> methodInfoList = new ArrayList();
        if (CollectionUtils.isNotEmpty(applyDTO.getInterfaceList())) {
            for (HttpAuthApplyDetailDTO interfaceDTO : applyDTO.getInterfaceList()) {
                StringBuffer interfaceBuffer = new StringBuffer();
                interfaceBuffer.append(interfaceDTO.getInterfaceName());
                if (StringUtils.isNotBlank(interfaceDTO.getInterfaceName())) {
                    interfaceBuffer.append("(" + interfaceDTO.getInterfaceCode() + ")");
                }
                for (HttpAuthApplyDetailDTO methodDTO : interfaceDTO.getMethodList()) {
                    HttpAuthApplyDetailXbpParam detailXbpParam = new HttpAuthApplyDetailXbpParam();
                    //接口名称
                    StringBuffer methodBuffer = new StringBuffer();
                    methodBuffer.append(methodDTO.getMethodName());
                    if (StringUtils.isNotBlank(methodDTO.getMethodCode())) {
                        methodBuffer.append("(" + methodDTO.getMethodCode() + ")");
                    }
                    detailXbpParam.setMethodInfo(methodBuffer.toString());
                    //路径
                    detailXbpParam.setPath(methodDTO.getPath());
                    //项目名称
                    detailXbpParam.setInterfaceInfo(interfaceBuffer.toString());
                    //鉴权标识
                    detailXbpParam.setAuthCode(methodDTO.getAuthCode());
                    methodInfoList.add(detailXbpParam);
                }
            }
            xbpParam.setMethodInfoList(methodInfoList);
        }
        log.info("#submitTicket.request={}", JSON.toJSONString(xbpParam));
        Integer ticketId = xbpService.submitTicket(xbpParam);
        log.info("#submitTicket.result={}", ticketId);
        applyDTO.setTicketId(NumberUtils.toString(ticketId));
        return ticketId;
    }

    /**
     * 添加申请单明细
     *
     * @param applyDetailDTO
     * @return
     */
    private int addAuthApplyDetail(HttpAuthApplyDetailDTO applyDetailDTO) {
        HttpAuthApplyDetail authApplyDetail = new HttpAuthApplyDetail();
        BeanUtils.copyProperties(applyDetailDTO, authApplyDetail);
        authApplyDetail.setAppCode(applyDetailDTO.getAppCode());
        authApplyDetail.setAppName(applyDetailDTO.getAppName());
        authApplyDetail.setCallAppCode(applyDetailDTO.getCallAppCode());
        authApplyDetail.setCallAppName(applyDetailDTO.getCallAppName());
        authApplyDetail.setAuthCode(applyDetailDTO.getAuthCode());
        authApplyDetail.setSite(applyDetailDTO.getSite());
        authApplyDetail.setId(null);
        authApplyDetail.setCreator(UserSessionLocal.getUser().getUserId());
        authApplyDetail.setModifier(UserSessionLocal.getUser().getUserId());
        authApplyDetail.setCreated(new Date());
        authApplyDetail.setModified(new Date());
        authApplyDetail.setYn(DataYnEnum.VALID.getCode());
        int authApplyDetailCount = httpAuthApplyDetailMapper.insert(authApplyDetail);
        return authApplyDetailCount;
    }

    /**
     * 添加鉴权标签申请
     *
     * @param addApplyDTOs
     * @return
     */
    private boolean addAuthApplyList(List<HttpAuthApplyDTO> addApplyDTOs) {
        if (CollectionUtils.isEmpty(addApplyDTOs)) {
            return false;
        }
        String userId = UserSessionLocal.getUser().getUserId();
        Date date = new Date();
        for (HttpAuthApplyDTO applyDTO : addApplyDTOs) {
            HttpAuthApply authApply = new HttpAuthApply();
            BeanUtils.copyProperties(applyDTO, authApply);
            authApply.setId(null);
            authApply.setCreator(userId);
            authApply.setModifier(userId);
            authApply.setCreated(date);
            authApply.setModified(date);
            authApply.setYn(DataYnEnum.VALID.getCode());
            httpAuthApplyMapper.insert(authApply);
        }
        return true;
    }

    /**
     * 检查提交参数
     *
     * @param applyParamDTO
     */
    private void checkSubmitParam(HttpAuthApplyParamDTO applyParamDTO) {
        Guard.notNull(applyParamDTO, "入参不能为空！");
        Guard.notNull(applyParamDTO.getApplyDesc(), "描述参数 applyDesc 不能为空！");
        Guard.notNull(applyParamDTO.getAuthApplyList(), "申请参数 authApplyList 不能为空！");
        Map<String, AppInfo> appInfoMap = new HashMap<>();
        for (HttpAuthApplyDTO applyDTO : applyParamDTO.getAuthApplyList()) {
            Guard.notNull(applyDTO.getSite(), "站点参数 site 不能为空！");
            Guard.notNull(applyDTO.getAppCode(), "应用编码参数 appCode 不能为空！");
            Guard.notNull(applyDTO.getCallAppCode(), "调用方应用参数 callAppCode 不能为空！");
            Guard.assertTrue(CollectionUtils.isNotEmpty(applyDTO.getInterfaceList()), "申请鉴权标识（" + applyDTO.getAuthCode() + "）失败，原因是interfaceList参数为空");
            //设置申请原因
            applyDTO.setApplyDesc(applyParamDTO.getApplyDesc());
            for (HttpAuthApplyDetailDTO interfaceDTO : applyDTO.getInterfaceList()) {
                Guard.notNull(interfaceDTO.getInterfaceId(), "接口ID参数 interfaceId 不能为空！");
                Guard.notNull(interfaceDTO.getInterfaceCode(), "接口编码参数 interfaceCode 不能为空！");
                Guard.assertTrue(CollectionUtils.isNotEmpty(interfaceDTO.getMethodList()), "方法参数列表 methodList 不能为空！");
                interfaceDTO.setAuthCode(applyDTO.getAuthCode());

                for (HttpAuthApplyDetailDTO methodDTO : interfaceDTO.getMethodList()) {
                    Guard.notNull(methodDTO.getMethodId(), "方法ID参数 methodId 不能为空！");
//                    Guard.notNull(methodDTO.getMethodCode(), "方法编码参数 methodCode 不能为空！");
                    Guard.notNull(methodDTO.getPath(), "请求路径参数 path 不能为空！");
                    Guard.notNull(methodDTO.getAuthCode(), "鉴权标识参数 authCode 不能为空！");
                    methodDTO.setSite(applyDTO.getSite());
                    methodDTO.setAppCode(applyDTO.getAppCode());
                    methodDTO.setAppName(applyDTO.getAppName());
                    methodDTO.setCallAppCode(applyDTO.getCallAppCode());
                    methodDTO.setCallAppName(applyDTO.getCallAppName());
                    methodDTO.setInterfaceId(interfaceDTO.getInterfaceId());
                    methodDTO.setInterfaceCode(interfaceDTO.getInterfaceCode());
                    methodDTO.setInterfaceName(interfaceDTO.getInterfaceName());
                }
            }

            //设置当前审批人
            AppInfo appInfo = appInfoMap.get(applyDTO.getAppCode());
            if (appInfo == null) {
                appInfo = appInfoService.findApp(applyDTO.getAppCode());
                if (appInfo != null) {
                    appInfoMap.put(appInfo.getAppCode(), appInfo);
                }
            }
            AppInfo callAppInfo = appInfoMap.get(applyDTO.getCallAppCode());
            if (callAppInfo == null) {
                callAppInfo = appInfoService.findApp(applyDTO.getCallAppCode());
                if (callAppInfo != null) {
                    appInfoMap.put(callAppInfo.getAppCode(), callAppInfo);
                }
            }

            Guard.notNull(appInfo, "申请应用（" + applyDTO.getAppCode() + "）未配置审批人！");
            Guard.notNull(appInfo.getMembers(), "申请应用（" + applyDTO.getAppCode() + "）未配置审批人！");
            applyDTO.setDevApprovers(UserUtils.getDevMembers(appInfo.getMembers()));
            applyDTO.setProductApprovers(UserUtils.getProductMembers(appInfo.getMembers()));
            applyDTO.setCallAppName(callAppInfo.getAppName());
            applyDTO.setAppName(appInfo.getAppName());
        }
    }


    /**
     * 转化为DTO
     *
     * @param entity
     * @return
     */
    private HttpAuthApplyDTO toDTO(HttpAuthApply entity) {
        if (entity == null) {
            return null;
        }
        HttpAuthApplyDTO dto = new HttpAuthApplyDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 转化为实体对象
     *
     * @param dto
     * @return
     */
    private HttpAuthApply toEntity(HttpAuthApplyDTO dto) {
        if (dto == null) {
            return null;
        }
        HttpAuthApply entity = new HttpAuthApply();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }


    /**
     * 转化为DTO
     *
     * @param records
     * @return
     */
    private List<HttpAuthApplyDTO> toDTOList(List<HttpAuthApply> records) {
        //转化为DTO
        return Optional.ofNullable(records).orElse(new ArrayList<>()).stream().map(v -> {
            return toDTO(v);
        }).collect(Collectors.toList());
    }

}
