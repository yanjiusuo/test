package com.jd.workflow.console.service.plugin;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.plugin.PluginInstallStatisticMapper;
import com.jd.workflow.console.dao.mapper.plugin.PluginStatisticMapper;
import com.jd.workflow.console.entity.plugin.PluginInstallStatistic;
import com.jd.workflow.console.entity.plugin.PluginStatistic;
import com.jd.workflow.console.entity.plugin.dto.PluginStatisticDto;
import com.jd.workflow.soap.common.lang.Guard;
import org.springframework.stereotype.Service;

@Service
public class PluginStatisticService extends ServiceImpl<PluginStatisticMapper, PluginStatistic> {
   public Long saveStatistic(PluginStatisticDto dto){
       Guard.notEmpty(dto.getType(),"type不可为空");
       PluginStatistic statistic = dto.toPluginStatistic();
       save(statistic);
       return statistic.getId();
   }
}
