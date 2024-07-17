package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.doc.CodingAppRelationMapper;
import com.jd.workflow.console.entity.CodingAppRelation;
import com.jd.workflow.console.service.CodingAppRealtionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 同步jsf文档日志 服务实现类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-06-07
 */
@Service
public class CodingAppRealtionServiceImpl extends ServiceImpl<CodingAppRelationMapper, CodingAppRelation> implements CodingAppRealtionService {

    @Override
    public String getByCodePath(String codePath) {
        LambdaQueryWrapper<CodingAppRelation> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(CodingAppRelation::getCodePath,codePath);
        CodingAppRelation relation=getOne(wrapper);
        if (null!=relation){
            return relation.getAppCode();
        }
        return "";
    }
}
