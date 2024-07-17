package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.cjg.acc.client.entity.po.UserTenantInfo;
import com.jd.cjg.flow.sdk.model.result.PageResult;
import com.jd.common.web.LoginContext;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceSimpleTypeEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dao.mapper.ApprovalManageMapper;
import com.jd.workflow.console.dto.ApprovalPageQuery;
import com.jd.workflow.console.dto.ApproveCreateDTO;
import com.jd.workflow.console.dto.jingme.ButtonDTO;
import com.jd.workflow.console.dto.jingme.CustomDTO;
import com.jd.workflow.console.dto.jingme.TemplateMsgDTO;
import com.jd.workflow.console.entity.ApproveManage;
import com.jd.workflow.console.service.IApproveService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.jingme.SendMsgService;
import com.jd.workflow.console.service.role.AccUserServiceAdapter;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApproveServiceImpl extends ServiceImpl<ApprovalManageMapper, ApproveManage> implements IApproveService {

    private static final Integer accept=1;
    private static final Integer reject=2;

    @Resource
    private ApprovalManageMapper approvalManageMapper;
    @Resource
    private SendMsgService sendMsgService;

    @Autowired
    private AccUserServiceAdapter accUserServiceAdapter;

    @Resource
    private IInterfaceManageService interfaceManageService;

    @Resource
    private IMethodManageService methodManageService;
    @Resource
    private ScheduledExecutorService defaultScheduledExecutor;
    @Override
    public Page<ApproveManage> pageList(ApprovalPageQuery req) {
        final LambdaQueryWrapper<ApproveManage> lqw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(req.getName())) {
            lqw.like(ApproveManage::getName, req.getName());
        }
        if (StringUtils.isNotBlank( req.getContributor())) {
            lqw.like(ApproveManage::getContributor, req.getContributor());
        }
        if (req.getStatus() !=null) {
            lqw.eq(ApproveManage::getStatus, req.getStatus());
        }
        lqw.eq(ApproveManage::getYn, DataYnEnum.VALID.getCode());
        lqw.orderByDesc(ApproveManage::getModified);
        lqw.orderByDesc(ApproveManage::getId);
        return page(initPage(req.getCurrent(), req.getSize(), 1000L), lqw);
    }


    /**
     * 创建审批
     * @param approveCreateDTO
     */
    @Override
    public Boolean createApprove(ApproveCreateDTO approveCreateDTO) {
        //增加审批信息
        Guard.notEmpty(approveCreateDTO.getName(), "接口名称不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(approveCreateDTO.getAppCode(), "关联应用不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(approveCreateDTO.getDocLink(), "文档链接不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        ApproveManage newApproveManage = new ApproveManage();
        BeanUtils.copyProperties(approveCreateDTO,newApproveManage);
        newApproveManage.setYn(1);
        newApproveManage.setCreator(LoginContext.getLoginContext().getPin());
        Integer res=approvalManageMapper.insert(newApproveManage);
        //创建审批完成，发送消息
        if(res>0){
            TemplateMsgDTO dto = getAllSendMsg("云文档贡献审批提交","@"+newApproveManage.getContributor()+"贡献了接口云文档",newApproveManage,true);
            Set<String> erps = getErps(newApproveManage,true);
            for (String erp : erps) {
                sendMsgService.sendUserJueMsg(erp, dto);
            }
        }else{
            return false;
        }
        return true;
    }



    /**
     * 审批失败
     * @param id
     */
    @Override
    public Boolean acceptApprove(Long id) {
        //更新审批状态为已通过
        ApproveManage manage = getById(id);
        if(null==manage){
            return false;
        }
        LambdaUpdateWrapper<ApproveManage> wrpper=new LambdaUpdateWrapper<>();
        wrpper.eq(ApproveManage::getId,id);
        wrpper.set(ApproveManage::getStatus,accept);
        wrpper.set(ApproveManage::getModifier,LoginContext.getLoginContext().getPin());
        approvalManageMapper.update(null,wrpper);
        //接口更新
        Boolean res=true;
        if (null == manage.getType() || InterfaceSimpleTypeEnum.Interface.getCode() == manage.getType()) {
            res=interfaceManageService.updateCloudFile(manage.getSourceId(),manage.getDocLink(),manage.getContents());
            log.info("更新云文档完成{}",manage.getDocLink());
        }else if(InterfaceSimpleTypeEnum.method.getCode()== manage.getType()) {
            res=methodManageService.updateCloudFile(manage.getSourceId(),manage.getDocLink(),manage.getContents());
        }

        if(res){
            //发送消息
            TemplateMsgDTO dto = getSendMsg("云文档贡献成功","@"+manage.getContributor()+"贡献了接口云文档",manage);
            Set<String> erps = getErps(manage,false);
            List<Future> futures=new ArrayList<>();
            for (String erp : erps) {
                Future future = defaultScheduledExecutor.submit(() -> {
                    sendMsgService.sendUserJueMsg(erp, dto);
                });
                futures.add(future);
            }
            return true;
//            for (Future future : futures) {
//                try {
//                    future.get();
//                } catch (Exception e) {
//                    log.error("发送消息失败:erps={}", erps, e);
//                }
//            }
        }else{
            return false;
        }


    }

    /**
     * 审批通过
     * @param id
     */
    @Override
    public Boolean rejectApprove(Long id) {
        ApproveManage manage = getById(id);
        if(null==manage){
            return false;
        }
        //更新审批状态为未通过
        manage.setStatus(reject);
        manage.setModifier(LoginContext.getLoginContext().getPin());
        Integer res= approvalManageMapper.updateById(manage);
        if(res>0){
            //驳回发消息
            TemplateMsgDTO dto = getSendMsg("云文档贡献失败","@"+manage.getContributor()+"贡献的接口云文档未通过审核",manage);
            Set<String> erps = getErps(manage,false);
            for (String erp : erps) {
                sendMsgService.sendUserJueMsg(erp, dto);
            }
        }else{
            return false;
        }
        return true;
    }


    @Override
    public Boolean removeApprove(Long id) {
        ApproveManage removeEntity = new ApproveManage();
        removeEntity.setId(id);
        removeEntity.setYn(DataYnEnum.INVALID.getCode());
        removeEntity.setModifier(LoginContext.getLoginContext().getPin());
        int update = approvalManageMapper.updateById(removeEntity);
        return update > 0;
    }

    @Override
    public ApproveCreateDTO converManage(Long id, String serviceCode, Integer serviceType, Long appId, String appCode, String path, Boolean noNeedApprove, String contents) {
        ApproveCreateDTO manage=new ApproveCreateDTO();
        manage.setSourceId(id);
        manage.setName(serviceCode);
        manage.setAppCode(appCode);
        manage.setAppId(appId);
        manage.setDocLink(path);
        manage.setStatus(noNeedApprove?1:0);
        manage.setContents(contents);
        manage.setType(serviceType);
        manage.setContributor(LoginContext.getLoginContext().getPin());
        return manage;
    }


    private Set<String> getErps(ApproveManage manage,Boolean approve) {
        Set<String> erps = new HashSet<String>();
        //贡献人、japi管理员
        if(!approve){
            erps.add(manage.getContributor());
            erps.add(manage.getCreator());
        }
        //获取japi管理员
        PageResult<UserTenantInfo> infos = accUserServiceAdapter.getUsersByChannel(UpLoginContextHelper.getUserPin(), 659L);
        List<String> japiAdmin = infos.getData().stream().map(UserTenantInfo::getUserName).collect(Collectors.toList());
        erps.addAll(japiAdmin);
        return erps;


    }

    private TemplateMsgDTO getSendMsg(String msg,String subMsg,ApproveManage manage){
        return getAllSendMsg(msg,subMsg,manage,false);
    }

    private TemplateMsgDTO getAllSendMsg(String msg,String subMsg,ApproveManage manage,Boolean approve){
        TemplateMsgDTO dto=new TemplateMsgDTO();
        List<CustomDTO> customDTOList = Lists.newArrayList();
        CustomDTO customDTO = new CustomDTO();
        customDTO.setName("接口名称:");
        customDTO.setDescription(manage.getName());
        CustomDTO customDTO2 = new CustomDTO();
        customDTO2.setName("云文档链接:");
        customDTO2.setDescription(manage.getDocLink());
        customDTOList.add(customDTO);
        customDTOList.add(customDTO2);

        dto.setCustomFields(customDTOList);

        List<ButtonDTO> buttonDTOList = Lists.newArrayList();
        ButtonDTO buttonDTO = new ButtonDTO();
        buttonDTO.setName("查看接口文档详情");

        String interfaceListPageUrl="http://console.paas.jd.com/idt/fe-app-view/demandManage/%s?interfaceType=3&methodId=%s&type=3";
        String methodPageUrl="http://console.paas.jd.com/idt/fe-app-view/demandManage/%s?interfaceType=3&methodId=%s&type=1";
        String approvel="http://console.paas.jd.com/idt/online/contributionDocument";
        String pageUrl=(null==manage.getType()||manage.getType()==0)?interfaceListPageUrl:methodPageUrl;
        buttonDTO.setPcUrl(String.format(pageUrl, manage.getAppId(), manage.getSourceId()));
        if(approve){
            buttonDTO.setName("查看审批单");
            buttonDTO.setPcUrl(approvel);
        }
        buttonDTOList.add(buttonDTO);
        dto.setButtons(buttonDTOList);

        dto.setHead(msg);
        dto.setSubHeading(subMsg);
        return dto;
    }
    private Page initPage(Long currentPage, Long pageSize, Long maxPageSize) {
        Long realCurrentPage = 1L;
        if (currentPage != null && currentPage > realCurrentPage) {
            realCurrentPage = currentPage;
        }
        Long realPageSize = maxPageSize;
        if (pageSize != null && pageSize > 0 && pageSize < maxPageSize) {
            realPageSize = pageSize;
        }
        return new Page(realCurrentPage, realPageSize);
    }
}
