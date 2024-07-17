package com.jd.workflow.console.dto.plugin.jdos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2021/8/3
 */
@Data
public class JdosGroup {
    /**
     * 包下载地址
     */
    private String packageUrl;
    /**
     * 下载地址类型	枚举 war, jar,zip,image
     */
    private String packageType;
    /**
     * 分组标识	小写字母开头，4-20位字母、数字和中划线组合
     */
    private String groupName;
    /**
     * 分组名 中文名
     */
    private String nickname;
    /**
     * 分组类别	枚举 test 测试环境，pre 预发，pro生产
     */
    private String environment;
    /**
     * 容器规格
     */
    private String flavor;
    /**
     * 磁盘大小	必填 磁盘大小(请输入5整数倍)
     */
    private Integer diskSize;
    /**
     * 磁盘类型	磁盘类型(目前仅支持SAS)
     */
    private String diskType="SAS";
    /**
     * 容器个数	必填至少一个
     */
    private Integer podNumber;
    /**
     * 机房
     */
    private String region;
    /**
     * zone	必填，但是没有用，随意写一个空间名
     */
    private String zone="default_c.n1r.7xlarge";
    /**
     * platformHosted	是否自动拉取	autoPull 值 true自动拉取 false否
     */
    private String platformHosted="true";
    /**
     * jsfStatus	是否注册了jsf	active(是)inactive(否)
     */
    private String jsfStatus;

    /**
     * nvs	环境变量	其中结构为{“key”:“xxx”,“value”:“sda”} 默认为null
     */
    private String envs;
    /**
     *
     */
    private String cpuModel="";

    private List<JdosPod> podList;

}
