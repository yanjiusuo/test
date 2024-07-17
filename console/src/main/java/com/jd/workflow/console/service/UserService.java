package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.condition.FieldQueryWrapper;
import com.jd.workflow.console.dao.mapper.UserMapper;
import com.jd.workflow.console.entity.UserInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, UserInfo> {
    List<UserInfo> getUser(){
        FieldQueryWrapper wrapper = new FieldQueryWrapper();
        return list(wrapper);
    }
}