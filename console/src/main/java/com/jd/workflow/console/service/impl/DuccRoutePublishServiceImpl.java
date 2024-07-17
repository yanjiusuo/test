package com.jd.workflow.console.service.impl;

import com.jd.jtfm.configcenter.ducc.manage.ConfigCenterManagerHelperOne2NDucc;
import com.jd.jtfm.configcenter.ducc.model.CodeConstant;
import com.jd.jtfm.configcenter.ducc.model.ConfigItem;
import com.jd.workflow.console.dto.PublishRecordDto;
import com.jd.workflow.console.service.IRoutePublishService;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@ConditionalOnProperty(value="route.useDuccPublisher",havingValue = "true")
public class DuccRoutePublishServiceImpl implements IRoutePublishService {
    /**
     * 推送ducc提前初始化的cjg应用id
     */
    @Value("${camel.config.appId:integration-paas}")
    private String camelConfigAppId;

    @Override
    public void publish(PublishRecordDto dto) {
        ConfigItem configItem = new ConfigItem();
        // 设置为其它也会在后台根据 appId + methodName + businessNmae 判断是否在更新为别的状态
        configItem.setOperateType(CodeConstant.ADD);
        // 全量的情况可以是 null
        configItem.setIp(null);

        String methodId = dto.getMethodId()+"";
        // 方法名称： 健康可以设置为流程编排内容保存的主键
        configItem.setMethodName(methodId);
        configItem.setRoutesContent(dto.getConfig());
        configItem.setPath(null);
        // 藏经阁注册一个，提前初始化
        if(StringUtils.isNotBlank(dto.getClusterCode())){
           configItem.setAppId(dto.getClusterCode());
        }else{
            configItem.setAppId(camelConfigAppId);
        }
        configItem.setRoutePattern(0);
        configItem.setBusinessName(methodId);
        configItem.setIpType(0);
        configItem.setVersion(dto.getPublishVersion());
        configItem.setGroup(null);
        try {
            ConfigCenterManagerHelperOne2NDucc.register(0, configItem);
        } catch (Exception e) {
            //e.printStackTrace();
            log.error("push config to ducc occur exception",e);
            throw new BizException("发布服务失败!",e);
        }
    }
}
