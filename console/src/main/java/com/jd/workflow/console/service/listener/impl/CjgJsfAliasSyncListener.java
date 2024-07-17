package com.jd.workflow.console.service.listener.impl;


import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.JsfAlias;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.JsfAliasService;
import com.jd.workflow.console.service.auth.InterfaceAuthService;
import com.jd.workflow.console.service.listener.JsfAliasChangeListener;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CjgJsfAliasSyncListener implements JsfAliasChangeListener {
    @Autowired
    JsfAliasService jsfAliasService;
    @Autowired
    InterfaceAuthService authService;
    @PostConstruct
    public void init(){
        jsfAliasService.addChangeListener(this);
    }

     @Override
    public void onAliasAdd(JsfAlias jsfAlias) {
        authService.syncJsfAlias(jsfAlias.getInterfaceId());
    }

    @Override
    public void onAliasUpdate(JsfAlias jsfAlias) {
        authService.syncJsfAlias(jsfAlias.getInterfaceId());
    }

    @Override
    public void onAliasRemove(JsfAlias jsfAlias) {
        authService.syncJsfAlias(jsfAlias.getInterfaceId());
    }
}
