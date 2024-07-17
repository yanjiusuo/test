package com.jd.workflow.console.entity.plugin.dto;

import com.jd.common.util.StringUtils;
import com.jd.workflow.console.entity.plugin.PluginStatistic;
import com.jd.workflow.console.service.plugin.PluginLoginService;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Map;

@Data
public class PluginStatisticDto {
    String remoteIp;
    /**
     * 类型
     */
    String type;
    /**
     * 操作人
     */
    String erp;

    String userToken;
    /**
     * 用户名
     */
    String userName;
    /**
     * 渠道
     */
    String channel;

    /**
     * 统计信息
     */
    String statisticData;
    /**
     * 额外信息
     */
    Map<String,Object> extInfos;

    private String pluginVersion;

    public PluginStatistic toPluginStatistic(){
        PluginStatistic statistic = new PluginStatistic();

        BeanUtils.copyProperties(this,statistic);
        if(StringUtils.isNotEmpty(userToken)){
            try{
                PluginLoginService.UserBaseInfo userInfo = PluginLoginService.getUserInfo(userToken);
                statistic.setErp(userInfo.getUserName());
            }catch (Exception e){

            }
        }
        return statistic;
    }
}
