package com.jd.workflow.console.utils;

import com.alibaba.excel.util.IoUtils;
import com.jd.jss.JingdongStorageService;
import com.jd.jss.domain.ObjectListing;
import com.jd.jss.domain.ObjectSummary;
import com.jd.jss.domain.StorageObject;
import com.jd.jss.service.ObjectService;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.config.JssConfig;
import com.jd.workflow.console.entity.doc.AppDocReportRecord;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/22
 */
@Component
@Slf4j
public class JfsUtils {
    /**
     *
     */
    private static final Pattern COMPILE = Pattern.compile("\\S*[?]\\S*");
    /**
     *
     */
    @Resource
    JssConfig jssConfig;

    @Resource(name = "getJssClient")
    JingdongStorageService jss;

    public void setJssConfig(JssConfig jssConfig) {
        this.jssConfig = jssConfig;
    }

    /**
     *
     */
    private static final int TIME_MAX = 3600 * 24 * 30 * 12 * 20;


    /**
     * 上传流
     *
     * @param urlPath    包地址
     * @param bucketName solution-id-方案名
     * @param objectKey  应用名-版本.后缀
     * @return oss包地址
     */
    public String uploadStream(String urlPath, String bucketName, String objectKey) {
        String urlStr = null;
        try {
            String suffix = parseSuffix(urlPath);
            objectKey = new StringBuilder(objectKey).append(".").append(suffix).toString();
            byte[] bytes = downloadBinary(urlPath);
            InputStream inputStream = new ByteArrayInputStream(bytes);
            boolean exist = jss.hasBucket(bucketName);
            if (!exist) {
                jss.bucket(bucketName).create();
            }
            jss.bucket(bucketName).object(objectKey).entity(bytes.length, inputStream).put();
            URI uri = jss.bucket(bucketName).object(objectKey).generatePresignedUrl(TIME_MAX);
            urlStr = uri.toString();
        } catch (Exception e) {
            log.info("上传异常：" + e.getMessage());
        }
        return urlStr;
    }

    /**
     * 下载二进制数组
     *
     * @param destUrl destUrl
     * @return 字节数组
     */
    private byte[] downloadBinary(String destUrl) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            URL url = new URL(destUrl);
            InputStream in = url.openStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public void downloadFile(String path, File target){
        StorageObject storageObject = null;
        BufferedOutputStream os = null;
        try {
            ObjectService object = jss.bucket("lht").object(path);
            storageObject = object.get();
            os = new BufferedOutputStream(new FileOutputStream(target));
            IoUtils.copy(storageObject.getInputStream(),os);
            os.flush();
        } catch (IOException e) {
            throw new BizException("下载文件失败",e);
        } finally {
            IOUtils.closeQuietly(os);
            if(storageObject !=null){
                IOUtils.closeQuietly(storageObject.getInputStream());
                storageObject.close();
            }
        }
    }

    /**
     * 获取后缀
     *
     * @param url url
     * @return 后缀
     */
    public String parseSuffix(String url) {
        Pattern pattern = COMPILE;
        Matcher matcher = pattern.matcher(url);
        String[] spUrl = url.split("/");
        int len = spUrl.length;
        String endUrl = spUrl[len - 1];
        String[] spEndUrl;
        if (matcher.find()) {
            spEndUrl = endUrl.split("\\?");
            return spEndUrl[0].split("\\.")[1];
        }
        spEndUrl = endUrl.split("\\.");
        return spEndUrl[spEndUrl.length - 1];
    }

    /**
     * 上传文件
     *
     * @param multipartFile multipartFile
     * @param bucketName    bucketName
     * @param objectKey     objectKey
     * @return s
     */
    public String uploadStream(MultipartFile multipartFile, String bucketName, String objectKey) {
        String urlStr;
        String fileName = multipartFile.getOriginalFilename();
        log.info("uploadStream fileName:{},bucketName:{},objectKey:{}", fileName, bucketName, objectKey);
        String suffix = parseSuffix(fileName);
        objectKey = objectKey + "." + suffix;
        urlStr = this.uploadToJss(multipartFile, bucketName, objectKey);
        return urlStr;
    }

    /**
     * 上传文件
     *
     * @param multipartFile multipartFile
     * @param bucketName    bucket
     * @return 下载地址
     */
    public String uploadStreamWithFileName(MultipartFile multipartFile, String bucketName) {
        String urlStr;
        String fileName = multipartFile.getOriginalFilename();
        String suffix = parseSuffix(fileName);
        String objectKey = fileName + "-" + System.currentTimeMillis() + "." + suffix;
        log.info("uploadStream fileName:{}, bucketName:{}, objectKey:{}", fileName, bucketName, objectKey);
        urlStr = this.uploadToJss(multipartFile, bucketName, objectKey);
        return urlStr;
    }
    public URI getChromePluginDownloadUrl(String directory){
        ObjectListing list = jss.bucket("lht").prefix(directory).listObject();
        List<String> paths = new ArrayList<>();
        for (ObjectSummary objectSummary : list.getObjectSummaries()) {
            paths.add(objectSummary.getKey());
        }
        Collections.sort(paths);
        URI uri =jss.bucket("lht").object(paths.get(paths.size() -  1)).generatePresignedUrl(3600);
        return uri;
    }
    public String uploadToJss(String content,String bucketName,String objectKey){
        String urlStr = null;
        try {
            boolean exist = jss.hasBucket(bucketName);
            if (!exist) {
                jss.bucket(bucketName).create();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String day = dateFormat.format(new Date());
            objectKey = "hotUpdateErrorLog" + day + "/" + objectKey;
            log.info("uploadToJss key:{}", objectKey);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(Charset.forName("utf-8")));
            String contentType = "text/html";
            jss.bucket(bucketName).object(objectKey).entity(byteArrayInputStream.available(), byteArrayInputStream).contentType(contentType).put();
            URI uri = jss.bucket(bucketName).object(objectKey).generatePresignedUrl(TIME_MAX);
            urlStr = uri.toString();
            // 返回公网域名链接
            urlStr = urlStr.replace("storage.jd.local", "storage.360buyimg.com");
        } catch (Exception e) {
            log.error("上传文件到JD oss异常：" + e.getMessage());
        }
        return urlStr;
    }
    private String uploadToJss(MultipartFile multipartFile, String bucketName, String objectKey) {
        String urlStr = null;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            boolean exist = jss.hasBucket(bucketName);
            if (!exist) {
                jss.bucket(bucketName).create();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String day = dateFormat.format(new Date());
            objectKey = "dataflow_" + day + "/" + objectKey;
            log.info("uploadToJss key:{}", objectKey);
            jss.bucket(bucketName).object(objectKey).entity(inputStream.available(), inputStream).put();
            URI uri = jss.bucket(bucketName).object(objectKey).generatePresignedUrl(TIME_MAX);
            urlStr = uri.toString();
            // 返回公网域名链接
            urlStr = urlStr.replace("storage.jd.local", "storage.360buyimg.com");
        } catch (IOException e) {
            log.error("上传文件到JD oss异常：" + e.getMessage());
        }
        return urlStr;
    }
    public boolean removeExistFile( String objectKey){
            String bucketName = "lht";

            boolean exist = jss.hasBucket(bucketName);
            if (!exist) {
                jss.bucket(bucketName).create();
            }
            ObjectService object = jss.bucket(bucketName).object(objectKey);
            if(!object.exist()){
                return false;

            }
            StorageObject storageObject = object.get();
            if(storageObject != null){
                object.delete();
                return true;
            }
            return false;

    }
    public String uploadFile(String urlPath, String bucketName, String objectKey) {
        String urlStr = null;
        try {
            File file = new File(urlPath);
            boolean exist = jss.hasBucket(bucketName);
            if (!exist) {
                jss.bucket(bucketName).create();
            }
            jss.bucket(bucketName).object(objectKey).entity(file).put();
            URI uri = jss.bucket(bucketName).object(objectKey).generatePresignedUrl(TIME_MAX);
            urlStr = uri.toString();
            // 返回公网域名链接
            urlStr = urlStr.replace("storage.jd.local", "storage.360buyimg.com");
        } catch (Exception e) {
            log.info("上传异常：" + e.getMessage());
        }
        return urlStr;
    }
    public String uploadContent(String content,String bucketName,String objectKey){
        String urlStr = null;
        try {
            boolean exist = jss.hasBucket(bucketName);
            if (!exist) {
                jss.bucket(bucketName).create();
            }
            byte[] bytes = content.getBytes("utf-8");
            jss.bucket(bucketName).object(objectKey).entity(bytes.length,new ByteArrayInputStream(bytes)).put();
            URI uri = jss.bucket(bucketName).object(objectKey).generatePresignedUrl(TIME_MAX);
            urlStr = uri.toString();
            // 返回公网域名链接
            urlStr = urlStr.replace("storage.jd.local", "storage.360buyimg.com");
        } catch (Exception e) {
            log.error("上传异常：" + e.getMessage(),e);
        }
        return urlStr;
    }


}
