package com.jd.workflow.console.dao.mapper.watch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.workflow.console.entity.model.ApiModelDelta;
import com.jd.workflow.console.entity.watch.CodeActivity;
import com.jd.workflow.console.entity.watch.dto.DayBuildActivityDto;

import java.util.List;

public interface CodeActivityMapper  extends BaseMapper<CodeActivity> {
    List<DayBuildActivityDto> queryDayBuildTime(String  day);
}

