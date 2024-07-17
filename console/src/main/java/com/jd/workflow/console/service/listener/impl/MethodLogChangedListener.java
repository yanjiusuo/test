package com.jd.workflow.console.service.listener.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.doc.MethodContentSnapshot;
import com.jd.workflow.console.dto.doc.method.HttpMethodDocConfig;
import com.jd.workflow.console.dto.doc.method.JsfMethodDocConfig;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;
import com.jd.workflow.console.jme.JdMEMessageUtil;
import com.jd.workflow.console.service.IHttpAuthService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import com.jd.workflow.console.service.doc.IMethodVersionModifyLogService;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.utils.ReqDemoBuildUtils;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 项目名称：parent
 * 类 名 称：MethodLogChangedListener
 * 类 描 述：方法修改记录监听
 * 创建时间：2022-11-23 14:21
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Component
public class MethodLogChangedListener implements InterfaceChangeListener {

    @Autowired
    private IMethodManageService methodManageService;

    @Autowired
    IInterfaceVersionService versionService;
    @Autowired
    IInterfaceManageService interfaceManageService;

    @Autowired
    private IMethodModifyLogService methodModifyLogService;

    @Autowired
    private IMethodVersionModifyLogService methodVersionModifyLogService;

    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;

    @Autowired
    IHttpAuthService httpAuthService;

    @Autowired
    IDocReportService docReportService;



    private void initVersion(InterfaceManage interfaceManage){
        versionService.initInterfaceVersion(interfaceManage);
    }

    @Override
    public void onInterfaceAdd(List<InterfaceManage> manages) {
        for (InterfaceManage manage : manages) {
            if(InterfaceTypeEnum.HTTP.getCode().equals(manage.getType())
                    ||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(manage.getType())
                    || InterfaceTypeEnum.JSF.getCode().equals(manage.getType())
                    || InterfaceTypeEnum.BEAN.getCode().equals(manage.getType())
            ){
                initVersion(manage);
                interfaceManageService.updateById(manage);
            }
        }

    }
    private void sendNotify(InterfaceManage manage){
        if(manage.getDocConfig() == null || manage.getDocConfig().getNoticeStatus() == null) return;
        if(manage.getDocConfig().getNoticeStatus() != 1) return;
        scheduleService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    httpAuthService.sendInterfaceManageChangeNotice(manage);
                    interfaceManageService.sendMessage(manage);
                }catch (Exception e){
                    log.error("interfaceManage.err_send_change_log:id={},name={}",manage.getId(),manage.getName(),e);
                }
            }
        });
    }

    @Override
    public void onMethodAdd(InterfaceManage interfaceManage, List<MethodManage> methods) {
        sendNotify(interfaceManage);
    }

    /**
     * 方法变更前将变更前的信息存下来
     * @param interfaceManage
     * @param methods 更新前的方法信息
     */
    @Override
    public  void onMethodBeforeUpdate(InterfaceManage interfaceManage, List<MethodManage> methods){
        for (MethodManage method : methods) {
            MethodModifyLog logObj = null;
            if(InterfaceTypeEnum.JSF.getCode().equals(method.getType())){
                logObj = new MethodModifyLog();
                MethodContentSnapshot methodContentSnapshot = new MethodContentSnapshot();
                MethodDocConfig config =  method.getDocConfig();
                if(method.getDocConfig() == null){
                    config = new JsfMethodDocConfig();
                }
                JsfStepMetadata model = JsonUtils.parse(method.getContent(),JsfStepMetadata.class);
                if(StringHelper.isEmpty(config.getInputExample())){
                    methodContentSnapshot.setInputExample(ReqDemoBuildUtils.getJsfInputDemoValue(model));
                }else{
                    methodContentSnapshot.setInputExample(config.getInputExample());
                }
                if(StringHelper.isEmpty(config.getOutputExample())){
                    methodContentSnapshot.setOutputExample(ReqDemoBuildUtils.getJsfOutputDemoValue(model));
                }else{
                    methodContentSnapshot.setOutputExample(config.getOutputExample());
                }
                logObj.setMethodContentSnapshot(methodContentSnapshot);
            }else if(InterfaceTypeEnum.HTTP.getCode().equals(method.getType())||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(method.getType())){
                logObj = new MethodModifyLog();
                MethodContentSnapshot methodContentSnapshot = new MethodContentSnapshot();
                HttpMethodModel model = JsonUtils.parse(method.getContent(),HttpMethodModel.class);
                MethodDocConfig config =  method.getDocConfig();
                if(config == null){
                    config = new HttpMethodDocConfig();
                }
                if(StringHelper.isEmpty(config.getInputExample())){
                    methodContentSnapshot.setInputExample(ReqDemoBuildUtils.buildHttpInput(model));
                }else{
                    methodContentSnapshot.setInputExample(config.getInputExample());
                }
                if(StringHelper.isEmpty(config.getOutputExample())){
                    methodContentSnapshot.setOutputExample(ReqDemoBuildUtils.buildHttpOutput(model));
                }else{
                    methodContentSnapshot.setOutputExample(config.getOutputExample());
                }
                logObj.setMethodContentSnapshot(methodContentSnapshot);
            }
            if(logObj!=null){
                logObj.getMethodContentSnapshot().setContent(method.getContent());
                //TODO 待确认是docInfo or desc
                logObj.getMethodContentSnapshot().setDesc(method.getDocInfo());
                logObj.getMethodContentSnapshot().setHttpMethod(method.getHttpMethod());
                logObj.setInterfaceId(interfaceManage.getId());
                logObj.setMethodId(method.getId());
                logObj.setVersion(interfaceManage.getLatestDocVersion());
                if(logObj.getVersion()==null){
                    logObj.setVersion("1.0.0");
                    log.warn("interface_manage:id={},latestDocVersion=null",interfaceManage.getId());
                }
                MethodVersionModifyLog  versionLog = new MethodVersionModifyLog();
                BeanUtils.copyProperties(logObj,versionLog);
                boolean result = methodModifyLogService.save(logObj);
                if(!result){
                    log.error("MethodLogChangedListener.onMethodUpdate save method modify log false>>>>>id={},methodId={}",interfaceManage.getId(),method.getId());
                    throw new BizException("保存方法操作日志异常！");
                }
                MethodVersionModifyLog lastObj = methodVersionModifyLogService.getOne(Wrappers.<MethodVersionModifyLog>lambdaQuery().eq(MethodVersionModifyLog::getInterfaceId, versionLog.getInterfaceId())
                        .eq(MethodVersionModifyLog::getMethodId, versionLog.getMethodId())
                        .eq(MethodVersionModifyLog::getVersion, versionLog.getVersion()));
                if(lastObj!=null){
                    versionLog.setId(lastObj.getId());
                    result = methodVersionModifyLogService.updateById(versionLog);
                }else{
                    result = methodVersionModifyLogService.save(versionLog);
                }
           /* if(!result){
                log.error("MethodLogChangedListener.onMethodUpdate save or update method version modify log false>>>>>id={},methodId={},versionId={}",interfaceManage.getId(),method.getId(),versionLog.getVersion());
                throw new BizException("保存或更新方法版本操作日志异常！");
            }*/
            }
            sendNotify(interfaceManage);
        }

    };



    @Override
    public void onMethodRemove(InterfaceManage interfaceManage, List<MethodManage> methods) {
        sendNotify(interfaceManage);
    }
}
