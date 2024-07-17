package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.Result;
import com.jd.workflow.console.dto.JsfAliasDTO;
import com.jd.workflow.console.entity.JsfAlias;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.service.listener.JsfAliasChangeListener;

import java.util.List;

public interface JsfAliasService extends IService<JsfAlias> {

    Long add(JsfAliasDTO jsfAliasDTO);

    Long edit(JsfAliasDTO jsfAliasDTO);

    Boolean remove(Long id);

    Page<JsfAlias> pageJsf(Integer current,Integer size,Long interfaceId);
    List<JsfAlias> aliasAll(Long interfaceId);
    List<JsfAlias> aliasAllByInterfaceName(String interfaceName);

    Result<List<Server>> getIps(String interfaceName, String alias)  throws Exception;
    public void addChangeListener(JsfAliasChangeListener listener);

    /**
     * 重新同步测试和线上jsf别名
     * @param interfaceId
     * @return
     */
    boolean initAliasAllById(Long interfaceId);


    /**
     * 从jsf查询全部接口别名
     * @param interfaceName
     * @return
     */
    List<JsfAlias> queryAliasFromJsf(String interfaceName);

    /**
     * 从jsf查询全部接口别名
     * @param interfaceId
     * @return
     */
    List<JsfAlias> queryAliasFromJsf(Long interfaceId);

    /**
     * 从远程查询jsf别名：线上查测试
     * @param interfaceId
     * @return
     */
    List<JsfAlias> queryAliasFromRemote(String interfaceName);
}
