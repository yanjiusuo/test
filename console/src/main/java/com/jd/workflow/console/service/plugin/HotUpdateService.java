package com.jd.workflow.console.service.plugin;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.jd.workflow.console.dto.manage.AppAndSecret;
import com.jd.workflow.console.dto.manage.AppSearchResult;
import com.jd.workflow.console.dto.plugin.AppDto;
import com.jd.workflow.console.dto.plugin.JdosAndJapiApp;
import com.jd.workflow.console.dto.plugin.jdos.JdosApps;
import com.jd.workflow.console.dto.plugin.jdos.JdosSystemApps;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.ducc.DuccConfigServiceAdapter;
import com.jd.workflow.console.service.ducc.entity.HotUpdateEnvironmentConf;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.plugin.jdos.JdosAbstract;
import com.jd.workflow.console.service.plugin.jdos.JdosFactoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;


@Service
@Slf4j
public class HotUpdateService {

    @Value("${ducc.hotUpdateEnvironment:[]}")
    private String hotUpdateEnvironment;

    @Resource
    JdosFactoryService jdosFactoryService;
    @Autowired
    AppInfoServiceImpl appInfoService;
    @Resource(name = "defaultScheduledExecutor")
    ScheduledThreadPoolExecutor defaultScheduledExecutor;


//    @Resource
//    private DuccConfigServiceAdapter duccConfigServiceAdapter;

    public List<HotUpdateEnvironmentConf> getEnvironmentList() {

        return JSONArray.parseArray(hotUpdateEnvironment, HotUpdateEnvironmentConf.class);
    }

    public HotUpdateEnvironmentConf getEnvByCode(String code) {
        List<HotUpdateEnvironmentConf> hotUpdateEnvironmentConfList = JSONArray.parseArray(hotUpdateEnvironment, HotUpdateEnvironmentConf.class);
        for (HotUpdateEnvironmentConf hotUpdateEnvironmentConf : hotUpdateEnvironmentConfList) {
            if (Objects.equals(hotUpdateEnvironmentConf.getCode(), code)) {
                return hotUpdateEnvironmentConf;
            }
        }
        return null;

    }

    private boolean contains(String str, String val) {
        return str != null && str.contains(val);
    }

    private List<JdosApps> getJdosSystemApps(String erp, String search) {
        List<JdosApps> appDtoList = Lists.newArrayList();
        JdosAbstract service = jdosFactoryService.getService("pre");
        List<JdosSystemApps> systemApps = service.getSystemApps(erp);
        for (JdosSystemApps systemApp : systemApps) {
            if (systemApp.getApps() == null) continue;
            for (JdosApps app : systemApp.getApps()) {

                if (StringUtils.isNotBlank(search)) {
                    if (contains(app.getAppName(), search) || contains(app.getNickname(), search)) {
                        appDtoList.add(app);
                    }
                } else {
                    appDtoList.add(app);
                }
            }
        }

        return appDtoList;
    }

    public List<JdosAndJapiApp> getUserApp(String erp, String search) {
        List<JdosAndJapiApp> result = new ArrayList<>();
        List<Future> futures = new ArrayList<>();
        futures.add(defaultScheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Long start = System.currentTimeMillis();
                List<JdosApps> apps = getJdosSystemApps(erp, search);
                log.info("jdos.get_system_app_cost:{}", System.currentTimeMillis() - start);
                for (JdosApps jdosApp : apps) {
                    JdosAndJapiApp app = new JdosAndJapiApp();
                    app.setType(JdosAndJapiApp.TYPE_JDOS);
                    app.setCode(jdosApp.getAppName());
                    app.setName(jdosApp.getNickname());
                    app.setRelatedJdosAppCode(jdosApp.getAppName());
                    result.add(app);
                }
            }
        }));
        futures.add(defaultScheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                List<AppAndSecret> appSearchResults = appInfoService.searchNoJdosApps(search, erp);
                for (AppAndSecret appSearchResult : appSearchResults) {
                    JdosAndJapiApp app = new JdosAndJapiApp();
                    app.setCode(appSearchResult.getAppCode());
                    app.setName(appSearchResult.getAppName());
                    app.setRelatedJdosAppCode(appSearchResult.getJdosAppCode());
                    app.setType(JdosAndJapiApp.TYPE_JAPI);
                    app.setAppSecret(appSearchResult.getAppSecret());
                    result.add(app);
                }
            }
        }));
        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(result, new Comparator<JdosAndJapiApp>() {
            @Override
            public int compare(JdosAndJapiApp o1, JdosAndJapiApp o2) {
                return o1.getType() - o2.getType();
            }
        });
        {
            //JAPI demo 应用数据默认添加
            JdosAndJapiApp jdosAndJapiApp = new JdosAndJapiApp();
            jdosAndJapiApp.setCode("J-dos-japi-demo");
            jdosAndJapiApp.setName("在线联调demo");
            jdosAndJapiApp.setRelatedJdosAppCode("japi-demo");
            jdosAndJapiApp.setType(JdosAndJapiApp.TYPE_JAPI);
            jdosAndJapiApp.setAppSecret("4f38d97edfb517f42c3b6c9bf2d8c069");

            //添加成列表第一个
            result.add(0, jdosAndJapiApp);

        }

        return result;
    }

    public List<JdosAndJapiApp> getUserAllApp(String erp) {
        List<JdosAndJapiApp> result = new ArrayList<>();


        List<Future> futures = new ArrayList<>();
        List<JdosAndJapiApp> jdosApps = new ArrayList<>();
        List<JdosAndJapiApp> japiApps = new ArrayList<>();
        futures.add(defaultScheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                List<JdosAndJapiApp> result = new ArrayList<>();
                Long start = System.currentTimeMillis();
                List<JdosApps> apps = getJdosSystemApps(erp, "");
                log.info("jdos.get_system_app_cost:{}", System.currentTimeMillis() - start);
                for (JdosApps jdosApp : apps) {
                    JdosAndJapiApp app = new JdosAndJapiApp();
                    app.setType(JdosAndJapiApp.TYPE_JDOS);
                    app.setCode(jdosApp.getAppName());
                    app.setName(jdosApp.getNickname());
                    app.setRelatedJdosAppCode(jdosApp.getAppName());
                    result.add(app);
                }
                jdosApps.addAll(result);
            }
        }));
          futures.add(defaultScheduledExecutor.submit(new Callable<List<JdosAndJapiApp>>() {
            @Override
            public List<JdosAndJapiApp> call() throws Exception {
                List<JdosAndJapiApp> result = new ArrayList<>();
                List<AppAndSecret> appSearchResults = appInfoService.searchNameNotSameApps(null, erp);
                for (AppAndSecret appSearchResult : appSearchResults) {
                    if ("J-dos-japi-demo".equals(appSearchResult.getAppCode())) {
                        continue;
                    }
                    JdosAndJapiApp app = new JdosAndJapiApp();
                    app.setCode(appSearchResult.getAppCode());
                    app.setName(appSearchResult.getAppName());
                    app.setRelatedJdosAppCode(appSearchResult.getJdosAppCode());
                    app.setType(JdosAndJapiApp.TYPE_JAPI);
                    app.setAppSecret(appSearchResult.getAppSecret());
                    result.add(app);
                }
                japiApps.addAll(result);

                return result;
            }

        }));
        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        Set<String> jdosAppCodes = jdosApps.stream().map(item->item.getCode()).collect(Collectors.toSet());
        result.addAll(jdosApps);

        for (JdosAndJapiApp japiApp : japiApps) {
            if(japiApp.getCode().startsWith("J-dos-") ){
                if(!jdosAppCodes.contains(japiApp.getCode().substring("J-dos-".length()))){
                    result.add(japiApp);
                }

                continue;
            }
            result.add(japiApp);
        }
        Collections.sort(result, new Comparator<JdosAndJapiApp>() {
            @Override
            public int compare(JdosAndJapiApp o1, JdosAndJapiApp o2) {
                return o1.getType() - o2.getType();
            }
        });
        {
            //JAPI demo 应用数据默认添加
            JdosAndJapiApp jdosAndJapiApp = new JdosAndJapiApp();
            jdosAndJapiApp.setCode("J-dos-japi-demo");
            jdosAndJapiApp.setName("在线联调demo");
            jdosAndJapiApp.setRelatedJdosAppCode("japi-demo");
            jdosAndJapiApp.setType(JdosAndJapiApp.TYPE_JAPI);
            jdosAndJapiApp.setAppSecret("4f38d97edfb517f42c3b6c9bf2d8c069");

            //添加成列表第一个
            result.add(0, jdosAndJapiApp);

        }

        return result;
    }

    public List<JdosAndJapiApp> getJdosApps(String erp, String search) {
        List<JdosAndJapiApp> result = new ArrayList<>();
        List<JdosApps> apps = getJdosSystemApps(erp, search);
        for (JdosApps jdosApp : apps) {
            JdosAndJapiApp app = new JdosAndJapiApp();
            app.setType(JdosAndJapiApp.TYPE_JDOS);
            app.setCode(jdosApp.getAppName());
            app.setName(jdosApp.getNickname());
            result.add(app);
        }

        return result;
    }
}
