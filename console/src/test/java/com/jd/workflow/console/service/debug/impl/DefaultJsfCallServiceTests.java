package com.jd.workflow.console.service.debug.impl;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.config.JssConfig;
import com.jd.workflow.console.service.debug.JsfJarCacheService;
import com.jd.workflow.console.utils.JfsUtils;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class DefaultJsfCallServiceTests  extends BaseTestCase {
    DefaultJsfCallService defaultJsfCallService;
    JsfJarCacheService jsfJarCacheService;
    MavenJarLocation location = new MavenJarLocation("com.jd.wjf.test","jar-demo","1.0-SNAPSHOT");
    @Before
    public void setUp(){
        defaultJsfCallService = new DefaultJsfCallService();
        defaultJsfCallService.mavenTempJarLocation = "D:/tempJsfLocation";

        jsfJarCacheService = new JsfJarCacheService();
        jsfJarCacheService.setJfsUtils(newJsfUtils());
        defaultJsfCallService.jsfJarCacheService = jsfJarCacheService;
    }
    @Test
    public void testUploadFile() throws DependencyCollectionException, DependencyResolutionException, IOException {

       // defaultJsfCallService.downloadJar(location);
        File jarPath = defaultJsfCallService.findJarPath(location);
        jsfJarCacheService.uploadJarToOss(location,jarPath);

    }
    @Test
    public void testDownload() throws DependencyCollectionException, DependencyResolutionException, IOException {

        // defaultJsfCallService.downloadJar(location);
        File jarPath = defaultJsfCallService.findJarPath(location);
        jsfJarCacheService.downloadMavenFileToLocal(jarPath,location);

    }
    JssConfig newJssConfig(){
        JssConfig jssConfig = new JssConfig();
        jssConfig.setAccessKey("ZDcl7q0ygh8Asm9a");
        jssConfig.setSecretKey("uqrdal5WRv7BC8iqJIUPwox76xdBK3r1BgGgxkpP");
        jssConfig.setHostName("storage.jd.local");
        jssConfig.setConnectionTimeout(50000);
        return jssConfig;
    }
    JfsUtils newJsfUtils(){
        JfsUtils jsfUtils = new JfsUtils();
        jsfUtils.setJssConfig(newJssConfig());
        return jsfUtils;
    }


}
