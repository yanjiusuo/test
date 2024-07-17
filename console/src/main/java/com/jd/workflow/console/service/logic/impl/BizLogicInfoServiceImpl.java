package com.jd.workflow.console.service.logic.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.logic.BizLogicInfoMapper;
import com.jd.workflow.console.entity.logic.BizLogicInfo;
import com.jd.workflow.console.entity.logic.BizLogicTypeEnum;
import com.jd.workflow.console.service.logic.IBizLogicInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 组件业务逻辑信息表 从藏经阁同步 服务实现类
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-06-17
 */
@Service
public class BizLogicInfoServiceImpl extends ServiceImpl<BizLogicInfoMapper, BizLogicInfo> implements IBizLogicInfoService {

    /**
     * 更新修改 BizLogicInfo 数据
     * @param bizLogicInfo
     * @return
     */
    public boolean saveOrUpdateData(BizLogicInfo bizLogicInfo){
        //校验方法如惨
        checkSaveOrUpdateParam(bizLogicInfo);
        //修改更新数据
        LambdaQueryWrapper<BizLogicInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizLogicInfo::getInterfaceName, bizLogicInfo.getInterfaceName());
        queryWrapper.eq(BizLogicInfo::getYn, DataYnEnum.VALID.getCode());
        queryWrapper.eq(BizLogicInfo::getType, bizLogicInfo.getType());
        if(StringUtils.isNotBlank(bizLogicInfo.getMethodName())){
            queryWrapper.eq(BizLogicInfo::getMethodName, bizLogicInfo.getMethodName());
        }
        return this.saveOrUpdate(bizLogicInfo, queryWrapper);
    }

    @Override
    public List<BizLogicInfo> obtainInfoListByInterfaceName(String interfaceName) {
        LambdaQueryWrapper<BizLogicInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizLogicInfo::getInterfaceName, interfaceName);
        queryWrapper.eq(BizLogicInfo::getYn, DataYnEnum.VALID.getCode());
        queryWrapper.eq(BizLogicInfo::getType, 1);
        return list(queryWrapper);
    }

    @Override
    public List<BizLogicInfo> obtainInfoListByInterfaceAndMethod(String interfaceName,String methodName) {
        LambdaQueryWrapper<BizLogicInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizLogicInfo::getInterfaceName, interfaceName);
        queryWrapper.eq(BizLogicInfo::getYn, DataYnEnum.VALID.getCode());
        queryWrapper.eq(BizLogicInfo::getMethodName, methodName);
        return list(queryWrapper);
    }

    private void checkSaveOrUpdateParam(BizLogicInfo bizLogicInfo) {
        if (Objects.isNull(bizLogicInfo)) {
            throw new BizException("入参不能为空");
        }
        if (StringUtils.isEmpty(bizLogicInfo.getInterfaceName())) {
            throw new BizException("接口名不能为空");
        }
        if (Objects.isNull(bizLogicInfo.getType()) || !BizLogicTypeEnum.check(bizLogicInfo.getType())) {
            throw new BizException("入参属性[type]非法，只能是1-3");
        }
        if (bizLogicInfo.getType() > 1 && StringUtils.isEmpty(bizLogicInfo.getMethodName())) {
            throw new BizException("入参属性[methodName]，不能为空");
        }
    }

}

