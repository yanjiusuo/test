package com.jd.workflow.console.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.cjg.config.bizcfg.ResponseResult;
import com.jd.cjg.config.http.model.CjgHttpAuth;
import com.jd.cjg.config.http.model.HttpAuthCfg;
import com.jd.cjg.config.http.model.HttpClientAuthCfg;
import com.jd.cjg.config.http.model.HttpCodeAuthCfg;
import com.jd.cjg.config.service.HttpAuthService;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.dao.mapper.HttpAuthConfigMapper;
import com.jd.workflow.console.dto.HttpAuthConfigDTO;
import com.jd.workflow.console.dto.QueryHttpAuthConfigReqDTO;
import com.jd.workflow.console.entity.HttpAuthApply;
import com.jd.workflow.console.entity.HttpAuthConfig;
import com.jd.workflow.console.service.IHttpAuthConfigService;
import com.jd.workflow.console.utils.NumberUtils;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 *
 * @author wangwenguang
 * @date 2023-01-06 11:07
 */
@Service
@Slf4j
public class HttpAuthConfigServiceImpl extends ServiceImpl<HttpAuthConfigMapper, HttpAuthConfig> implements IHttpAuthConfigService {

    /**
     * 鉴权配置管理
     */
    @Resource
    private HttpAuthConfigMapper httpAuthConfigMapper;

    /**
     * ducc配置服务
     */
    @Resource
    private HttpAuthService duccHttpAuthService;

    /**
     * 查询应用配置信息
     *
     * @param queryDTO
     * @return
     */
    @Override
    public HttpAuthConfigDTO queryOne(QueryHttpAuthConfigReqDTO queryDTO) {
        List<HttpAuthConfig> records = httpAuthConfigMapper.queryList(queryDTO);

        if (CollectionUtils.isNotEmpty(records)) {
             HttpAuthConfigDTO result = toDTO(records.get(0));

            return result;
        }
        return null;
    }

    /**
     * 新增app
     *
     * @param queryDTO
     * @return
     */
    @Override
    public Page<HttpAuthConfigDTO> queryListPage(QueryHttpAuthConfigReqDTO queryDTO) {
        //分页处理
        Page<HttpAuthConfigDTO> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getPageSize());
        long total = NumberUtils.toLong(httpAuthConfigMapper.queryListCount(queryDTO));
        page.setTotal(total);

        if (total > 0) {
            List<HttpAuthConfig> records = httpAuthConfigMapper.queryList(queryDTO);
            //转化为DTO
            List<HttpAuthConfigDTO> recordDTOs = Optional.ofNullable(records).orElse(new ArrayList<>()).stream().map(v -> {
                return toDTO(v);
            }).collect(Collectors.toList());
            page.setRecords(recordDTOs);
        }

        return page;
    }

    /**
     * 查询
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<HttpAuthConfigDTO> queryAllList(QueryHttpAuthConfigReqDTO queryDTO) {
        List<HttpAuthConfig> httpAuthList = httpAuthConfigMapper.queryAllList(queryDTO);
        //转化为DTO
        List<HttpAuthConfigDTO> recordDTOs = Optional.ofNullable(httpAuthList).orElse(new ArrayList<>()).stream().map(v -> {
            return toDTO(v);
        }).collect(Collectors.toList());

        return recordDTOs;
    }

    /**
     * 增加接口鉴权配置
     *
     * @param addDTO
     * @return
     */
    @Override
    public HttpAuthConfigDTO add(HttpAuthConfigDTO addDTO) {
        //参数校验
        Guard.notNull(addDTO, "添加鉴权配置，入参不能为空");
        Guard.notNull(addDTO.getAppCode(), "添加鉴权配置，appCode 入参不能为空");

        //校验该应用是否已存在
        QueryHttpAuthConfigReqDTO authConfigReqDTO = new QueryHttpAuthConfigReqDTO();
        authConfigReqDTO.setAppCode(addDTO.getAppCode());
        long total = NumberUtils.toLong(httpAuthConfigMapper.queryListCount(authConfigReqDTO));
        if (total > 0) {
            Guard.assertTrue(false, "该应用(" + authConfigReqDTO.getAppCode() + ")已经初始化过鉴权配置");
        }

        //初始化鉴权配置对象
        HttpAuthConfig addAuthConfig = initHttpAuthConfig(addDTO);

        //初始化ducc配置
        initDuccAuthConfig(addAuthConfig);

        //初始化ducc成功之后，再进行写库操作
        boolean success = save(addAuthConfig);
        Guard.assertTrue(success, "该应用(" + addAuthConfig.getAppCode() + ") 更新鉴权配置失败，请联系运维值班人员！");

        //组织结果
        HttpAuthConfigDTO result = null;
        if (success) {
            result = selectById(addAuthConfig.getId());
        }
        return result;
    }

    /**
     * 初始化鉴权配置
     *
     * @param addDTO
     * @return
     */
    private HttpAuthConfig initHttpAuthConfig(HttpAuthConfigDTO addDTO) {
        HttpAuthConfig addAuthConfig = toEntity(addDTO);
        Date date = new Date();
        addAuthConfig.setCreated(date);
        addAuthConfig.setModified(date);
        addAuthConfig.setYn(DataYnEnum.VALID.getCode());
        UserInfoInSession userInfoInSession = UserSessionLocal.getUser();
        String userPin = "system";
        if(userInfoInSession!=null){
            userPin = userInfoInSession.getUserId();
        }
        addAuthConfig.setModifier(userPin);
        addAuthConfig.setCreator(userPin);
        addAuthConfig.setForceValid(ForceValidEnum.INVALID.getCode());
        addAuthConfig.setValid(ValidEnum.VALID.getCode());
        return addAuthConfig;
    }

    /**
     * 初始化ducc鉴权配置
     *
     * @param authConfig
     * @return
     */
    private boolean initDuccAuthConfig(HttpAuthConfig authConfig) {
        boolean result = false;
        //组装信息
        CjgHttpAuth cjgHttpAuth = getCjgHttpAuth(authConfig);
        HttpAuthCfg httpAuthCfg = getCjgHttpAuthCfg(authConfig);
        log.info("#addDuccAuthConfig.httpAuthCfg= {} ,cjgHttpAuth= {} ", JSON.toJSONString(httpAuthCfg), JSON.toJSONString(cjgHttpAuth));
        ResponseResult responseResult = duccHttpAuthService.addHttpAuth(httpAuthCfg, null, cjgHttpAuth);
        log.info("#addDuccAuthConfig.responseResult= {}", JSON.toJSONString(responseResult));
        if (responseResult == null || !responseResult.isStatus()) {
            Guard.assertTrue(false, "该应用(" + authConfig.getAppCode() + ") 初始化过鉴权配置到ducc失败，请联系运维值班人员！");
        }else {
            result = true;
        }
        return result;
    }


    /**
     * 增加ducc鉴权配置
     *
     * @param authConfig
     * @return
     */
    private boolean addDuccAuthConfig(HttpAuthConfig authConfig, List<HttpClientAuthCfg>  clientAuthCfgs) {
        boolean result = false;
        //组装信息
        CjgHttpAuth cjgHttpAuth = getCjgHttpAuth(authConfig);
        HttpAuthCfg httpAuthCfg = getCjgHttpAuthCfg(authConfig);
        log.info("#addDuccAuthConfig.httpAuthCfg= {} ,cjgHttpAuth= {}，clientAuthCfgs={} ", JSON.toJSONString(httpAuthCfg),
                JSON.toJSONString(cjgHttpAuth), JSON.toJSONString(clientAuthCfgs));
        ResponseResult responseResult = duccHttpAuthService.addHttpAuth(httpAuthCfg, clientAuthCfgs, cjgHttpAuth);
        log.info("#addDuccAuthConfig.responseResult= {}", JSON.toJSONString(responseResult));
        if (responseResult == null || !responseResult.isStatus()) {
            log.error("该应用(" + authConfig.getAppCode() + ") 添加鉴权标识到ducc失败，请联系运维值班人员！");
        }else {
            result = true;
        }
        return result;
    }

    /**
     * 初始化ducc鉴权配置
     *
     * @param authConfig
     * @return
     */
    private ResponseResult updateDuccAuthConfig(HttpAuthConfig authConfig) {
        //组装信息
        CjgHttpAuth cjgHttpAuth = getCjgHttpAuth(authConfig);
        HttpAuthCfg httpAuthCfg = getCjgHttpAuthCfg(authConfig);

        log.info("#updateDuccAuthConfig.httpAuthCfg= {} ,cjgHttpAuth= {} ", JSON.toJSONString(httpAuthCfg), JSON.toJSONString(cjgHttpAuth));
        ResponseResult responseResult = duccHttpAuthService.updateHttpAuth(httpAuthCfg, cjgHttpAuth);
        log.info("#updateDuccAuthConfig.responseResult= {}", JSON.toJSONString(responseResult));
        if (responseResult == null || !responseResult.isStatus()) {
            Guard.assertTrue(false, "该应用(" + authConfig.getAppCode() + ") 更新鉴权配置到ducc失败，请联系运维值班人员！");
        }
        return responseResult;
    }

    /**
     * 获取ducc鉴权配置
     * @param authConfig
     * @return
     */
    private HttpAuthCfg getCjgHttpAuthCfg(HttpAuthConfig authConfig) {
        HttpAuthCfg httpAuthCfg = new HttpAuthCfg();
        httpAuthCfg.setAppName(authConfig.getAppCode());
        httpAuthCfg.setValid(ValidEnum.getValue(authConfig.getValid())); //默认不降级
        httpAuthCfg.setForceValid(ForceValidEnum.getValue(authConfig.getForceValid())); //默认不鉴权
        if(authConfig.getEnableAuditLog() != null){
            httpAuthCfg.setEnableAuditLog(authConfig.getEnableAuditLog() == 1 ? true : false);
        }

        return httpAuthCfg;
    }

    /**
     * 组装藏经阁配置
     * @param authConfig
     * @return
     */
    private CjgHttpAuth getCjgHttpAuth(HttpAuthConfig authConfig) {
        CjgHttpAuth cjgHttpAuth = new CjgHttpAuth();
        cjgHttpAuth.setAppCode(authConfig.getAppCode());
        cjgHttpAuth.setAppName(authConfig.getAppName());
        cjgHttpAuth.setSiteCode(authConfig.getSite());
        cjgHttpAuth.setSiteName(SiteEnum.getName(authConfig.getSite()));
        return cjgHttpAuth;
    }

    /**
     * 更新接口鉴权配置
     *
     * @param updateDTO
     * @return
     */
    @Override
    public HttpAuthConfigDTO update(HttpAuthConfigDTO updateDTO) {
        //校验参数
        Guard.notNull(updateDTO, "更新鉴权配置，入参不能为空");
        Guard.notNull(updateDTO.getId(), "更新鉴权配置，id 入参不能为空");

        //获取DB数据
        HttpAuthConfig dbAuthConfig = getById(updateDTO.getId());
        Guard.notNull(dbAuthConfig, "更新鉴权配置不存在，请核实id参数");

        //组装入参
        dbAuthConfig.setModified(new Date());
        dbAuthConfig.setModifier(UserSessionLocal.getUser().getUserId());
        if (StringUtils.isNotBlank(updateDTO.getValid())){
            dbAuthConfig.setValid(updateDTO.getValid());
        }
        if(updateDTO.getEnableAuditLog() != null){
            dbAuthConfig.setEnableAuditLog(updateDTO.getEnableAuditLog()== true ? 1 : 0);
        }
        if (StringUtils.isNotBlank(updateDTO.getForceValid())){
            dbAuthConfig.setForceValid(updateDTO.getForceValid());
        }

        //更新ducc信息
        updateDuccAuthConfig(dbAuthConfig);

        //更新操作
        boolean success = updateById(dbAuthConfig);
        Guard.assertTrue(success, "该应用(" + dbAuthConfig.getAppCode() + ") 更新鉴权配置失败，请联系运维值班人员！");

        //组织结果
        HttpAuthConfigDTO result = null;
        if (success) {
            result = selectById(dbAuthConfig.getId());
        }
        return result;
    }

    /**
     * 根据ID获取鉴权配置
     *
     * @param id
     * @return
     */
    @Override
    public HttpAuthConfigDTO selectById(Long id) {
        HttpAuthConfig authConfig = httpAuthConfigMapper.selectById(id);
        HttpAuthConfigDTO authConfigDTO = toDTO(authConfig);
        return authConfigDTO;
    }

    /**
     * 推送鉴权标识到ducc
     * @param authApply
     */
    @Override
    public boolean pushAuthCodeToDucc(HttpAuthApply authApply) {
        log.info("#pushAuthCodeToDucc.authApply= {}", JSON.toJSONString(authApply));
        //校验该应用是否已存在
        QueryHttpAuthConfigReqDTO authConfigReqDTO = new QueryHttpAuthConfigReqDTO();
        authConfigReqDTO.setAppCode(authApply.getAppCode());
        authConfigReqDTO.setSite(authApply.getSite());
        List<HttpAuthConfig> authConfigList = httpAuthConfigMapper.queryList(authConfigReqDTO);
        HttpAuthConfig authConfig = null;
        if (CollectionUtils.isEmpty(authConfigList)){
            HttpAuthConfigDTO addDTO = new HttpAuthConfigDTO();
            addDTO.setAppCode(authApply.getAppCode());
            addDTO.setAppName(authApply.getAppName());
            addDTO.setSite(authApply.getSite());
            authConfig = initHttpAuthConfig(addDTO);
            //初始化ducc成功之后，再进行写库操作
            boolean success = save(authConfig);
            if (success){
                log.error("#pushAuthCodeToDucc 该应用(" + authConfig.getAppCode() + ") 初始化鉴权配置成功！");
            }else {
                log.error("#pushAuthCodeToDucc 该应用(" + authConfig.getAppCode() + ") 初始化鉴权配置失败，请联系运维值班人员！");
            }
        }else {
            authConfig = authConfigList.get(0);
        }
        //组装信息
        List<HttpClientAuthCfg>  clientAuthCfgs = new ArrayList<>();
        HttpClientAuthCfg authCfg = new HttpClientAuthCfg();
        authCfg.setClientAppName(authApply.getCallAppCode());

        //组装鉴权标识以及token
        HttpCodeAuthCfg httpCodeAuthCfg = new HttpCodeAuthCfg();
        httpCodeAuthCfg.setAuthCode(authApply.getAuthCode());
        httpCodeAuthCfg.setToken(authApply.getToken());
        authCfg.getAuthList().add(httpCodeAuthCfg);
        clientAuthCfgs.add(authCfg);
        boolean response = addDuccAuthConfig(authConfig,clientAuthCfgs);
        authApply.setDuccStatus(DuccStatusEnum.getEnumByValue(response).getCode());
        return response;
    }


    public Map<String,Object> getAuthConfig(HttpAuthConfigDTO authConfig){
        CjgHttpAuth cjgHttpAuth = new CjgHttpAuth();
        cjgHttpAuth.setAppCode(authConfig.getAppCode());
        cjgHttpAuth.setAppName(authConfig.getAppName());
        cjgHttpAuth.setSiteCode(authConfig.getSite());
        cjgHttpAuth.setSiteName(SiteEnum.getName(authConfig.getSite()));
        return duccHttpAuthService.getAuthConfig(cjgHttpAuth);
    }
    /**
     * 转化为DTO
     *
     * @param authConfig
     * @return
     */
    private HttpAuthConfigDTO toDTO(HttpAuthConfig authConfig) {
        if (authConfig == null) {
            return null;
        }
        HttpAuthConfigDTO authConfigDTO = new HttpAuthConfigDTO();
        BeanUtils.copyProperties(authConfig, authConfigDTO);
        if(authConfig.getEnableAuditLog() != null){
            authConfigDTO.setEnableAuditLog(authConfig.getEnableAuditLog() == 1 ? true : false);
        }
        return authConfigDTO;
    }

    /**
     * 转化为实体对象
     *
     * @param authConfigDTO
     * @return
     */
    private HttpAuthConfig toEntity(HttpAuthConfigDTO authConfigDTO) {
        if (authConfigDTO == null) {
            return null;
        }
        HttpAuthConfig authConfig = new HttpAuthConfig();
        BeanUtils.copyProperties(authConfigDTO, authConfig);
        return authConfig;
    }


}
