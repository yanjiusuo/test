package com.jd.workflow.console.service.listener;

import com.jd.workflow.console.entity.JsfAlias;

public interface JsfAliasChangeListener {
    public void onAliasAdd(JsfAlias jsfAlias);
    public void onAliasUpdate(JsfAlias jsfAlias);
    public void onAliasRemove(JsfAlias jsfAlias);
}
