package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.HttpAuthConfigDTO;
import com.jd.workflow.console.dto.HttpAuthDTO;
import com.jd.workflow.console.dto.QueryHttpAuthConfigReqDTO;
import com.jd.workflow.console.dto.QueryHttpAuthReqDTO;
import com.jd.workflow.console.entity.HttpAuth;
import com.jd.workflow.console.entity.HttpAuthApply;
import com.jd.workflow.console.entity.HttpAuthConfig;

import java.util.List;
import java.util.Map;

/**
 * 项目名称：parent
 * 类 名 称：IAppInfoService
 * 类 描 述：应用service
 * 创建时间：2022-11-16 16:50
 * 创 建 人：wangxiaofei8
 */
public interface IHttpAuthConfigService extends IService<HttpAuthConfig> {


    /**
     * 查询应用配置信息
     * @param queryDTO
     * @return
     */
    public HttpAuthConfigDTO queryOne(QueryHttpAuthConfigReqDTO queryDTO);

    /**
     * 查询http接口鉴权配置
     * @param queryDTO
     * @return
     */
    public Page<HttpAuthConfigDTO> queryListPage(QueryHttpAuthConfigReqDTO queryDTO);

    /**
     * 查询列表
     * @param queryDTO
     * @return
     */
    public List<HttpAuthConfigDTO> queryAllList(QueryHttpAuthConfigReqDTO queryDTO);

    /**
     * 增加接口鉴权配置
     * @param addAuthConfigDTO
     * @return
     */
    public HttpAuthConfigDTO add(HttpAuthConfigDTO addAuthConfigDTO);

    /**
     * 增加接口鉴权配置
     * @param updateAuthConfigDTO
     * @return
     */
    public HttpAuthConfigDTO update(HttpAuthConfigDTO updateAuthConfigDTO);

    /**
     * 根据ID获取鉴权配置
     * @param id
     * @return
     */
    public HttpAuthConfigDTO selectById(Long id);

    /**
     * 推送到ducc
     * @param authApply
     */
    public boolean pushAuthCodeToDucc(HttpAuthApply authApply);
    public Map<String,Object> getAuthConfig(HttpAuthConfigDTO authApply);
}
