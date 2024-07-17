package com.jd.workflow.console.service.remote.api.dto.jdos;


import lombok.Data;

import java.util.List;

/**
 * @author wufagang
 * @description
 * @date 2023年02月02日 18:26
 */
@Data
public class JdosAppMembers {
    //系统研发
    private List<String> systemAdmin;
    //应用研发
    private List<String> appAdmin;
    //应用测试
    private List<String> appTester;
    //系统运维
    private List<String> systemOp;
    //应用负责人
    private List<String> appOwner;
    //应用运维人员
    private List<String> appOp;
    //系统负责人
    private List<String> systemOwner;
    //系统测试人
    private List<String> systemTester;

}
