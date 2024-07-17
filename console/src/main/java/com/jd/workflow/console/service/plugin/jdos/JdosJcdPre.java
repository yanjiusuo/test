package com.jd.workflow.console.service.plugin.jdos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dto.plugin.jdos.JdosApps;
import com.jd.workflow.console.dto.plugin.jdos.JdosGroup;
import com.jd.workflow.console.dto.plugin.jdos.JdosPod;
import com.jd.workflow.console.dto.plugin.jdos.JdosSystemApps;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * jdos 3.0 的实现
 * </p>
 *
 * @author sunchao81
 * @date 2022-09-19
 */
@Slf4j
@Service
public class JdosJcdPre extends JdosAbstract {

    /**
     *
     */
    @Resource
    JdosRestPreImpl jdosRestPreImpl;

    /**
     * 01 获取自己的系统和应用
     */
    @Override
    public List<JdosSystemApps> getSystemApps(String erp) {
        List<JdosSystemApps> jdosSystemApps = Lists.newArrayList();
        this.setJdosRest(jdosRestPreImpl);
        String format = "http://api.jcd-gateway.jd.com/api/v2/apps?userErp=%s&pageNum=1&pageSize=1000&allApp=true";
        String url = String.format(format, erp);
        String lsResponse = jdosRest.invokeGet(url, "JDD");
        if (StringUtils.isNotEmpty(lsResponse)) {
            jdosSystemApps.addAll(getJdosSystemApps(lsResponse));
        }
        return jdosSystemApps;
    }

    /**
     * 02 获取分组
     */
    @Override
    public List<JdosGroup> getGroups(String appCode) {
        String response = null;
        List<JdosGroup> groupNameList = new ArrayList<>();
        try {
            response = getTestJdosGroupName(appCode);
            log.info("fetchJdosGroupNameList.response:[{},{}]", appCode, response);
            JSONArray jsonArray = JSON.parseArray(response);
            if (CollectionUtils.isNotEmpty(jsonArray)) {
                for (Object o : jsonArray) {
                    JSONObject jsonObject = JSON.parseObject(o.toString());
                    if (StringUtils.isNotEmpty(jsonObject.getString("environment"))
                            && "pre".equals(jsonObject.getString("environment"))) {
                        JdosGroup jdosGroup = new JdosGroup();
                        jdosGroup.setGroupName(jsonObject.getString("groupName"));
                        jdosGroup.setNickname(jsonObject.getString("nickname"));
                        groupNameList.add(jdosGroup);
                    }
                }
            }
            return groupNameList;
        } catch (Exception e) {
            log.error("fetchJdosGroupNameList.error:[{}]", appCode, e);
            return groupNameList;
        }
    }


    /**
     * 03 获取IP
     */
    @Override
    public List<JdosPod> getIps(String appName, String groupName) {
        List<JdosPod> podList = new ArrayList<>();
        this.setJdosRest(jdosRestPreImpl);
        try {
            String format = "http://api.jcd-gateway.jd.com//api/v2/apps/%s/groups/%s/cluster/pods";
            String url = String.format(format, appName, groupName);
            String response = jdosRest.invokeGet(url);
            JSONObject resObj = JSON.parseObject(response);
            if (resObj.getBoolean("success")) {
                String list = resObj.getJSONObject("data").getString("list");
                JSONArray jsonArray = JSON.parseArray(list);
                if (CollectionUtils.isNotEmpty(jsonArray)) {
                    for (Object o : jsonArray) {
                        JSONObject jsonObject = JSON.parseObject(o.toString());
                        JdosPod pod = new JdosPod();
                        pod.setPodIp(jsonObject.getString("podIP"));
                        pod.setImage(jsonObject.getString("image"));
                        podList.add(pod);
                    }
                }
            }
            return podList;
        } catch (Exception e) {
            log.error("getIps.error:[{},{}]", appName, groupName, e);
            return podList;
        }
    }

    /**
     * 02 获取分组
     *
     * @param appCode
     * @return
     */
    @Override
    public String getTestJdosGroupName(String appCode) {
        this.setJdosRest(jdosRestPreImpl);
        String format = "http://api.jcd-gateway.jd.com/api/v2/apps/%s/groups?pageSize=50";
        String url = String.format(format, appCode);
        String response = jdosRest.invokeGet(url);
        JSONObject resObj = JSON.parseObject(response);
        if (resObj.getBoolean("success")) {
            String list = resObj.getJSONObject("data").getString("list");
            JSONArray listArr = JSON.parseArray(list);
            Integer records = resObj.getJSONObject("data").getInteger("records");
            if (records > 50) {
                int page = records / 50 + 2;

                for (int i = 2; i < page; i++) {
                    String pageFormat = "http://api.jcd-gateway.jd.com/api/v2/apps/%s/groups?pageNum=%s&pageSize=50";
                    url = String.format(pageFormat, appCode, i);
                    String responsePage = jdosRest.invokeGet(url);
                    JSONObject resObjPage = JSON.parseObject(responsePage);
                    if (resObjPage.getBoolean("success")) {

                        listArr.addAll(JSON.parseArray(resObjPage.getJSONObject("data").getString("list")));

                    }
                }

            }


            return listArr.toJSONString();
        }
        return "[]";
    }


    /**
     * @param response
     * @return
     */
    private List<JdosSystemApps> getJdosSystemApps(String response) {
        List<JdosSystemApps> jdosSystemApps = Lists.newArrayList();
        JSONObject resObj = JSON.parseObject(response);
        String list = resObj.getJSONObject("data").getString("list");
        List<JdosApps> jdosApps = JSON.parseObject(list, new TypeReference<List<JdosApps>>() {
        });
        if (CollectionUtils.isNotEmpty(jdosApps)) {
            Map<String, List<JdosApps>> map = jdosApps.stream().filter(jdosApps1 -> "NORMAL".equals(jdosApps1.getCategory())).collect(Collectors.groupingBy(JdosApps::getSystemName));
            List<String> systemList = jdosApps.stream().map(JdosApps::getSystemName).distinct().collect(Collectors.toList());
            for (String systemName : systemList) {
                JdosSystemApps jdosSystem = new JdosSystemApps();
                jdosSystem.setSystemName(systemName);
                jdosSystem.setNickname(systemName);
                jdosSystem.setApps(map.get(systemName));
                if (CollectionUtils.isNotEmpty(map.get(systemName))) {
                    jdosSystem.setId(map.get(systemName).get(0).getId());
                } else {
                    jdosSystem.setId(0 + "");
                }
                jdosSystemApps.add(jdosSystem);
            }
            return jdosSystemApps;
        }
        return jdosSystemApps;
    }


    /**
     * 通过分组取文件列表
     *
     * @param appCode
     * @param groupName
     * @return
     */
    @Override
    public String getConfigUuids(String appCode, String groupName) {
        this.setJdosRest(jdosRestPreImpl);
//        String format = "http://api.jcd-gateway.jd.com/api/v2/apps/%s/groups/%s/configs";
        String format = "http://api.jcd-gateway.jd.com/api/v2/apps/%s/groups/%s/onlineConfigs";
        String url = String.format(format, appCode, groupName);
        String response = jdosRest.invokeGet(url);
        JSONObject resObj = JSON.parseObject(response);
        if (resObj.getBoolean("success")) {
            JSONArray data = resObj.getJSONArray("data");
            if (!data.isEmpty()) {
                return data.get(0).toString();
            }
        }
        return "{}";
    }

    /**
     * 根据uuid 查文件内容
     *
     * @param appCode
     * @param groupName
     * @param uuid
     * @return
     */
    @Override
    public String getConfigContent(String appCode, String groupName, String uuid) {
        this.setJdosRest(jdosRestPreImpl);
        String format = "http://api.jcd-gateway.jd.com/api/v2/apps/%s/groups/%s/configFiles/%s";
        String url = String.format(format, appCode, groupName, uuid);
        String response = jdosRest.invokeGet(url);
        return response;
    }

    /**
     * @param systemName
     * @param appName
     * @param gitUrl
     * @param filePath
     * @param tag
     * @param baseImage
     * @param gitBranch
     * @return
     */
    @Override
    public String appBuildByDockerfile(String systemName, String appName, String gitUrl, String filePath, String tag, String baseImage, String gitBranch, String erp) {
        // 创建任务
        this.setJdosRest(jdosRestPreImpl);
        String format = "http://api.jcd-gateway.jd.com/api/v2/builder/systems/%s/apps/%s/tasks?operator=%s";
        String url = String.format(format, systemName, appName, erp);
        HashMap<String, Object> param = new HashMap<>();
        param.put("build_method", "dockerfile");
        param.put("code_branch", gitBranch);
        param.put("code_repo", gitUrl);
        param.put("git_type", 1);
        param.put("runtime_image", baseImage);
        param.put("dockerfile_path", filePath);
        param.put("tag", tag);
        String task = jdosRest.invokePost(url, JsonUtils.toJSONString(param));
        if (StringUtils.isNotEmpty(task)) {
            String taskId = JSON.parseObject(task).getString("data");
            if (StringUtils.isNotEmpty(taskId)) {
                // 执行任务
                String callFormat = "http://api.jcd-gateway.jd.com/api/v2/builder/systems/%s/apps/%s/tasks/%s?operator=%s";
                String callUrl = String.format(callFormat, systemName, appName, taskId, erp);
                HashMap<String, Object> callParam = new HashMap<>();
                callParam.put("image_tag", tag);
                String response = jdosRest.invokePost(callUrl, JsonUtils.toJSONString(callParam));
                return response;
            }
        }
        return null;
    }

    /**
     * @param appCode
     * @return
     */
    @Override
    public Boolean isV3App(String appCode) {
        this.setJdosRest(jdosRestPreImpl);
        String format = "http://api.jcd-gateway.jd.com/api/v2/apps/%s";
        String url = String.format(format, appCode);
        String response = jdosRest.invokeGet(url);
        JSONObject resObj = JSON.parseObject(response);
        String appVersion = resObj.getJSONObject("data").getString("appVersion");
        if (StringUtils.isNotEmpty(appVersion) && appVersion.equalsIgnoreCase("JDOS_V3")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean getAppExists(String systemName, String appName) {
        return false;
    }

    @Override
    public String createApp(String systemName, String appName, String userErp) {
        return null;
    }

    @Override
    public ResponseEntity<String> queryCompileString(String system, String app, String tag, String erp) {
        return null;
    }
}
