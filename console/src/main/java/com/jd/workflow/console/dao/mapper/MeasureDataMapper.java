package com.jd.workflow.console.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.entity.MeasureData;

import java.util.List;

/**
 * @author yza
 * @description
 * @date 2024/1/12
 */
public interface MeasureDataMapper extends BaseMapper<MeasureData> {

    List<String> selectErps();
}
