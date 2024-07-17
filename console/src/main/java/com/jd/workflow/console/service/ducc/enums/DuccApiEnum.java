package com.jd.workflow.console.service.ducc.enums;

import com.jd.workflow.console.service.ducc.entity.DuccApiPathVars;
import org.springframework.http.HttpMethod;

import java.util.Objects;

/**
 * DuccApiEnum
 *
 * @author wangxianghui6
 * @date 2022/3/2 10:21 AM
 */
public enum DuccApiEnum {
    /**
     *
     */
    API_CREATE_CONFIG("/v1/namespace/:nsId/config", HttpMethod.POST, "创建配置"),
    /**
     *
     */
    API_DELETE_CONFIG("/v1/namespace/:nsId/config/:configId", HttpMethod.DELETE, "删除配置"),
    /**
     *
     */
    API_QUERY_ALL_CONFIG("/v1/namespace/:nsId/configs", HttpMethod.GET, "查询所有配置"),
    /**
     *
     */
    API_QUERY_ITEMS("/v1/namespace/:nsId/config/:configId/profile/:profileId/items", HttpMethod.GET,"查看所有配置项"),
    /**
     *
     */
    API_QUERY_ITEM("/v1/namespace/:nsId/config/:configId/profile/:profileId/item/:itemId", HttpMethod.GET,"查看某个配置项"),
    /**
     *
     */
    API_DELETE_CONFIGITEM("/v1/namespace/:nsId/config/:configId/profile/:profileId/item/:itemId", HttpMethod.DELETE, "删除配置项"),
    /**
     *
     */
    API_CREATE_PROFILE("/v1/namespace/:nsId/config/:configId/profile", HttpMethod.POST, "创建环境"),
    /**
     *
     */
    API_DELETE_PROFILE("/v1/namespace/:nsId/config/:configId/profile/:profileId", HttpMethod.DELETE, "删除环境"),
    /**
     *
     */
    API_QUERY_ALL_PROFILE("/v1/namespace/:nsId/config/:configId/profiles", HttpMethod.GET, "查询所有环境"),
    /**
     *
     */
    API_RELEASE_PROFILE("/v1/namespace/:nsId/config/:configId/profile/:profileId/release", HttpMethod.PUT, "发布环境"),
    /**
     *
     */
    API_UPDATE_ITEMS("/v1/namespace/:nsId/config/:configId/profile/:profileId/items", HttpMethod.PUT, "更新配置项"),
    /**
     *
     */
    API_ADD_ITEMS("/v1/namespace/:nsId/config/:configId/profile/:profileId/item", HttpMethod.POST, "添加配置项"),
    /**
     *
     */
    API_UPDATE_ITEM("/v1/namespace/:nsId/config/:configId/profile/:profileId/item/:itemId", HttpMethod.PUT,"更新某个配置项");

    /**
     * api路径
     */
    private final String path;

    /**
     * 方法
     */
    private final HttpMethod method;

    /**
     * 接口描述
     */
    private final String description;

    DuccApiEnum(String path, HttpMethod method, String description) {
        this.path = path;
        this.method = method;
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getDescription() {
        return description;
    }

    public String getPath(DuccApiPathVars pathVars) {
        String finalPath = this.path;
        if (Objects.nonNull(pathVars.getNamespaceId())) {
            finalPath = finalPath.replaceAll(":nsId", pathVars.getNamespaceId());
        }
        if (Objects.nonNull(pathVars.getConfigId())) {
            finalPath = finalPath.replaceAll(":configId", pathVars.getConfigId());
        }
        if (Objects.nonNull(pathVars.getProfileId())) {
            finalPath = finalPath.replaceAll(":profileId", pathVars.getProfileId());
        }
        if (Objects.nonNull(pathVars.getItemId())) {
            finalPath = finalPath.replaceAll(":itemId", pathVars.getItemId());
        }
        return finalPath;
    }

    public String getAbsolutePath(String host, DuccApiPathVars pathVars) {
        if (host.endsWith("/")) {
            return host.substring(0, host.length() - 2) + getPath(pathVars);
        } else {
            return host + getPath(pathVars);
        }
    }
}
