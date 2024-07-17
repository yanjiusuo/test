package com.jd.workflow.console.service;

import com.jd.businessworks.domain.FlowBeanInfo;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.method.ClassMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Service
@Slf4j
public class JavaBeanService {
    private static final String CLS_PREFIX = ".class";
    private static final String PACKAGE_SPLIT = ".";

    private static final String FILE_PATH_SPLIT = "/";
    public  List<FlowBeanInfo> parseJavaBean(MultipartFile multipartFile)  {
        String fileDir = UUID.randomUUID().toString();
        if(!multipartFile.getOriginalFilename().endsWith(".jar")){
            throw new BizException("无效的jar包文件");
        }
        File tempFile = null;
        try{
            tempFile = File.createTempFile(fileDir, ".jar");
            try( FileOutputStream outputStream = new FileOutputStream(tempFile)){
                IOUtils.copy(multipartFile.getInputStream(),outputStream);
            }
            return parseMultiJavaBean(tempFile);
        }catch (StdException e){
            throw e;
        }catch (Exception e){
            log.info("bean.err_parse_java_bean",e);
            throw new BizException("解析jar包失败:"+e.getMessage(),e);
        }finally {
            if(tempFile != null){
                tempFile.delete();
            }
        }

    }
    public  List<FlowBeanInfo> parseMultiJavaBean(File tempFile){
        try{
            List<FlowBeanInfo> beans = new ArrayList<>();
            URLClassLoader loader = new URLClassLoader(new URL[]{tempFile.toURI().toURL()},Thread.currentThread().getContextClassLoader());
            JarFile jar = new JarFile(tempFile.getPath());
            Enumeration<JarEntry> enumFiles  = jar.entries();
            JarEntry entry;

            while (enumFiles.hasMoreElements()) {
                entry = (JarEntry)enumFiles.nextElement();
                String classFullName = entry.getName();
                if(classFullName.endsWith(CLS_PREFIX)) {
                    String clzName = classFullName.replace(FILE_PATH_SPLIT, PACKAGE_SPLIT);
                    clzName = clzName.substring(0, clzName.length() - 6);
                    try{
                        Class<?> clz = loader.loadClass(clzName);
                        if(!FlowBeanScanner.filterClazz(clz)) continue;

                        beans.add(FlowBeanScanner.beanMethodInfo(clz));
                    }catch (ClassNotFoundException classNotFoundException){
                        continue;
                    }catch (Error e){
                        continue;
                    }



                }
            }
            return beans;
        }catch (Exception e){
            log.error("file.err_parse_java_bean");
            throw new BizException("解析jar失败",e);
        }
    }
}
