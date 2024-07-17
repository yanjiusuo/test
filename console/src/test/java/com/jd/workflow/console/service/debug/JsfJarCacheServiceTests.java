package com.jd.workflow.console.service.debug;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.config.JssConfig;
import com.jd.workflow.console.utils.JfsUtils;
import com.jd.workflow.jsf.analyzer.AetherJarDownloader;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class JsfJarCacheServiceTests extends BaseTestCase {
    JsfJarCacheService jsfJarCacheService;
    MavenJarLocation location = new MavenJarLocation("com.jd.wjf.test","jar-demo","1.0-SNAPSHOT");
    @Before
    public void before(){
        jsfJarCacheService = new JsfJarCacheService();
    }

    @Test
    public void testUploadFile(){
        File file = new File("D:/tempJsfLocation");

        jsfJarCacheService.downloadMavenFileToLocal(file,location);
    }

    @Test
    public void testAetherVersion(){
        Long    start = System.currentTimeMillis();
        String jarLatestVersion = AetherJarDownloader.getJarLatestVersion(location);
        System.out.println(jarLatestVersion);
        System.out.println(System.currentTimeMillis()-start);
    }
    @Test
    public void testDownloadFile(){

    }
}
