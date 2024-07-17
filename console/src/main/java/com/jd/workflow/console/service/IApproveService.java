package com.jd.workflow.console.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.ApprovalPageQuery;
import com.jd.workflow.console.dto.ApproveCreateDTO;
import com.jd.workflow.console.entity.ApproveManage;

public interface IApproveService extends IService<ApproveManage> {



    Page<ApproveManage> pageList(ApprovalPageQuery req);


    Boolean createApprove(ApproveCreateDTO approveCreateDTO);

    Boolean acceptApprove(Long id);

    Boolean rejectApprove(Long id);


    Boolean removeApprove(Long id);


    ApproveCreateDTO converManage(Long id, String serviceCode, Integer serviceType, Long appId, String appCode, String path, Boolean noNeedApprove, String contents);
}
