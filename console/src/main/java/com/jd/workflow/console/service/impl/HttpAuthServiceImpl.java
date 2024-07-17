package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.dao.mapper.HttpAuthMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.mail.SendMailService;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.console.utils.UserUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
@Service("cjgHttpAuthService")
@Slf4j
public class HttpAuthServiceImpl extends ServiceImpl<HttpAuthMapper, HttpAuth> implements IHttpAuthService {

    /**
     * 鉴权管理
     */
    @Resource
    private HttpAuthMapper httpAuthMapper;

    /**
     * 补充用户名称
     */
    @Autowired
    private UserHelper userHelper;

    /**
     * 鉴权明细管理
     */
    @Resource
    private IHttpAuthDetailService httpAuthDetailService;

    @Resource
    InterfaceFollowListService interfaceFollowListService;

    @Resource
    SendMailLimiter sendMailLimiter;
    /**
     * 邮件发送服务
     */
    @Resource
    private SendMailService sendMailService;

    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    IMemberRelationService relationService;

    /**
     * 新增app
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthDTO> queryListPage(QueryHttpAuthReqDTO queryDTO) {
        //分页处理
        Page<HttpAuthDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        long total = NumberUtils.toLong(httpAuthMapper.queryListCount(queryDTO));
        page.setTotal(total);
        if (total > 0) {
            List<HttpAuth> records = httpAuthMapper.queryList(queryDTO);
            //转化为DTO
            List<HttpAuthDTO> recordDTOs = toDTOs(records);
            //设置应用负责人信息
            setOwnerInfo(recordDTOs);
            page.setRecords(recordDTOs);
        }
        return page;
    }


    /**
     * 设置应用负责人信息
     *
     * @param recordDTOs
     */
    private void setOwnerInfo(List<HttpAuthDTO> recordDTOs) {
        if (CollectionUtils.isEmpty(recordDTOs)) {
            return;
        }
        Map<String, String> ownerMap = new HashMap<>();
        for (HttpAuthDTO authDTO : recordDTOs) {
            List<String> ownerList = UserUtils.getOwners(authDTO.getMembers());
            if (CollectionUtils.isNotEmpty(ownerList)) {
                String ownerErp = ownerList.get(0);
                String ownerName = ownerMap.get(ownerErp);
                if (StringUtils.isNotBlank(ownerName)) {
                    authDTO.setOwnerErp(ownerErp);
                    authDTO.setOwnerName(ownerName);
                    continue;
                }
                UserVo userVo = userHelper.getUserBaseInfoByUserName(ownerList.get(0));
                if (userVo != null) {
                    authDTO.setOwnerErp(ownerErp);
                    authDTO.setOwnerName(userVo.getRealName());
                    ownerMap.put(ownerErp, userVo.getRealName());
                }
            }
        }
    }

    /**
     * 查询列表
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthDTO> queryListGroupByAppAndSite(QueryHttpAuthReqDTO queryDTO) {
        List<HttpAuth> records = httpAuthMapper.queryListGroupByAppAndSite(queryDTO);
        //转化为DTO
        List<HttpAuthDTO> recordDTOs = toDTOs(records);
        //设置应用负责人信息
        setOwnerInfo(recordDTOs);
        return recordDTOs;
    }

    /**
     * 上报鉴权标识列表
     *
     * @param reportAuthDTOs
     * @param reportAuthDetailDTOs
     */
    @Override
    public void reportHttpAuth(List<HttpAuthDTO> reportAuthDTOs, List<HttpAuthDetailDTO> reportAuthDetailDTOs,
                               AppInfo appInfo, Long interfaceId) {
        if (CollectionUtils.isEmpty(reportAuthDTOs) || CollectionUtils.isEmpty(reportAuthDetailDTOs)
                || appInfo == null || interfaceId==null) {
            log.info("#reportHttpAuth 上报数据异常！");
            return;
        }

        QueryHttpAuthReqDTO queryAuthDTO = new QueryHttpAuthReqDTO();
        queryAuthDTO.setAppCode(appInfo.getAppCode());
        queryAuthDTO.setSite(SiteEnum.China.getCode());
        List<HttpAuth> authList = httpAuthMapper.queryAllList(queryAuthDTO);


        QueryHttpAuthDetailReqDTO queryAuthDetailDTO = new QueryHttpAuthDetailReqDTO();
        queryAuthDetailDTO.setAppCode(appInfo.getAppCode());
        queryAuthDetailDTO.setSite(SiteEnum.China.getCode()); //默认主站
        queryAuthDetailDTO.setInterfaceId(interfaceId);
        List<HttpAuthDetail> authDetailList = httpAuthDetailService.queryAllSourceList(queryAuthDetailDTO);

        //邮件通知变更的鉴权标签
        List<HttpAuthDetailDTO> changeAuthDetailDTOs = new ArrayList();
        for (HttpAuthDetailDTO authDetailDTO : reportAuthDetailDTOs) { // code1 code2
            String reportAppCode = authDetailDTO.getAppCode();
            String reportAuthCode = authDetailDTO.getAuthCode();
            String reportPath = authDetailDTO.getPath();
            for (HttpAuthDetail authDetail : authDetailList) { // code1 code2
                String appCode = authDetail.getAppCode();
                String authCode = authDetail.getAuthCode();
                String path = authDetail.getPath();
                //设置老的鉴权标识
                authDetailDTO.setOldAuthCode(authCode);
                if (reportAppCode.equals(appCode) && reportPath.equals(path)) {
                    if (StringUtils.isNotBlank(reportAuthCode) && StringUtils.isNotBlank(authCode)
                            && !reportAuthCode.equals(authCode)) { //code1 code2
                        changeAuthDetailDTOs.add(authDetailDTO);
                    }
                    break;
                }
            }
        }

        //发送变更鉴权标识邮件通知
        sendMail(appInfo, changeAuthDetailDTOs);

        //删除鉴权明细列表，再增加新的鉴权明细列表
        boolean removeAuthDetailResult = httpAuthDetailService.removeBatch(authDetailList);
        if (removeAuthDetailResult) {
            httpAuthDetailService.saveBatch(reportAuthDetailDTOs);
        }

        //删除鉴权列表，再增加新的鉴权列表
        Iterator<HttpAuth> iterator = authList.iterator();
        while (iterator.hasNext()){
            HttpAuth auth = iterator.next();
            QueryHttpAuthDetailReqDTO authDetailDTO = new QueryHttpAuthDetailReqDTO();
            authDetailDTO.setAppCode(appInfo.getAppCode());
            authDetailDTO.setSite(SiteEnum.China.getCode()); //默认主站
            authDetailDTO.setNotInterfaceId(interfaceId); //查询其他接口
            authDetailDTO.setAuthCode(auth.getAuthCode());
            Long count = httpAuthDetailService.queryListCount(authDetailDTO);
            //判断该应用下是否存在相同的鉴权标识，如果存在不能删除
            if (NumberUtils.toLong(count) > 0){
                iterator.remove();
            }
        }
        //删除废弃的鉴权标识
        if (CollectionUtils.isNotEmpty(authList)){
            boolean removeAuthResult = removeBatch(authList);
            if (!removeAuthResult) {
                log.error("#reportHttpAuth removeBatch 应用鉴权标识数据异常！");
            }else {
                log.info("#reportHttpAuth removeBatch 应用鉴权标识成功");
            }
        }
        batchSaveAuthOrUpdate(reportAuthDTOs);
    }



    /**
     * 发送邮件
     * @param appInfo
     * @param changeAuthDetailDTOs
     */
    private void sendMail(AppInfo appInfo, List<HttpAuthDetailDTO> changeAuthDetailDTOs) {
        try {
            if (CollectionUtils.isNotEmpty(changeAuthDetailDTOs)) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("authDetailList", changeAuthDetailDTOs);
                dataMap.put("appInfo", appInfo);
                // 发送邮件提醒
                String htmlText = sendMailService.getHtmlText("post_msg.html", dataMap);
                // 接收最终邮件的地址信息
                List<String> erpList = UserUtils.splitMembers(appInfo.getMembers());
                log.info("#sendMail.erpList={}",erpList);
                String[] to = getEmailList(erpList);
                log.info("#sendMail.to={}",to);
                if (to!=null && to.length>0){
                    sendMailService.send(to, "【在线联调-接口上报-鉴权标识调整提醒】", htmlText);
                }
            }
        }catch (Exception e){
            log.error("#sendMail.error",e);
        }
    }

    /**
     * 设置应用负责人信息
     *
     * @param erpList
     */
    private String[] getEmailList(Collection<String> erpList) {
        String[] result = new String[]{};

        try {
            if (CollectionUtils.isEmpty(erpList)) {
                return result;
            }
            List<String> emailList = new ArrayList<>();
            Map<String, UserVo> erpMap = new HashMap<>();
            for (String erp : erpList) {
                if (erpMap.get(erp) == null) {
                    UserVo userVo = userHelper.getUserBaseInfoByUserName(erp);
                    if (userVo != null && StringUtils.isNotBlank(userVo.getEmail())) {
                        emailList.add(userVo.getEmail());
                        erpMap.put(erp, userVo);
                    }
                }
            }
            result = emailList.toArray(new String[emailList.size()]);
        }catch (Exception e){
            log.error("#getEmailList.error ={}",e);
        }
        return result;
    }
    public void sendInterfaceManageChangeNotice(InterfaceManage manage){
        try {
            boolean canSend = sendMailLimiter.canSend("interface:"+manage.getId());
            if(!canSend){
                return ;
            }
            AppInfo appInfo = appInfoService.getById(manage.getAppId());
            Set<String> members = new HashSet<>();
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("manage", manage);
                // 发送邮件提醒
                String htmlText = sendMailService.getHtmlText("interface_change.html", dataMap);
                // 接收最终邮件的地址信息
                List<String> erpList = UserUtils.splitMembers(appInfo.getMembers());
                members.addAll(erpList);
                members.addAll(interfaceFollowListService.getInterfaceFollowUser(manage.getId()));
                MemberRelationDTO dto = new MemberRelationDTO();
                dto.setResourceId(manage.getId());

                List<Integer> resourceRoleList = new ArrayList<>();
            for (ResourceRoleEnum value : ResourceRoleEnum.values()) {
                resourceRoleList.add(value.getCode());
            }
                dto.setResourceRoleList(resourceRoleList);
                dto.setResourceType(ResourceTypeEnum.INTERFACE.getCode());

                List<String> userCodes = relationService.listUserCodeByResource(dto);
                members.addAll(userCodes);
                log.info("#sendMail.erpList={}",members);
                String[] to = getEmailList(members);
                //String[] to = new String[]{"wangjingfang3@jd.com"};
                log.info("#sendMail.to={}",to);
                if (to!=null && to.length>0){
                    sendMailService.send(to, "【在线联调-接口变更通知-"+manage.getName()+"】", htmlText);
                }

        }catch (Exception e){
            log.error("interface#sendMail.error",e);
        }
    }
    /**
     * 批量添加鉴权标识列表
     *
     * @param authDTOS
     */
    @Override
    public boolean batchSaveAuthOrUpdate(List<HttpAuthDTO> authDTOS) {
        if (CollectionUtils.isEmpty(authDTOS)) {
            return false;
        }
        List<HttpAuth> authList = toEntitys(authDTOS);
        log.info("#HttpAuthServiceImpl.batchSaveAuthOrUpdate.request= {}", JsonUtils.toJSONString(authList));

        for (HttpAuth auth : authList){
            QueryHttpAuthReqDTO queryAuthDTO = new QueryHttpAuthReqDTO();
            queryAuthDTO.setAppCode(auth.getAppCode());
            queryAuthDTO.setSite(SiteEnum.China.getCode());
            queryAuthDTO.setAuthCode(auth.getAuthCode());
            Long count = httpAuthMapper.queryListCount(queryAuthDTO);
            if (NumberUtils.toLong(count)==0){
                boolean success = save(auth);
                log.info("#HttpAuthServiceImpl.batchSaveAuthOrUpdate.save= {}", success);
            }
        }
        return true;
    }


    /**
     * 批量删除
     *
     * @param authList
     * @return
     */
    @Override
    public boolean removeBatch(List<HttpAuth> authList) {
        if (CollectionUtils.isEmpty(authList)) {
            return true;
        }
        List<Long> idList = toIdList(authList);
        log.info("#HttpAuthServiceImpl.removeBatch.idList= {}", idList);
        boolean success = removeByIds(idList);
        log.info("#HttpAuthServiceImpl.removeBatch.result= {}", success);
        return success;
    }

    /**
     * 转化为
     *
     * @param httpAuthList
     */
    private List<HttpAuthDTO> toDTOs(List<HttpAuth> httpAuthList) {
        //转化为DTO
        List<HttpAuthDTO> dtos = Optional.ofNullable(httpAuthList).orElse(new ArrayList<>()).stream().map(v -> {
            return toDTO(v);
        }).collect(Collectors.toList());

        return dtos;
    }

    /**
     * 转化为
     *
     * @param httpAuthDTOList
     */
    private List<HttpAuth> toEntitys(List<HttpAuthDTO> httpAuthDTOList) {
        //转化为DTO
        List<HttpAuth> dtos = Optional.ofNullable(httpAuthDTOList).orElse(new ArrayList<>()).stream().map(v -> {
            return toEntity(v);
        }).collect(Collectors.toList());

        return dtos;
    }

    /**
     * 转化为DTO
     *
     * @param auth
     * @return
     */
    private HttpAuthDTO toDTO(HttpAuth auth) {
        if (auth == null) {
            return null;
        }
        HttpAuthDTO authDTO = new HttpAuthDTO();
        authDTO.setAppId(auth.getAppCode());
        BeanUtils.copyProperties(auth, authDTO);
        return authDTO;
    }

    /**
     * 转化为实体对象
     *
     * @param authDTO
     * @return
     */
    private HttpAuth toEntity(HttpAuthDTO authDTO) {
        if (authDTO == null) {
            return null;
        }
        HttpAuth auth = new HttpAuth();
        BeanUtils.copyProperties(authDTO, auth);
        return auth;
    }

    /**
     * 转化为ID列表
     *
     * @param records
     * @return
     */
    private List<Long> toIdList(List<HttpAuth> records) {
        //转化为DTO
        return Optional.ofNullable(records).orElse(new ArrayList<>()).stream().map(v -> {
            return v.getId();
        }).collect(Collectors.toList());
    }

}
