package com.jd.workflow.console.service.listener.impl;

import com.jd.workflow.console.elastic.service.EsInterfaceService;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.impl.ScoreManageService;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
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
public class SyncEsListener implements InterfaceChangeListener {


    @Autowired
    ScoreManageService scoreManageService;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    IInterfaceManageService interfaceManageService;

    @Autowired
    EsInterfaceService esInterfaceService;
    @Autowired
    IAppInfoService appInfoService;

    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;

    private boolean isVisible(InterfaceManage interfaceManage){
        return interfaceManage.getVisibility() != null && interfaceManage.getVisibility().equals(0);
    }
    @Override
    public  void onAppAdd(AppInfo appInfo){}
    @Override
    public  void onAppUpdate(AppInfo before,AppInfo after){

           schedule(new Runnable() {
               @Override
               public void run() {
                   try {
                       log.info("app.update_es_index:appCode={}",after.getAppCode());
                       List<InterfaceManage> interfaces = interfaceManageService.getAppInterfaces(after.getId());
                       esInterfaceService.saveOrUpdateInterface(interfaces,after);
                   }catch (Exception e){
                       log.info("es.err_rebuild_index",e);
                   }

               }
           });

    }
    private void schedule(Runnable runnable){
        scheduleService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    runnable.run();
                }catch (Exception e){
                    log.error("update_es_index_error",e);
                }
            }
        });
    }

    @Override
    public  void onAppRemove(AppInfo app){


        schedule(new Runnable() {
            @Override
            public void run() {
                try{
                    esInterfaceService.removeAppDoc(app.getId());
                }catch (Exception e){
                    log.error("es.update_interface_docs:methodIds={}",app.getId(),e);
                }
            }
        });
    }
    private List<InterfaceManage> filterVisibles(List<InterfaceManage> manages){
        return manages.stream().filter(item->isVisible(item)).collect(Collectors.toList());
    }
    @Override
    // 新增接口
    public  void onInterfaceAdd(List<InterfaceManage> manages){


        schedule(new Runnable() {
            @Override
            public void run() {
                try{
                    AppInfo appInfo = appInfoService.getById(manages.get(0).getAppId());
                    esInterfaceService.saveInterfaceDoc(manages,appInfo);
                }catch (Exception e){
                    log.error("es.update_interface_docs:methodIds={}",manages.stream().map(item->item.getId()).collect(Collectors.toList()),e);
                }
            }
        });
    }
    @Override
    public  void onInterfaceUpdate(InterfaceManage before,InterfaceManage after){


        schedule(new Runnable() {
            @Override
            public void run() {
                try{
                    AppInfo appInfo = appInfoService.getById(after.getAppId());
                    esInterfaceService.saveOrUpdateInterface(Collections.singletonList(after),appInfo);
                    if(!ObjectHelper.equals(before.getVisibility(),after.getVisibility())){
                        esInterfaceService.rebuildInterfaceMethodIndex(after);
                    }
                }catch (Exception e){
                    log.error("es.update_interface_docs:methodIds={}",after.getId(),e);
                }
            }
        });
    }
    @Override
    public  void onInterfaceRemove(List<InterfaceManage> manages){


        schedule(new Runnable() {
            @Override
            public void run() {
                try{
                    esInterfaceService.removeInterfaceDoc(manages.stream().map(item->item.getId()).collect(Collectors.toList()));
                }catch (Exception e){
                    log.error("es.err_remove_interfacer_doc:methodIds={}",manages.stream().map(item->item.getId()).collect(Collectors.toList()),e);
                }
            }
        });
    }


    @Override
    public  void onMethodAdd(InterfaceManage interfaceManage, List<MethodManage> methods){
        scheduleService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    esInterfaceService.saveMethodDoc(methods,interfaceManage);
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
    @Override
    public  void onMethodUpdate(InterfaceManage interfaceManage, List<MethodManage> methods){
        scheduleService.execute(new Runnable() {
            @Override
            public void run() {
                try{

                    List<MethodManage> newMethods = copyMethods(methods);
                    methodManageService.initMethodRefAndDelta(newMethods,interfaceManage.getAppId());
                    esInterfaceService.saveOrUpdateMethodDoc(newMethods,interfaceManage);
                }catch (Exception e){
                    log.error("method.err_update_score",e);
                }
            }
        });
    }
    public  void onMethodRemove(InterfaceManage interfaceManage, List<MethodManage> methods){
        schedule(new Runnable() {
            @Override
            public void run() {
                try{
                    esInterfaceService.removeMethodDoc(methods.stream().map(item->item.getId()).collect(Collectors.toList()));
                }catch (Exception e){
                    log.error("es.err_remove_method_doc:methodIds={}",methods.stream().map(item->item.getId()).collect(Collectors.toList()),e);
                }
            }
        });
    }



}
