package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dao.mapper.InterfaceManageMapper;
import com.jd.workflow.console.dao.mapper.InterfaceExtParamMapper;
import com.jd.workflow.console.dto.PublicParamsDTO;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.InterfaceExtParam;
import com.jd.workflow.console.service.InterfaceExtService;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class InterfaceExtServiceImpl extends ServiceImpl<InterfaceExtParamMapper, InterfaceExtParam> implements InterfaceExtService {

    @Resource
    InterfaceManageMapper interfaceManageMapper;

    final UserInfoInSession user = UserSessionLocal.getUser();

    @Override
    public Long add(PublicParamsDTO publicParamsDTO) {
        Guard.notEmpty(publicParamsDTO,"传入参数不能为空");
        Guard.notEmpty(publicParamsDTO.getType(), "接口类型不能为空");
        //interfaceId存在校验
        interfaceCheck(publicParamsDTO.getInterfaceId());
        //type校验
        if(publicParamsDTO.getType() != 3) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER);
        }
        InterfaceExtParam publicParams = convertTo(publicParamsDTO);
        save(publicParams);
        return publicParams.getId();
    }

    private InterfaceExtParam convertTo(PublicParamsDTO publicParamsDTO) {
        InterfaceExtParam publicParams = new InterfaceExtParam();
        publicParams.setId(publicParamsDTO.getId());
        publicParams.setType(publicParamsDTO.getType());
        publicParams.setInterfaceId(publicParamsDTO.getInterfaceId());
        publicParams.setContent(JsonUtils.toJSONString(publicParamsDTO.getContent()));
        return publicParams;
    }

    private Boolean interfaceCheck(Long id){
        InterfaceManage interfaceManage = interfaceManageMapper.selectById(id);
        if(EmptyUtil.isEmpty(interfaceManage)){
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        return true;
    }

    @Override
    public Long edit(PublicParamsDTO publicParamsDTO) {
        Guard.notEmpty(publicParamsDTO,"传入参数不能为空");
        Guard.notEmpty(publicParamsDTO.getType(), "接口类型不能为空");
        //interfaceId存在校验
        interfaceCheck(publicParamsDTO.getInterfaceId());
        //type校验
        if(publicParamsDTO.getType() != 3) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER);
        }
        InterfaceExtParam publicParams = convertTo(publicParamsDTO);
        updateById(publicParams);
        return publicParams.getId();
    }

    @Override
    public Boolean remove(Long id) {
        interfaceCheck(id);
        return removeById(id);
    }

    @Override
    public InterfaceExtParam getByInterfaceId(Long interfaceId) {
        LambdaQueryWrapper<InterfaceExtParam> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceExtParam::getInterfaceId,interfaceId);
        return getOne(lqw);
    }

    @Override
    public Page<InterfaceExtParam> page(Integer page) {
        Guard.notEmpty(page,"入参不能为空");
        //查询条件
        LambdaQueryWrapper<InterfaceExtParam> lqw = new LambdaQueryWrapper();
        lqw.select(InterfaceExtParam.class, x -> !x.getColumn().equals("content"));
        lqw.eq(InterfaceExtParam::getCreator, user.getUserId());

        PublicParamsDTO publicParamsDTO = new PublicParamsDTO();
        //分页
        Page<InterfaceExtParam> paramsPage = new Page<>(page, publicParamsDTO.getSize());
        Page<InterfaceExtParam> pageParams = page(paramsPage,lqw);
        return pageParams;
    }
}
