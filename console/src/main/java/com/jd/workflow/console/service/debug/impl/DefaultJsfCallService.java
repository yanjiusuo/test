package com.jd.workflow.console.service.debug.impl;

import com.jd.workflow.console.base.IpUtil;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.debug.MavenLoadStatus;
import com.jd.workflow.console.dto.flow.param.JsfOutputExt;
import com.jd.workflow.console.dto.jsf.JarJsfDebugDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.debug.JsfJarRecord;
import com.jd.workflow.console.entity.debug.dto.JarLoadStatus;
import com.jd.workflow.console.entity.plugin.PluginStatistic;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMethodManageService;
import com.jd.workflow.console.service.debug.IJsfJarCallService;
import com.jd.workflow.console.service.debug.JsfJarCacheService;
import com.jd.workflow.console.service.debug.JsfJarRecordService;
import com.jd.workflow.console.service.doc.SwaggerParserService;
import com.jd.workflow.console.service.plugin.PluginStatisticService;
import com.jd.workflow.console.utils.RpcUtils;
import com.jd.workflow.jsf.analyzer.AetherJarDownloader;
import com.jd.workflow.jsf.analyzer.MavenJarLocation;
import com.jd.workflow.soap.common.cache.ICacheExpireListener;
import com.jd.workflow.soap.common.cache.impl.MemoryCache;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Service
@Slf4j
public class DefaultJsfCallService implements IJsfJarCallService {
    static final int FAIL_REASON_LENGTH = 65535;
    static final String DEFAULT_JSF_CACHE_KEY = "jsf_call_service";
    @Value("${maven.tempJarLocation}")
     String mavenTempJarLocation;
    @Value("${maven.classLoaderCacheTime:86400}")
    private Integer classLoaderCacheTime = 3600*24;
    @Autowired
    JsfJarRecordService jsfJarRecordService;
    @Autowired
    JsfJarCacheService jsfJarCacheService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IMethodManageService methodManageService;
    @Autowired
    PluginStatisticService pluginStatisticService;


    @Autowired
    @Resource(name = "defaultScheduledExecutor")
    ScheduledThreadPoolExecutor defaultScheduledExecutor;
    MemoryCache cache =new MemoryCache();

    @PostConstruct
    public void init(){
        cache.addListener(new ICacheExpireListener() {
            @Override
            public void onExpire(Object obj) {
                if(obj != null && obj instanceof URLClassLoader){

                    URLClassLoader loader = (URLClassLoader) obj;
                    log.info("close_exist_url_class_loader:"+loader.getURLs());
                    IOUtils.closeQuietly(loader);
                }
            }
        });
        initAllRecordFile();
    }

    public void downloadMavenJar(MavenJarLocation location){
        File jarPath = findJarPath(new MavenJarLocation(location.getGroupId(), location.getArtifactId(), location.getVersion()));
        jsfJarCacheService.downloadMavenFileToLocal(
                jarPath,
                new MavenJarLocation(location.getGroupId(), location.getArtifactId(), location.getVersion()));
        JsfJarRecord record = new JsfJarRecord();
        record.setGroupId(location.getGroupId());
        record.setArtifactId(location.getArtifactId());
        record.setVersion(location.getVersion());
        record.setJarActualVersion(AetherJarDownloader.getJarLatestVersion(location));
        record.setLastUpdatedTime(new Date());
        record.setCurrentCachedServerIp(IpUtil.getLocalIp());

        jsfJarRecordService.save(record);
    }
    private void initAllRecordFile(){
        List<Future> futures = new ArrayList<>();
        for (JsfJarRecord record : jsfJarRecordService.listAllLocalRecords()) {
            MavenJarLocation jarLocation = new MavenJarLocation(record.getGroupId(), record.getArtifactId(), record.getVersion());
            File jarPath = findJarPath(jarLocation);
            if(!jarPath.exists()){
                log.info("初始化jar包文档：{}",record);
                if(!JarLoadStatus.LOADED.getCode().equals(record.getLoadStatus())  ){
                    continue;
                }
               Future future = defaultScheduledExecutor.submit(()->{
                    try {
                        record.changeLoadingStatus(JarLoadStatus.LOADING.getCode());
                        jsfJarRecordService.updateById(record);
                        jsfJarCacheService.downloadMavenFileToLocal(
                                jarPath,
                                jarLocation);
                        log.info("下载jar包完成：{}",record);
                        record.changeLoadingStatus(JarLoadStatus.LOADED.getCode());
                        jsfJarRecordService.updateById(record);
                    } catch (Exception e) {
                        record.changeLoadingStatus(JarLoadStatus.LOAD_FAIL.getCode());
                        String desc = "下载"+jarLocation.toString()+"包失败:"+e.getMessage();
                        record.setFailReasonDesc(SwaggerParserService.truncateStr(desc,200));
                        record.setFailReasonDetail(SwaggerParserService.truncateStr(getStackTrace(e),FAIL_REASON_LENGTH));
                        jsfJarRecordService.updateById(record);
                        log.error("下载"+jarLocation.toString()+"包失败",e);
                    }
                });
               futures.add(future);
            }
        }
        for (Future future : futures) {
            try {
                future.get();
                log.info("jar包初始化完成");
            } catch (Exception e) {
                log.error("下载jar包失败",e);
            }
        }
    }
    private boolean isCausedByArtifactNotFoundException(Exception e){
        Throwable cause = e.getCause();
        while (cause != null){
            if(cause instanceof org.eclipse.aether.transfer.ArtifactNotFoundException ){
                return true;
            }
            cause =  cause.getCause();
        }
        return false;
    }
    public File downloadJar(MavenJarLocation location) throws DependencyCollectionException, DependencyResolutionException, IOException {
        long start = System.currentTimeMillis();
        File root = new File(mavenTempJarLocation,location.toFolder());
        if(!root.exists()){
            root.mkdirs();
        }
        List<File> files = null;
        try{
           files = AetherJarDownloader.downloadAllJar(location, root);
        }catch (DependencyResolutionException e){
            if(isCausedByArtifactNotFoundException(e)){
                throw new BizException("下载jar包失败，无效的maven坐标: "+location.getGroupId()+":"+location.getArtifactId()+":"+location.getVersion(),e);
            }
            throw e;

        }

        if(files.isEmpty()){
            throw new BizException("下载jar包失败，无效的maven坐标");
        }
        for (File file : files) {
            FileUtils.moveFile(file,new File(root,file.getName()));
        }
        for (File file : root.listFiles()) {
            if(file.isDirectory()){
                FileUtils.deleteDirectory(file);
            }
        }
        long end = System.currentTimeMillis();
        log.info("下载jar包耗时：jar={},{}ms",location,end-start);
        return root;
    }

    public void clearExistJar(MavenJarLocation location) {
        File root = new File(mavenTempJarLocation,location.toFolder());
        if(!root.exists()){
            root.mkdirs();
        }
        for (File file : root.listFiles()) {
            FileUtils.deleteQuietly(file);
        }
    }
    private JsfJarRecord newRecord(MavenJarLocation location){
        JsfJarRecord record = new JsfJarRecord();
        record.setGroupId(location.getGroupId());
        record.setArtifactId(location.getArtifactId());
        record.setVersion(location.getVersion());
        record.setLastUpdatedTime(new Date());
        record.setLoadStatus(JarLoadStatus.LOADING.getCode())  ;
        record.setCurrentCachedServerIp(IpUtil.getLocalIp());
        record.setCreator(UserSessionLocal.getUser().getUserId());
        record.setModifier(UserSessionLocal.getUser().getUserId());
        return record;
    }

    public boolean parseJsfJar(MavenJarLocation location) {
        File jarPath = findJarPath(location);
        log.info("开始解下jsf jar包：{}",location);
        statisticUserOp("parseJsfJar",location.toString());
        JsfJarRecord exist = jsfJarRecordService.getJsfRecord(location);
        if(exist == null){
            exist = newRecord(location);
            jsfJarRecordService.save(exist);
        }
        else {
            if (JarLoadStatus.LOADING.getCode().equals(exist.getLoadStatus())){
                throw new BizException("jar包正在解析中，不可重复解析,请耐心等待");
            }else if(JarLoadStatus.LOADED.getCode().equals(exist.getLoadStatus())){
                throw new BizException("jar包已存在，请勿重复导入");
            }
            exist.setLastUpdatedTime(new Date());
            exist.changeLoadingStatus(JarLoadStatus.LOADING.getCode());
            jsfJarRecordService.updateById(exist);
        }
        String latestVesion = AetherJarDownloader.getJarLatestVersion(location);

        parseAndDownloadJar(exist,location,latestVesion);
        log.info("结束jsf jar包：{}",location);
        return true;
    }

    private void processJarParseException(JsfJarRecord record, Exception e,String msg) {
        record.setFailReasonDesc(msg);
        record.setFailReasonDetail(SwaggerParserService.truncateStr(getStackTrace(e),FAIL_REASON_LENGTH));
        record.setLoadStatus(JarLoadStatus.LOAD_FAIL.getCode());
        jsfJarRecordService.updateById(record);
    }
    private void parseAndDownloadJar(JsfJarRecord record,MavenJarLocation location,String jarLatestVersion){
        record.setJarActualVersion(jarLatestVersion);
        try {

            clearExistJar(location);
            File file = downloadJar(location);
            String downloadUrl = jsfJarCacheService.uploadJarToOss(location, file);
            record.setJarActualVersion(jarLatestVersion);
            record.setLastUpdatedTime(new Date());
            record.setOssDownloadUrl(downloadUrl);
            record.changeLoadingStatus(JarLoadStatus.LOADED.getCode());
            jsfJarRecordService.updateById(record);

        } catch (DependencyCollectionException e) {
            processJarParseException(record,e,"maven依赖收集异常:"+e.getMessage());
            throw new BizException("maven依赖收集异常："+e.getMessage(),e);
        } catch (DependencyResolutionException e) {
            processJarParseException(record,e,"maven依赖定位失败:"+e.getMessage());
            throw new BizException("maven依赖定位失败："+e.getMessage(),e);
        } catch (BizException e){
            processJarParseException(record, (Exception) e.getCause(),e.getMessage());
            throw new BizException(e.getMessage(),e);
        }catch (Exception e) {
            processJarParseException(record,e,"下载jar包失败:"+e.getMessage());
            throw new BizException("下载jar包失败:"+e.getMessage(),e);
        }
    }
    @Override
    public boolean reParseJsfJar(MavenJarLocation location) {
        log.info("重新解析jsf jar包：{}",location);
        statisticUserOp("reParseJsfJar",location.toString());
        JsfJarRecord record = jsfJarRecordService.getJsfRecord(location);
        if(record == null){
            return parseJsfJar(location);
        }
        String jarLatestVersion = AetherJarDownloader.getJarLatestVersion(location);

        if(JarLoadStatus.LOADING.getCode().equals(record.getLoadStatus())){
            throw new BizException("jar包正在解析中，不可重复解析");
        }
        if(Objects.equals(jarLatestVersion,record.getJarActualVersion())){
            throw new BizException("jar包已存在，请勿重复导入");
        }

        String localIp = IpUtil.getLocalIp();

        if(record != null){
            if(!(record.getVersion().contains("SNAPSHOT") || record.getVersion().contains("snapshot"))){
                throw new BizException("只有SNAPSHOT版本才能重新导入");
            }
        }else if(!Objects.equals(localIp,record.getCurrentCachedServerIp())) {
            return ForwardJsfCallService.getInstance(record.getCurrentCachedServerIp()).reParseJsfJar(location);
        }

        record.setLastUpdatedTime(new Date());
        record.changeLoadingStatus(JarLoadStatus.LOADING.getCode());
        jsfJarRecordService.updateById(record);
        parseAndDownloadJar(record,location,jarLatestVersion);
        log.info("结束解析jsf jar包：{}",location);
        return true;
    }

    public File findJarPath(MavenJarLocation location){
        File root = new File(mavenTempJarLocation,location.toFolder());

        return root;
    }
    private void updateInterfacePath(JarJsfDebugDto dto){
        defaultScheduledExecutor.execute(()->{
            try{
                MethodManage manage = methodManageService.getMethodExcludeBigTextField(dto.getMethodId());
                InterfaceManage interfaceManage = interfaceManageService.getOneExcludeBigTextField(manage.getInterfaceId());
                if(interfaceManage.getPath() == null){
                    interfaceManage.setPath(dto.getLocation().toString());
                    interfaceManageService.updateById(interfaceManage);
                }
            }catch (Exception e){
                log.info("更新接口坐标失败：{}",e.getMessage());

            }
        });
    }
    @Override
    public JsfOutputExt jarCallJsf(JarJsfDebugDto dto) {
        {
            long methodId = dto.getMethodId()==null?0:dto.getMethodId();
            Map<String,Object> extDatas = new HashMap<>();
            extDatas.put("methodId",methodId);
            statisticUserOp("jarCallJsf",dto.getLocation().toString(),extDatas);
        }

        Guard.notEmpty(dto.getAlias(),"别名不能为空");
        JsfJarRecord record = jsfJarRecordService.getJsfRecord(dto.getLocation());
        if(record == null){
            throw new BizException("未解析jar包，请先下载jar包依赖");
        }
        String localIp = IpUtil.getLocalIp();
         if(!Objects.equals(localIp,record.getCurrentCachedServerIp())) {
            return ForwardJsfCallService.getInstance(record.getCurrentCachedServerIp()).jarCallJsf(dto);
        }

        if(dto.getMethodId() != null){
            updateInterfacePath(dto);
        }
        if(JarLoadStatus.LOAD_FAIL.getCode().equals(record.getLoadStatus())){
            throw new BizException("jar包解析失败，不可调用："+record.getFailReasonDesc());
        }

        File jarRoot = findJarPath(dto.getLocation());
        if(!jarRoot.exists()){
            throw new BizException("未解析jar包，请先下载jar包依赖");
        }
        URLClassLoader classLoader = cache.hGet("DEFAULT_JSF_CACHE_KEY", "urlClassLoader:"+dto.getLocation().toString());
        if(classLoader == null){
            classLoader = urlClassLoader(jarRoot);
            cache.hSet("DEFAULT_JSF_CACHE_KEY", "urlClassLoader:"+dto.getLocation().toString(), classLoader, classLoaderCacheTime);
        }
        JsfOutputExt jsfOutput = RpcUtils.invokeJarJsfCaller(dto,classLoader);
        return jsfOutput;
    }
    private static String getStackTrace(Throwable e){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintWriter(outputStream,true));
        return outputStream.toString();
    }
    private URLClassLoader urlClassLoader(File root){
        List<URL> urls = new ArrayList<>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try{
            for (File file : root.listFiles()) {
                if(file.getName().endsWith(".jar")){
                    urls.add(file.toURI().toURL());
                }
            }
            URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]) ,contextClassLoader);
            return urlClassLoader;
        }catch (Exception e){
            throw new BizException("调用失败：加载classloader失败",e);
        }

    }

    public MavenLoadStatus mavenIsLoaded(MavenJarLocation location) {
        JsfJarRecord record = jsfJarRecordService.getJsfRecord(location);

        if(record != null){
            MavenLoadStatus status = new MavenLoadStatus(true, false);
            status.setLoadStatus(record.getLoadStatus());
            long mills = System.currentTimeMillis() - record.getLastUpdatedTime().getTime();
            status.setCostTime(Variant.valueOf(mills/1000).toInt());
            status.setOperator(record.getModifier());
            return status;
        }
        MavenLoadStatus status = new MavenLoadStatus(false, false);
        status.setLoadStatus(JarLoadStatus.UNLOAD.getCode());
        return status;
    }
    public void statisticUserOp(String type, String data, Map<String,Object> extParams){
        String erp = UserSessionLocal.getUser().getUserId();
      defaultScheduledExecutor.submit(new Runnable() {
          @Override
          public void run() {
              try {

                  PluginStatistic statistic = new PluginStatistic();
                  statistic.setErp(erp);
                  statistic.setType(type);
                  statistic.setStatisticData(data);
                  statistic.setCreator(erp);
                  statistic.setModifier(erp);
                  statistic.setExtInfos(extParams);
                  pluginStatisticService.save(statistic);
              }catch (Exception e){
                  log.error("统计用户操作失败",e);
              }
          }
      });
    }
    public void statisticUserOp(String type,String data){
       statisticUserOp(type,data,null);

    }
    public boolean mavenCanReload(MavenJarLocation location) {
        JsfJarRecord record = jsfJarRecordService.getJsfRecord(location);

        if(record != null && JarLoadStatus.LOADED.getCode().equals(record.getLoadStatus())){
            boolean canReload = false;
            if(location.isSnapshot()){
                String jarLatestUpdateTime = AetherJarDownloader.getJarLatestVersion(location);
                if(Objects.equals(jarLatestUpdateTime,location.getVersion())){
                    throw new BizException("maven依赖不存在");
                }
                if(!Objects.equals(jarLatestUpdateTime,record.getJarActualVersion())){
                    canReload = true;
                }
            }


            return canReload;
        }
        return false;
    }

    public Boolean removeRecord(MavenJarLocation location) {
        JsfJarRecord record = jsfJarRecordService.getJsfRecord(location);
        if(record != null){
            jsfJarRecordService.removeById(record.getId());
            File jarPath = findJarPath(location);
            FileUtils.deleteQuietly(jarPath);
            return true;
        }
        return false;
    }
    public void moveJarDoc(String srcIp){
        List<JsfJarRecord> records = jsfJarRecordService.listByIp(srcIp);
        List<Future> futures =  new ArrayList<>();
        for (JsfJarRecord record : records) {
           Future future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try{
                        log.info("开始移动jar包");
                        File jarPath = findJarPath(new MavenJarLocation(record.getGroupId(), record.getArtifactId(), record.getVersion()));
                        jsfJarCacheService.downloadMavenFileToLocal(
                                jarPath,
                                new MavenJarLocation(record.getGroupId(), record.getArtifactId(), record.getVersion()));
                        record.setCurrentCachedServerIp(IpUtil.getLocalIp());
                        jsfJarRecordService.updateById(record);
                        log.info("移动jar包完成");
                    }catch (Exception e){
                        log.error("移动jar包失败:record={}",record,e);
                        String desc = "移动jar包失败:"+e.getMessage();
                        record.setCurrentCachedServerIp(IpUtil.getLocalIp());
                        record.setLoadStatus(JarLoadStatus.LOAD_FAIL.getCode());
                        record.setFailReasonDesc(SwaggerParserService.truncateStr(desc,200));
                        record.setFailReasonDetail(SwaggerParserService.truncateStr(getStackTrace(e),FAIL_REASON_LENGTH));
                        jsfJarRecordService.updateById(record);
                    }
                }
            });
              futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("移动jar包失败",e);
            }
        }
    }
}
