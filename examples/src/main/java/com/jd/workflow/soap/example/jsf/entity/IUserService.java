package com.jd.workflow.soap.example.jsf.entity;

import java.util.Map;

public interface IUserService {
    public CommonResult<Long> addUser(UserDto user);
    public CommonResult<UserDto> getUser(Long id);
    public CommonResult<Long> removeUser(Long id);
    public Person save(Person person);
    public SimpleTypeClass simpleType(SimpleTypeClass simpleTypeClass);



    Map<String,Object> mapType(Map<String,Object> map);

    public void noOutput(int a,int b);
}
