package com.jd.workflow.console.service.remote.api.dto.jagile;

import lombok.Data;

import java.util.List;

@Data
public class JagileMember {
    List<String> systemAdmin; // 系统研发 -
    List<String> administrator;// 超级管理员
    List<String> appAdmin;// 应用研发 -
    List<String> appTester;// 应用测试 -
    List<String> systemOp;// 系统运维 -
    List<String> appOwner;// 应用负责人 -
    List<String> appOp;// 应用运维 -
    List<String> systemOwner;// 系统负责人 -
    List<String> user;// 应用访客
    List<String> systemTester;// 应用测试 -

}
