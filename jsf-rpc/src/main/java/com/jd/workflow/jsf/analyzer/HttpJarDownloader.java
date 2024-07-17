package com.jd.workflow.jsf.analyzer;



import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 项目名称：parent
 * 类 名 称：HttpJarDownloader
 * 类 描 述：jar检测下载
 * 创建时间：2022-06-16 20:21
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Data
public class HttpJarDownloader {

    private static final String CENTRAL_STORE = "http://artifactory.jd.com/libs-releases/";

    private static final String SNAPSHOTS = "http://artifactory.jd.com/libs-snapshots/";

    private static final String JAR_FILE_EXT_NAME = ".jar";

    private static final String POM_FILE_EXT_NAME = ".pom";

    private static final String URL_SPLIT = "/";

    private static final String FILE_NAME_SPLIT = "-";

    /**
     * 检测远程jar是否存在
     * @param location
     * @return
     */
    public static boolean checkExistedRemoteJar(MavenJarLocation location){
        return readRemotePath(location,true);
    }

    /**
     * 检测远程pom是否存在
     * @param location
     * @return
     */
    public static boolean checkExistedRemotePom(MavenJarLocation location){
        return readRemotePath(location,false);
    }


    /**
     * 检测本地jar是否存在
     * @param location
     * @return
     */
    public static boolean checkExistedLocalJar(MavenJarLocation location){
        return getMavenLocalFile(location,true)!=null;
    }

    /**
     * 检测本地pom是否存在
     * @param location
     * @return
     */
    public static boolean checkExistedLocalPom(MavenJarLocation location){
        return getMavenLocalFile(location,false)!=null;
    }

    /**
     * 下载jar包
     * @param location
     * @return
     */
    public static File downLoadMavenJar(MavenJarLocation location){
        return download(location,true);
    }

    /**
     * 下载pom包
     * @param location
     * @return
     */
    public static File downLoadMavenPom(MavenJarLocation location){
        return download(location,false);
    }


    /**
     * 优先查询本地、本地没有则查询远程
     * @param location
     * @param isJar
     * @return
     */
    private static File download(MavenJarLocation location,boolean isJar){
        File f = getMavenLocalFile(location,isJar);
        if(f!=null){
            System.out.println(">>>>>>>>>from local file>>>>>>>>"+f.getPath());
            log.info("download result form local file = {}",f.getPath());
            return f;
        }
        String filePath = downloadRemote(location,isJar);
        if(filePath!=null){
            return new File(filePath);
        }
        return null;
    }


    /**
     * 远程下载jar包或pom
     * @return jar、pom下载到本地的路径
     */
    private static String downloadRemote(MavenJarLocation location,boolean isJar) {
        String url = getMavenRemoteUrl(location,isJar);
        String fileName = isJar?getJarFileName(location):getPomFileName(location);
        log.info("center url : {}", url);
        String path = null;
        try {
            path = downLoadFromUrl(url, fileName, getBasePath());
        } catch (IOException e) {
            log.error("jar-pom location : {}, url : {}", location, url);
            log.error("maven download jar or pom file error", e);
        }
        return path;
    }

    /**
     * 获取本地 File
     * @param location
     * @param isJar
     * @return
     */
    private static File getMavenLocalFile(MavenJarLocation location,boolean isJar){
        String url = getMavenLocalUrl(location,isJar);
        File f =  new File(url);
        if(f.exists()){
            if(!isSnapShot(location)){
                return f;
            }
            //如果是大于1分钟则删除TODO 待定
            if(System.currentTimeMillis()-f.lastModified()>1*60*1000){
                f.delete();
            }else{
                return f;
            }
        }
        return null;
    }

    /**
     * 访问远程jar或者pom
     * @param location
     * @param isJar
     * @return
     */
    private static boolean readRemotePath(MavenJarLocation location,boolean isJar) {
        String url = getMavenRemoteUrl(location,isJar);
        log.info("center url : {}", url);
        Integer responseCode = null;
        try {
            responseCode = readFromUrl(url);
        } catch (IOException e) {
            log.error("readRemotePath location : {}, url : {}", location, url);
            log.error("readRemotePath error", e);
        }
        return Objects.equals(responseCode, 200);
    }


    /**
     * 获得maven远程url
     * @param location
     * @param isJar
     * @return
     */
    private static String getMavenRemoteUrl(MavenJarLocation location,boolean isJar){
        String domain = CENTRAL_STORE;
        if (isSnapShot(location)) {
            domain = SNAPSHOTS;
        }
        return domain + getLocationPath(location)+(isJar?getJarFileName(location):getPomFileName(location));
    }

    /**
     * 获得maven本地url
     * @param location
     * @param isJar
     * @return
     */
    private static String getMavenLocalUrl(MavenJarLocation location,boolean isJar){
        String domain = CENTRAL_STORE;
        if (isSnapShot(location)) {
            domain = SNAPSHOTS;
        }
        return getBasePath() + File.separator+(isJar?getJarFileName(location):getPomFileName(location));
    }


    /**
     * maven私服上jar包路径
     * 例：com/jd/platform/parse/
     * @param location
     * @return
     */
    private static String getLocationPath(MavenJarLocation location) {
        return location.getGroupId().replace(".", "/") + URL_SPLIT + location.getArtifactId() + URL_SPLIT
                + location.getVersion() + URL_SPLIT;
    }


    /**
     * 拼接jar包文件名
     * 例：jcf-1.3.9.8-SNAPSHOT.jar
     * @param location
     * @return
     */
    private static String getJarFileName(MavenJarLocation location) {
        return location.getArtifactId() + FILE_NAME_SPLIT + location.getVersion() + JAR_FILE_EXT_NAME;
    }

    /**
     * 拼接jar包文件名
     * 例：jcf-1.3.9.8-SNAPSHOT.jar
     * @param location
     * @return
     */
    private static String getPomFileName(MavenJarLocation location) {
        return location.getArtifactId() + FILE_NAME_SPLIT + location.getVersion() + POM_FILE_EXT_NAME;
    }

    /**
     * 是否是快照
     * @param location
     * @return
     */
    private static boolean isSnapShot(MavenJarLocation location) {
        if (StringUtils.containsIgnoreCase(location.getVersion(), "SNAPSHOT")) {
            return true;
        }
        return false;
    }

    /**
     * jar包下载目录
     * @return
     */
    private static String getBasePath() {
        return "/export/data/jar";
    }

    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    private static String  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //得到输入流
        InputStream inputStream = null;
        try {
            inputStream = conn.getInputStream();
        } catch (FileNotFoundException e) {
            log.error("file.err_download_file",e);
            throw StdException.adapt(e);
        }
        //获取自己数组
        byte[] getData = readInputStream(inputStream);
        //文件保存位置
        File saveDir = new File(savePath);
        if(!saveDir.exists()){
            saveDir.mkdir();
        }
        File file = new File(saveDir+File.separator+fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if(fos!=null){
            fos.close();
        }
        if(inputStream!=null){
            inputStream.close();
        }
        return file.getPath();
    }

    /**
     * 判断网络Url中
     * @param urlStr
     * @throws IOException
     */
    private static Integer  readFromUrl(String urlStr) throws IOException {
        Integer responseCode = 0;
        //得到输入流
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3*1000);
            conn.setReadTimeout(3*1000);
            responseCode = conn.getResponseCode();
        } catch (FileNotFoundException e) {
            log.error("url is error >>>>>>>>>{}",urlStr);
            throw new IOException(e);
        }
        return responseCode;
    }


    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }



}
