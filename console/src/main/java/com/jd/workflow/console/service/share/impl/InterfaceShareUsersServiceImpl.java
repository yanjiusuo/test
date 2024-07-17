package com.jd.workflow.console.service.share.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.dao.mapper.share.InterfaceShareUsersMapper;
import com.jd.workflow.console.entity.FlowParamGroup;
import com.jd.workflow.console.entity.share.InterfaceShareUser;
import com.jd.workflow.console.service.share.IInterfaceShareUsersService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 16:44
 * @Description:
 */
@Service
public class InterfaceShareUsersServiceImpl extends ServiceImpl<InterfaceShareUsersMapper, InterfaceShareUser> implements IInterfaceShareUsersService {
    @Override
    public InterfaceShareUser getShareUserByGroupIdAndUserCoe(Long groupId, String userCode) {
        InterfaceShareUser lastObj = this.getOne(Wrappers.<InterfaceShareUser>lambdaQuery()
                .eq(InterfaceShareUser::getGroupId, groupId)
                .eq(InterfaceShareUser::getSharedUserCode, userCode)
                .eq(InterfaceShareUser::getYn, DataYnEnum.VALID.getCode()));
        return lastObj;
    }

    @Override
    public List<String> getSharedUserCodeList(Long groupId) {
        return null;
    }
}
