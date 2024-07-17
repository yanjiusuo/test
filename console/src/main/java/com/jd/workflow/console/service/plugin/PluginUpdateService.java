package com.jd.workflow.console.service.plugin;

import com.alibaba.excel.util.IoUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.jss.JingdongStorageService;
import com.jd.jss.domain.ObjectListing;
import com.jd.jss.domain.ObjectSummary;
import com.jd.jss.domain.StorageObject;
import com.jd.jss.service.ObjectService;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.config.JssConfig;
import com.jd.workflow.console.dto.plugin.PluginConfig;
import com.jd.workflow.console.dto.plugin.PluginConfigItem;
import com.jd.workflow.console.dto.test.deeptest.TestResult;
import com.jd.workflow.console.entity.doc.AppDocReportRecord;
import com.jd.workflow.console.service.doc.impl.DocReportServiceImpl;
import com.jd.workflow.console.service.plugin.jdos.EoneLaneResponse;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.Md5Utils;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class PluginUpdateService {
    @Autowired
    JssConfig jssConfig;
    @Value("${plugin.downloadUrl:http://data-flow.jd.com}")
    private String downloadUrl;
    @Value("${eone.baseUrl:http://eone-api.jd.com}")
    private String eoneBaseUrl="http://eone-api.jd.com";
    @Autowired
    DocReportServiceImpl docReportService;

    public List<PluginConfigItem> buildPluginConfig(){
        ObjectService objectService = jssConfig.getJssClient().bucket("lht").object("idea_plugins/config.json");
        StorageObject storageObject = null;
        try {
            storageObject = objectService.get();
            String config = IOUtils.toString(storageObject.getInputStream(), "utf-8");
            List<PluginConfigItem> items = JsonUtils.parseArray(config,PluginConfigItem.class);
            for (PluginConfigItem item : items) {
                item.setUrl(downloadUrl+"/plugin/downloadApiPlugin/"+item.getId());
            }
            return items;
        } catch (IOException e) {
            throw new BizException("更新插件失败",e);
        }finally {
            if(storageObject != null){
                storageObject.close();
                IOUtils.closeQuietly(storageObject);
            }

        }


    }
    private static Date parseDate(String dateString){
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        // 因为原日期字符串是GMT时区，所以这里要设置时区，以便于正确解析
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            // 使用parse方法将字符串日期转换为Date对象
            Date date = dateFormat.parse(dateString);
            return date;
        } catch (ParseException e) {
           return null;
        }
    }
    public Long getLatestAgentFileVersion(){
        String lastModified = getLatestAgentFile().getLastModified();
        if(lastModified == null){
            return null;
        }
        Date date = parseDate(lastModified);
        if(date == null) return null;
        return date.getTime();
    }
    public void downloadAgentFile(String remoteIp, HttpServletResponse response){
        JingdongStorageService jss = jssConfig.getJssClient();
        ObjectListing list = jss.bucket("lht").prefix("idea-agent-jars/").listObject();
        StorageObject storageObject = null;
        try {
             ObjectSummary summary =  getLatestAgentFile();

            AppDocReportRecord record = new AppDocReportRecord();
            record.setReportTime(new Date());
            record.setAppCode("hotswapAgent");
            record.setHttpAppCode(null);
            record.setIp(remoteIp);
            record.setCreator(UserSessionLocal.getUser().getUserId());
            record.setModifier(UserSessionLocal.getUser().getUserId());
            record.setDigest("1");
            Map<String,Object> config = new HashMap<>();
            config.put("ip",remoteIp);
            String path = summary.getKey();
            String zipName = StringHelper.lastPart(path,'/');
            config.put("version",zipName);
            record.setContent(JsonUtils.toJSONString(config));
            docReportService.save(record);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");

            ObjectService object = jss.bucket("lht").object(path);
            storageObject = object.get();
            IoUtils.copy(storageObject.getInputStream(),response.getOutputStream());

            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BizException("更新插件失败",e);
        } finally {
            if(storageObject !=null){
                IOUtils.closeQuietly(storageObject.getInputStream());
                storageObject.close();
            }
        }
    }
    public ObjectSummary getLatestAgentFile(){
        JingdongStorageService jss = jssConfig.getJssClient();
        ObjectListing list = jss.bucket("lht").prefix("idea-agent-jars/").listObject();

            List<ObjectSummary> paths = new ArrayList<>();
            for (ObjectSummary objectSummary : list.getObjectSummaries()) {
                paths.add(objectSummary);

            }
            Collections.sort(paths, new Comparator<ObjectSummary>() {
                @Override
                public int compare(ObjectSummary o1, ObjectSummary o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            return paths.get(paths.size() - 1);
    }
    public void download(String pluginId,String remoteIp, HttpServletResponse response){
        JingdongStorageService jss = jssConfig.getJssClient();
        ObjectListing list = jss.bucket("lht").prefix("idea_plugins/"+pluginId).listObject();
        StorageObject storageObject = null;
        try {
            List<String> paths = new ArrayList<>();
            for (ObjectSummary objectSummary : list.getObjectSummaries()) {
                paths.add(objectSummary.getKey());
            }
            Collections.sort(paths);

            AppDocReportRecord record = new AppDocReportRecord();
            record.setReportTime(new Date());
            record.setAppCode("plugin_"+pluginId);
            record.setHttpAppCode(null);
            record.setIp(remoteIp);
            record.setCreator(UserSessionLocal.getUser().getUserId());
            record.setModifier(UserSessionLocal.getUser().getUserId());
            record.setDigest("1");
            Map<String,Object> config = new HashMap<>();
            config.put("ip",remoteIp);
            String path = paths.get(paths.size() - 1);
            String zipName = StringHelper.lastPart(path,'/');
            config.put("version",zipName);
            record.setContent(JsonUtils.toJSONString(config));
            docReportService.save(record);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipName + "\"");

            ObjectService object = jss.bucket("lht").object(path);
            storageObject = object.get();
            IoUtils.copy(storageObject.getInputStream(),response.getOutputStream());

            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BizException("更新插件失败",e);
        } finally {
            if(storageObject !=null){
                IOUtils.closeQuietly(storageObject.getInputStream());
                storageObject.close();
            }
        }
    }


    public List<EoneLaneResponse> queryEoneLaneList(String codeRepo,String codeBranch,String username,String jdosEnv){
        String path = "/open/v1/task/query-by-code";
        Map<String,Object> params = buildEoneParams(codeRepo, codeBranch, username, jdosEnv);;
        RequestClient requestClient = new RequestClient(eoneBaseUrl,makeEoneHeaders(username));
        TestResult<List<EoneLaneResponse>> result = requestClient.get(path, params, new TypeReference<TestResult<List<EoneLaneResponse>>>() {
        });
        return result.getData();
    }
    private Map<String,Object> makeEoneHeaders(String username){
        //String secret = "419b4d91a3d81727";
        String secret = "0b2cbae71e78f6e8";
        Map<String,Object> params = new HashMap<>();
        long timestamp = System.currentTimeMillis();
        params.put("Eone-Api-Timestamp", timestamp);
        params.put("Eone-Api-App-Code","dataflow");
        params.put("Eone-User-Erp",username);
        params.put("Eone-Api-Token", Md5Utils.md5(timestamp+secret));
        return params;
    }
    private Map<String,Object> buildEoneParams(String codeRepo,String codeBranch,String username,String jdosEnv){
        Map<String,Object> params = new HashMap<>();
        params.put("codeRepo", codeRepo);
        params.put("codeBranch",codeBranch);
        params.put("username",username);
        params.put("jdosEnv",0);
        return params;
    }
    public String queryCreateUrl(String codeRepo,String codeBranch,String username,String jdosEnv){
        String path = "/open/v1/task/create-url/query-by-code";
        Map<String,Object> params = buildEoneParams(codeRepo, codeBranch, username, jdosEnv);
        RequestClient requestClient = new RequestClient(eoneBaseUrl,makeEoneHeaders(username));
        TestResult<String> result = requestClient.get(path, params, new TypeReference<TestResult<String>>() {
        });
        return result.getData();
    }

    public static void main(String[] args) {
        JssConfig config = new JssConfig();
        config.setAccessKey("ZDcl7q0ygh8Asm9a");
        config.setSecretKey("uqrdal5WRv7BC8iqJIUPwox76xdBK3r1BgGgxkpP");
        config.setHostName("storage.jd.local");
        config.setConnectionTimeout(1000);
        PluginUpdateService updateService = new PluginUpdateService();
        updateService.jssConfig = config;
        String lastModified = updateService.getLatestAgentFile().getLastModified();
        Date date = parseDate(lastModified);
        System.out.println(lastModified);
    }
}
