package com.jd.workflow.soap.example.jsf.entity;

import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.Map;

public class UserServiceJImpl implements IUserService{
    @Override
    public CommonResult<Long> addUser(UserDto user) {
        return CommonResult.buildSuccessResult(user.getId());
    }

    @Override
    public CommonResult<UserDto> getUser(Long id) {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setUserCode("wjf");
        dto.setUserName("测试");
        dto.setPassword("abc111");
        return CommonResult.buildSuccessResult(dto);
    }

    @Override
    public CommonResult<Long> removeUser(Long id) {
        return CommonResult.buildSuccessResult(id);
    }

    @Override
    public Person save(Person person) {
        return person;
    }

    @Override
    public SimpleTypeClass simpleType(SimpleTypeClass simpleTypeClass) {
        return simpleTypeClass;
    }

    @Override
    public Map<String, Object> mapType(Map<String, Object> map) {
        return map;
    }

    @Override
    public void noOutput(int a, int b) {

    }
}
