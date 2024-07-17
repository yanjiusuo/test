package com.jd.workflow.console.service.remote.api;


import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.console.service.remote.api.dto.jdos.JdosAppMembers;
import com.jd.workflow.console.service.remote.api.dto.jdos.SystemAppInfo;

import java.util.List;

/**
 * JDOS 对外开放接口
 *
 * @author: jialixian1
 * @date: 2021/11/02
 */
public interface JDosAppOpenService {

    /**
     * 获取JDos系统及应用信息
     *
     * @param
     * @param
     * @return
     */
    List<SystemAppInfo>findSysAppInfo();

    public JdosAppMembers queryJdosAppMembersAppCode(String appCode, String site);

    JDosAppInfo queryJdosAppInfo(String appCode, String site);
}
