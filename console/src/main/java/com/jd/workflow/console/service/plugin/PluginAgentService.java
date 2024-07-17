package com.jd.workflow.console.service.plugin;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/3/6
 */

import com.jd.common.util.StringUtils;
import com.jd.fastjson.JSON;
import com.jd.fastjson.JSONArray;
import com.jd.fastjson.JSONObject;
import com.jd.jim.cli.Cluster;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/3/6
 */
@Service
@Slf4j
public class PluginAgentService {


    /**
     * 版本信息jimdb map key
     */
    private static String HASH_VERSION_COUNT_KEY = "hash_agent_version_count";

    /**
     * 记录所有应用使用版本的jimdb hash map
     */
    private static String HASH_APP_ALL_VERSION_KEY = "hash_app_all_version";

    /***
     * 记录每个应用容器ip的jimdb set
     */
    private static String SET_APP_IPS_FORMAT = "set_app_ips_%s";

    /**
     * 记录每日应用使用版本的jimdb hash map，过期时间7天
     */
    private static String HASH_APP_ALL_VERSION_DAY_KEY_FORMAT = "hash_app_all_version_day_%s";

    /**
     * agent下载地址
     */
    @Value("${ducc.agent.urlformat:http://storage.jd.local/solution/hot/fast-deploy-%s.jar}")
    private String agent_url_format;


    @Value("${ducc.agent.defaultVersion:1.1-SNAPSHOT}")
    private String defaultVersion;

    @Value("${ducc.agent.publishVersion:[]}")
    private String publishVersion;

    /**
     * redis 客户端
     */
    @Resource(name = "jimClient")
    private Cluster jimClient;

    /**
     * 获取应用网关下载地址
     *
     * @param appCode
     * @param ip
     * @return
     */
    public String getAgentJarUrl(String appCode, String ip) {

        String defaultVersionUrl = String.format(agent_url_format, defaultVersion);

        if (StringUtils.isEmpty(appCode) || StringUtils.isEmpty(ip)) {
            return defaultVersionUrl;
        }

        Map<String, String> appVersionMap = jimClient.hGetAll(HASH_APP_ALL_VERSION_KEY);

        if (MapUtils.isNotEmpty(appVersionMap)) {
            if (appVersionMap.containsKey(appCode)) {
                String version = appVersionMap.get(appCode);
                if (StringUtils.isEmpty(version)) {
                    //为空说明版本被重置了等价为不存在版本。
                    defaultVersionUrl = getDefaultUrl(appCode, ip);

                } else {
                    //存在就取现有的版本
                    defaultVersionUrl = String.format(agent_url_format, version);
                    addAppIp(appCode, ip);
                    addTodayAppVersion(appCode, version);
                }
            } else {
                defaultVersionUrl = getDefaultUrl(appCode, ip);
            }
        } else {

            defaultVersionUrl = getDefaultUrl(appCode, ip);

        }
        return defaultVersionUrl;
    }

    /**
     * 应用启动就刷新，所以一直显示的是线上使用中的版本
     *
     * @param appCode
     * @param version
     */
    private void addTodayAppVersion(String appCode, String version) {
        Date now = new Date();
        String today = DateFormatUtils.ISO_DATE_FORMAT.format(now);
        String key = String.format(HASH_APP_ALL_VERSION_DAY_KEY_FORMAT, today);
        jimClient.hSet(key, appCode, version);
        jimClient.expire(HASH_APP_ALL_VERSION_DAY_KEY_FORMAT, 7, TimeUnit.DAYS);

    }

    private void addAppIp(String appCode, String ip) {
        //容器ip添加
        String key = String.format(SET_APP_IPS_FORMAT, appCode);
        jimClient.sAdd(key, ip);
    }

    private String getDefaultUrl(String appCode, String ip) {
        String defaultVersionUrl;
        String pubVersion = getPublishVersion();
        defaultVersionUrl = String.format(agent_url_format, pubVersion);
        //设置发布版本
        jimClient.hSet(HASH_APP_ALL_VERSION_KEY, appCode, pubVersion);
        //版本数统计+1
        jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, pubVersion, 1);
        //容器ip添加
        addAppIp(appCode, ip);

        addTodayAppVersion(appCode, pubVersion);
        return defaultVersionUrl;
    }

    private String getPublishVersion() {
        JSONArray versionArr = JSON.parseArray(publishVersion);
        if (Objects.isNull(versionArr) || versionArr.size() == 0) {
            return defaultVersion;
        }
        Map<String, String> versionCountMap = jimClient.hGetAll(HASH_VERSION_COUNT_KEY);
        for (int i = 0; i < versionArr.size(); i++) {
            JSONObject version = versionArr.getJSONObject(i);
            if (!version.containsKey("version")) {
                continue;
            }
            if (!version.containsKey("count")) {
                continue;
            }
            if ("all".equals(version.getString("count"))) {
                return version.getString("version");
            }
            String currentVersion = version.getString("version");
            Integer currentCount = version.getInteger("count");
            if (!versionCountMap.containsKey(currentVersion)) {
                jimClient.hSet(HASH_VERSION_COUNT_KEY, currentVersion, "0");
            } else {
                String countStr = jimClient.hGet(HASH_VERSION_COUNT_KEY, currentVersion);
                try {
                    if (Integer.parseInt(countStr) < currentCount) {
                        return currentVersion;
                    } else {
                        continue;
                    }
                } catch (Exception ex) {

                }
            }


        }
        return defaultVersion;
    }

    /**
     * 清空指定app的版本
     *
     * @param appCode
     * @return
     */
    public boolean clearAppVersion(String appCode) {
        String version = jimClient.hGet(HASH_APP_ALL_VERSION_KEY, appCode);
        if (StringUtils.isEmpty(version)) {
            return true;
        }

        jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, version, -1);
        jimClient.hSet(HASH_APP_ALL_VERSION_KEY, appCode, "");
        return true;
    }

    /**
     * 所有旧版本更新到新版本
     *
     * @param oldVersion
     * @param newVersion
     * @return
     */
    public Integer updateOldVersion(String oldVersion, String newVersion) {
        Integer count = 0;
        Map<String, String> appVersionMap = jimClient.hGetAll(HASH_APP_ALL_VERSION_KEY);

        if (Objects.isNull(appVersionMap)) {
            return count;
        }
        for (String appCode : appVersionMap.keySet()) {
            if (oldVersion.equals(appVersionMap.get(appCode))) {
                jimClient.hSet(HASH_APP_ALL_VERSION_KEY, appCode, newVersion);
                jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, oldVersion, -1);
                jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, newVersion, 1);
                count++;
            }
        }
        return count;
    }

    /**
     * 更新指定应用到新版本
     *
     * @param appCode
     * @param newVersion
     * @return
     */
    public boolean updateAppVersion(String appCode, String newVersion) {
        String oldVersion = jimClient.hGet(HASH_APP_ALL_VERSION_KEY, appCode);
        jimClient.hSet(HASH_APP_ALL_VERSION_KEY, appCode, newVersion);
        if (StringUtils.isNotEmpty(oldVersion)) {
            jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, oldVersion, -1);
        }
        jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, newVersion, 1);
        return true;
    }

    /**
     * 获取指定应用的版本
     *
     * @param appCode
     * @return
     */
    public String getAppVersion(String appCode) {
        return jimClient.hGet(HASH_APP_ALL_VERSION_KEY, appCode);
    }

    public Map<String, String> getVersionMap() {
        return jimClient.hGetAll(HASH_VERSION_COUNT_KEY);
    }

    public boolean clearVersionCount(String version) {
        return jimClient.hSet(HASH_VERSION_COUNT_KEY, version, "0");
    }

    public Map<String, String> getAppMap() {
        return jimClient.hGetAll(HASH_APP_ALL_VERSION_KEY);
    }

    public Set<String> getAppIps(String appCode) {
        String key = String.format(SET_APP_IPS_FORMAT, appCode);
        return jimClient.sMembers(key);
    }

    public Map<String, String> getTodayAppMap() {
        Date now = new Date();
        String today = DateFormatUtils.ISO_DATE_FORMAT.format(now);
        String key = String.format(HASH_APP_ALL_VERSION_DAY_KEY_FORMAT, today);
        return jimClient.hGetAll(key);
    }

    public Integer publishNewVersion(String newVersion) {
        Integer count = 0;
        Map<String, String> appVersionMap = jimClient.hGetAll(HASH_APP_ALL_VERSION_KEY);

        if (Objects.isNull(appVersionMap)) {
            return count;
        }
        for (String appCode : appVersionMap.keySet()) {
            String oldVersion = appVersionMap.get(appCode);
            jimClient.hSet(HASH_APP_ALL_VERSION_KEY, appCode, newVersion);
            jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, oldVersion, -1);
            jimClient.hIncrBy(HASH_VERSION_COUNT_KEY, newVersion, 1);
            count++;

        }
        return count;
    }

}
