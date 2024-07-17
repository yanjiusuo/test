package com.jd.workflow.console.service.listener;


import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;

import java.util.List;

public interface InterfaceChangeListener {
    public default void onAppAdd(AppInfo appInfo){}
    public default void onAppUpdate(AppInfo before,AppInfo after){}
    public default void onAppRemove(AppInfo before){}
    // 新增接口
    public default void onInterfaceAdd(List<InterfaceManage> manages){};

    public default void onInterfaceUpdate(InterfaceManage before,InterfaceManage after){};

    public default void onInterfaceRemove(List<InterfaceManage> manages){};

    public default void onMethodAdd(InterfaceManage interfaceManage, List<MethodManage> methods){}

    /**
     *
     * @param interfaceManage
     * @param methods 更新前的方法信息
     */
    public default void onMethodBeforeUpdate(InterfaceManage interfaceManage, List<MethodManage> methods){};

    public default void onMethodUpdate(InterfaceManage interfaceManage, List<MethodManage> methods){}

    public default void onMethodAfterUpdate(InterfaceManage interfaceManage, List<MethodManage> after,List<MethodManage> before){};

    public default void onMethodRemove(InterfaceManage interfaceManage, List<MethodManage> methods){}
}
