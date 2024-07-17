package com.jd.workflow.jsf.analyzer;

import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.jsf.parser.JsfClassParser;
import com.jd.workflow.soap.common.parser.ClassParser;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 项目名称：parent
 * 类 名 称：JarParser
 * 类 描 述：jar包解析
 * 创建时间：2022-06-17 17:45
 * 创 建 人：wangxiaofei8
 */
@Slf4j
public class JarParser {

    private static final String CLS_PREFIX = ".class";

    private static final String PACKAGE_SPLIT = ".";

    private static final String FILE_PATH_SPLIT = "/";

    private static final Map<String, Class<?>> PRIMITIVE_TYPES = new ConcurrentHashMap<>();

    static {

        // object \ array \  long double string float integer boolean    --- char byte short

        PRIMITIVE_TYPES.put("int", Integer.TYPE);
        PRIMITIVE_TYPES.put("long", Long.TYPE);
        PRIMITIVE_TYPES.put("double", Double.TYPE);
        PRIMITIVE_TYPES.put("float", Float.TYPE);
        PRIMITIVE_TYPES.put("boolean", Boolean.TYPE);
        PRIMITIVE_TYPES.put("char", Character.TYPE);
        PRIMITIVE_TYPES.put("byte", Byte.TYPE);
        PRIMITIVE_TYPES.put("short", Short.TYPE);

        PRIMITIVE_TYPES.put("java.lang.Integer", Integer.TYPE);
        PRIMITIVE_TYPES.put("java.lang.Long", Long.TYPE);
        PRIMITIVE_TYPES.put("java.lang.Double", Double.TYPE);
        PRIMITIVE_TYPES.put("java.lang.Float", Float.TYPE);
        PRIMITIVE_TYPES.put("java.lang.Boolean", Boolean.TYPE);
        PRIMITIVE_TYPES.put("java.lang.Character", Character.TYPE);
        PRIMITIVE_TYPES.put("java.lang.Byte", Byte.TYPE);
        PRIMITIVE_TYPES.put("java.lang.Short", Short.TYPE);
        PRIMITIVE_TYPES.put("java.lang.String", Character.TYPE);

        //PRIMITIVE_TYPES.put("void", Void.TYPE);
    }


    /**
     * 解析jar包，为了效率，先解析单个jar包，若不满足，则下载全部jar包
     * @param location
     * @param interfaceId
     * @return
     */
    public static List<JsfStepMetadata> parseJsfInterface(MavenJarLocation location,String interfaceId){

         List<File> jarFiles;
        File tempFile = null;
        try {
            Path tempPath = Files.createTempDirectory(UUID.randomUUID().toString());
              tempFile = tempPath.toFile();
            List<JsfStepMetadata> list = null;
            try {

                File jarFile = AetherJarDownloader.downloadSingleJar(location,tempFile);
                //log.info("maven.downloadSingleJar:jars={}",jarFile);


                try{
                    list = parseStepMetadata(Collections.singletonList(jarFile),interfaceId);
                }catch (ClassNotFoundException e){
                    throw new BizException("加载类"+interfaceId+"失败，请确保该类存在:"+e.getMessage(),e);
                }catch (Exception |Error e ){
                    log.error("maven.err_process_single_jar:msg={}",e.getMessage());
                    jarFiles = AetherJarDownloader.downloadAllJar(location,tempFile);
                    //log.info("maven.downloadAllJar:jars={}",jarFiles);
                    try {
                        list = parseStepMetadata(jarFiles,interfaceId);
                    } catch (Exception |Error ex) {

                        log.error("maven.err_process_all_jarr:msg={}",ex.getMessage(),ex);
                        throw new BizException("加载类"+interfaceId+"失败，请确保该类存在:"+ex.getMessage(),ex);
                    }
                }

            } catch (DependencyResolutionException | DependencyCollectionException | ArtifactResolutionException e) {

                throw new BizException("下载jar失败,确保jar已上传（若已上传可能是maven服务不稳定，请重试）："+e.getMessage(),e);
            }

            return list;
        }catch ( IOException e){
            log.error("jsf.err_dowwload_jar:location={}",location,e);
            throw new BizException("下载jar失败，创建临时目录失败",e);
        }finally {
            if(tempFile != null){
                tempFile.delete();
            }

        }

    }


    public static boolean isValidMavenLocation(MavenJarLocation location){

        List<File> jarFiles;
        File tempFile = null;
        try {
            Path tempPath = Files.createTempDirectory(UUID.randomUUID().toString());
            tempFile = tempPath.toFile();
            List<JsfStepMetadata> list = null;

                boolean result = AetherJarDownloader.existMockJar(location,tempFile);
                return result;


        }catch ( IOException e){
            log.error("jsf.err_dowwload_jar:location={}",location,e);
            throw new BizException("下载jar失败，创建临时目录失败",e);
        }finally {
            if(tempFile != null){
                tempFile.delete();
            }

        }

    }

    private static List<JsfStepMetadata> parseStepMetadata(List<File> jarFiles,String interfaceId) throws Exception {
        ClassParser classParser = new ClassParser();
        List<JsfStepMetadata> list = new ArrayList<>();
        Error[] exceptions = new Error[1];
        loadClass(jarFiles,interfaceId,aClass->{
            try{
                for (Method method : aClass.getMethods()) {
                    list.add(JsfClassParser.buildMethodInfo(aClass,method.getName()));
                }
                return null;
            }catch (Error e){
                exceptions[0] = e;
                return null;
            }
        });
        if(exceptions[0] != null){
            throw exceptions[0];
        }
        return list;
    }

    /**
     * jar文件扫描
     * @param f
     * @return
     */
    public static ClazzContainer scanJarClazz(File f){
        ClazzContainer container = new ClazzContainer();
        try {
            //初始化classloader、jarfile
            URLClassLoader loader = new URLClassLoader(new URL[]{f.toURI().toURL()},Thread.currentThread().getContextClassLoader());
            JarFile jar = new JarFile(f.getPath());
            Enumeration<JarEntry> enumFiles  = jar.entries();
            JarEntry entry;
            while (enumFiles.hasMoreElements()) {
                entry = (JarEntry)enumFiles.nextElement();
                String classFullName = entry.getName();
                if(classFullName.endsWith(CLS_PREFIX)) {
                    String clzName = classFullName.replace(FILE_PATH_SPLIT, PACKAGE_SPLIT);
                    clzName = clzName.substring(0, clzName.length() - 6);
                    try {
                        Class<?> clz = loader.loadClass(clzName);
                        clz.getDeclaredMethods();
                        if(clz.isInterface()){
                            container.allInterfaces.put(clzName,clz);
                        }else{
                            container.allClazzs.put(clzName,clz);
                            TypeVariable<? extends Class<?>>[] typeParameters = clz.getTypeParameters();
                            if(typeParameters!=null&&typeParameters.length>0){
                                Map<String, Integer> map = new HashMap<>();
                                for (int i = 0; i < typeParameters.length; i++) {
                                    map.put(typeParameters[i].getName(),i);
                                }
                                container.allClazzNameToTypeParameter.put(clzName,map);
                            }else{
                                container.allClazzNameToTypeParameter.put(clzName, null);
                            }
                        }
                    } catch (Throwable e) {
                        log.error("class name : {} load error!", clzName);
                        log.error("class load error!", e);
                    }
                }
            }
            loader = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return container;
    }



    /**
     * 过滤final and static
     * @param field
     * @return
     */
    public static boolean matches(Field field){
        return !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()));
    }

    private static List<Method> getClassMethods(Class<?> clazz) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }
            currentClass = currentClass.getSuperclass();
        }
        Collection<Method> methods = uniqueMethods.values();
        return new ArrayList<>(uniqueMethods.values());
    }

    private static void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                String signature = getSignature(currentMethod);
                if (!uniqueMethods.containsKey(signature)) {
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    private static String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType.getName()).append('#');
        }
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            sb.append(i == 0 ? ':' : ',').append(parameters[i].getName());
        }
        return sb.toString();
    }

    static class ClazzContainer{
        public Map<String,Class<?>> allClazzs = new HashMap<>();
        public Map<String,Class<?>> allInterfaces = new HashMap<>();
        public Map<String, Map<String, Integer>> allClazzNameToTypeParameter = new HashMap<>();
    }
    private  static Class loadClass(List<File> jarFiles, String fullClassName, Function<Class,Void> callback) throws ClassNotFoundException {
        URLClassLoader loader = null;
        try{
            URL[] urls = new URL[jarFiles.size()];
            for (int i = 0; i < jarFiles.size(); i++) {
                try {
                    urls[i] = jarFiles.get(i).toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new BizException("加载类失败："+e.getMessage(),e);
                }
            }
             loader = new URLClassLoader(urls,Thread.currentThread().getContextClassLoader());

               Class clazz = loader.loadClass(fullClassName);
               callback.apply(clazz);
               return clazz;


        }finally {
            if(loader != null){
                try {
                    loader.close();
                } catch (IOException e) {
                    log.error("classloader.err_close_loader:fullClassName={},jarFiles={}",fullClassName,jarFiles,e);
                }
            }
        }


    }
    public static void main(String[] args) throws IOException, ArtifactResolutionException, DependencyResolutionException, DependencyCollectionException, ClassNotFoundException {
        MavenJarLocation location = new MavenJarLocation();
        location.setGroupId("com.jd.unifiedmetadata");
        location.setArtifactId("metadata-update-rpc");
        location.setVersion("1.1-SNAPSHOT");
        final List<JsfStepMetadata> jsfStepMetadata = parseJsfInterface(location, "com.jd.metadata.service.IMetaService");
        log.info("sffdssfd:meta={}", JsonUtils.toJSONString(jsfStepMetadata));
        /*Path tempPath = Files.createTempDirectory(UUID.randomUUID().toString());
        final File tempFile = tempPath.toFile();
        List<File> jarFiles = AetherJarDownloader.downloadAllJar(location,tempFile);
       Class clazz = loadClass(jarFiles,"com.jd.bdp.jcm.api.account.service.external.AccountExternalInterface",(loadedClass)->{
           return null;
       });
        System.out.println(jarFiles);*/
    }

}
