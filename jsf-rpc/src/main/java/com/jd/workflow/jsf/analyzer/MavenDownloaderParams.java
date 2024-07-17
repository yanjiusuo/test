package com.jd.workflow.jsf.analyzer;

import lombok.Data;

@Data
public class MavenDownloaderParams {

    /**
     * jar包在maven仓库中的groupId
     */
    private String groupId;
    /**
     * jar包在maven仓库中的artifactId
     */
    private String artifactId;
    /**
     * jar包在maven仓库中的version
     */
    private String version;
    /**
     * 远程maven仓库的URL地址，默认使用bw30的远程maven-public库
     */
   // private String repository="http://ae.mvn.bw30.com/repository/maven-public/";
    private String repository="https://maven.aliyun.com/repository/public";
    /**
     * 下载的jar包存放的目标地址，默认为./target/repo
     */
    private String target="d:/tmp/target";
    /**
     * 登录远程maven仓库的用户名，若远程仓库不需要权限，设为null，默认为null
     */
    private String username=null;
    /**
     * 登录远程maven仓库的密码，若远程仓库不需要权限，设为null，默认为null
     */
    private String password=null;
}
