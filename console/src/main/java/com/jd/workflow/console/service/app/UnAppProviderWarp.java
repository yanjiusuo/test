package com.jd.workflow.console.service.app;

import com.jd.cjg.result.CjgResult;
import com.jd.cjg.unapp.request.AppBaseQueryReq;
import com.jd.cjg.unapp.vo.AppInfoVo;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/7/15
 */
@Service
public interface UnAppProviderWarp {

    AppInfoVo obtainAppInfoVo(String appCode);

}
