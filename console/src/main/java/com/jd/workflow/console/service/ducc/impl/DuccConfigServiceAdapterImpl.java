package com.jd.workflow.console.service.ducc.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.jd.workflow.console.entity.Menu;
import com.jd.workflow.console.service.ducc.DuccBizConfigProperties;
import com.jd.workflow.console.service.ducc.DuccConfigServiceAdapter;
import com.jd.workflow.console.service.ducc.DuccUtils;
import com.jd.workflow.console.service.ducc.entity.*;
import com.jd.workflow.console.service.ducc.enums.DuccApiEnum;
import com.jd.workflow.console.service.ducc.enums.ProfileEnum;
import com.jd.workflow.soap.common.ducc.Item;
import com.jd.workflow.soap.common.exception.BizException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.jd.workflow.console.service.ducc.enums.DuccApiEnum.*;

/**
 * DuccConfigServiceAdapterImpl
 *
 * @author wangxianghui6
 * @date 2022/2/28 5:04 PM
 */
@Service
public class DuccConfigServiceAdapterImpl implements DuccConfigServiceAdapter, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(DuccConfigServiceAdapterImpl.class);

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private DuccBizConfigProperties bizConfigProperties;

    private HttpHeaders commonHeaders;

    private String getApiUrl(DuccApiEnum apiEnum) {
        DuccApiPathVars pathVars = new DuccApiPathVars();
        pathVars.setNamespaceId(bizConfigProperties.getNamespaceId());
        return apiEnum.getAbsolutePath(bizConfigProperties.getHost(), pathVars);
    }

    private String getApiUrl(DuccApiEnum apiEnum, String duccConfigId) {
        DuccApiPathVars pathVars = new DuccApiPathVars();
        pathVars.setNamespaceId(bizConfigProperties.getNamespaceId());
        pathVars.setProfileId(bizConfigProperties.getProfile().getName());
        pathVars.setConfigId(duccConfigId);
        return apiEnum.getAbsolutePath(bizConfigProperties.getHost(), pathVars);
    }

    private String getApiUrlItem(DuccApiEnum apiEnum,String confingId, String itemId) {
        DuccApiPathVars pathVars = new DuccApiPathVars();
        pathVars.setNamespaceId(bizConfigProperties.getNamespaceId());
        pathVars.setProfileId("prd");
        pathVars.setConfigId(confingId);
        pathVars.setItemId(itemId);
        logger.info(apiEnum.getAbsolutePath(bizConfigProperties.getHost(), pathVars));
        return apiEnum.getAbsolutePath(bizConfigProperties.getHost(), pathVars);
    }

    private String getApiUrl(DuccApiEnum apiEnum, String duccConfigId, String profileId, String itemId) {
        DuccApiPathVars pathVars = new DuccApiPathVars();
        pathVars.setNamespaceId(bizConfigProperties.getNamespaceId());
        pathVars.setConfigId(duccConfigId);
        pathVars.setProfileId(profileId);
        pathVars.setItemId(itemId);
        String uri = apiEnum.getAbsolutePath(bizConfigProperties.getHost(), pathVars);
        return uri;
    }

    private String getApiUrl(DuccApiEnum apiEnum, String duccConfigId, ProfileEnum profile) {
        return getApiUrl(apiEnum, duccConfigId, profile.getName());
    }

    private String getApiUrl(DuccApiEnum apiEnum, String duccConfigId, String profileId) {
        DuccApiPathVars pathVars = new DuccApiPathVars();
        pathVars.setNamespaceId(bizConfigProperties.getNamespaceId());
        pathVars.setConfigId(duccConfigId);
        pathVars.setProfileId(profileId);
        return apiEnum.getAbsolutePath(bizConfigProperties.getHost(), pathVars);
    }

    @Override
    public Long createConfig(String code, String name, String description) {
        DuccConfigCreateParam param = new DuccConfigCreateParam();
        param.setCode(code);
        param.setName(name);
        param.setDescription(description);
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(param, this.commonHeaders);

        ParameterizedTypeReference<DuccResult<DuccConfig>> reference = new ParameterizedTypeReference<DuccResult<DuccConfig>>() {
        };
        Long duccConfigId = null;
        ResponseEntity<DuccResult<DuccConfig>> response = restTemplate.exchange(
                getApiUrl(DuccApiEnum.API_CREATE_CONFIG),
                DuccApiEnum.API_CREATE_CONFIG.getMethod(),
                entity,
                reference);
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccConfig> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value() && Objects.nonNull(duccResult.getData())) {
                duccConfigId = duccResult.getData().getId();
            }
        }
        if (Objects.isNull(duccConfigId)) {
            logger.error("创建ducc配置失败！param: {}, response: {}", JSON.toJSONString(param), JSON.toJSON(response));
            throw new BizException("创建ducc配置失败！");
        }

        return duccConfigId;
    }

    @Override
    public void deleteConfig(String duccConfigCode) {
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);
        ParameterizedTypeReference<DuccResult<Void>> reference = new ParameterizedTypeReference<DuccResult<Void>>() {
        };

        List<DuccProfile> profiles = queryProfiles(duccConfigCode, null);
        profiles.forEach(duccProfile -> deleteProfile(duccConfigCode, duccProfile.getCode()));

        ResponseEntity<DuccResult<Void>> response = restTemplate.exchange(
                getApiUrl(DuccApiEnum.API_DELETE_CONFIG, duccConfigCode),
                DuccApiEnum.API_DELETE_CONFIG.getMethod(),
                entity,
                reference);
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<Void> duccResult = response.getBody();
            if (Objects.isNull(duccResult) || duccResult.getCode() != HttpStatus.OK.value()) {
                logger.error("删除ducc配置失败！duccConfigCode: {}, response: {}", duccConfigCode, JSON.toJSON(response));
                throw new BizException("删除ducc配置失败！");
            }
        }
    }

    @Override
    public void deleteConfigItem(Long templateId, Integer site, String duccConfigItemId) {
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);
        ParameterizedTypeReference<DuccResult<Void>> reference = new ParameterizedTypeReference<DuccResult<Void>>() {
        };


        ResponseEntity<DuccResult<Void>> response = restTemplate.exchange(
                getApiUrl(DuccApiEnum.API_DELETE_CONFIGITEM, templateId+"", DuccUtils.getProfile(site), duccConfigItemId),
                DuccApiEnum.API_DELETE_CONFIGITEM.getMethod(),
                entity,
                reference);
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<Void> duccResult = response.getBody();
            if (Objects.isNull(duccResult) || duccResult.getCode() != HttpStatus.OK.value()) {
                logger.error("删除ducc配置失败！duccConfigCode: {}, response: {}", duccConfigItemId, JSON.toJSON(response));
                throw new BizException("删除ducc配置失败！");
            }
        }
    }

    @Override
    public boolean isConfigExist(String duccConfigCode) {
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);
        ParameterizedTypeReference<DuccResult<List<DuccConfig>>> reference = new ParameterizedTypeReference<DuccResult<List<DuccConfig>>>() {
        };

        ResponseEntity<DuccResult<List<DuccConfig>>> response = restTemplate.exchange(
                getApiUrl(DuccApiEnum.API_QUERY_ALL_CONFIG),
                DuccApiEnum.API_QUERY_ALL_CONFIG.getMethod(),
                entity,
                reference);

        boolean isExist = false;
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<List<DuccConfig>> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                List<DuccConfig> duccConfigs = duccResult.getData();
                if (Objects.nonNull(duccConfigs)) {
                    isExist = duccConfigs.stream().anyMatch(duccConfig -> duccConfigCode.equals(duccConfig.getCode()));
                }
            }
        }
        return isExist;
    }

    @Override
    public boolean isConfigExist(Long templateId, Integer site, String duccConfigItemId) {
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);
        ParameterizedTypeReference<DuccResult<DuccConfig>> reference = new ParameterizedTypeReference<DuccResult<DuccConfig>>() {
        };

        ResponseEntity<DuccResult<DuccConfig>> response = restTemplate.exchange(
                getApiUrl(DuccApiEnum.API_QUERY_ITEM, templateId+"", DuccUtils.getProfile(site), duccConfigItemId),
                DuccApiEnum.API_QUERY_ITEM.getMethod(),
                entity,
                reference);
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccConfig> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                DuccConfig duccConfigs = duccResult.getData();
                if(duccConfigs != null){
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean profileSeted(List<DuccProfile> profiles, String profile){
        if(CollectionUtils.isEmpty(profiles)){
            return false;
        }
        for (DuccProfile duccProfile : profiles) {
            if(duccProfile.getCode().equals(profile)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Long updateProperties(Long duccConfigId, Properties properties, Integer site) {
        String propertiesText = properties.keySet()
                .stream()
                .map(key -> key + "=" + properties.get(key))
                .collect(Collectors.joining("\n"));
        HttpEntity<String> entity = new HttpEntity<>(propertiesText, this.commonHeaders);

        ParameterizedTypeReference<DuccResult<Long>> reference = new ParameterizedTypeReference<DuccResult<Long>>() {
        };
        ResponseEntity<DuccResult<Long>> response = restTemplate.exchange(
                getApiUrl(API_UPDATE_ITEMS, String.valueOf(duccConfigId), DuccUtils.getProfile(site)),
                API_UPDATE_ITEMS.getMethod(),
                entity,
                reference);

        Long count = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<Long> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                count = duccResult.getData();
            }
        }
        if (Objects.isNull(count)) {
            logger.error("更新ducc配置项失败！duccConfigId: {}, param: {}", duccConfigId, JSON.toJSONString(properties));
            throw new BizException("创建ducc配置失败！");
        }
        return count;
    }

    public List<Item> getItems(String duccConfigId, String profile) {
        duccConfigId = DuccUtils.getConfig(duccConfigId);
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);

        ParameterizedTypeReference<DuccResult<List<Item>>> reference = new ParameterizedTypeReference<DuccResult<List<Item>>>() {
        };
        ResponseEntity<DuccResult<List<Item>>> response = restTemplate.exchange(
                getApiUrl(API_QUERY_ITEMS, duccConfigId, profile),
                API_QUERY_ITEMS.getMethod(),
                entity,
                reference);

        List<Item> duccItems = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<List<Item>> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                duccItems = duccResult.getData();
            }
        }

        return duccItems;
    }

    /**
     * @param duccConfigItemId 可以是configId和code
     * @return
     */
    public Properties queryItemByConfigId(String templateId, String profile, String duccConfigItemId) {
//        templateId = DuccUtils.getConfig(templateId);
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);

        ParameterizedTypeReference<DuccResult<DuccItem>> reference = new ParameterizedTypeReference<DuccResult<DuccItem>>() {
        };
        ResponseEntity<DuccResult<DuccItem>> response = restTemplate.exchange(
                getApiUrl(API_QUERY_ITEM, templateId, profile, duccConfigItemId),
                API_QUERY_ITEM.getMethod(),
                entity,
                reference);

        Properties properties = new Properties();
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccItem> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                DuccItem duccItem = duccResult.getData();
                if (Objects.nonNull(duccItem)) {
                    properties.put(duccItem.getKey(), duccItem.getValue());
                }
            }else {
                return null;
            }
        }else{
            return null;
        }

        return properties;
    }

    /**
     * @param duccConfigItemId 可以是configId和code
     * @return
     */
    public Properties queryItemByConfigId(Long templateId, Integer site, String duccConfigItemId) {
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);

        ParameterizedTypeReference<DuccResult<DuccItem>> reference = new ParameterizedTypeReference<DuccResult<DuccItem>>() {
        };
        ResponseEntity<DuccResult<DuccItem>> response = restTemplate.exchange(
                getApiUrl(API_QUERY_ITEM, templateId+"", DuccUtils.getProfile(site), duccConfigItemId),
                API_QUERY_ITEM.getMethod(),
                entity,
                reference);

        Properties properties = new Properties();
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccItem> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                DuccItem duccItem = duccResult.getData();
                if (Objects.nonNull(duccItem)) {
                    properties.put(duccItem.getKey(), duccItem.getValue());
                }
            }else {
                return null;
            }
        }else{
            return null;
        }

        return properties;
    }

    @Override
    public void releaseProfile(Long tempId, String profile) {
        HttpEntity<String> entity = new HttpEntity<>("{}", this.commonHeaders);
        String configCode = DuccUtils.getConfig(tempId);
        ParameterizedTypeReference<DuccResult<String>> reference = new ParameterizedTypeReference<DuccResult<String>>() {
        };
        ResponseEntity response = restTemplate.exchange(
                getApiUrl(API_RELEASE_PROFILE, configCode, profile),
                API_RELEASE_PROFILE.getMethod(),
                entity,
                String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<String> duccResult = JSON.parseObject(response.getBody().toString(), new TypeReference<DuccResult<String>>(){});
            if (Objects.isNull(duccResult) || duccResult.getCode() != HttpStatus.OK.value()) {
                logger.error("发布ducc配置失败！response=：{},duccConfigId: {}, profile: {}",JSON.toJSONString(response), configCode, bizConfigProperties.getProfile().getName());
                throw new BizException("发布ducc配置失败！");
            }
        }
    }

    @Override
    public void releaseProfile(Long duccConfigId, Integer site) {
        HttpEntity<String> entity = new HttpEntity<>("{}", this.commonHeaders);

        ParameterizedTypeReference<DuccResult<String>> reference = new ParameterizedTypeReference<DuccResult<String>>() {
        };
        ResponseEntity response = restTemplate.exchange(
                getApiUrl(API_RELEASE_PROFILE, String.valueOf(duccConfigId), DuccUtils.getProfile(site)),
                API_RELEASE_PROFILE.getMethod(),
                entity,
                String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<String> duccResult = JSON.parseObject(response.getBody().toString(), new TypeReference<DuccResult<String>>(){});
            if (Objects.isNull(duccResult) || duccResult.getCode() != HttpStatus.OK.value()) {
                logger.error("发布ducc配置失败！response=：{},duccConfigId: {}, profile: {}",JSON.toJSONString(response), duccConfigId, bizConfigProperties.getProfile().getName());
                throw new BizException("发布ducc配置失败！");
            }
        }
    }

    @Override
    public Properties queryPropertiesByConfigId(Long duccConfigId) {
        return queryPropertiesByConfigId(String.valueOf(duccConfigId));
    }

    @Override
    public Properties queryPropertiesByConfigCode(String duccConfigCode) {
        return queryPropertiesByConfigId(duccConfigCode);
    }

    @Override
    public List<Menu> getItemsByConfigCode(String duccConfigCode) {
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);

        ParameterizedTypeReference<DuccResult<DuccItem>> reference = new ParameterizedTypeReference<DuccResult<DuccItem>>() {
        };
        ResponseEntity<DuccResult<DuccItem>> response = restTemplate.exchange(
                getApiUrlItem(API_QUERY_ITEM,"config", duccConfigCode),
                API_QUERY_ITEM.getMethod(),
                entity,
                reference);


        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccItem> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                List<Menu> duccItems = JSONArray.parseArray(duccResult.getData().getValue(),Menu.class);
                return duccItems;
            }
        }
        return null;
    }

    /**
     * @param duccConfigId 可以是configId和code
     * @return
     */
    private Properties queryPropertiesByConfigId(String duccConfigId) {
        HttpEntity<DuccConfigCreateParam> entity = new HttpEntity<>(this.commonHeaders);

        ParameterizedTypeReference<DuccResult<List<DuccItem>>> reference = new ParameterizedTypeReference<DuccResult<List<DuccItem>>>() {
        };
        ResponseEntity<DuccResult<List<DuccItem>>> response = restTemplate.exchange(
                getApiUrl(API_QUERY_ITEMS, duccConfigId),
                API_QUERY_ITEMS.getMethod(),
                entity,
                reference);

        Properties properties = new Properties();
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<List<DuccItem>> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                List<DuccItem> duccItems = duccResult.getData();
                if (Objects.nonNull(duccItems)) {
                    duccItems.forEach(duccItem -> {
                        properties.put(duccItem.getKey(), duccItem.getValue());
                    });
                }
            }
        }

        return properties;
    }

    public Boolean createProfile(String duccConfigId, String profile) {
        duccConfigId = DuccUtils.getConfig(duccConfigId);
        DuccProfileCreateParam param = new DuccProfileCreateParam();
        param.setCode(profile);
        param.setName(profile);
        param.setDescription(profile);

        HttpEntity<DuccProfileCreateParam> entity = new HttpEntity<>(param, this.commonHeaders);

        ParameterizedTypeReference<DuccResult<DuccProfile>> reference = new ParameterizedTypeReference<DuccResult<DuccProfile>>() {
        };
        ResponseEntity<DuccResult<DuccProfile>> response = restTemplate.exchange(
                getApiUrl(API_CREATE_PROFILE, duccConfigId),
                API_CREATE_PROFILE.getMethod(), entity, reference);

        Boolean duccProfile = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccProfile> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                duccProfile = true;
            }
        }

        if (Objects.isNull(duccProfile)) {
            logger.error("创建ducc配置profile失败！param: {}, response: {}", JSON.toJSONString(param), JSON.toJSONString(response));
            throw new BizException("创建ducc配置profile失败！");
        }
        return duccProfile;
    }

    private DuccProfile createProfile(Long duccConfigId, String profile) {
        DuccProfileCreateParam param = new DuccProfileCreateParam();
        param.setCode(profile);
        param.setName(profile);
        param.setDescription(profile);

        HttpEntity<DuccProfileCreateParam> entity = new HttpEntity<>(param, this.commonHeaders);

        ParameterizedTypeReference<DuccResult<DuccProfile>> reference = new ParameterizedTypeReference<DuccResult<DuccProfile>>() {
        };
        ResponseEntity<DuccResult<DuccProfile>> response = restTemplate.exchange(
                getApiUrl(API_CREATE_PROFILE, String.valueOf(duccConfigId)),
                API_CREATE_PROFILE.getMethod(), entity, reference);

        DuccProfile duccProfile = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccProfile> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                duccProfile = duccResult.getData();
            }
        }

        if (Objects.isNull(duccProfile)) {
            logger.error("创建ducc配置profile失败！param: {}, response: {}", JSON.toJSONString(param), JSON.toJSONString(response));
            throw new BizException("创建ducc配置profile失败！");
        }
        return duccProfile;
    }

    private void deleteProfile(String duccConfigCode, String profileId) {
        HttpEntity<Object> entity = new HttpEntity<>(this.commonHeaders);

        ParameterizedTypeReference<DuccResult<DuccProfile>> reference = new ParameterizedTypeReference<DuccResult<DuccProfile>>() {
        };
        ResponseEntity<DuccResult<DuccProfile>> response = restTemplate.exchange(
                getApiUrl(API_DELETE_PROFILE, duccConfigCode, profileId),
                API_DELETE_PROFILE.getMethod(), entity, reference);

        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<DuccProfile> duccResult = response.getBody();
            if (Objects.isNull(duccResult) || duccResult.getCode() != HttpStatus.OK.value()) {
                logger.error("删除ducc配置profile失败！duccConfigCode: {}, profileId: {}, response: {}", duccConfigCode, profileId, JSON.toJSONString(response));
                throw new BizException("删除ducc配置profile失败！");
            }
        }
    }

    private List<DuccProfile> queryProfiles(String duccConfigCode, String profile) {
        HttpEntity<DuccProfileCreateParam> entity = new HttpEntity<>(this.commonHeaders);
        ParameterizedTypeReference<DuccResult<List<DuccProfile>>> reference = new ParameterizedTypeReference<DuccResult<List<DuccProfile>>>() {
        };
        ResponseEntity<DuccResult<List<DuccProfile>>> response = restTemplate.exchange(
                getApiUrl(API_QUERY_ALL_PROFILE, duccConfigCode, profile),
                API_QUERY_ALL_PROFILE.getMethod(), entity, reference);

        List<DuccProfile> duccProfiles = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            DuccResult<List<DuccProfile>> duccResult = response.getBody();
            if (Objects.nonNull(duccResult) && duccResult.getCode() == HttpStatus.OK.value()) {
                duccProfiles = duccResult.getData();
            }
        }

        if (Objects.isNull(duccProfiles)) {
            duccProfiles = new ArrayList<>();
        }
        return duccProfiles;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.commonHeaders = new HttpHeaders();
        commonHeaders.add("application", bizConfigProperties.getApplication());
        commonHeaders.add("token", bizConfigProperties.getToken());
        commonHeaders.add("Content-Type", "application/json;charset=utf-8");
    }
}
