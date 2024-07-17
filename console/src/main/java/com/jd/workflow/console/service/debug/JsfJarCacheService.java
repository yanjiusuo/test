package com.jd.workflow.console.service.debug;

import cn.hutool.core.util.ZipUtil;
import com.google.common.io.Files;
import com.jd.workflow.console.utils.JfsUtils;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * jsf jar包缓存功能
 */
@Service
@Slf4j
public class JsfJarCacheService {
    static final String DEFAULT_JSF_BUCKET_NAME = "jsf-cached-jar/";
    @Autowired
    JfsUtils jfsUtils;

    public void setJfsUtils(JfsUtils jfsUtils) {
        this.jfsUtils = jfsUtils;
    }

    public String uploadJarToOss(MavenJarLocation location, File file){
        long start = System.currentTimeMillis();
        File tempDir = Files.createTempDir();
        // 将file压缩到tempFile
        File tempFile = new File(tempDir, "temp.zip");
        try {

            ZipUtil.zip(file.getPath(), tempFile.getPath());
            log.info("file.begin_zip_file:{}", tempFile.getAbsolutePath());
            jfsUtils.removeExistFile(DEFAULT_JSF_BUCKET_NAME+location.toFolder()+".zip");
            jfsUtils.uploadFile(tempFile.getAbsolutePath(),"lht",DEFAULT_JSF_BUCKET_NAME+location.toFolder()+".zip");
            log.info("file.upload_file_to_oss:{}", location.toFolder());
            log.info("file.upload_file_to_oss_cost:jar={},cost={}",location.toString(), System.currentTimeMillis() - start);
            return DEFAULT_JSF_BUCKET_NAME+location.toFolder()+".zip";
        } catch (Exception e) {
            throw new BizException("上传依赖jar包失败",e);
        } finally {
           FileUtils.deleteQuietly(tempDir);
        }
    }
    public void downloadMavenFileToLocal(File targetFile,MavenJarLocation location){
        if(!targetFile.exists()){
            targetFile.mkdirs();
        }
        File zipFile = new File(targetFile,"temp.zip");
        try{

            jfsUtils.downloadFile(DEFAULT_JSF_BUCKET_NAME+location.toFolder()+".zip",zipFile);
            ZipUtil.unzip(zipFile,targetFile);
        }finally {
            FileUtils.deleteQuietly(zipFile);
        }




    }
}
