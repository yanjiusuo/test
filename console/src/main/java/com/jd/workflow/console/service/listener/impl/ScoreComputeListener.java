package com.jd.workflow.console.service.listener.impl;

import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.impl.ScoreManageService;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScoreComputeListener implements InterfaceChangeListener {


    @Autowired
    ScoreManageService scoreManageService;
    @Autowired
    MethodManageServiceImpl methodManageService;

    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;
    @Override
    public  void onInterfaceAdd(List<InterfaceManage> manages){

    }

    public  void onMethodAdd(InterfaceManage interfaceManage, List<MethodManage> methods){
        scheduleService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    scoreManageService.updateMethodsScore(methods);
                    scoreManageService.updateInterfaceScore(Collections.singletonList(interfaceManage));
                }catch (Exception e){
                    log.error("method.err_update_score",e);
                }
            }
        });
    }
    private List<MethodManage> copyMethods(List<MethodManage> methods){
        return methods.stream().map(item->{
            if(item.getContentObject() == null){
                 methodManageService.initContentObject(item);
            }
            MethodManage newMethod = new MethodManage();
            BeanUtils.copyProperties(item,newMethod);
            newMethod.setContent(JsonUtils.toJSONString(item.getContentObject()));
            methodManageService.initContentObject(newMethod);
            return newMethod;
        }).collect(Collectors.toList());
    }
    public  void onMethodUpdate(InterfaceManage interfaceManage, List<MethodManage> methods){
        scheduleService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    List<MethodManage> newMethods = copyMethods(methods);
                    methodManageService.initMethodRefAndDelta(newMethods,interfaceManage.getAppId());
                    scoreManageService.updateMethodsScore(newMethods);
                    scoreManageService.updateInterfaceScore(Collections.singletonList(interfaceManage));
                }catch (Exception e){
                    log.error("method.err_update_score",e);
                }
            }
        });
    }


}
