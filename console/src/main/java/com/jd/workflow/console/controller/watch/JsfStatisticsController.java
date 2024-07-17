package com.jd.workflow.console.controller.watch;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.jsf.open.api.ConsumerService;
import com.jd.jsf.open.api.domain.app.AppDictVo;
import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.BaseRequest;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.dto.manage.InterfaceAppSearchDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.parser.InterfaceInfoDown;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.impl.InterfaceManageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.LocalTaskExecutorThreadPool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/jsf")
public class JsfStatisticsController {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private InterfaceManageServiceImpl interfaceManageService;


    @Autowired
    private AppInfoServiceImpl appInfoService;

    @Autowired
    private ScheduledThreadPoolExecutor defaultScheduledExecutor;

    private static List<JsfStaticsVo> vos=new CopyOnWriteArrayList<JsfStaticsVo>();

    @GetMapping(path = "/count")
    public void getJsfCount(String dept,String pin) {
        //根据appId聚合
        LambdaQueryWrapper<AppInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppInfo::getYn, 1);
        wrapper.isNotNull(AppInfo::getJdosAppCode);
        wrapper.eq(StringUtils.isNotBlank(dept), AppInfo::getDept, dept);
        wrapper.like(StringUtils.isNotBlank(pin), AppInfo::getMembers, pin);
        Page<AppInfo> infos = appInfoService.page(new Page<>(1, 1000), wrapper);
        log.info("计算数据-总数"+infos.getTotal());
        for (AppInfo record : infos.getRecords()) {
            defaultScheduledExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    JsfStaticsVo vo = new JsfStaticsVo(record.getJdosAppCode(), 0, 0, 0);
                    Long current = 1L;
                    Long pages = 1L;
                    do {
                        pages = calJsfNum(record.getId(), current, vo);
                        current++;
                    } while (current <= pages);
                    vos.add(vo);
                    log.info("计算数据" + record.getJdosAppCode() + "完成");
                }
            });
        }
        while (defaultScheduledExecutor.getTaskCount() != defaultScheduledExecutor.getCompletedTaskCount()) {
            log.info("计算总数:" + defaultScheduledExecutor.getTaskCount() + "已完成："+defaultScheduledExecutor.getCompletedTaskCount());
        }
    }

    @GetMapping(path = "/getVos")
    @ResponseBody
    public List<JsfStaticsVo> getVos(){
        return vos;
    }

    private Long calJsfNum(Long id,Long current, JsfStaticsVo vo){
        InterfaceAppSearchDto query=new InterfaceAppSearchDto();
        query.setAppId(id);
        query.setType(3);
        query.setCurrent(current);
        query.setSize(50L);
        Page<InterfaceManage> jsfInterfaces= interfaceManageService.listInterface(query);
        if(CollectionUtils.isEmpty(jsfInterfaces.getRecords())){
            return 0L;
        }
        for (InterfaceManage record : jsfInterfaces.getRecords()) {
            Integer appNum= buildSetConsumerAppNo(record.getServiceCode());
            if(appNum>10){
                vo.setSNum(vo.getSNum()+1);
            }
            if(appNum<10&&appNum>0){
                vo.setANum(vo.getANum()+1);
            }
            if(appNum==0){
                vo.setBNum(vo.getBNum()+1);
            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
        return jsfInterfaces.getPages();
    }


    private Integer buildSetConsumerAppNo(String interfaceName) {
        try {
            log.error("JsfTestController.buildSetConsumerAppNo"+interfaceName);
            BaseRequest baseRequest = JSFArgBuilder.buildSetRequestInfo(new BaseRequest());
            baseRequest.setData(interfaceName);
            Result<List<AppDictVo>> appInfo = consumerService.getAppInfo(baseRequest);
            return appInfo.getData().size();
        } catch (Exception e) {
            log.error("JsfTestController.buildSetConsumerAppNo exception ", e);
        }
        return 0;
    }

}
