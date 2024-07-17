package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dao.mapper.InterfaceFollowListMapper;
import com.jd.workflow.console.entity.InterfaceFollowList;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterfaceFollowListService extends ServiceImpl<InterfaceFollowListMapper, InterfaceFollowList> {
     @Autowired
     IInterfaceManageService interfaceManageService;


    @Autowired
    IMethodManageService methodManageService;

    public Long followInterface(Long interfaceId){
         Guard.notEmpty(interfaceId,"接口id不可为空");
         InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
         Guard.notEmpty(interfaceManage,"无效的接口id");
         InterfaceFollowList followList = getUserFollow(interfaceId);
         if(followList == null){
             InterfaceFollowList list = new InterfaceFollowList();
             list.setErp(UserSessionLocal.getUser().getUserId());
             list.setInterfaceId(interfaceId);
             save(list);
             return list.getId();
         }else{
             throw new BizException("当前用户已关注该接口");
         }
     }

    public Long followInterfaceByMethodId(Long methodId){
        Guard.notEmpty(methodId,"接口id不可为空");
        MethodManage methodManage =methodManageService.getById(methodId);
        Guard.notEmpty(methodManage,"无效的接口id");
        InterfaceFollowList followList = getUserFollowByMethodId(methodId);
        if(followList == null){
            InterfaceFollowList list = new InterfaceFollowList();
            list.setErp(UserSessionLocal.getUser().getUserId());
            list.setInterfaceId(methodManage.getInterfaceId());
            list.setMethodId(methodId);
            save(list);
            return list.getId();
        }else{
            throw new BizException("当前用户已关注该接口方法");
        }
    }

    public void unFollowInterfaceByMethodId(Long methodId){
        Guard.notEmpty(methodId,"方法id不可为空");
        MethodManage manage = methodManageService.getById(methodId);
        Guard.notEmpty(manage,"无效的接口id");

        LambdaQueryWrapper<InterfaceFollowList> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceFollowList::getErp, UserSessionLocal.getUser().getUserId());
        lqw.eq(InterfaceFollowList::getMethodId, methodId);
        lqw.eq(InterfaceFollowList::getInterfaceId, manage.getInterfaceId());
        remove(lqw);
    }

    public void unFollowInterface(Long interfaceId){
        Guard.notEmpty(interfaceId,"接口id不可为空");
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceManage,"无效的接口id");

        LambdaQueryWrapper<InterfaceFollowList> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceFollowList::getErp, UserSessionLocal.getUser().getUserId());
        lqw.eq(InterfaceFollowList::getInterfaceId, interfaceId);
        remove(lqw);
    }
    public Set<String> getInterfaceFollowUser(Long interfaceId){
        LambdaQueryWrapper<InterfaceFollowList> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceFollowList::getInterfaceId, interfaceId);
        List<InterfaceFollowList> list = list(lqw);
        if(list == null || list.isEmpty()){
            return Collections.emptySet();
        }
        return list.stream().map(vs->vs.getErp()).collect(Collectors.toSet());
    }
     private InterfaceFollowList getUserFollow(Long interfaceId){
         LambdaQueryWrapper<InterfaceFollowList> lqw = new LambdaQueryWrapper<>();
         lqw.eq(InterfaceFollowList::getErp, UserSessionLocal.getUser().getUserId());
         lqw.eq(InterfaceFollowList::getInterfaceId, interfaceId);
         List<InterfaceFollowList> list = list(lqw);
         if(list == null || list.isEmpty()){
             return null;
         }
        return list.get(0);
     }
    private InterfaceFollowList getUserFollowByMethodId(Long methodId){
        LambdaQueryWrapper<InterfaceFollowList> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceFollowList::getErp, UserSessionLocal.getUser().getUserId());
        lqw.eq(InterfaceFollowList::getMethodId, methodId);
        List<InterfaceFollowList> list = list(lqw);
        if(list == null || list.isEmpty()){
            return null;
        }
        return list.get(0);
    }
}