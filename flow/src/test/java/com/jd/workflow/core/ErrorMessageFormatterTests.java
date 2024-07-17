package com.jd.workflow.core;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ErrorMessageFormatterTests extends BaseTestCase {
    static String TEMPLATE_PATH = "META-INF/services";

    public static void main(String[] args) throws IOException {

//liunx 解析对应的jar，循环文件夹，找到TEMPLATE_PATH文件所在位置
        URL url = Thread.currentThread().getContextClassLoader().getResource(TEMPLATE_PATH);
//截取jar路径
        String jarPath = url.toString().substring(0, url.toString().indexOf(TEMPLATE_PATH));
        URL jarURL = new URL(jarPath);
        JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
        JarFile jarFile = jarCon.getJarFile();
        Enumeration<JarEntry> jarEntrys = jarFile.entries();
//循环jar中所有文件夹
        while (jarEntrys.hasMoreElements()) {
            JarEntry entry = jarEntrys.nextElement();
            String className = entry.getName();
//判断已路径开头的文件夹及子文件
            if (className.startsWith(TEMPLATE_PATH) && !entry.isDirectory()) {
                String fileName = className.replace(TEMPLATE_PATH + "/", "");
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(className);
                File file = new File(String.valueOf(inputStream));
                System.out.println("fileName::"+fileName);
            }


        }

    }
    @Test
    public void testErrorMsgFormatter() throws IOException, URISyntaxException {
        ErrorMessageFormatter.main(null);
        ErrorMessageFormatter.loadXmlFile("META-INF");
    }
    @Test
    public void loadResources() throws IOException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(TEMPLATE_PATH);
        while (urls.hasMoreElements()){
            URL jarURL = urls.nextElement();
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntrys = jarFile.entries();
//循环jar中所有文件夹
            while (jarEntrys.hasMoreElements()) {
                JarEntry entry = jarEntrys.nextElement();
                String className = entry.getName();
//判断已路径开头的文件夹及子文件
                if (className.startsWith(TEMPLATE_PATH) && !entry.isDirectory()) {
                    String fileName = className.replace(TEMPLATE_PATH + "/", "");
                    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(className);
                    File file = new File(String.valueOf(inputStream));
                    System.out.println("fileName::"+fileName);
                }


            }
        }

    }
    @Test
    public void PathMatchingResourcePatternResolver1()  throws Exception{

        ResourcePatternResolver loader = new PathMatchingResourcePatternResolver();
        Resource[] resources = loader.getResources("classpath:"+TEMPLATE_PATH+"/**");
        for(int i=0;i< resources.length;i++) {
            System.out.println(resources[i].getURL().getFile());
        }
        ServiceLoader serviceLoader = null;
        serviceLoader.reload();
    }

}
