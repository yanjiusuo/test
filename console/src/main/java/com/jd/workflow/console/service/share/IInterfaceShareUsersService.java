package com.jd.workflow.console.service.share;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.entity.share.InterfaceShareUser;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 16:42
 * @Description:
 */
public interface IInterfaceShareUsersService extends IService<InterfaceShareUser> {

    InterfaceShareUser getShareUserByGroupIdAndUserCoe(Long groupId, String userCode);

    /**
     * 获取已分享
     *
     * @param groupId
     * @return 根据分组id获取已分享人员erp集合
     */
    List<String> getSharedUserCodeList(Long groupId);


}
