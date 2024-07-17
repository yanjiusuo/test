package com.jd.workflow.console.service.listener.impl;

import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.flow.utils.ParametersUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class EasyMockInterfaceChangeListener implements InterfaceChangeListener {
   @Autowired
   EasyMockRemoteService testEasyMockRemoteService;
    @Autowired
    EasyMockRemoteService onlineEasyMockRemoteService;
    ParametersUtils utils = new ParametersUtils();
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IMethodManageService methodManageService;



    /**
     * jsf接口 jar包下载需要支持一段时间
     * @param manages
     */
    @Override
    public void onInterfaceAdd(List<InterfaceManage> manages) {  // 打开mock页面的时候会做校验，这里异常直接跳过即可
        for (InterfaceManage manage : manages) {
            if(!InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) return;
            try{
                testEasyMockRemoteService.addOrUpdateJsfInterface(manage);
                testEasyMockRemoteService.openOrCloseInterface(manage,true);
            }catch (Exception e){
                log.error("easymock.err_add_test_process_interface_add_task",e);
            }
            try{
                onlineEasyMockRemoteService.addOrUpdateJsfInterface(manage);
                onlineEasyMockRemoteService.openOrCloseInterface(manage,true);
            }catch (Exception e){
                log.error("easymock.err_add_online_process_interface_add_task",e);
            }
        }
    }


    @Override
    public void onInterfaceUpdate(InterfaceManage before,InterfaceManage manage) {

            if(!InterfaceTypeEnum.JSF.getCode().equals(manage.getType())) return;
            try{
                testEasyMockRemoteService.addOrUpdateJsfInterface(manage);
                testEasyMockRemoteService.openOrCloseInterface(manage,true);
            }catch (Exception e){
                log.error("easymock.err_update_test_process_interface_update_task",e);
            }
            try{
                onlineEasyMockRemoteService.addOrUpdateJsfInterface(manage);
                onlineEasyMockRemoteService.openOrCloseInterface(manage,true);
            }catch (Exception e){
                log.error("easymock.err_update_online_process_interface_update_task",e);
            }



    }




}
