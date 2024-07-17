package com.jd.workflow.console.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jd.jsf.open.api.ProviderAliaService;
import com.jd.jsf.open.api.ProviderService;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.QueryInterfaceRequest;
import com.jd.jsf.open.api.vo.request.QueryProviderRequest;
import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.base.enums.StringMatchEnum;
import com.jd.workflow.console.dao.mapper.InterfaceManageMapper;
import com.jd.workflow.console.dao.mapper.JsfAliasMapper;
import com.jd.workflow.console.dto.JsfAliasDTO;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.JsfAlias;
import com.jd.workflow.console.service.JsfAliasService;
import com.jd.workflow.console.service.auth.InterfaceAuthService;
import com.jd.workflow.console.service.listener.JsfAliasChangeListener;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JsfAliasServiceImpl extends ServiceImpl<JsfAliasMapper, JsfAlias> implements JsfAliasService {

    @Resource
    InterfaceManageMapper interfaceManageMapper;

    @Resource
    ProviderAliaService providerAliaService;
    @Autowired
    InterfaceAuthService authService;
    @Resource
    ProviderService providerService;

    @Value("${jsf.api.env}")
    private String aliasEnv;

    @Value("${jsf.api.site}")
    private String aliasSite;
    @Value("${jsf.api.remote:}")
    private String aliasRemote;

    List<JsfAliasChangeListener> listeners = new ArrayList<>();

    final UserInfoInSession user = UserSessionLocal.getUser();

    private void validateUnique(JsfAliasDTO dto) {
        LambdaQueryWrapper<JsfAlias> lqw = new LambdaQueryWrapper<>();
        lqw.eq(JsfAlias::getAlias, dto.getAlias());
        lqw.eq(JsfAlias::getInterfaceId, dto.getInterfaceId());
        lqw.eq(JsfAlias::getSite, dto.getSite());
        lqw.eq(JsfAlias::getEnv, dto.getEnv());
        final List<JsfAlias> list = list(lqw);
        if (list.size() > 0) {
            throw new BizException("该别名已存在,请确保同一环境下别名录入不重复");
        }
    }

    @Override
    public Long add(JsfAliasDTO jsfAliasDTO) {
        //校验
        Guard.notEmpty(jsfAliasDTO, ServiceErrorEnum.DATA_EMPTY_ERROR.getMsg());
        Guard.notEmpty(jsfAliasDTO.getAlias(), "接口别名不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        if (!jsfAliasDTO.getAlias().matches(StringMatchEnum.NUMBER_LETTER.getMatch()) || jsfAliasDTO.getAlias().length() > 50) {
            throw ServiceException.withCommon("别名只能为字母和数字，不超过50位。");
        }
        Guard.notEmpty(jsfAliasDTO.getEnv(), "jsf接口环境不能为空");
        Guard.notEmpty(jsfAliasDTO.getSite(), "jsf站点不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        validateUnique(jsfAliasDTO);
       /* if(!"中国站".equals(site) || !"测试站".equals(site) || !"泰国站".equals(site) || !"印尼站".equals(site)) {
            throw ServiceException.with(ServiceErrorEnum.SERVICE_INVALID_PARAMETER);
        }*/
        //接口存在校验
        interfaceCheck(jsfAliasDTO.getInterfaceId());
        JsfAlias jsfAlias = convertTo(jsfAliasDTO);
        save(jsfAlias);
        for (JsfAliasChangeListener listener : listeners) {
            listener.onAliasAdd(jsfAlias);
        }
        return jsfAlias.getId();
    }

    private JsfAlias convertTo(JsfAliasDTO jsfAliasDTO) {
        JsfAlias jsfAlias = new JsfAlias();
        jsfAlias.setId(jsfAliasDTO.getId());
        jsfAlias.setInterfaceId(jsfAliasDTO.getInterfaceId());
        jsfAlias.setAlias(jsfAliasDTO.getAlias());
        jsfAlias.setEnv(jsfAliasDTO.getEnv());
        jsfAlias.setSite(jsfAliasDTO.getSite().name());
        return jsfAlias;
    }


    private Boolean interfaceCheck(Long id) {
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(id);
        if (EmptyUtil.isEmpty(interfaceManage)) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        return true;
    }

    @Override
    public Long edit(JsfAliasDTO jsfAliasDTO) {
        //校验
        Guard.notEmpty(jsfAliasDTO.getId(), "id不可为空");
        Guard.notEmpty(jsfAliasDTO.getAlias(), "接口别名不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        if (!jsfAliasDTO.getAlias().matches(StringMatchEnum.NUMBER_LETTER.getMatch()) || jsfAliasDTO.getAlias().length() > 50) {
            throw ServiceException.withCommon("请输入字母和数字，不超过50位。");
        }
        Guard.notEmpty(jsfAliasDTO.getEnv(), "jsf接口环境不能为空");
        Guard.notEmpty(jsfAliasDTO.getSite(), "jsf站点不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());

        //接口存在校验
        interfaceCheck(jsfAliasDTO.getInterfaceId());
        JsfAlias jsfAlias = convertTo(jsfAliasDTO);
        updateById(jsfAlias);

        for (JsfAliasChangeListener listener : listeners) {
            listener.onAliasUpdate(jsfAlias);
        }


        return jsfAliasDTO.getId();
    }

    @Override
    public Boolean remove(Long id) {
        //校验
        final JsfAlias removed = getById(id);
        boolean result = removeById(id);
        for (JsfAliasChangeListener listener : listeners) {
            listener.onAliasRemove(removed);
        }
        return result;
    }

    @Override
    public Page<JsfAlias> pageJsf(Integer current, Integer size, Long interfaceId) {
        Guard.notEmpty(interfaceId, "接口id不可为空");
        Guard.notEmpty(size, "分页大小不可为空");
        Guard.notEmpty(current, "当前页不可为空");

        //查询条件
        LambdaQueryWrapper<JsfAlias> lqw = new LambdaQueryWrapper<>();
        lqw.eq(JsfAlias::getInterfaceId, interfaceId);
        JsfAliasDTO jsfAliasDTO = new JsfAliasDTO();


        //分页
        Page<JsfAlias> page = new Page<>(current, jsfAliasDTO.getSize());
        Page<JsfAlias> aliasList = page(page, lqw);

        return aliasList;
    }

    @Override
    public List<JsfAlias> aliasAll(Long interfaceId) {
        Guard.notEmpty(interfaceId, "接口id不可为空");
        List<JsfAlias> resultList = Lists.newArrayList();
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(interfaceId);

        QueryInterfaceRequest req = JSFArgBuilder.buildQueryInterfaceRequest();

        req.setInterfaceName(interfaceManage.getServiceCode());
        //jsf查询到的别名
       /* Result<List<String>> result = providerAliaService.getAliasByInterfaceName(req);
        if (CollectionUtils.isNotEmpty(result.getData())) {
            for (String alais : result.getData()) {
                JsfAlias jsfAlias = new JsfAlias();
                jsfAlias.setAlias(alais);
                jsfAlias.setInterfaceId(interfaceId);
                resultList.add(jsfAlias);
            }
        }*/

        LambdaQueryWrapper<JsfAlias> lqw = new LambdaQueryWrapper<>();
        lqw.eq(JsfAlias::getInterfaceId, interfaceId);

        List<JsfAlias> jsfAliasList = list(lqw);
        resultList.addAll(jsfAliasList);
        return resultList;
    }

    @Override
    public List<JsfAlias> aliasAllByInterfaceName(String interfaceName) {
        List<JsfAlias> resultList = Lists.newArrayList();

        QueryInterfaceRequest req = JSFArgBuilder.buildQueryInterfaceRequest();
        req.setInterfaceName(interfaceName);
        //jsf查询到的别名
        Result<List<String>> result = providerAliaService.getAliasByInterfaceName(req);
        if (CollectionUtils.isNotEmpty(result.getData())) {
            for (String alais : result.getData()) {
                JsfAlias jsfAlias = new JsfAlias();
                jsfAlias.setAlias(alais);
                jsfAlias.setInterfaceId(null);
                resultList.add(jsfAlias);
            }
        }

        return resultList;
    }

    @Override
    public Result<List<Server>> getIps(String interfaceName, String alias) throws Exception {
        Guard.notEmpty(interfaceName, "接口名不可为空");
      /*  LambdaQueryWrapper<InterfaceManage> wr = new LambdaQueryWrapper<>();
        wr.eq(InterfaceManage::getName, interfaceName);
        List<InterfaceManage> interfaceManages = interfaceManageMapper.selectList(wr);
        if (CollectionUtils.isEmpty(interfaceManages)) {
            throw new BizException("接口不存在");
        }*/
        QueryProviderRequest req = JSFArgBuilder.buildQueryProviderRequest();
        req.setInterfaceName(interfaceName);
        if (StringUtils.isNotEmpty(alias)) {
            req.setAlias(alias);
        }
        //jsf查询到的别名
        Result<List<Server>> result = providerService.query(req);
        return result;
    }

    @Override
    public void addChangeListener(JsfAliasChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean initAliasAllById(Long interfaceId) {

        log.info("initAliasAllById interfaceId :{}", interfaceId);
        List<JsfAlias> resultList = Lists.newArrayList();

        List<JsfAlias> jsfAliasListLocal = queryAliasFromJsf(interfaceId);
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(interfaceId);
        List<JsfAlias> jsfAliasListRemote = queryAliasFromRemote(interfaceManage.getServiceCode());

        LambdaQueryWrapper<JsfAlias> lqw = new LambdaQueryWrapper<>();
        lqw.eq(JsfAlias::getInterfaceId, interfaceId)
                .eq(BaseEntity::getYn, 1);

        List<JsfAlias> jsfAliasListAll = list(lqw);
        // 20240415:相同alias报错：Duplicate key JsfAlias 问题处理
        Map<String, JsfAlias> jsfAliasMapAll = jsfAliasListAll.stream().collect(Collectors.toMap(item -> item.getAlias() + ":" + item.getEnv(), item->item));

        if (CollectionUtils.isNotEmpty(jsfAliasListLocal)) {
            for (JsfAlias jsfAlias : jsfAliasListLocal) {
                if (jsfAliasMapAll.containsKey(jsfAlias.getAlias() + ":" + jsfAlias.getEnv())) {

                } else {
                    resultList.add(jsfAlias);
                }
            }
            log.info("initAliasAllById interfaceId :{} :jsfAliasListLocal count:{}", interfaceId, jsfAliasListLocal.size());
        }

        if (CollectionUtils.isNotEmpty(jsfAliasListRemote)) {
            for (JsfAlias jsfAlias : jsfAliasListRemote) {
                if (jsfAliasMapAll.containsKey(jsfAlias.getAlias() + ":" + jsfAlias.getEnv())) {

                } else {
                    resultList.add(jsfAlias);
                }
            }
            log.info("initAliasAllById interfaceId :{} :jsfAliasListRemote count:{}", interfaceId, jsfAliasListRemote.size());
        }

        log.info("initAliasAllById interfaceId :{} :count:{}", interfaceId, resultList.size());
        boolean result = saveBatch(resultList);

        authService.syncJsfAlias(interfaceId);
        return result;


    }

    @Override
    public List<JsfAlias> queryAliasFromJsf(String interfaceName) {
        List<JsfAlias> resultList = Lists.newArrayList();
        log.info("queryAliasFromJsf interfaceName :{},aliasSite:{},aliasEnv:{}", interfaceName, aliasSite, aliasEnv);

        QueryInterfaceRequest req = JSFArgBuilder.buildQueryInterfaceRequest();
        req.setInterfaceName(interfaceName);
        log.info("queryAliasFromJsf interfaceName :{},req:{}", interfaceName, JSON.toJSONString(req));
        Result<List<String>> result = providerAliaService.getAliasByInterfaceName(req);
        log.info("queryAliasFromJsf interfaceName :{},req:{},result:{}", interfaceName, JSON.toJSONString(req), JSON.toJSONString(result));
        if (CollectionUtils.isNotEmpty(result.getData())) {
            for (String alais : result.getData()) {
                JsfAlias jsfAlias = new JsfAlias();
                jsfAlias.setAlias(alais);
                jsfAlias.setInterfaceId(0L);
                jsfAlias.setSite(aliasSite);
                jsfAlias.setEnv(aliasEnv);
                resultList.add(jsfAlias);
            }

        }
        return resultList;
    }

    @Override
    public List<JsfAlias> queryAliasFromJsf(Long interfaceId) {
        List<JsfAlias> resultList = Lists.newArrayList();
        log.info("queryAliasFromJsf interfaceId :{},aliasSite:{},aliasEnv:{}", interfaceId, aliasSite, aliasEnv);
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(interfaceId);
        QueryInterfaceRequest req = JSFArgBuilder.buildQueryInterfaceRequest();
        req.setInterfaceName(interfaceManage.getServiceCode());
        log.info("queryAliasFromJsf interfaceId :{},req:{}", interfaceId, JSON.toJSONString(req));
        Result<List<String>> result = providerAliaService.getAliasByInterfaceName(req);
        log.info("queryAliasFromJsf interfaceId :{},req:{},result:{}", interfaceId, JSON.toJSONString(req), JSON.toJSONString(result));
        if (CollectionUtils.isNotEmpty(result.getData())) {
            for (String alais : result.getData()) {
                JsfAlias jsfAlias = new JsfAlias();
                jsfAlias.setAlias(alais);
                jsfAlias.setInterfaceId(interfaceId);
                jsfAlias.setSite(aliasSite);
                jsfAlias.setEnv(aliasEnv);
                resultList.add(jsfAlias);
            }

        }
        return resultList;
    }


    @Override
    public List<JsfAlias> queryAliasFromRemote(String interfaceName) {
        log.info("queryAliasFromRemote interfaceName :{},aliasRemote:{}", interfaceName, aliasRemote);
        List<JsfAlias> result = Lists.newArrayList();
        if (StringUtils.isEmpty(aliasRemote)) {
            return result;
        }
        HttpGet get = new HttpGet();
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            get.setURI(new URI(String.format("http://%s/jsfAlias/queryAliasFromJsf?interfaceName=%s", aliasRemote, interfaceName)));
            CloseableHttpResponse response = client.execute(get);
            log.info("queryAliasFromRemote callUrl:{}", String.format("http://%s/jsfAlias/queryAliasFromJsf?interfaceName=%s", aliasRemote, interfaceName));
            String responseJson = EntityUtils.toString(response.getEntity());
            log.info("queryAliasFromRemote callUrl:{},responseJson:{}", String.format("http://%s/jsfAlias/queryAliasFromJsf?interfaceName=%s", aliasRemote, interfaceName), responseJson);
            Map responseMap = JSON.parseObject(responseJson, Map.class);
            if (responseMap.containsKey("data")) {
                if (StringUtils.isNotEmpty(responseMap.get("data").toString())) {
                    result = JSON.parseObject(responseMap.get("data").toString(), new TypeReference<List<JsfAlias>>() {
                    });
                }
            }
        } catch (Exception e) {
            log.error("queryAliasFromRemote error", e);
        }


        return result;
    }


}
