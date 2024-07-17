package com.jd.workflow.console.service.doc.importer;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.dto.importer.JddjApp;
import com.jd.workflow.console.dto.importer.JddjResult;
import com.jd.workflow.console.service.doc.importer.dto.DjApiGroup;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.console.service.remote.api.impl.JagileServiceImpl;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class DjApiImporterTest extends BaseTestCase {
    JddjApiImporter importer = new JddjApiImporter();
    @Test
    public void testApiImporter(){
         JddjResult<List<JddjApp>> jddjResult = importer.getJddjApp(null, null);
         List<String> multiGroupApps = new ArrayList<>();
         List<String> errorAppCodes = new ArrayList<>();
         List<String> multiEnvApps = new ArrayList<>();
        if(jddjResult.isSuccess()){
            for (JddjApp jddjApp : jddjResult.getResult()) {
                if(jddjApp.getEnvList().size() > 1) {
                    multiEnvApps.add(jddjApp.getAppCode());
                    continue;
                }
                try{
                    for (String env : jddjApp.getEnvList()) {
                        final JddjResult<List<DjApiGroup>> result = importer.getApiGroups(null, jddjApp.getAppCode(), env);
                        if(result.isSuccess()){
                            if(result.getResult() == null){
                                errorAppCodes.add(jddjApp.getAppCode());
                            }else if(result.getResult().size() > 1){
                               multiGroupApps.add(jddjApp.getAppCode());
                           }
                        }else{
                            throw new BizException("获取分组失败："+result.getMsg());
                        }
                    }

                }catch (Exception e){
                    log.error("log.err_get_app_group:app={}", JsonUtils.toJSONString(jddjApp),e);
                }
            }
        }
        log.info("multiEnvAppsCnt={},multiGroupAppCnt={},errorCodeCnt={},multiEnvApps={},multiGroupApps={},errorAppCodes={}",
                multiEnvApps.size(),multiGroupApps.size(),errorAppCodes.size(),multiEnvApps
                ,multiGroupApps,errorAppCodes);
    }
    public void getValidAppsTests() {
        JagileServiceImpl service = new JagileServiceImpl();
        /*List<JDosAppInfo> jdosApps = service.getJdosAppInfo("wangjingfang3");
        System.out.println(JsonUtils.toJSONString(jdosApps));*/
        String cookie = "BJ.4935BB8AA2598EC568EC7BCB9747D7AB.4620221214082022";
        JddjApiImporter importer = new JddjApiImporter();
        final JddjResult<List<JddjApp>> result = importer.getJddjApp(cookie, null);
        List<String> missApps = new ArrayList<>();
        List<String> successApps = new ArrayList<>();
        int i = 0;
        try{
            if(result.isSuccess()){

                for (JddjApp jddjApp : result.getResult()) {
                    try{
                        JDosAppInfo jdosAppResult = service.getAppInfo(jddjApp.getAppCode());
                        log.info("jdos.get_app_info:index={},app={}",i,JsonUtils.toJSONString(jdosAppResult));
                        i++;
                       /* if(jdosAppResult.getCode() != 200){
                            log.error("jdos.miss_app:appCode={},i={}",jddjApp.getAppCode(),i);
                            //throw new BizException("miss app code"+jddjApp.getAppCode());
                            missApps.add(jddjApp.getAppCode());
                        }else{*/
                        if(jdosAppResult == null){
                            missApps.add(jddjApp.getAppCode());
                        }else{
                            successApps.add(jddjApp.getAppCode());
                        }
                        //}
                    }catch (Exception e){
                        log.error("jdos.err_get_app",e);
                        missApps.add(jddjApp.getAppCode());
                    }


                }
            }
        }catch (Exception e){
            log.error("jdos.err_get_app_info:index={},missApps={},successApps={}",i,missApps,successApps,e);
        }
        log.error("jdos.succ_get_app_info:index={},missApps={},successApps={}",i,missApps,successApps);


    }
    @Test
    public void testGetVersionInterface(){
        List<Map<String, Object>> result = importer.getVersionInterfaces(null, "o2o-eagle-yunying-web", "pre1", null);
        final JddjResult<List<DjApiGroup>> groupResult = importer.getApiGroups(null, "o2o-eagle-yunying-web", "pre1");
        System.out.println("groupResult::::"+JsonUtils.toJSONString(groupResult));
        System.out.println("result::::"+JsonUtils.toJSONString(result));
    }
}
