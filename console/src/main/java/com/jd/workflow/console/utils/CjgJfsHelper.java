package com.jd.workflow.console.utils;

import com.alibaba.excel.util.IoUtils;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.jd.common.util.StringUtils;
import com.jd.jim.cli.Cluster;
import com.jd.jss.JingdongStorageService;
import com.jd.jss.domain.ObjectListing;
import com.jd.jss.domain.ObjectSummary;
import com.jd.jss.domain.StorageObject;
import com.jd.jss.service.ObjectService;
import com.jd.workflow.console.config.JssConfig;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/22
 */
@Component
@Slf4j
public class CjgJfsHelper {


    @Resource(name = "getCjgJssClient")
    JingdongStorageService jss;

    /**
     * redis 客户端
     */
    @Resource(name = "jimClient")
    private Cluster jimClient;

    /**
     * 从藏经阁oss获取markdown数据
     * @param bucket
     * @param key
     * @return
     */
    public String firstFromCacheDownloadJss(String bucket, String key){
        String jssData = jimClient.get(key);
        if(StringUtils.isNotBlank(jssData)){
            return jssData;
        }
        try {
            jssData = downloadFromJss(bucket, key);
            if(StringUtils.isNotBlank(jssData)){
                jimClient.set(key,jssData,12, TimeUnit.HOURS,false);
            }
        } catch (IOException e) {
            log.error("CjgJfsHelper.firstFromCacheDownloadJss");
        }
        return jssData;
    }

    public String downloadFromJss(String bucket, String key) throws IOException {
        log.error("准备从云存储下载对象，bucket={},key={}", bucket, key);
        String jssData;
        ObjectService objectService = jss.bucket(bucket).object(key);
        if (!objectService.exist()) {
            return null;
        }
        StorageObject storageObject = objectService.get();
        InputStream is = storageObject.getInputStream();
        try {
            jssData = new String(ByteStreams.toByteArray(is), Charsets.UTF_8);
        } finally {
            is.close();
            storageObject.close();
        }
        return jssData;
    }


}
