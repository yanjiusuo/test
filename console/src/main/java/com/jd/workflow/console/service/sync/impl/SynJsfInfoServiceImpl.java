package com.jd.workflow.console.service.sync.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.sync.SynJsfInfoMapper;
import com.jd.workflow.console.entity.sync.SynJsfInfo;
import com.jd.workflow.console.service.sync.SynJsfInfoService;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * jsf平台同步的接口信息表 服务实现类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-07-08
 * @date 2024-07-08 20:32
 */
@Service
public class SynJsfInfoServiceImpl extends ServiceImpl<SynJsfInfoMapper, SynJsfInfo> implements SynJsfInfoService {

    @Override
    public List<SynJsfInfo> queryList() {
        LambdaQueryWrapper<SynJsfInfo> lqwOr = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<SynJsfInfo> lqw = new LambdaQueryWrapper<>();
        lqw.likeRight(SynJsfInfo::getCjgDepartment, "京东集团-京东零售-平台产品与研发中心-端技术研发部").or().likeRight(SynJsfInfo::getCjgDepartment, "京东集团-京东零售-平台产品与技术研发中心-端技术研发部");
        return list(lqw);
    }

    public Integer getJsfNoByCodingAddress(String codingAddress){
        LambdaQueryWrapper<SynJsfInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SynJsfInfo::getCodeAddress, codingAddress);
        int count = count(lqw);
        return count;
    }
}
