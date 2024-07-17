package com.jd.workflow.console.service.plugin.jdos;

import com.jd.workflow.console.dto.plugin.jdos.JdosGroup;
import com.jd.workflow.console.dto.plugin.jdos.JdosPod;
import com.jd.workflow.console.dto.plugin.jdos.JdosSystemApps;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * <p>
 * jdos 抽象
 * </p>
 *
 * @author sunchao81
 * @date 2022-09-19
 */
public abstract class JdosAbstract {

    /**
     *
     */
    JdosRest jdosRest;

    /**
     *
     */
    public void setJdosRest(JdosRest jdosRest) {
        this.jdosRest = jdosRest;
    }

    /**
     *
     */
    public abstract boolean getAppExists(String systemName, String appName);

    /**
     *
     */
    public abstract String createApp(String systemName, String appName, String userErp);


    /**
     *
     * @param system
     * @param app
     * @param tag
     * @return
     */
    public abstract ResponseEntity<String> queryCompileString(String system, String app, String tag, String erp);

    /**
     *
     */
    public abstract List<JdosSystemApps> getSystemApps(String erp);

    /**
     * 通过环境查分组
     * @param appCode
     * @return
     */
    public abstract String getTestJdosGroupName(String appCode);

    /**
     * 通过分组取文件列表
     * @param appCode
     * @param groupName
     * @return
     */
    public abstract String getConfigUuids(String appCode, String groupName);

    /**
     * 根据uuid 查文件内容
     * @param appCode
     * @param groupName
     * @param uuid
     * @return
     */
    public abstract String getConfigContent(String appCode, String groupName, String uuid);

    /**
     *
     * @param systemName
     * @param appName
     * @param gitUrl
     * @param filePath
     * @param tag
     * @param baseImage
     * @param gitBranch
     * @return
     */
    public abstract String appBuildByDockerfile(String systemName, String appName, String gitUrl, String filePath, String tag, String baseImage, String gitBranch,String erp);

    /**
     *
     * @param appCode
     * @return
     */
    public abstract Boolean isV3App(String appCode);

    /**
     * 03 获取IP
     */
    public abstract List<JdosPod> getIps(String appName, String groupName);

    /**
     * 02 获取分组
     */
    public abstract List<JdosGroup> getGroups(String appCode);

}
