package com.jd.workflow.console.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jd.cjg.RpcResponse;
import com.jd.cjg.unapp.UnAppOpenProvider;
import com.jd.cjg.unapp.UnAppProvider;
import com.jd.cjg.unapp.request.GitAppReq;
import com.jd.cjg.unapp.vo.AppInfoVo;
import com.jd.common.util.StringUtils;
import com.jd.common.web.LoginContext;
import com.jd.workflow.console.dto.jingme.ButtonDTO;
import com.jd.workflow.console.dto.jingme.TemplateMsgDTO;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.doc.SyncJsfDocLog;
import com.jd.workflow.console.service.doc.SyncJsfDocLogService;
import com.jd.workflow.console.service.ducc.DuccConfigServiceAdapter;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.jingme.SendMsgService;
import com.jd.workflow.console.utils.UUIDUtil;
import com.jd.workflow.webhook.Project;
import com.jd.workflow.webhook.WebHookVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WebHookServiceImpl {

    @Resource
    DuccConfigServiceAdapter duccConfigServiceAdapter;
    @Resource
    private UnAppOpenProvider unAppOpenProvider;


    @Resource
    private AppInfoServiceImpl appInfoService;

    @Resource
    DevCodeService devCodeService;

    @Autowired
    private SyncJsfDocLogService syncJsfDocLogService;
    @Autowired
    private CodingAppRealtionService codingAppRelationService;


    @Resource
    private SendMsgService sendMsgService;


    private static final String bundleId = "cjg_api_doc";
    //获取指定组件指定版本的信息
    private static final String queryVersionUrl = "http://bamboo.jd.com/rest_api/v1/rpc/get-pipeline-bundle-versions";

    //http://rdpe.jd.com/upipe/api/bundle-service/appendix/#324-%E8%A7%A6%E5%8F%91%E7%BB%84%E4%BB%B6%E5%AE%9E%E4%BE%8B
    //触发组件实例
    private static final String griggerUrl = "http://bamboo.jd.com/rest_api/v2/rpc/trigger-pipeline-bundle";
    private static String token = "token c89f726a5576959344fda05934ce2885e506418c";


    public JSONObject jagline(WebHookVo param) throws Exception {

        //http://taishan.jd.com/ducc/web/nswork?nsId=9570&nsName=up_portal_config&cId=54858&cName=config&defAppId=9115&dataType=0&envId=82413&envName=prd
        Properties config = duccConfigServiceAdapter.queryPropertiesByConfigCode("config");
        String flag = config.getProperty("masterReport", "true");

        if ((ObjectUtils.nullSafeEquals(0, param.getType()) || param.getType() == null) && "true".equals(flag)) {
            if (!param.getRef().contains("master")) {
                log.info("非master分支，不进行上报，可以ducc配置中打开测试开关，进行其他分支测试");
                throw new Exception("非master分支，不进行上报，可以ducc配置中打开测试开关，进行其他分支测试");
            }
        }

        GitAppReq req = new GitAppReq();
        req.setGit(param.getProject().getGit_http_url());
        RpcResponse<List<AppInfoVo>> vos = unAppOpenProvider.codingApps(req);
        if (null == vos || CollectionUtils.isEmpty(vos.getData())) {
            throw new Exception("查询不到应用code信息，代码地址为：" + param.getProject().getGit_http_url());
        }
        String appCode=codingAppRelationService.getByCodePath(param.getProject().getGit_http_url());
        if(StringUtils.isNotBlank(appCode)){
            param.setAppCode(appCode);
            param.setDept(vos.getData().get(0).getDept());
        }else{
            CodingAppRelation entity=new CodingAppRelation();
            entity.setCodePath(param.getProject().getGit_http_url());
            entity.setAppCode(vos.getData().get(0).getAppAlias());
            entity.setDept(vos.getData().get(0).getDept());
            entity.setCreated(LocalDateTime.now());
            entity.setCreator("add Relation");
            codingAppRelationService.save(entity);
            param.setDept(vos.getData().get(0).getDept());
            param.setAppCode(vos.getData().get(0).getAppAlias());
        }
        if (StringUtils.isNotBlank(vos.getData().get(0).getAppLanguage()) && !vos.getData().get(0).getAppLanguage().toLowerCase().contains("java")) {
            SyncJsfDocLog docLog = new SyncJsfDocLog();
            docLog.setFlowId(param.getFlowId());
            docLog.setType(param.getType());
            docLog.setFlowType(1);
            docLog.setStatus(3);
            docLog.setDept(param.getDept());
            docLog.setAppCode(param.getAppCode());
            docLog.setCodePath(param.getProject().getGit_http_url());
            docLog.setRemart("非java代码,语言为"+vos.getData().get(0).getAppLanguage());
            insertResult(docLog);
            throw new Exception("非java代码：" + param.getProject().getGit_http_url());
        }



        HttpGet get = new HttpGet();
        get.addHeader("Authorization", token);
        get.addHeader("Content-Type", "application/x-www-form-urlencoded");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            get.setURI(new URI(String.format(queryVersionUrl + "?bundle_id=%s", bundleId)));
            log.info("---获取流水线组件版本");
            CloseableHttpResponse result = httpClient.execute(get);
            List<PipeLine> pipeInPuts = new ArrayList<>();
            List<AtomInfo> atomInfos = new ArrayList<>();
            JSONObject versionInfo = new JSONObject();
            if (result.getEntity() != null) {
                String params = EntityUtils.toString(result.getEntity(), StandardCharsets.UTF_8);
                log.info("result:{}", params);
                JSONArray verisonInfos = JSONObject.parseArray(JSONObject.parseObject(params).getString("data"));
                versionInfo = JSONObject.parseObject(String.valueOf(verisonInfos.get(0)));
                pipeInPuts = JSONArray.parseArray(versionInfo.getString("input"), PipeLine.class);
                atomInfos = JSONArray.parseArray(versionInfo.getString("stages"), AtomInfo.class);
            }

            if (!CollectionUtils.isEmpty(pipeInPuts)) {
                fillParamValue(pipeInPuts, atomInfos, param);
                HttpPost httpPost = new HttpPost(griggerUrl);
                httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                httpPost.addHeader("Authorization", token);
                Map<String, Object> bodyInfo = new HashMap<String, Object>();
                bodyInfo.put("bundle_id", bundleId);
                bodyInfo.put("version", versionInfo.getString("versionName"));
                bodyInfo.put("parameters", pipeInPuts);
                bodyInfo.put("stages", atomInfos);
                String body = JSON.toJSONString(bodyInfo);
                httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                log.info("---触发流水线组件开始{}", body);
                CloseableHttpResponse response1 = httpClient.execute(httpPost);
                String resp = EntityUtils.toString(response1.getEntity(), "utf-8");
                log.info("---触发流水线组件结束{}", resp);

                SyncJsfDocLog docLog = new SyncJsfDocLog();
                docLog.setFlowId(param.getFlowId());
                docLog.setType(param.getType());
                docLog.setFlowType(1);
                docLog.setStatus(0);
                docLog.setSubStatus(5);
                docLog.setAppCode(param.getAppCode());
                docLog.setRemart(resp);
                docLog.setDept(param.getDept());
                docLog.setInterfaceName(param.getLanguage());
                if (null != JSONObject.parseObject(resp).getJSONObject("data")) {
                    docLog.setBuildUrl(JSONObject.parseObject(resp).getJSONObject("data").getString("build_detail_url"));
                }
                docLog.setCodePath(param.getProject().getGit_http_url());
                insertResult(docLog);
                return JSONObject.parseObject(resp);

            }
        } catch (Exception e) {
            log.error("触发流水线异常", e);
            throw e;
        }
        return new JSONObject();

    }



    public void insertResult(SyncJsfDocLog docLog) {

        Properties config = duccConfigServiceAdapter.queryPropertiesByConfigCode("config");
        String admingString = config.getProperty("sendJmeAdmins", "");
        List<String> admins=JSONArray.parseArray(admingString,String.class);
        // 应用负责人
        String appErps = config.getProperty("sendJmeAppErps", "");
        if(StringUtils.isNotBlank(appErps)){
            Map<String,Object> app2Erps=JSONObject.parseObject(appErps);
            Object erps=app2Erps.get(docLog.getAppCode());
            if(null!=erps){
                admins.addAll(JSONArray.parseArray(JSONObject.toJSONString(erps),String.class));
            }
        }

        if (3 == docLog.getFlowType() && 1 == docLog.getStatus()) {
            LambdaQueryWrapper<AppInfo> wrar = new LambdaQueryWrapper();
            wrar.eq(AppInfo::getAppCode, "J-dos-" + docLog.getAppCode());
            wrar.eq(AppInfo::getYn, 1);
            docLog.setSubStatus(0);
            List<AppInfo> infos = appInfoService.list(wrar);
            if (!CollectionUtils.isEmpty(infos)) {
                AppInfo info = infos.get(0);
                if (null != info) {
                    String docUrl = "http://console.paas.jd.com/idt/fe-app-view/demandManage/" + info.getId();
                    docLog.setResultUrl(docUrl);
                    docLog.setDept(info.getDept());
                } else {
                    docLog.setRemart("J-dos-" + docLog.getAppCode() + " 查不到对应appId");
                }
            } else {
                docLog.setRemart("J-dos-" + docLog.getAppCode() + " 查不到对应appId");
            }
            sendUserJueMsg(admins,docLog);
        }
        //原子阶段失败
        if(2 == docLog.getStatus()){
            LambdaQueryWrapper<SyncJsfDocLog> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(SyncJsfDocLog::getFlowId,docLog.getFlowId());
            wrapper.eq(SyncJsfDocLog::getAppCode,docLog.getAppCode());
            wrapper.eq(SyncJsfDocLog::getYn,1);
            List<SyncJsfDocLog> logs=syncJsfDocLogService.list(wrapper);
            if(!CollectionUtils.isEmpty(logs)){
                docLog.setBuildUrl(logs.get(0).getBuildUrl());
                docLog.setId(logs.get(0).getId());
            }
            sendUserJueMsg(admins,docLog);
        }

        LambdaQueryWrapper<SyncJsfDocLog> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SyncJsfDocLog::getFlowId, docLog.getFlowId());
        wrapper.eq(SyncJsfDocLog::getYn, 1);
        List<SyncJsfDocLog> docLogs = syncJsfDocLogService.list(wrapper);
        if (!CollectionUtils.isEmpty(docLogs)) {
            SyncJsfDocLog existLog=docLogs.get(0);
            existLog.setFlowType(docLog.getFlowType());
            existLog.setRemart(docLog.getRemart());
            if(StringUtils.isNotBlank(docLog.getResultUrl())) {
                existLog.setResultUrl(docLog.getResultUrl());
            }
            if(StringUtils.isNotBlank(docLog.getBuildUrl())){
                existLog.setBuildUrl(docLog.getBuildUrl());
            }
            if(null!=docLog.getJsfNum()){
                existLog.setJsfNum(docLog.getJsfNum());
            }
            if(null!=docLog.getHttpNum()){
                existLog.setHttpNum(docLog.getHttpNum());
            }
            if(StringUtils.isNotBlank(docLog.getDept())){
                existLog.setDept(docLog.getDept());
            }
            existLog.setSubStatus(docLog.getSubStatus());
            existLog.setStatus(docLog.getStatus());
            existLog.setModified(LocalDateTime.now());
            existLog.setYn(1);
            syncJsfDocLogService.updateById(existLog);
        } else {
            docLog.setInterfaceName("");
            docLog.setYn(1);
            docLog.setCreated(LocalDateTime.now());
            String appCode=codingAppRelationService.getByCodePath(docLog.getCodePath());
            if(StringUtils.isNotBlank(appCode)){
                docLog.setAppCode(appCode);
            }

            Boolean rest = syncJsfDocLogService.save(docLog);

            log.info("insertResult保存结果{},", rest);
        }

    }


    public void fillParamValue(List<PipeLine> lines, List<AtomInfo> atomInfos, WebHookVo param) throws Exception {
        GitAppReq req = new GitAppReq();
        req.setGit(param.getProject().getGit_http_url());
        String appCode = param.getAppCode();
        if (StringUtils.isEmpty(appCode)) {
            RpcResponse<List<AppInfoVo>> vos = unAppOpenProvider.codingApps(req);
            if (null == vos || CollectionUtils.isEmpty(vos.getData())) {
                throw new Exception("查询不到应用code信息，代码地址为：" + param.getProject().getGit_http_url());
            }
            appCode = vos.getData().get(0).getAppAlias();
            param.setAppCode(vos.getData().get(0).getAppAlias());
        }
        log.info("上报参数应用code为:" + appCode);

        Map<String, String> result = new HashMap<String, String>();
        result.put("codePath", param.getProject().getGit_http_url());

        if (StringUtils.isBlank(param.getRef())) {
            param.setRef("master");
        }
        String[] branchs = param.getRef().split("/");
        String branch = branchs[branchs.length - 1];
        String pomFileDir = null;
        String profile = null;
        String language = param.getLanguage();


        List<DevCodeInfo> infos=getDevInfo(param.getProject().getGit_http_url(),param.getAppCode());

        if (!CollectionUtils.isEmpty(infos)) {
            saveDevInfo(infos.get(0).getId(),"",branch,param.getProject().getGit_http_url(),appCode,"updateBranch");
            pomFileDir = infos.get(0).getCompilePath();
            if (StringUtils.isNotBlank(pomFileDir)) {
                char firstChar = pomFileDir.charAt(0);
                // 判断第一个字符是否是 '/'
                if (firstChar == '/') {
                    pomFileDir = pomFileDir.substring(1, pomFileDir.length());
                }

            }
            if(StringUtils.isBlank(language)){
                language=infos.get(0).getLanguage();
                param.setLanguage(language);
            }

            if(StringUtils.isBlank(param.getProfile())){
                profile = infos.get(0).getCompileParam();
            }else if(!"/".equals(param.getProfile())){
                profile = param.getProfile();
            }
//            //dev_code_info 中的branch
//            if(StringUtils.isNotBlank(infos.get(0).getBranch())){
//                branch = infos.get(0).getBranch();
//            }
        }else{
            saveDevInfo(null,"",branch,param.getProject().getGit_http_url(),appCode,"insertInfo");
        }

        for (PipeLine line : lines) {
            if ("SCM_URL".equals(line.getKey())) {
                line.setValue(param.getProject().getGit_http_url());
            }
            if ("SCM_BRANCH".equals(line.getKey())) {
                line.setValue(branch);
            }
            if ("SCM_TAR_DIR".equals(line.getKey())) {
                line.setValue(line.getValue() + "/" + appCode);
            }
            if ("JDOS_APP_CODE".equals(line.getKey())) {
                line.setValue(appCode);
            }
            if ("PLUGIN_VERISON".equals(line.getKey())) {
                Properties config = duccConfigServiceAdapter.queryPropertiesByConfigCode("config");
                line.setValue(config.getProperty("japiMavenPluginVersion", "1.0.3-SNAPSHOT"));
            }
            if ("FUNCITON".equals(line.getKey())) {
                line.setValue("japidoc");
            }
            if (StringUtils.isNotBlank(param.getProfile())) {
                profile = param.getProfile();
            }
            if ("PROFILE".equals(line.getKey()) && StringUtils.isNotBlank(profile)) {
                line.setValue(profile);
            }
            if (StringUtils.isNotBlank(param.getCompilePath())) {
                pomFileDir = param.getCompilePath();
            }
            if ("POMFILEDIR".equals(line.getKey()) && StringUtils.isNotBlank(pomFileDir)) {
                if (pomFileDir.equals("./")) {
                    continue;
                }
                line.setValue(pomFileDir);
            }
            if ("FLOWID".equals(line.getKey()) && StringUtils.isNotBlank(param.getFlowId())) {
                line.setValue(param.getFlowId());
            }
        }

        for (AtomInfo atomInfo : atomInfos) {
            if ("DOCUPLOAD".equals(atomInfo.getAtomId())) {
//                JDosAppInfo info = jagileService.getAppInfo(appCode);
                //http://xingyun.jd.com/upipe/imagestore/detail/1
                String mavenMirror="hub.jdcloud.com/inner/maven:3.6.3-jdk__1.8.0_331.20230824144406278";
                String version="3.6.3";
                if("java-11".equalsIgnoreCase(language)||"jdk-11".equalsIgnoreCase(language)){
                    version="3.5.4";
                    mavenMirror="hub.jdcloud.com/inner/maven:3.5.4-jdk__11.0.11.20230824144405499";
                }
                if("java-17".equalsIgnoreCase(language)||"jdk-17".equalsIgnoreCase(language)){
                    mavenMirror="hub.jdcloud.com/inner/maven:3.6.3-jdk__17.0.7.20230824144407185";
                }
                if("java-1.6".equalsIgnoreCase(language)||"java-6".equalsIgnoreCase(language)||"jdk-6".equalsIgnoreCase(language)||"jdk-1.6".equalsIgnoreCase(language)){
                    version="3.5.4";
                    mavenMirror="hub.jdcloud.com/inner/maven:3.5.4-jdk__1.6.0_25.20240705175600658";
                }
                if("java-1.7".equalsIgnoreCase(language)||"java-7".equalsIgnoreCase(language)||"jdk-7".equalsIgnoreCase(language)||"jdk-1.7".equalsIgnoreCase(language)){
                    version="3.5.4";
                    mavenMirror="hub.jdcloud.com/inner/maven:3.5.4-jdk__1.7.0_71.20240314170225972";
                }


                atomInfo.setAtomVersion(version);
                atomInfo.setAtomImage(mavenMirror);
            }
        }
    }


    private List<DevCodeInfo> getDevInfo(String codePath,String appCode){
        LambdaQueryWrapper<DevCodeInfo> wr = new LambdaQueryWrapper<>();
        wr.eq(DevCodeInfo::getName, appCode);
        String path = "";
        if (path.contains("http")) {
            path = codePath.substring(7, codePath.length());
        }
        if (path.contains("https")) {
            path =codePath.substring(8, codePath.length());
        }
        wr.like(DevCodeInfo::getCodePath, path);
        return devCodeService.list(wr);
    }


    public void sendUserJueMsg(List<String> erps,SyncJsfDocLog log) {
        Properties config = duccConfigServiceAdapter.queryPropertiesByConfigCode("config");
        String flag = config.getProperty("sendJme", "false");
        if ("false".equals(flag)) {
            return;
        }
        TemplateMsgDTO templateMsgDTO = new TemplateMsgDTO();
        templateMsgDTO.setHead("接口文档上报通知");
        templateMsgDTO.setSubHeading("应用"+log.getAppCode()+"进行了行云部署，接口文档进行更新");
//        if (org.apache.commons.lang.StringUtils.isNotEmpty(dto.getErp())) {
//            UserVo userVo = userHelper.getUserBaseInfoByUserName(dto.getErp());
//            UserDTO userDTO = new UserDTO();
//            userDTO.setTenantCode("ee");
//            userDTO.setErp(dto.getErp());
//            userDTO.setRealName(userVo.getRealName());
//            List<UserDTO> userDTOList = Lists.newArrayList();
//            userDTOList.add(userDTO);
//            templateMsgDTO.setAtUsers(userDTOList);
//
//        }

        List<ButtonDTO> buttonDTOList = Lists.newArrayList();
        ButtonDTO buttonDTO = new ButtonDTO();


        if(log.getStatus()==1){
            buttonDTO.setName("查看详情");
            buttonDTO.setPcUrl(log.getResultUrl());

            LambdaQueryWrapper<SyncJsfDocLog> wrapper=new LambdaQueryWrapper<SyncJsfDocLog>();
            wrapper.eq(SyncJsfDocLog::getFlowId,log.getFlowId());
//            wrapper.eq(SyncJsfDocLog::getType,log.getType());
            wrapper.eq(SyncJsfDocLog::getYn,1);
            SyncJsfDocLog docLog=syncJsfDocLogService.getOne(wrapper);

            templateMsgDTO.setContent("上报id:"+log.getFlowId()+",上报成功,jsf数:"+docLog.getJsfNum()+",http数:"+docLog.getHttpNum());
            buttonDTOList.add(buttonDTO);
        }else{
            errorHandle(log,buttonDTOList,templateMsgDTO);
        }

        templateMsgDTO.setButtons(buttonDTOList);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(erps)) {
            for (String receiveErp : erps) {
                sendMsgService.sendUserJueMsg(receiveErp, templateMsgDTO);
            }
        }
    }

    private void  errorHandle(SyncJsfDocLog log, List<ButtonDTO> buttonDTOList,TemplateMsgDTO templateMsgDTO) {
        List<DevCodeInfo> infos = getDevInfo(log.getCodePath(), log.getAppCode());
        templateMsgDTO.setContent("上报id:"+log.getFlowId()+","+log.getRemart());
        WebHookVo webHookVo = new WebHookVo();
        boolean repeatUpload=false;
        if(log.getSubStatus()==1){
            //处理依赖包找不到
            repeatUpload=true;
            webHookVo.setCompilePath("/");
            webHookVo.setProfile("/");

        }
        if(log.getSubStatus()==2){
            repeatUpload=true;
            Long id=null;
            //pom文件找不到
            if(CollectionUtils.isEmpty(infos)){
                id=infos.get(0).getId();
            }
            templateMsgDTO.setContent(log.getRemart()+
                              "\n修改参数链接：http://data-flow.jd.com/webhook/updateParam?id="+id+"&pomPath=/");


        }
        //编译版本不匹配,需要java
        if(log.getSubStatus()==3){
            repeatUpload=true;
            //处理language
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(log.getRemart());
            if (matcher.find()) {
                System.out.println("找到java版本数字: " + matcher.group());
                Long id=CollectionUtils.isEmpty(infos)?null:infos.get(0).getId();
                saveDevInfo(id,"JAVA-"+matcher.group(),"",log.getCodePath(),log.getAppCode(),"updateLanguage");
            }
        }


        if(repeatUpload) {
            webHookVo.setFlowId(log.getFlowId());
            //脚本
            webHookVo.setType(1);
            webHookVo.setRef(CollectionUtils.isEmpty(infos) ? "master" : infos.get(0).getBranch());
            Project project = new Project();
            project.setGit_http_url(log.getCodePath());
            webHookVo.setProject(project);

            String url = "http://data-flow.jd.com/webhook/batchExe?appCodeStr="+log.getAppCode()+"&path="+log.getCodePath()+"&flowId="+log.getFlowId();
            ButtonDTO buttonDTO2 = new ButtonDTO();
            buttonDTO2.setName("重新上报");
            buttonDTO2.setPcUrl(url);
            buttonDTOList.add(buttonDTO2);
        }
        ButtonDTO buttonDTO = new ButtonDTO();
        buttonDTO.setName("查看流水线");
        buttonDTO.setPcUrl(log.getBuildUrl());
        buttonDTOList.add(buttonDTO);
    }

    private void saveDevInfo(Long id, String language,String branch,String codePath,String appCode,String creator) {
        if(null==id){
            DevCodeInfo info=new DevCodeInfo();
            info.setYn(1);
            info.setCreated(new Date());
            info.setCreator(creator);
            info.setCodePath(codePath);
            info.setBranch("master");
            info.setName(appCode);
            devCodeService.save(info);
            log.info("devCodeService保存结果{},",info.getId());
        }else{
            LambdaUpdateWrapper<DevCodeInfo> wrapper=new LambdaUpdateWrapper<>();
            wrapper.set(StringUtils.isNotEmpty(language),DevCodeInfo::getLanguage,language);
            wrapper.set(StringUtils.isNotEmpty(branch),DevCodeInfo::getBranch,branch);
            wrapper.set(DevCodeInfo::getCodePath,codePath);
            wrapper.set(DevCodeInfo::getModifier,creator);
            wrapper.set(DevCodeInfo::getModified, new Date());
            wrapper.eq(DevCodeInfo::getId,id);
            devCodeService.update(wrapper);
            log.info("devCodeService更新{},",id);
        }

    }
}
