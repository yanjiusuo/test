package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.ProxyDebuggerRegistryMapper;
import com.jd.workflow.console.entity.ProxyDebuggerRegistry;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ProxyDebuggerRegistryService extends ServiceImpl<ProxyDebuggerRegistryMapper, ProxyDebuggerRegistry> {


    public void removeProxy(Long id) {
        ProxyDebuggerRegistry proxyDebuggerRegistry = getById(id);
        if(Objects.nonNull(proxyDebuggerRegistry)){
            proxyDebuggerRegistry.setYn(0);
            updateById(proxyDebuggerRegistry);
        }

    }

}
