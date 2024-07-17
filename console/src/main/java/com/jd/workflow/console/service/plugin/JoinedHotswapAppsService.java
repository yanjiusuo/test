package com.jd.workflow.console.service.plugin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.plugin.JoinedHotswapAppsMapper;
import com.jd.workflow.console.dto.plugin.JoinedAppsDto;
import com.jd.workflow.console.entity.plugin.JoinedHotswapApps;
import com.jd.workflow.soap.common.lang.Guard;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class JoinedHotswapAppsService extends ServiceImpl<JoinedHotswapAppsMapper, JoinedHotswapApps> {
    public void saveOrUpdateJoinedApps(JoinedAppsDto joinedAppsDto){
        Guard.notEmpty(joinedAppsDto.getSite(),"无效的站点");
        Guard.notEmpty(joinedAppsDto.getJdosAppCode(),"无效的app编码");
        JoinedHotswapApps joinedApps = getJoinedApps(joinedAppsDto.getSite(), joinedAppsDto.getJdosAppCode());
        if(joinedApps == null){
            JoinedHotswapApps apps = new JoinedHotswapApps();
            BeanUtils.copyProperties(joinedAppsDto,apps);
            apps.setYn(1);
            save(apps);
        }else{
            BeanUtils.copyProperties(joinedAppsDto,joinedApps);
            updateById(joinedApps);
        }
    }
    public JoinedHotswapApps getJoinedApps(String site,String appCode){
        LambdaQueryWrapper<JoinedHotswapApps> lqw = new LambdaQueryWrapper<>();
        lqw.eq(JoinedHotswapApps::getSite,site);
        lqw.eq(JoinedHotswapApps::getJdosAppCode,appCode);
        lqw.eq(JoinedHotswapApps::getYn,1);
        return getOne(lqw);
    }
}
