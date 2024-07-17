package com.jd.workflow.console.service.app.impl;

import com.jd.cjg.result.CjgResult;
import com.jd.cjg.unapp.UnAppProvider;
import com.jd.cjg.unapp.request.AppBaseQueryReq;
import com.jd.cjg.unapp.vo.AppInfoVo;
import com.jd.workflow.console.service.app.UnAppProviderWarp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/7/15
 */
@Service
public class UnAppProviderWarpImpl implements UnAppProviderWarp {

    @Autowired
    private UnAppProvider unAppProvider;

    /**
     * 从藏经阁获取应用的coding地址
     * @param appCode
     * @return
     */
    public AppInfoVo obtainAppInfoVo(String appCode) {
        AppInfoVo appInfoVo = null;
        AppBaseQueryReq appBaseQueryReq = new AppBaseQueryReq();
        appBaseQueryReq.setTenant("JDD");
        appBaseQueryReq.setAppSource("Jagile");
        appBaseQueryReq.setAppAlias(appCode);
        CjgResult<AppInfoVo> appInfoVoCjgResult = unAppProvider.appDetail(appBaseQueryReq);
        if (Objects.nonNull(appInfoVoCjgResult) && Objects.nonNull(appInfoVoCjgResult.getModel())) {
            appInfoVo = appInfoVoCjgResult.getModel();
        }
        return appInfoVo;
    }
}
